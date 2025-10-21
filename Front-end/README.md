# Student e42t Hub

Ce projet est une application web complète conçue pour les étudiants de 42, offrant des fonctionnalités de suivi de présence, de gestion de certificats, et de visualisation de calendrier académique.

L'application se compose de deux parties principales :
- Un **back-end** en Java Spring Boot qui gère la logique métier et les interactions avec la base de données et l'API de 42.
- Un **front-end** en React (avec Vite) qui offre une interface utilisateur moderne et réactive.

## Prérequis

Avant de commencer, assurez-vous d'avoir les outils suivants installés sur votre système :

- **Java Development Kit (JDK)** : Version 17 ou supérieure.
- **Apache Maven** : Pour la gestion des dépendances et la construction du projet back-end.
- **Node.js** : Version 18 ou supérieure.
- **npm** (ou yarn) : Inclus avec Node.js, pour la gestion des dépendances du front-end.

## Installation

1.  Clonez le dépôt sur votre machine locale :
    ```bash
    git clone https://github.com/tramitso/student-e42t-hub.git
    cd student-e42t-hub
    ```

2.  Installez les dépendances du front-end :
    ```bash
    npm install
    ```

## Lancement de l'application

Pour que l'application fonctionne, le back-end et le front-end doivent être lancés simultanément.

### 1. Lancement du Back-end (Java)

Le back-end est configuré pour s'exécuter sur le port `9090`.

1.  Naviguez vers le répertoire du projet back-end :
    ```bash
    cd 42
    ```

2.  Utilisez le script fourni pour compiler et lancer le serveur Spring Boot :
    ```bash
    ./run_spring.sh
    ```

    Alternativement, vous pouvez utiliser Maven directement :
    ```bash
    mvn spring-boot:run
    ```

Le serveur back-end est maintenant en écoute. Laissez ce terminal ouvert.

### 2. Lancement du Front-end (React)

Le front-end est servi par Vite et est configuré pour communiquer avec le back-end.

1.  Ouvrez un **nouveau terminal** et placez-vous à la racine du projet.

2.  Lancez le serveur de développement Vite :
    ```bash
    npm run dev
    ```

3.  Ouvrez votre navigateur et accédez à l'URL affichée dans le terminal (généralement `http://localhost:5173`).

L'application devrait maintenant être entièrement fonctionnelle, vous redirigeant vers la page de connexion de l'intranet 42 pour l'authentification.

## Structure du Projet

- `42/` : Contient le projet back-end Java Spring Boot.
  - `src/main/java/` : Code source de l'application.
  - `pom.xml` : Fichier de configuration Maven.
- `src/` : Contient le projet front-end React.
  - `components/` : Composants React réutilisables.
  - `pages/` : Pages principales de l'application.
  - `styles/` : Feuilles de style CSS.
  - `App.jsx` : Composant racine de l'application.
- `package.json` : Dépendances et scripts du projet front-end.
- `vite.config.js` : Configuration du build tool Vite.