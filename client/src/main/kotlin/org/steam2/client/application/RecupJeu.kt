package org.steam2.client.application

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.steam2.client.daos.JeuVideoDAO
import org.steam2.client.entites.JeuVideo

class RecupJeu (
    private val consumer: KafkaConsumer<String, GenericRecord>,
    private val topic: String,
    private val jeuVideoDAO: JeuVideoDAO
) {
    private val log = LoggerFactory.getLogger(RecupJeu::class.java)
    fun recuperer(jeu: JeuVideo) {
        val schemaStream = this.javaClass.classLoader.getResourceAsStream("avro/JeuVideo.avsc")
        val schema = Schema.Parser().parse(schemaStream)


    }
}