package org.steam2.client.application

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.steam2.client.entites.Commentaire
import org.steam2.client.entites.Incident
import java.sql.Timestamp

class EnvoiIncidents (
    private val producer: KafkaProducer<String, GenericRecord>,
    private val topic: String
) {
    fun envoyer(incident: Incident) {
        val schemaStream = this.javaClass.classLoader.getResourceAsStream("avro/Incident.avsc")
        val schema = Schema.Parser().parse(schemaStream)

        val record = GenericData.Record(schema).apply {
            put("details",incident.details)
            put("date", Timestamp.valueOf(incident.date).time)
            put("jeuId",incident.jeu.id)
        }

        // envoie
        producer.send(ProducerRecord(topic,record))
        producer.flush()

        // 2em flush necessaire lors de la creation du topic
        producer.flush()
    }
}