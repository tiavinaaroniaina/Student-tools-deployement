CREATE OR REPLACE FUNCTION moyenne_heures_utilisateurs()
RETURNS TABLE (
    login VARCHAR,
    moyenne_heure_depuis_debut NUMERIC,
    moyenne_heure_depuis_3_mois NUMERIC,
    moyenne_heure_depuis_1_mois NUMERIC,
    moyenne_heure_depuis_1_semaine NUMERIC
)
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.login,
        
        -- Moyenne depuis le début (ex: depuis le 03/03/2025)
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur('2025-03-03'::DATE, CURRENT_DATE::DATE) tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_debut,
         
        -- Moyenne des 3 derniers mois
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur((CURRENT_DATE - INTERVAL '3 months')::DATE, CURRENT_DATE::DATE) tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_3_mois,
         
        -- Moyenne du dernier mois
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur((CURRENT_DATE - INTERVAL '1 month')::DATE, CURRENT_DATE::DATE) tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_1_mois,

        -- Moyenne de la dernière semaine
        (SELECT ROUND(AVG(tp.heures_total), 2)
         FROM moyennes_heures_par_semaine_par_utilisateur((CURRENT_DATE - INTERVAL '1 week')::DATE, CURRENT_DATE::DATE) tp
         WHERE tp.login = u.login) AS moyenne_heure_depuis_1_semaine
         
    FROM user_ u; -- récupère tous les logins de la table user_
END;
$$ LANGUAGE plpgsql;