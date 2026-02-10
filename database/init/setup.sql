CREATE TABLE editeur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    nom VARCHAR(50) NOT NULL UNIQUE,
    password CHAR(64) NOT NULL -- hash sha256
);

INSERT INTO editeur(type,
                    nom,
                    password)
VALUES ('INDEPENDANT',
        'remiCorp',
        '967520ae23e8ee14888bae72809031b98398ae4a636773e18fff917d77679334' -- motdepasse
);