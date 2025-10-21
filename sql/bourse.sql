-- Création de la table pour les catégories de questions
CREATE TABLE categorie (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL UNIQUE
);

-- Création de la table pour les questions
CREATE TABLE question (
    id SERIAL PRIMARY KEY,
    texte TEXT NOT NULL,
    categorie_id INT NOT NULL,
    FOREIGN KEY (categorie_id) REFERENCES categorie(id) ON DELETE CASCADE
);

-- Création de la table pour les réponses
CREATE TABLE reponse (
    id SERIAL PRIMARY KEY,
    question_id INT NOT NULL,
    texte TEXT NOT NULL,
    score INT NOT NULL,
    FOREIGN KEY (question_id) REFERENCES question(id) ON DELETE CASCADE
);

-- Création de la table pour les familles
CREATE TABLE famille (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE, -- Login de l'utilisateur associé
    nom VARCHAR(255) NOT NULL,
    animateur_nom VARCHAR(255),
    nombre_personnes INT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(user_id) REFERENCES User_(user_id) ON DELETE CASCADE
);

-- Création de la table pour stocker les réponses données par une famille
CREATE TABLE famille_reponse (
    id SERIAL PRIMARY KEY,
    famille_id INT NOT NULL,
    reponse_id INT NOT NULL,
    date_reponse TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (famille_id) REFERENCES famille(id) ON DELETE CASCADE,
    FOREIGN KEY (reponse_id) REFERENCES reponse(id) ON DELETE CASCADE,
    UNIQUE(famille_id, reponse_id) -- Assure qu''une famille ne donne qu''une réponse par question
);

-- Insertion des catégories basées sur le fichier
INSERT INTO categorie (nom) VALUES
('ECONOMIE'),
('SANTE'),
('EDUCATION'),
('SOCIAL'),
('CONFORT DE VIE');

creer moi une fonction sql qui calcule le score total des reponses pour chaque famille et qui retourne uniquement les familles dont le score total est inferieur ou egal a 30. la fonction doit retourner le nom de la famille et le login de l'utilisateur associe.

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
  
  exemple d'utilisation:
-- SELECT * FROM familles_elligibles_pour_bourse();     

creer moi une fonction qui check l'elligibilte par categorie pour chaque famille et qui retourne le nom de la famille, le login de l'utilisateur associe et le score par categorie.
genrre il est elligible si le score dans chaque categorie est inferieur ou egal a 4.
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

-- SELECT * FROM familles_elligibles_par_categorie();
