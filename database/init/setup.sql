-- CREATION DES TABLES

CREATE TABLE editeur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    nom VARCHAR(50) NOT NULL UNIQUE,
    password CHAR(64) NOT NULL -- hash sha256
);

CREATE TABLE jeu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    editeur_id INT,
    nom VARCHAR(50) NOT NULL UNIQUE,
    plateforme VARCHAR(50) NOT NULL,
    CONSTRAINT fk_jeu_editeur_id
         FOREIGN KEY (editeur_id) REFERENCES editeur(id)
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
    commentaire_editeur VARCHAR(1024),
    generation INT NOT NULL,
    revision INT NOT NULL DEFAULT 0,
    correction INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_version_jeu
        FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);

CREATE TABLE commentaire (
    id INT AUTO_INCREMENT PRIMARY KEY,
    commentaire VARCHAR(1024) NOT NULL,
    date DATETIME,
    jeu_id INT NOT NULL,
    CONSTRAINT fk_commentaire_jeu
        FOREIGN KEY (jeu_id) REFERENCES jeu(id)
);








-- INSERTION DE DONNEES

INSERT INTO editeur(type,
                    nom,
                    password)
VALUES ('INDEPENDANT',
        'remiCorp',
        '967520ae23e8ee14888bae72809031b98398ae4a636773e18fff917d77679334' -- motdepasse
);