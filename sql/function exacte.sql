CREATE OR REPLACE FUNCTION moyenne_heures_utilisateurs()
RETURNS TABLE (
    login VARCHAR,
    moyenne_heure_depuis_debut NUMERIC,
    moyenne_heure_depuis_3_mois NUMERIC,
    moyenne_heure_depuis_1_mois NUMERIC,
    moyenne_heure_depuis_1_semaine NUMERIC
)
AS $$
DECLARE
    current_week INT := EXTRACT(WEEK FROM CURRENT_DATE);
    last_complete_week INT := current_week - 1; -- l'avant-dernière semaine complète
BEGIN
    RETURN QUERY
    SELECT 
        u.login,

        -- Moyenne depuis le début (depuis la semaine 10 par ex.)
        ROUND(
            SUM(CASE WHEN tp.semaine >= 10 THEN tp.heures_total ELSE 0 END) 
            / NULLIF(COUNT(DISTINCT CASE WHEN tp.semaine >= 10 THEN tp.semaine END), 0)
        ,2) AS moyenne_heure_depuis_debut,

        -- Moyenne des 3 derniers mois (≈ 12 semaines)
        ROUND(
            SUM(CASE WHEN tp.semaine >= last_complete_week - 11 THEN tp.heures_total ELSE 0 END)
            / 12.0
        ,2) AS moyenne_heure_depuis_3_mois,

        -- Moyenne du dernier mois (≈ 4 semaines)
        ROUND(
            SUM(CASE WHEN tp.semaine >= last_complete_week - 3 THEN tp.heures_total ELSE 0 END)
            / 4.0
        ,2) AS moyenne_heure_depuis_1_mois,

        -- Moyenne de la dernière semaine complète (avant-dernière)
        ROUND(
            SUM(CASE WHEN tp.semaine = last_complete_week THEN tp.heures_total ELSE 0 END)
        ,2) AS moyenne_heure_depuis_1_semaine

    FROM user_ u
    LEFT JOIN moyennes_heures_par_semaine_par_utilisateur('2025-03-03', CURRENT_DATE) tp
        ON tp.login = u.login
    WHERE u.kind = 'student'
    GROUP BY u.login
    ORDER BY u.login;
END;
$$ LANGUAGE plpgsql;



