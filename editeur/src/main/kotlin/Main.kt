package org.steam2.editeur

import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import jakarta.persistence.Persistence
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.steam2.editeur.application.MenuPrincipal
import org.steam2.editeur.entities.type.TypeEditeur
import org.steam2.entites.Editeur
import org.steam2.entites.EditeurDAO
import org.steam2.exceptions.LoginException
import java.io.File
import java.util.Properties

fun main() {
    println("Bienvenue dans l'application de l'éditeur !")

    // DAOs
    val emf = Persistence.createEntityManagerFactory("steam2-editeur")
    val editeurDAO = EditeurDAO(emf)

    // Récupération du paramétrage Kafka
    val props = Properties()
    val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka.properties")
    props.load(inputStream)
    val topic = props.getProperty("topic.name")

    // Kafka
    val producer = KafkaProducer<String, GenericRecord>(props)
    val consumer = KafkaConsumer<String, GenericRecord>(props)
    consumer.subscribe(listOf(topic))

    // Préparation de l'interface (fenêtre)
    val terminal = DefaultTerminalFactory().setTerminalEmulatorTitle("Steam2 - Editeur").createTerminal()
    val screen = TerminalScreen(terminal)
    screen.startScreen()

    // Lancement de l'interface
    val editeur: Editeur? = MenuPrincipal(editeurDAO).afficher(screen)

    if(editeur == null) {
        throw LoginException("Editeur non connecté")
    }

    println("L'editeur ${editeur.nom} s'est connecté")






    //test


    val schemaStream = object {}.javaClass.classLoader.getResourceAsStream("avro/Editeur.avsc")

    val schema = Schema.Parser().parse(schemaStream)

    val record: GenericRecord = GenericData.Record(schema).apply {
        put("id", 12345)
        put("nom", "TEST")
        put("password", "aaaaaaaaaa")
        put("typeEditeur", TypeEditeur.INDEPENDANT.name) //mauvais type
    }

    val pr = ProducerRecord("test", "TEST", record)
    producer.send(pr)

}