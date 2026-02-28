package org.steam2.editeur.application

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.steam2.editeur.daos.CommentaireDAO
import org.steam2.editeur.daos.JeuVideoDAO
import org.steam2.editeur.entites.Commentaire
import org.steam2.editeur.entites.JeuVideo
import org.steam2.editeur.entites.VersionJeu
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Service d'envoi de jeux sur le topic Kafka
 * @author remi
 */
class EnvoiJeux(private val producer: KafkaProducer<String, GenericRecord>,
                private val topic: String,
                private val jeuVideoDAO: JeuVideoDAO) {

    private val log = LoggerFactory.getLogger(EnvoiJeux::class.java)

    fun envoyer(jeu: JeuVideo, version: VersionJeu) {
        val schemaStream = this.javaClass.classLoader.getResourceAsStream("avro/JeuVideo.avsc")
        val schema = Schema.Parser().parse(schemaStream)

        // on force la récupération des genres
        val listeGenres = jeuVideoDAO.getGenresByJeuVideo(jeu)

        val record = GenericData.Record(schema).apply {
            put("id", jeu.id)
            put("nom", jeu.nom)
            put("editeur_id", jeu.editeur.id)
            put("prix", jeu.prix)

            put("plateforme", GenericData.EnumSymbol(schema.getField("plateforme").schema(), jeu.plateforme.name))

            put("jeu_parent_id", jeu.jeuParent?.id) // null si non DLC

            val versionSchema = schema.getField("version").schema()
            val versionRecord = GenericData.Record(versionSchema).apply {
                put("commentaire_editeur", version.commentaireEditeur)

                put("generation", version.generation)
                put("revision", version.revision)
                put("correction", version.correction)

                // Cas d'un patch
                if(version.listeDesModifications != null && version.listeDesModifications.isNotEmpty()) {
                    val arraySchema = versionSchema.getField("listeDesModifications").schema().types[1]
                    val itemSchema = arraySchema.elementType

                    val avroList = GenericData.Array<GenericRecord>(version.listeDesModifications.size, arraySchema)

                    version.listeDesModifications.forEach { modif ->
                        val modifRecord = GenericData.Record(itemSchema).apply {
                            val enumSymbol = GenericData.EnumSymbol(itemSchema.getField("type_modification").schema(), modif.typeModificationPatch.name)
                            put("type_modification", enumSymbol)
                            put("commentaire", modif.commentaire)
                        }
                        avroList.add(modifRecord)
                    }

                    put("listeDesModifications", avroList)
                }
                else {
                    put("listeDesModifications", null)
                }
            }
            put("version", versionRecord)

            val nomsDesGenres = listeGenres.map { it.nom }
            put("genres", nomsDesGenres)
        }

        // envoi
        producer.send(ProducerRecord(topic, record))
        producer.flush()

        // 2 flush nécessaires lors de la création du topic
        producer.flush()
    }

}