-- Fonction pour calculer le taux de présence par utilisateur (sans ceux qui ont 0 jour présent)
CREATE OR REPLACE FUNCTION taux_presence_par_utilisateur(start_date DATE, end_date DATE)
RETURNS TABLE(
    user_id VARCHAR(250),
    login VARCHAR(250),
    displayname VARCHAR(250),
    first_name VARCHAR(250),
    last_name VARCHAR(250),
    jours_present INT,
    jours_totaux INT,
    taux_presence NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    WITH date_range AS (
        SELECT generate_series(start_date, end_date, interval '1 day')::date AS day
    )
    SELECT 
        u.user_id,
        u.login,
        u.displayname,
        u.first_name,
        u.last_name,
        COUNT(DISTINCT s.date_)::INT AS jours_present,
        (SELECT COUNT(*)::INT FROM date_range) AS jours_totaux,
        ROUND(
            COUNT(DISTINCT s.date_)::NUMERIC 
            / NULLIF((SELECT COUNT(*)::NUMERIC FROM date_range),0) * 100,
            2
        ) AS taux_presence
    FROM User_ u
    LEFT JOIN Stats s
        ON s.user_id = u.user_id
        AND s.date_ BETWEEN start_date AND end_date
    GROUP BY u.user_id, u.login, u.displayname, u.first_name, u.last_name
    HAVING COUNT(DISTINCT s.date_) > 0
    ORDER BY taux_presence DESC;
END;
$$ LANGUAGE plpgsql;


-- Fonction pour calculer le taux de présence global
CREATE OR REPLACE FUNCTION taux_presence_global(
    start_date DATE,
    end_date DATE,
    capacite_jour INT DEFAULT 250  -- nombre max d'utilisateurs/jour
)
RETURNS NUMERIC AS $$
DECLARE
    result NUMERIC;
BEGIN
    WITH date_range AS (
        SELECT generate_series(start_date, end_date, interval '1 day')::date AS day
    )
    SELECT 
        ROUND(SUM(CASE WHEN s.date_ IS NOT NULL THEN 1 ELSE 0 END)::numeric 
              / ((SELECT COUNT(*) FROM date_range) * capacite_jour) * 100, 2)
    INTO result
    FROM Stats s
    WHERE s.date_ BETWEEN start_date AND end_date;

    RETURN result;
END;
$$ LANGUAGE plpgsql;

-- Exemple d'appel de la fonction
SELECT * FROM taux_presence_global('2025-07-01', '2025-08-15', 250);
SELECT * FROM taux_presence_par_utilisateur('2025-07-01', '2025-08-15');



CREATE OR REPLACE FUNCTION familles_elligibles_pour_bourse()
RETURNS TABLE(
    nom_famille VARCHAR(255),
    login_utilisateur VARCHAR(50)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        f.nom AS nom_famille,
        f.user_id AS login_utilisateur
    FROM famille f
    JOIN famille_reponse fr ON f.id = fr.famille_id
    JOIN reponse r ON fr.reponse_id = r.id
    GROUP BY f.id, f.nom, f.user_id
    HAVING SUM(r.score) <= 30;
END;
$$ LANGUAGE plpgsql;  
  
 -- exemple d'utilisation:
-- SELECT * FROM familles_elligibles_pour_bourse();     

--creer moi une fonction qui check l'elligibilte par categorie pour chaque famille et qui --retourne le nom de la famille, le login de l'utilisateur associe et le score par categorie.
--genrre il est elligible si le score dans chaque categorie est inferieur ou egal a 4.:

CREATE OR REPLACE FUNCTION familles_elligibles_par_categorie()
RETURNS TABLE(
    nom_famille VARCHAR(255),
    login_utilisateur VARCHAR(50),
    categorie_nom VARCHAR(255),
    score_categorie BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        f.nom AS nom_famille,
        f.user_id AS login_utilisateur,
        c.nom AS categorie_nom,
        SUM(r.score) AS score_categorie 
    FROM famille f
    JOIN famille_reponse fr ON f.id = fr.famille_id
    JOIN reponse r ON fr.reponse_id = r.id
    JOIN question q ON r.question_id = q.id
    JOIN categorie c ON q.categorie_id = c.id
    GROUP BY f.id, f.nom, f.user_id, c.id, c.nom
    HAVING SUM(r.score) <= 4;
END;
$$ LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION moyennes_heures_par_semaine_par_utilisateur(
    date_debut DATE,
    date_fin DATE
)
RETURNS TABLE (
    login VARCHAR,
    semaine INTEGER,
    date_semaine DATE,
    heures_total NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    WITH weeks AS (
        -- Génère toutes les semaines entre date_debut et date_fin en forçant DATE
        SELECT (generate_series(
            DATE_TRUNC('week', date_debut)::DATE,
            DATE_TRUNC('week', date_fin)::DATE,
            INTERVAL '1 week'
        ))::DATE AS start_of_week
    )
    SELECT 
        u.login,
        EXTRACT(WEEK FROM w.start_of_week)::INT AS semaine,
        w.start_of_week AS date_semaine,
        COALESCE(ROUND(SUM(EXTRACT(EPOCH FROM s.duration) / 3600), 2), 0) AS heures_total
    FROM User_ u
    CROSS JOIN weeks w
    LEFT JOIN Stats s
        ON s.user_id = u.user_id
       AND DATE_TRUNC('week', s.date_)::DATE = w.start_of_week
    WHERE u.kind = 'student'
    GROUP BY u.login, semaine, date_semaine
    ORDER BY u.login, semaine;
END;
$$ LANGUAGE plpgsql;
   

--
SELECT * 
FROM moyennes_heures_par_semaine_par_utilisateur('2025-03-03', '2025-10-20') where user_id='243819;



CREATE OR REPLACE FUNCTION utilisateurs_absents_consecutifs_2(
    p_granularite_jours_min INT DEFAULT NULL,
    p_granularite_jours_max INT DEFAULT NULL,
    p_date_debut DATE DEFAULT '2000-01-01'
)
RETURNS TABLE(
    user_id VARCHAR(250),
    login VARCHAR(250),
    displayname VARCHAR(250),
    first_name VARCHAR(250),
    last_name VARCHAR(250),
    jours_absents_consecutifs INT,
    total_absences INT
) AS $$
BEGIN
    RETURN QUERY
    WITH RECURSIVE absence_streak AS (
        -- Premier jour d’absence (hier)
        SELECT 
            u.user_id,
            u.login,
            u.displayname,
            u.first_name,
            u.last_name,
            1 AS jours_absents,
            CURRENT_DATE - INTERVAL '1 day' AS date_check
        FROM User_ u
        WHERE u.kind = 'student'
          AND NOT EXISTS (
              SELECT 1 FROM Stats s 
              WHERE s.user_id = u.user_id 
                AND s.date_ = CURRENT_DATE - INTERVAL '1 day'
          )

        UNION ALL

        -- Absences successives
        SELECT 
            a.user_id,
            a.login,
            a.displayname,
            a.first_name,
            a.last_name,
            a.jours_absents + 1,
            a.date_check - INTERVAL '1 day'
        FROM absence_streak a
        WHERE a.date_check >= p_date_debut
          AND NOT EXISTS (
              SELECT 1 FROM Stats s 
              WHERE s.user_id = a.user_id 
                AND s.date_ = a.date_check - INTERVAL '1 day'
          )
    ),
    absences_totales AS (
        -- Compter tous les jours d’absence (non consécutifs) pendant la période
        SELECT 
            u.user_id,
            COUNT(d)::INT AS total_abs
        FROM User_ u
        CROSS JOIN generate_series(p_date_debut, CURRENT_DATE, INTERVAL '1 day') d
        LEFT JOIN Stats s 
            ON s.user_id = u.user_id 
           AND s.date_ = d
        WHERE u.kind = 'student'
          AND s.date_ IS NULL
        GROUP BY u.user_id
    )
    SELECT 
        a.user_id,
        a.login,
        a.displayname,
        a.first_name,
        a.last_name,  
        MAX(a.jours_absents) AS jours_absents_consecutifs,
        at.total_abs AS total_absences
    FROM absence_streak a
    JOIN absences_totales at ON at.user_id = a.user_id
    GROUP BY a.user_id, a.login, a.displayname, a.first_name, a.last_name, at.total_abs
    HAVING 
        (p_granularite_jours_min IS NULL OR MAX(a.jours_absents) >= p_granularite_jours_min)
        AND (p_granularite_jours_max IS NULL OR MAX(a.jours_absents) <= p_granularite_jours_max)
    ORDER BY jours_absents_consecutifs DESC;
END;
$$ LANGUAGE plpgsql;

--SELECT login, jours_absents_consecutifs, total_absences
--FROM utilisateurs_absents_consecutifs_2(5, 10, '2025-03-01');


