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
import org.steam2.plateforme.daos.JoueurDAO
import org.steam2.plateforme.entites.Commentaire
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * Tansférer les commentaires des joueurs
 * @author Wilhem
 */
class TransfererCommentaire(private val consumer : KafkaConsumer<String, GenericRecord>,private val producer: KafkaProducer<String, GenericRecord>, private val topic: String,
                            private val commentaireDAO: CommentaireDAO, private val jeuVideoDAO: JeuVideoDAO, private val joueurDAO : JoueurDAO) {

    companion object {
        const val DELAI_ATTENTE : Long = 5000; //5s
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private var isRunning = true

    //Schéma d'un commentaire
    private val schema: Schema = Schema.Parser().parse(
        this.javaClass.classLoader.getResourceAsStream("avro/Commentaire.avsc")
    )

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
                    val note = genericCommentaire.get("note") as Int
                    val jeu_id = genericCommentaire.get("jeuId") as Int
                    val joueur_username = genericCommentaire.get("joueurUsername").toString()

                    //calculer la date
                    val commentaire_date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()

                    //récupérer le jeu
                    val jeu = jeuVideoDAO.getJeuVideoById(jeu_id)
                        ?: throw RuntimeException("Jeu introuvable : $jeu_id")

                    //récupérer le joueur
                    val joueur = joueurDAO.getJoueurByUsername(joueur_username)

                    val commentaire = Commentaire()

                    commentaire.commentaire = comTexte
                    commentaire.date = commentaire_date
                    commentaire.note = note
                    commentaire.jeu = jeu
                    commentaire.joueur = joueur

                    commentaireDAO.persister(commentaire)
                    log.info("Commentaire enregistré : ${commentaire.id}")

                    //maj note
                    jeuVideoDAO.majNoteJeu(jeu)
                    log.info("Nouvelle note du jeu : ${jeu.note}")
                    log.info("Nouveau prix du jeu :${jeu.prix_vente}")

                    //Envoyé à l'éditeur
                    log.info("Début du transfert")

                    val recordEnvoi = GenericData.Record(schema).apply{
                        put("commentaire",comTexte)
                        put("date",timestamp)
                        put("note",note)
                        put("jeuId",jeu_id)
                        put("joueurUsername",joueur_username)
                    }

                    //envoi
                    producer.send(ProducerRecord(topic, recordEnvoi))

                    log.info("Commentaire transférer à Editeur")

                }
            }
        } finally {
            consumer.close()
            producer.close()
        }
    }
}