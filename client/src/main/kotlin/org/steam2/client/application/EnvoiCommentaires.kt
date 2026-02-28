package org.steam2.client.application

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.steam2.client.entites.Commentaire
import java.sql.Timestamp

class EnvoiCommentaires(
    private val producer: KafkaProducer<String, GenericRecord>,
    private val topic: String
) {

    fun envoyer(commentaire: Commentaire) {
        val schemaStream = this.javaClass.classLoader.getResourceAsStream("avro/Commentaire.avsc")
        val schema = Schema.Parser().parse(schemaStream)

        val record = GenericData.Record(schema).apply {
            put("commentaire",commentaire.commentaire)
            put("date", Timestamp.valueOf(commentaire.date).time)
            put("note", commentaire.note)
            put("jeuId",commentaire.jeu.id)
            put("joueurUsername",commentaire.joueur.username)
        }

        // envoie
        producer.send(ProducerRecord(topic,record))
        producer.flush()

        // 2em flush necessaire lors de la creation du topic
        producer.flush()
    }
}