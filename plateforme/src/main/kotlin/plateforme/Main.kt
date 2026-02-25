package org.steam2.plateforme

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread
import org.steam2.plateforme.daos.CommentaireDAO
import org.steam2.plateforme.daos.EditeurDAO
import org.steam2.plateforme.daos.GenreDAO
import org.steam2.plateforme.daos.JeuVideoDAO
import jakarta.persistence.Persistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.steam2.plateforme.application.RecupererJeuxVideos
import java.util.Properties

private val log = LoggerFactory.getLogger("Main")

fun main() = runBlocking {
    log.info("Démarrage de l'application de la plateforme")

    //DAOS
    val emf = Persistence.createEntityManagerFactory("steam2-plateforme")
    val editeurDAO = EditeurDAO(emf)
    val jeuVideoDAO = JeuVideoDAO(emf)
    val genreDAO = GenreDAO(emf)
    val commentaireDAO = CommentaireDAO(emf)

    //paramètres Kafka
    val propsJeux = Properties()
    val inputStreamCommonJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/common.properties")
    val inputStreamJeux = Thread.currentThread().contextClassLoader.getResourceAsStream("kafka/jeux.properties")
    propsJeux.load(inputStreamCommonJeux)
    propsJeux.load(inputStreamJeux)
    val topicJeux = propsJeux.getProperty("topic.name")


    //Kafka
    val consumerJeux = KafkaConsumer<String, GenericRecord>(propsJeux)
    consumerJeux.subscribe(listOf(topicJeux))


    log.info("L'application Plateforme est prête")

    //récupérer les infos en arrière plan avec Co Routines Kotlin

    //JV
    val serviceScopeJeux = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val serviceRecuperationJeux = RecupererJeuxVideos(consumerJeux, jeuVideoDAO, editeurDAO, genreDAO)

    val jobServiceJeux = serviceScopeJeux.launch(Dispatchers.IO) {
        serviceRecuperationJeux.launch()
    }


    //WHILE TRUE (pour le moment)
    while (true) {
        val input = readLine()
        if (input.equals("exit", ignoreCase = true)) {
            println("Arrêt demandé...")
            break
        }
    }

    //Fin de programme
    log.info("Fermeture de l'application Plateforme")

    //hibernate
    emf.close()
    AbandonedConnectionCleanupThread.checkedShutdown()
    //kafka
    //fenetre
    //services
    serviceRecuperationJeux.stop()
    log.info("Attente de la fermeture des services de récupération des jeux... " +
            "(${RecupererJeuxVideos.DELAI_ATTENTE * 2 / 1000} secondes max)")
    jobServiceJeux.join()
    serviceScopeJeux.cancel()

    log.info("Merci d'avoir utilisé notre application !")
}