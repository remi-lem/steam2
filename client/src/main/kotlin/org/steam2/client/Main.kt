package org.steam2.client

import jakarta.persistence.Persistence
import kotlinx.coroutines.runBlocking
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import org.steam2.client.daos.*;
import java.util.Properties

private val log = LoggerFactory.getLogger("Main")

fun main() = runBlocking {
    log.info("Démarrage de l'application client")

    // DAOs
    val emf = Persistence.createEntityManagerFactory("steam2-client")
    val editeurDAO = EditeurDAO(emf)
    val jeuVideoDAO = JeuVideoDAO(emf)
    val commentaireDAO = CommentaireDAO(emf)
    val genreDAO = GenreDAO(emf)
    val joueurDAO = JoueurDAO(emf)
    val sessionDAO = SessionDAO(emf)

    // Récupération du paramétrage Kafka
    val props = Properties()
    val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka.properties")
    props.load(inputStream)
    val topic = props.getProperty("topic.name")

    // Kafka
    val producer = KafkaProducer<String, GenericRecord>(props)
    val consumer = KafkaConsumer<String, GenericRecord>(props)
    consumer.subscribe(listOf(topic))
}