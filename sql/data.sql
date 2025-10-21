-- Insertion des données de base pour le questionnaire de bourse.
-- Les catégories sont supposées être déjà insérées par le script bourse.sql.

-- Questions et réponses pour la catégorie ECONOMIE

INSERT INTO question (texte, categorie_id) VALUES ('Nombre de personnes à charge par adulte ayant un revenu', (SELECT id FROM categorie WHERE nom = 'ECONOMIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Nombre de personnes à charge par adulte ayant un revenu'), '4 ou plus', 0),
((SELECT id FROM question WHERE texte = 'Nombre de personnes à charge par adulte ayant un revenu'), '3', 1),
((SELECT id FROM question WHERE texte = 'Nombre de personnes à charge par adulte ayant un revenu'), '2', 2),
((SELECT id FROM question WHERE texte = 'Nombre de personnes à charge par adulte ayant un revenu'), '0 ou 1', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Activité de l''adulte principal gagne-pain du foyer', (SELECT id FROM categorie WHERE nom = 'ECONOMIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Activité de l''adulte principal gagne-pain du foyer'), 'Aucune (par exemple les retraités)', 0),
((SELECT id FROM question WHERE texte = 'Activité de l''adulte principal gagne-pain du foyer'), 'Occasionnelle, régulière', 1),
((SELECT id FROM question WHERE texte = 'Activité de l''adulte principal gagne-pain du foyer'), 'Régulière mais non déclarée ou multiple', 2),
((SELECT id FROM question WHERE texte = 'Activité de l''adulte principal gagne-pain du foyer'), 'Régulière et formelle / diversifiée', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Régularité de l''épargne', (SELECT id FROM categorie WHERE nom = 'ECONOMIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Régularité de l''épargne'), 'Aucune', 0),
((SELECT id FROM question WHERE texte = 'Régularité de l''épargne'), 'De manière informelle (chez soi, tontine, etc.)', 1),
((SELECT id FROM question WHERE texte = 'Régularité de l''épargne'), 'Sur un compte bancaire mais irrégulièrement', 2),
((SELECT id FROM question WHERE texte = 'Régularité de l''épargne'), 'Régulièrement sur un compte bancaire', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Estimation des dépenses / jour / pers.', (SELECT id FROM categorie WHERE nom = 'ECONOMIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Estimation des dépenses / jour / pers.'), '< 5 000 Ar', 0),
((SELECT id FROM question WHERE texte = 'Estimation des dépenses / jour / pers.'), '5 000–10 000 Ar', 1),
((SELECT id FROM question WHERE texte = 'Estimation des dépenses / jour / pers.'), '10 000–20 000 Ar', 2),
((SELECT id FROM question WHERE texte = 'Estimation des dépenses / jour / pers.'), '> 20 000 Ar', 3);

-- Questions et réponses pour la catégorie SANTE

INSERT INTO question (texte, categorie_id) VALUES ('Nutrition', (SELECT id FROM categorie WHERE nom = 'SANTE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Nutrition'), 'Malnutrition sévère, visible / cas de survie OU 1 repas/jour', 0),
((SELECT id FROM question WHERE texte = 'Nutrition'), '2 repas / jour', 1),
((SELECT id FROM question WHERE texte = 'Nutrition'), 'Repas réguliers (3/jour) mais pas varié', 2),
((SELECT id FROM question WHERE texte = 'Nutrition'), 'Repas réguliers (3/jour) et varié', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Possibilité de payer pour une dépense de santé "importante"', (SELECT id FROM categorie WHERE nom = 'SANTE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Possibilité de payer pour une dépense de santé "importante"'), 'Pas de solution (pas de soins ou emprunt)', 0),
((SELECT id FROM question WHERE texte = 'Possibilité de payer pour une dépense de santé "importante"'), 'Aide proche (famille, voisins, ONG)', 1),
((SELECT id FROM question WHERE texte = 'Possibilité de payer pour une dépense de santé "importante"'), 'Paiement via épargne ou ressources propres', 2),
((SELECT id FROM question WHERE texte = 'Possibilité de payer pour une dépense de santé "importante"'), 'Système d''assurance formelle', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Mortalité infantile', (SELECT id FROM categorie WHERE nom = 'SANTE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Mortalité infantile'), '2 enfants ou plus sont déjà décédés dans la famille durant les 5 dernières années', 0),
((SELECT id FROM question WHERE texte = 'Mortalité infantile'), '1 enfant est déjà décédé dans la famille durant les 5 dernières années', 1),
((SELECT id FROM question WHERE texte = 'Mortalité infantile'), '1 enfant est déjà décédé dans la famille (pas de limite de date)', 2),
((SELECT id FROM question WHERE texte = 'Mortalité infantile'), 'Pas de décès d''enfant dans la famille', 3);

-- Questions et réponses pour la catégorie EDUCATION

INSERT INTO question (texte, categorie_id) VALUES ('Scolarisation des enfants', (SELECT id FROM categorie WHERE nom = 'EDUCATION'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Scolarisation des enfants'), 'Aucun enfant en âge d''aller à l''école n''est scolarisé', 0),
((SELECT id FROM question WHERE texte = 'Scolarisation des enfants'), 'Au moins 1 enfant est scolarisé, pas nécessairement dans le cycle correspondant à leur âge', 1),
((SELECT id FROM question WHERE texte = 'Scolarisation des enfants'), 'Au moins la moitié des enfants sont scolarisés, pas nécessairement dans le cycle correspondant à leur âge', 2),
((SELECT id FROM question WHERE texte = 'Scolarisation des enfants'), 'Tous les enfants en âge d''aller à l''école sont scolarisés / pas d''enfants en âge d''aller à l''école', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Niveau d''éducation maximum dans la famille', (SELECT id FROM categorie WHERE nom = 'EDUCATION'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Niveau d''éducation maximum dans la famille'), 'Analphabète', 0),
((SELECT id FROM question WHERE texte = 'Niveau d''éducation maximum dans la famille'), 'Niveau primaire atteint', 1),
((SELECT id FROM question WHERE texte = 'Niveau d''éducation maximum dans la famille'), 'Niveau secondaire atteint', 2),
((SELECT id FROM question WHERE texte = 'Niveau d''éducation maximum dans la famille'), 'Baccalauréat et plus', 3);

-- Questions et réponses pour la catégorie SOCIAL

INSERT INTO question (texte, categorie_id) VALUES ('Documents administratifs', (SELECT id FROM categorie WHERE nom = 'SOCIAL'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Documents administratifs'), 'Aucun document valide pour aucun membre', 0),
((SELECT id FROM question WHERE texte = 'Documents administratifs'), 'Au moins 1 copie manquante ou invalide', 1),
((SELECT id FROM question WHERE texte = 'Documents administratifs'), 'Aucune copie manquante mais au moins 1 CIN manquante ou invalide', 2),
((SELECT id FROM question WHERE texte = 'Documents administratifs'), 'Aucun papier administratif ne manque', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Niveau d''hygiène (corporel / vestimentaire / habitat)', (SELECT id FROM categorie WHERE nom = 'SOCIAL'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Niveau d''hygiène (corporel / vestimentaire / habitat)'), 'Problèmes sur les 4 domaines', 0),
((SELECT id FROM question WHERE texte = 'Niveau d''hygiène (corporel / vestimentaire / habitat)'), 'Problèmes sur 2 ou 3 domaines', 1),
((SELECT id FROM question WHERE texte = 'Niveau d''hygiène (corporel / vestimentaire / habitat)'), 'Problèmes sur 1 domaine', 2),
((SELECT id FROM question WHERE texte = 'Niveau d''hygiène (corporel / vestimentaire / habitat)'), 'Aucun problème sur les 4 domaines', 3);

-- Questions et réponses pour la catégorie CONFORT DE VIE

INSERT INTO question (texte, categorie_id) VALUES ('Électricité', (SELECT id FROM categorie WHERE nom = 'CONFORT DE VIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Électricité'), 'Pas d''éclairage', 0),
((SELECT id FROM question WHERE texte = 'Électricité'), 'Éclairage à la bougie / pétrole / lampe torche', 1),
((SELECT id FROM question WHERE texte = 'Électricité'), 'Électricité illégale', 2),
((SELECT id FROM question WHERE texte = 'Électricité'), 'Réseau légal / Groupe électrogène / Convertisseur', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Toilettes', (SELECT id FROM categorie WHERE nom = 'CONFORT DE VIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Toilettes'), 'Pas d''équipement : défécation à l''air libre', 0),
((SELECT id FROM question WHERE texte = 'Toilettes'), 'Aménagements en commun', 1),
((SELECT id FROM question WHERE texte = 'Toilettes'), 'Aménagement individuel (fosse / latrine)', 2),
((SELECT id FROM question WHERE texte = 'Toilettes'), 'Toilettes individuelles reliées à un égout', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Eau potable', (SELECT id FROM categorie WHERE nom = 'CONFORT DE VIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Eau potable'), 'Pas d''accès à une eau potable', 0),
((SELECT id FROM question WHERE texte = 'Eau potable'), 'Achat d''eau potable en bidon ou point éloigné', 1),
((SELECT id FROM question WHERE texte = 'Eau potable'), 'Point d''eau potable à moins de 30 min aller-retour', 2),
((SELECT id FROM question WHERE texte = 'Eau potable'), 'Eau potable courante au domicile', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Habitat', (SELECT id FROM categorie WHERE nom = 'CONFORT DE VIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Habitat'), 'Problème sur les 4 domaines : mur, toit, sol, taille', 0),
((SELECT id FROM question WHERE texte = 'Habitat'), 'Problème sur 2 ou 3 domaines', 1),
((SELECT id FROM question WHERE texte = 'Habitat'), 'Problème sur 1 domaine', 2),
((SELECT id FROM question WHERE texte = 'Habitat'), 'Aucun problème sur les 4 domaines', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Combustible pour la cuisine', (SELECT id FROM categorie WHERE nom = 'CONFORT DE VIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Combustible pour la cuisine'), 'Aucun', 0),
((SELECT id FROM question WHERE texte = 'Combustible pour la cuisine'), 'Bois, tout-venant', 1),
((SELECT id FROM question WHERE texte = 'Combustible pour la cuisine'), 'Charbon', 2),
((SELECT id FROM question WHERE texte = 'Combustible pour la cuisine'), 'Gaz, pétrole ou autre', 3);

INSERT INTO question (texte, categorie_id) VALUES ('Équipement / électroménager', (SELECT id FROM categorie WHERE nom = 'CONFORT DE VIE'));
INSERT INTO reponse (question_id, texte, score) VALUES
((SELECT id FROM question WHERE texte = 'Équipement / électroménager'), 'Minimum (natte, ustensiles de cuisine, pas de meuble ni électroménager)', 0),
((SELECT id FROM question WHERE texte = 'Équipement / électroménager'), 'Petit mobilier rudimentaire et petit équipement électroménager (radio OU ventilateur OU télévision)', 1),
((SELECT id FROM question WHERE texte = 'Équipement / électroménager'), 'Mobilier de base (lit, placard), plusieurs équipements électroménagers', 2),
((SELECT id FROM question WHERE texte = 'Équipement / électroménager'), 'Confort (table, chaise, frigo, …)', 3);
