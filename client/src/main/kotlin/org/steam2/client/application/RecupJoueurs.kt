package org.steam2.client.application

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.Consumer
import org.slf4j.LoggerFactory
import org.steam2.client.daos.JeuJoueurDAO
import org.steam2.client.daos.JoueurDAO
import org.steam2.client.entites.Joueur
import org.steam2.client.entites.type.PlateformeJeu
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class RecupJoueurs (
    private val consumer: Consumer<String, GenericRecord>,
    private val joueurDAO: JoueurDAO
) {
    companion object {
        const val DELAI_ATTENTE : Long = 5000; //5s
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private var isRunning = true;

    fun stop(){
        isRunning = false;
    }

    fun launch(){
        try{
            while(isRunning){
                val records = consumer.poll(Duration.ofMillis(DELAI_ATTENTE))
                records.isEmpty
                for (record in records){
                    log.info("Nouveau joueur reçu : ${record.value()}")
                    val genericJoueur: GenericRecord = record.value()


                    val newPlayerUsername = genericJoueur.get("username") as String
                    val newPlayerNom = genericJoueur.get("nom") as String
                    val newPlayerPrenom = genericJoueur.get("prenom") as String
                    val newPlayerDateNaissance = Instant.ofEpochMilli(genericJoueur.get("date_naissance") as Long).atZone(
                        ZoneId.systemDefault()).toLocalDate()
                    val newPlayerDateCreation = Instant.ofEpochMilli(genericJoueur.get("date_naissance") as Long).atZone(
                        ZoneId.systemDefault()).toLocalDate()
                    val newPlayerPlateformeJeu = PlateformeJeu.valueOf(genericJoueur.get("plateforme") as String)
                    val newPlayerPassword = genericJoueur.get("password") as String

                    val nouveauJoueur = Joueur().apply {
                        username = newPlayerUsername
                        nom = newPlayerNom
                        prenom = newPlayerPrenom
                        date_naissance = newPlayerDateNaissance
                        date_creation = newPlayerDateCreation
                        plateformeJeu = newPlayerPlateformeJeu
                        password = newPlayerPassword
                    }

                    joueurDAO.persister(nouveauJoueur)


//


                    log.info("joueur sauvegardé : id=${nouveauJoueur.id}")
                }
            }
        } finally {
            consumer.close()
        }
    }
}