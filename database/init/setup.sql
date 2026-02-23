-- CREATION DES BASES

CREATE DATABASE steam2_editeur;
CREATE DATABASE steam2_plateforme;

CREATE USER 'editeur'@'%' IDENTIFIED BY 'VsOLkPWO2VbOKp60nH3z';
GRANT ALL PRIVILEGES ON steam2_editeur.* TO 'editeur'@'%';

-- CREATION DES TABLES POUR LA BASE EDITEUR

USE steam2_editeur;

CREATE TABLE editeur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    nom VARCHAR(50) NOT NULL UNIQUE,
    password CHAR(64) NOT NULL -- hash sha256
);

CREATE TABLE joueur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password CHA(64) NOT NULL, -- hash sha256
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    date_naissance DATETIME NOT NULL
);

CREATE TABLE jeu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    editeur_id INT NOT NULL,
    nom VARCHAR(50) NOT NULL UNIQUE,
    plateforme VARCHAR(50) NOT NULL,
    jeu_parent_id INT,
    CONSTRAINT fk_jeu_editeur_id
        FOREIGN KEY (editeur_id) REFERENCES editeur(id),
    CONSTRAINT fk_jeu_parent_id
        FOREIGN KEY (jeu_parent_id) REFERENCES jeu(id)
);

CREATE TABLE session (
    id INT AUTO_INCREMENT PRIMARY KEY,
    jeu_id INT,
    joueur_id INT,
    temps_joue_m INT,
    date_session DATE,
    CONSTRAINT fk_session_jeu_id
        FOREIGN KEY (jeu_id) REFERENCES jeu(id),
    CONSTRAINT fk session_joueur_id
        FOREIGN KEY (joueur_id) REFERENCES joueur(id)
)

CREATE TABLE joueur_jeu (
    joueur_id INT,
    jeu_id INT,
    CONSTRAINT pk_joueur_jeu
        PRIMARY KEY (joueur_id, jeu_id),
    CONSTRAINT fk_joueur_jeu_joueur_id
        FOREIGN KEY (joueur_id) REFERENCES joueur(id),
    CONSTRAINT fk_joueur_jeu_jeu_id
        FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);

CREATE TABLE genre (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE jeu_genre (
    jeu_id INT,
    genre_id INT,
    CONSTRAINT pk_jeu_genre
        PRIMARY KEY (jeu_id, genre_id),
    CONSTRAINT fk_jeu_genre_jeu_id
        FOREIGN KEY (jeu_id) REFERENCES jeu(id),
    CONSTRAINT fk_jeu_genre_genre_id
        FOREIGN KEY (genre_id) REFERENCES genre(id)
);

CREATE TABLE version_jeu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    jeu_id INT NOT NULL,
    commentaire_editeur VARCHAR(1024) NOT NULL,
    generation INT NOT NULL,
    revision INT NOT NULL DEFAULT 0,
    correction INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_version_jeu
        FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);

CREATE TABLE commentaire (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commentaire VARCHAR(1024) NOT NULL,
    date DATETIME NOT NULL,
    jeu_id INT NOT NULL,
    CONSTRAINT fk_commentaire_jeu
        FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);

CREATE TABLE incident (
    id INT AUTO_INCREMENT PRIMARY KEY,
    details VARCHAR(1024) NOT NULL,
    date DATETIME NOT NULL,
    jeu_id INT NOT NULL,
    CONSTRAINT fk_incident_jeu
        FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);

CREATE TABLE detail_modif_patch (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type_modification VARCHAR(50) NOT NULL,
    commentaire VARCHAR(50) NOT NULL,
    version_id INT NOT NULL,
    CONSTRAINT fk_version_detail_modif
        FOREIGN KEY (version_id) REFERENCES version_jeu(id)
);

-- INSERTION DE DONNEES DANS LA BASE EDITEUR

INSERT INTO editeur(type,
                    nom,
                    password)
VALUES ('INDEPENDANT',
        'remiCorp',
        '967520ae23e8ee14888bae72809031b98398ae4a636773e18fff917d77679334' -- motdepasse
);


-- CREATION DES TABLES POUR LA BASE PLATEFORME

USE steam2_plateforme;

