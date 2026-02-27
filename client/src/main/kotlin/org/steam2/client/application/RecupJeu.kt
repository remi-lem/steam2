package org.steam2.client.application

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.steam2.client.daos.JeuVideoDAO
import org.steam2.client.entites.JeuVideo
import org.steam2.client.entites.type.PlateformeJeu
import java.math.BigDecimal
import java.time.Duration

class RecupJeu (
    private val consumer: KafkaConsumer<String, GenericRecord>,
    private val jeuVideoDAO: JeuVideoDAO
) {
    private val log = LoggerFactory.getLogger(RecupJeu::class.java)
    private var isRunning = true

    fun stop(){
        isRunning = false
    }

    fun recuperer() {
        //val schemaStream = this.javaClass.classLoader.getResourceAsStream("avro/JeuVideo.avsc")
        //val schema = Schema.Parser().parse(schemaStream)

        while (isRunning){
            val records = consumer.poll(Duration.ofMillis(5000))
            for (record in records) {
                log.info("Nouveau jeu reçu")
                val genericJeu: GenericRecord = record.value()

                val jeu_id = genericJeu.get("id") as Int
                val jeu_nom = genericJeu.get("nom").toString()
                val prix_editeur = BigDecimal.valueOf((genericJeu.get("prix") as Float).toDouble())

                val plateformeStr = genericJeu.get("plateforme").toString()
                val jeu_parent_id = genericJeu.get("jeu_parent_id") as? Int

                //check si jeu existant
                var jeu = jeuVideoDAO.getJeuVideoById(jeu_id)
                if (jeu == null) {
                    log.info("Nouveau jeu ! creation...")
                    jeu = JeuVideo()
                    jeu.id = jeu_id
                } else {
                    log.info("Jeu existant ! Mise à jour")
                }

                jeu.nom = jeu_nom
                jeu.prix_editeur = prix_editeur
                jeu.plateforme = PlateformeJeu.valueOf(plateformeStr)

                if (jeu_parent_id != null){
                    val parent = jeuVideoDAO.getJeuVideoById(jeu_parent_id)
                    jeu.jeuParent = parent
                }

                jeuVideoDAO.persister(jeu)

                log.info("Jeu sauvegardé : id=$jeu_id")

            }
        }
    }
}