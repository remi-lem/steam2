package org.steam2.plateforme.plateforme.application

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.steam2.plateforme.daos.IncidentDAO
import org.steam2.plateforme.daos.JeuVideoDAO
import java.time.Duration
import org.steam2.plateforme.entites.Incident
import java.time.Instant
import java.time.ZoneId

/**
 * Tansferer les rapport d'incident des joueurs avec Kafka
 * @author Wilhem
 */
class TransfererIncidents (private val consumer : KafkaConsumer<String, GenericRecord>,private val producer: KafkaProducer<String, GenericRecord>, private val topic: String,
                           private val incidentDAO : IncidentDAO, private val jeuVideoDAO: JeuVideoDAO) {

    companion object {
        const val DELAI_ATTENTE : Long = 5000; //5s
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    //Schéma d'un incident
    private val schema: Schema = Schema.Parser().parse(
        this.javaClass.classLoader.getResourceAsStream("avro/Incident.avsc")
    )

    private var isRunning = true;

    fun stop(){
        isRunning = false;
    }

    suspend fun launch(){
        try {
            while (isRunning) {
                //Enregistrer un incident
                val records = consumer.poll(Duration.ofMillis(TransfererIncidents.Companion.DELAI_ATTENTE))

                for (record in records){
                    log.info("Nouveau rapport d'incident reçu : ${record.value()}");
                    val genericIncident : GenericRecord = record.value();

                    val incident_details = genericIncident.get("details").toString()
                    val timestamp = genericIncident.get("date") as Long
                    val jeu_id = genericIncident.get("jeuId") as Int

                    //calculer la date
                    val incident_date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()

                    //récupérer le jeu
                    val jeu = jeuVideoDAO.getJeuVideoById(jeu_id)
                        ?: throw RuntimeException("Jeu introuvable : $jeu_id")

                    val incident = Incident()

                    incident.details = incident_details
                    incident.date = incident_date
                    incident.jeu = jeu

                    incidentDAO.persister(incident)
                    log.info("Incident enregistré : ${incident.id}")

                    //Maj du prix
                    jeuVideoDAO.majPrixVenteJeu(jeu)
                    log.info("Nouveau prix du jeu :${jeu.prix_vente}")

                    //envoyé à Editeur
                    log.info("Début du transfert")


                    val recordEnvoi = GenericData.Record(schema).apply {
                        put("details", incident_details)
                        put("date", timestamp)
                        put("jeuId", jeu_id)
                    }

                    // envoi
                    producer.send(ProducerRecord(topic, recordEnvoi))
                }
            }
        } finally {
            consumer.close()
            producer.close()
        }
    }
}