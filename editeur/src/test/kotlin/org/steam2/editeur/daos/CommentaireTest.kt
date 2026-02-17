package org.steam2.editeur.daos

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.junit.jupiter.api.RepeatedTest
import org.steam2.editeur.entites.Commentaire
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Properties

/**
 * Tests de l'entité Commentaire
 * @author remi
 */
internal class CommentaireTest {
    /**
     * Test de création de commentaires clients
     * Cette méthode envoie les commentaires au format Avro sur le topic Kafka, qui sont ensuite lus par le service
     * @see org.steam2.editeur.application.RecupererCommentaires
     * @author remi
     */
    @RepeatedTest(30)
    fun creerCommentaires() {
        // Récupération du paramétrage Kafka
        val props = Properties()
        val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("kafkaCommentaires.properties")
        props.load(inputStream)
        val topic = props.getProperty("topic.name")

        // Kafka
        val producer = KafkaProducer<String, GenericRecord>(props)

        val schemaStream = this.javaClass.classLoader.getResourceAsStream("avro/Commentaire.avsc")
        val schema = Schema.Parser().parse(schemaStream)

        // objets java
        val com = Commentaire()
        com.commentaire = "Test Rémi"
        com.date = LocalDateTime.now()

        val timestamp = com.date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val record = GenericData.Record(schema).apply {
            put("jeuId", 1)
            put("date", timestamp)
            put("commentaire", com.commentaire)
        }

        // envoi
        producer.send(ProducerRecord(topic, record))
        producer.flush()

    }
}