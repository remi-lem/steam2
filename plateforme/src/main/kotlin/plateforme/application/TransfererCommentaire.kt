package org.steam2.plateforme.plateforme.application

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.steam2.plateforme.daos.CommentaireDAO
import org.steam2.plateforme.daos.JeuVideoDAO
import org.steam2.plateforme.entites.Commentaire
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * Tansférer les commentaires des joueurs
 * @author Wilhem
 */
class TransfererCommentaire(private val consumer : KafkaConsumer<String, GenericRecord>,private val producer: KafkaProducer<String, GenericRecord>, private val topic: String,
                            private val commentaireDAO: CommentaireDAO, private val jeuVideoDAO: JeuVideoDAO) {

    companion object {
        const val DELAI_ATTENTE : Long = 5000; //5s
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private var isRunning = true;

    fun stop(){
        isRunning = false;
    }

    suspend fun launch(){
        try {
            while (isRunning) {
                //Enregistrer un commentaire
                val records = consumer.poll(Duration.ofMillis(TransfererIncidents.Companion.DELAI_ATTENTE))

                for (record in records){
                    log.info("Nouveau commentaire reçu : ${record.value()}")
                    val genericCommentaire : GenericRecord = record.value()

                    val comTexte = genericCommentaire.get("commentaire").toString()
                    val timestamp = genericCommentaire.get("date") as Long
                    val jeu_id = genericCommentaire.get("jeuId") as Int

                    //calculer la date
                    val commentaire_date = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()

                    //récupérer le jeu
                    val jeu = jeuVideoDAO.getJeuVideoById(jeu_id)
                        ?: throw RuntimeException("Jeu introuvable : $jeu_id")

                    val commentaire = Commentaire()

                    commentaire.commentaire = comTexte
                    commentaire.date = commentaire_date
                    commentaire.jeu = jeu

                    commentaireDAO.persister(commentaire)
                    log.info("Commentaire enregistré : ${commentaire.id}")

                    //Envoyé à l'éditeur
                    log.info("Début du transfert")

                    val schemaStream = this.javaClass.classLoader.getResourceAsStream("avro/Commentaire.avsc")
                    val schema = Schema.Parser().parse(schemaStream)

                    val recordEnvoi = GenericData.Record(schema).apply{
                        put("commentaire",comTexte)
                        put("date",timestamp)
                        put("jeuId",jeu_id)
                    }

                    //envoi
                    producer.send(ProducerRecord(topic, recordEnvoi))
                    producer.flush()

                    // 2 flush nécessaires lors de la création du topic
                    producer.flush()

                }
            }
        } finally {
            consumer.close()
        }
    }
}