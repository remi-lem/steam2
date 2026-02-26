# Projet JVM & Data

## Présentation

Projet réalisé en APP4 à Polytech Paris-Saclay.

Projet réalisé par :
- [Rémi L.](https://github.com/remi-lem/)
- [Wilhem M.](https://github.com/Guenks)
- [Jules C.](https://github.com/JJulles)
- [Nino K.](https://github.com/riioze)

## Organisation de l'application

Ce dépot comprend les différents sous-projets qui le composent :
- [plateforme](./plateforme)
- [client](./client)
- [editeur](./editeur)

## Mise en place

- Mettre en place Apache Kafka, Schema Registry et MySQL via Docker Compose :
```bash
docker compose up -d
```

- Compiler les 3 sous-projets Maven :
```bash
mvn compile -f plateforme
mvn compile -f client
mvn compile -f editeur
```

## Utilisation

- Lancer les 3 sous-projets Maven via leurs classes `Main.kt`
  - Soit via la configuration IntelliJ inclue dans chaque sous-projet (recommandé)
  - Soit via maven :
```bash
mvn exec:java -f plateforme
mvn exec:java -f client
mvn exec:java -f editeur
```

- Accès à Kafka UI : [http://localhost:8080/](http://localhost:8080/)

- Accès à PhpMyAdmin : [http://localhost:9090/](http://localhost:9090/)

Si le lancement dans IntelliJ affiche des erreurs, vider les caches dans : \
`File > Invalidate caches > Clear file system cache and Local History`

## Réinitialisation de la base de données

Éxécuter :
```bash
docker container stop mysql
docker container rm mysql
docker volume rm steam2_mysql_data
```

## Réinitialisation de Kafka

On peut vouloir purger un topic kafka si des données de précédents tests restent dedans, pour cela, le plus simple est dans [kafka-ui](https://localhost:8080/)

## Détails techniques

Ce programme a été codé et testé avec `openjdk-21`

## Librairies utilisées
- `Kotlin` version `2.2.21`
- `JUnit Jupiter` version `5.10.0`
- `Kotlin Coroutines` version `1.10.2`
- `Project Lombok` version `1.18.42`
- `Kotlin Maven Lombok` version `2.2.21`
- `Maven Compiler Plugin` version `3.15.0`
- `MySql Connector J` version `9.6.0`
- `Hibernate` version `7.2.4.Final`
- `Kafka clients` version `8.1.1-ccs`
- `Kafka Avro Serializer` version `8.1.1`
- `Apache Avro` version `1.12.1`
- `Google Guava` version `33.5.0-jre`
- `Google Lanterna` version `3.1.3`
- `slf4j Simple` version `1.7.36`

## Icônes des languages, librairies et outils utilisés
- ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
- ![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
- ![JUnit5](https://img.shields.io/badge/JUnit5-f5f5f5?style=for-the-badge&logo=junit5&logoColor=dc524a)
- ![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
- ![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
- ![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka)
- ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
- ![Maven](https://img.shields.io/badge/apachemaven-C71A36.svg?style=for-the-badge&logo=apachemaven&logoColor=white)
- ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)