CREATE TABLE editeur (
    id INT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    nom VARCHAR(50) NOT NULL UNIQUE,
);

CREATE TABLE jeu (
    id INT PRIMARY KEY,
    editeur_id INT NOT NULL,
    nom VARCHAR(50) NOT NULL UNIQUE,
    plateforme VARCHAR(50) NOT NULL,
    jeu_parent_id INT,
    prix_editeur DECIMAL(5,2) NOT NULL CHECK (prix_editeur >=0),
    CONSTRAINT fk_jeu_editeur_id
     FOREIGN KEY (editeur_id) REFERENCES editeur(id),
    CONSTRAINT fk_jeu_parent_id
     FOREIGN KEY (jeu_parent_id) REFERENCES jeu(id)
);

CREATE TABLE version_jeu (
    id INT PRIMARY KEY,
    jeu_id INT NOT NULL,
    commentaire_editeur VARCHAR(1024) NOT NULL,
    generation INT NOT NULL,
    revision INT NOT NULL DEFAULT 0,
    correction INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_version_jeu
     FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);

CREATE TABLE genre (
   id INT PRIMARY KEY,
   nom VARCHAR(50) NOT NULL UNIQUE
);


CREATE TABLE jeu_genre (
   jeu_id INT,
   genre_id INT,
   CONSTRAINT pk_jeu_genre
       PRIMARY KEY (jeu_id, genre_id),
   CONSTRAINT fk_jeu_genre_jeu_id
       FOREIGN KEY (jeu_id) REFERENCES jeu(id),
   CONSTRAINT fk_jeu_genre_genre_id
       FOREIGN KEY (genre_id) REFERENCES genre(id)
);

CREATE TABLE incident (
    id INT PRIMARY KEY,
    details VARCHAR(1024) NOT NULL,
    date DATETIME NOT NULL,
    jeu_id INT NOT NULL,
    CONSTRAINT fk_incident_jeu
      FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);

CREATE TABLE joueur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password CHAR(64) NOT NULL, -- hash sha256
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    date_naissance DATETIME NOT NULL,
    date_creation DATETIME NOT NULL
);

CREATE TABLE jeu_joueur (
    jeu_id INT,
    joueur_id INT,
    temps_jeu TIME NOT NULL,
    CONSTRAINT pk_jeu_joueur
        PRIMARY KEY (jeu_id,joueur_id),
    CONSTRAINT fk_jeu_joueur_jeu_id
        FOREIGN KEY (jeu_id) REFERENCES jeu(id),
    CONSTRAINT fk_jeu_joueur_joueur_id
        FOREIGN KEY (joueur_id) REFERENCES joueur(id)
);

CREATE TABLE commentaire (
    id INT PRIMARY KEY,
    commentaire VARCHAR(1024) NOT NULL,
    note INT NOT NULL CHECK (note BETWEEN 0 AND 10),
    date DATETIME NOT NULL,
    jeu_id INT NOT NULL,
    joueur_id INT NOT NULL,
    CONSTRAINT fk_commentaire_jeu
        FOREIGN KEY (jeu_id) REFERENCES jeu(id),
    CONSTRAINT fk_commentaire_joueur
        FOREIGN KEY (joueur_id) REFERENCES joueur(id)
);

CREATE TABLE amitie (
    joueur1_id INT,
    joueur2_id INT,
    CONSTRAINT pk_amitie
        PRIMARY KEY (joueur1_id,joueur2_id),
    CONSTRAINT fk_amitie_joueur1_id
        FOREIGN KEY (joueur1_id) REFERENCES joueur(id),
    CONSTRAINT fk_amitie_joueur2_id
        FOREIGN KEY (joueur2_id) REFERENCES joueur(id),
    CONSTRAINT check_amitie
        CHECK (joueur1_id <> joueur2_id) --2 joueurs différents
);

CREATE TABLE abonnement (
    joueur_id INT,
    editeur_id INT,
    CONSTRAINT pk_abonnement
        PRIMARY KEY (joueur_id,editeur_id),
    CONSTRAINT fk_abonnement_joueur_id
        FOREIGN KEY (joueur_id) REFERENCES joueur(id),
    CONSTRAINT fk_abonnement_editeur_id
        FOREIGN KEY (editeur_id) REFERENCES editeur(id)
);