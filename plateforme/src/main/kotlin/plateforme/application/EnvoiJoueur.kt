package org.steam2.plateforme.plateforme.application

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.steam2.plateforme.daos.JeuVideoDAO
import org.steam2.plateforme.entites.Joueur

/**
 * Service d'envoi de joueur sur le topic kafka, lors de l'inscription d'un joueur
 *
 * @author Jules
 */
class EnvoiJoueur(
    private val producer: KafkaProducer<String, GenericRecord>,
    private val topic: String,
    private val jeuVideoDAO: JeuVideoDAO) {

    private val log = LoggerFactory.getLogger(EnvoiJoueur::class.java)

    /**
     * Fonction envoyant sur le topic un nouveau joueur pour qu'il soit créé
     *
     * @param joueur: Joueur, Joueur à envoyer
     * @author Jules
     */
    fun envoyer(joueur: Joueur){

        // Récupération du schéma Avro
        val schemaStream = this.javaClass.classLoader.getResourceAsStream("avro/Joueur.avsc")
        val schema = Schema.Parser().parse(schemaStream)

        val record = GenericData.Record(schema).apply {
        }

        // Envoi du joueur sur le topic
        producer.send(ProducerRecord(topic, record))
        producer.flush()

        // 2 flush nécessaires lors de la création du topic
        producer.flush()
    }

}