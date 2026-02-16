package org.steam2.editeur.application

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.steam2.editeur.daos.CommentaireDAO
import org.steam2.editeur.daos.IncidentDAO
import org.steam2.editeur.daos.JeuVideoDAO
import org.steam2.editeur.entites.Commentaire
import org.steam2.editeur.entites.Incident
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class RecupererIncidents(private val consumer: KafkaConsumer<String, GenericRecord>,
                         private val jeuVideoDAO: JeuVideoDAO,
                         private val incidentDAO: IncidentDAO) {

    companion object {
        const val DELAI_ATTENTE: Long = 5000 // 5 secondes
    }

    private val log = LoggerFactory.getLogger(RecupererIncidents::class.java)

    private var isRunning = true

    fun stop() {
        isRunning = false
    }

    suspend fun launch() {
        try {
            while (isRunning) {
                val records = consumer.poll(Duration.ofMillis(DELAI_ATTENTE))

                for (record in records) {
                    log.info("Nouvel incident reçu : ${record.value()}")
                    val genericIncident: GenericRecord = record.value()

                    val jeuId = genericIncident.get("jeuId") as Int
                    val timestamp = genericIncident.get("date") as Long
                    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())

                    val details = genericIncident.get("details").toString()

                    val incidentObj = Incident()
                    incidentObj.jeu = jeuVideoDAO.getJeuVideoById(jeuId)
                    incidentObj.date = date
                    incidentObj.details = details

                    incidentDAO.persister(incidentObj)
                }
            }
        } finally {
            consumer.close()
        }
    }
}
