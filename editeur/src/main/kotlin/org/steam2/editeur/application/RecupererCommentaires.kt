package org.steam2.editeur.application

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.steam2.editeur.daos.CommentaireDAO
import org.steam2.editeur.daos.JeuVideoDAO
import org.steam2.editeur.entites.Commentaire
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Service de récupération des commentaires
 * @author remi
 */
class RecupererCommentaires(private val consumer: KafkaConsumer<String, GenericRecord>,
                            private val jeuVideoDAO: JeuVideoDAO,
                            private val commentaireDAO: CommentaireDAO) {

    companion object {
        const val DELAI_ATTENTE: Long = 5000 // 5 secondes
    }

    private val log = LoggerFactory.getLogger(RecupererCommentaires::class.java)

    private var isRunning = true

    fun stop() {
        isRunning = false
    }

    suspend fun launch() {
        try {
            while (isRunning) {
                val records = consumer.poll(Duration.ofMillis(DELAI_ATTENTE))

                for (record in records) {
                    log.info("Nouveau commentaire reçu : ${record.value()}")
                    val genericCommentaire: GenericRecord = record.value()

                    val jeuId = genericCommentaire.get("jeuId") as Int
                    val timestamp = genericCommentaire.get("date") as Long
                    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())

                    val commentaireTxt = genericCommentaire.get("commentaire").toString()

                    val commentaireObj = Commentaire()
                    commentaireObj.jeu = jeuVideoDAO.getJeuVideoById(jeuId)
                    commentaireObj.date = date
                    commentaireObj.commentaire = commentaireTxt

                    commentaireDAO.persister(commentaireObj)
                }
            }
        } finally {
            consumer.close()
        }
    }
}
