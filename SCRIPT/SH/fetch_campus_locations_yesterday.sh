#!/bin/bash

# ========================
# Configuration
# ========================
CAMPUS_ID="65"
OUTPUT_DIR="DATA/$CAMPUS_ID/LOCATIONS"
OUTPUT_FILE="$OUTPUT_DIR/campus${CAMPUS_ID}_locations_yesterday.json"
USER_IDS_FILE="$OUTPUT_DIR/campus${CAMPUS_ID}_user_ids_yesterday.txt"
PAGE_SIZE=100
BATCH_SIZE=10  # 🔹 Sauvegarde toutes les 10 pages

# Date d'hier à Madagascar (UTC+3)
BEGIN_AT="$(TZ='Indian/Antananarivo' date -d '5 days ago' +%Y-%m-%d)T00:00:00Z"
END_AT="$(TZ='Indian/Antananarivo' date -d 'today' +%Y-%m-%d)T23:59:59Z"

CLIENT_ID="u-s4t2af-23f031abd5ab1c7afcd6b43148ddd70b2ae20692602fb8c142f94fabb55b5373"
CLIENT_SECRET="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"

# ========================
# Dépendances
# ========================
command -v curl >/dev/null 2>&1 || { echo "Erreur: curl non installé"; exit 1; }
command -v jq >/dev/null 2>&1 || { echo "Erreur: jq non installé"; exit 1; }

# ========================
# Dossiers et checkpoint
# ========================
mkdir -p "$OUTPUT_DIR"
CHECKPOINT_FILE="${OUTPUT_FILE}.checkpoint"

PAGE=1
TOTAL_LOCATIONS=0
BATCH_FILES=()

if [[ -f "$CHECKPOINT_FILE" ]]; then
    echo "🔄 Reprise depuis le checkpoint..."
    PAGE=$(cat "$CHECKPOINT_FILE")
    echo "➡️ Reprise à la page $PAGE"
fi

# ========================
# Fonction token
# ========================
get_token() {
    echo "Obtention du token d'accès..."
    token_response=$(curl -s -X POST "https://api.intra.42.fr/oauth/token" \
        -u "$CLIENT_ID:$CLIENT_SECRET" \
        -d "grant_type=client_credentials&scope=public projects profile tig elearning forum")
    ACCESS_TOKEN=$(echo "$token_response" | jq -r '.access_token')
    [[ -z "$ACCESS_TOKEN" || "$ACCESS_TOKEN" == "null" ]] && { echo "Token invalide"; exit 1; }
}
get_token

# ========================
# Interruption propre
# ========================
cleanup() {
    echo ""
    echo "🛑 Interruption détectée. Sauvegarde du numéro de page..."
    echo "$PAGE" > "$CHECKPOINT_FILE"
    echo "💾 Checkpoint sauvegardé : $CHECKPOINT_FILE"
    exit 130
}
trap cleanup SIGINT SIGTERM

# ========================
# Téléchargement paginé
# ========================
echo "⏱ Début du téléchargement des locations pour le campus $CAMPUS_ID"
START_TIME=$(date +%s)

BATCH_LOCATIONS_FILE="$(mktemp)"
> "$BATCH_LOCATIONS_FILE"
BATCH_COUNT=0

while true; do
    URL="https://api.intra.42.fr/v2/locations?campus_id=${CAMPUS_ID}&sort=begin_at&range%5Bbegin_at%5D=${BEGIN_AT},${END_AT}&page%5Bsize%5D=${PAGE_SIZE}&page%5Bnumber%5D=${PAGE}"

    response=$(curl -s -w "HTTP_CODE:%{http_code}" -H "Authorization: Bearer $ACCESS_TOKEN" "$URL")
    http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    response_body=$(echo "$response" | sed 's/HTTP_CODE:[0-9]*$//')

    if [[ "$http_code" -eq 401 ]]; then
        echo "⚠️  Token expiré, régénération..."
        get_token
        continue
    elif [[ "$http_code" -ne 200 ]]; then
        echo "Erreur HTTP $http_code"
        break
    fi

    count=$(echo "$response_body" | jq 'length')
    [[ "$count" -eq 0 ]] && break

    if [[ $BATCH_COUNT -eq 0 ]]; then
        echo "$response_body" > "$BATCH_LOCATIONS_FILE"
    else
        tmp="$(mktemp)"
        jq --slurpfile new <(echo "$response_body") '. + $new[0]' "$BATCH_LOCATIONS_FILE" > "$tmp" && mv "$tmp" "$BATCH_LOCATIONS_FILE"
    fi

    BATCH_COUNT=$((BATCH_COUNT + 1))
    TOTAL_LOCATIONS=$((TOTAL_LOCATIONS + count))

    CURRENT_TIME=$(date +%s)
    ELAPSED=$((CURRENT_TIME - START_TIME))
    printf "Page %d: %d locations récupérées (Total: %d) | Temps écoulé: %02dh:%02dm:%02ds\n" \
        "$PAGE" "$count" "$TOTAL_LOCATIONS" "$((ELAPSED/3600))" "$(((ELAPSED%3600)/60))" "$((ELAPSED%60))"

    # 🔹 Sauvegarde batch
    if (( BATCH_COUNT >= BATCH_SIZE )); then
        batch_file="$OUTPUT_DIR/batch_${PAGE}.json"
        mv "$BATCH_LOCATIONS_FILE" "$batch_file"
        BATCH_FILES+=("$batch_file")
        echo "💾 Batch sauvegardé : $batch_file"
        BATCH_LOCATIONS_FILE="$(mktemp)"
        > "$BATCH_LOCATIONS_FILE"
        BATCH_COUNT=0
        echo "$PAGE" > "$CHECKPOINT_FILE"
    fi

    PAGE=$((PAGE + 1))
    sleep 0.2
done

# 🔹 Sauvegarde du dernier batch si non vide
if [[ "$BATCH_COUNT" -gt 0 ]]; then
    batch_file="$OUTPUT_DIR/batch_${PAGE}.json"
    mv "$BATCH_LOCATIONS_FILE" "$batch_file"
    BATCH_FILES+=("$batch_file")
    echo "💾 Dernier batch sauvegardé : $batch_file"
fi

# ========================
# Fusion finale
# ========================
echo "🧩 Fusion de tous les batchs..."
jq -s 'add' "${BATCH_FILES[@]}" > "$OUTPUT_FILE"

USER_IDS=$(jq -r '[.[].user.id] | unique | .[]' "$OUTPUT_FILE")
echo "$USER_IDS" > "$USER_IDS_FILE"

rm -f "$CHECKPOINT_FILE" "${BATCH_FILES[@]}" 2>/dev/null

END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))

printf "✅ Locations sauvegardées dans %s\n" "$OUTPUT_FILE"
printf "✅ IDs utilisateurs sauvegardés dans %s\n" "$USER_IDS_FILE"
printf "Nombre total de locations: %d\n" "$TOTAL_LOCATIONS"
printf "Nombre d'utilisateurs uniques: %d\n" "$(echo "$USER_IDS" | wc -l)"
printf "Période: %s à %s\n" "$BEGIN_AT" "$END_AT"
printf "⏱ Temps total écoulé: %02dh:%02dm:%02ds\n" "$((TOTAL_TIME/3600))" "$(((TOTAL_TIME%3600)/60))" "$((TOTAL_TIME%60))"
