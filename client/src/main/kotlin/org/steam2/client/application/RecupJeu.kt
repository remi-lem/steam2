package org.steam2.client.application

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.steam2.client.daos.EditeurDAO
import org.steam2.client.daos.GenreDAO
import org.steam2.client.daos.JeuVideoDAO
import org.steam2.client.entites.JeuVideo
import org.steam2.client.entites.type.Plateforme
import java.math.BigDecimal
import java.time.Duration

class RecupJeu (
    private val consumer: KafkaConsumer<String, GenericRecord>,
    private val jeuVideoDAO: JeuVideoDAO,
    private val editeurDAO: EditeurDAO,
    private val genreDAO: GenreDAO
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
            log.info("Récupération de potentiels nouveau jeux...")
            for (record in records) {
                log.info("Nouveau jeu reçu")
                val genericJeu: GenericRecord = record.value()

                val jeu_id = genericJeu.get("id") as Int
                val jeu_nom = genericJeu.get("nom").toString()
                val editeur_id = genericJeu.get("editeur_id") as Int
                val prix_editeur = BigDecimal.valueOf((genericJeu.get("prix") as Float).toDouble())

                val plateformeStr = genericJeu.get("plateforme").toString()
                val jeu_parent_id = genericJeu.get("jeu_parent_id") as? Int

                val listNomGenres = (genericJeu.get("genres") as List<*>).map {it.toString()}

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
                jeu.plateforme = Plateforme.valueOf(plateformeStr)

                val editeur = editeurDAO.getEditeurById(editeur_id)
                    ?: throw RuntimeException("Editeur introuvable : $editeur_id")

                jeu.editeur = editeur

                if (jeu_parent_id != null){
                    val parent = jeuVideoDAO.getJeuVideoById(jeu_parent_id)
                    jeu.jeuParent = parent
                }

                val genres = genreDAO.getListGenreByNom(listNomGenres)

                //warn si des genres ont été perdu
                if (genres.size != listNomGenres.size){
                    log.warn("Des genres ont été perdu dans le transfert car inexistant dans la base")
                }
                jeu.genres = genres

                jeuVideoDAO.persister(jeu)

                log.info("Jeu sauvegardé : id=$jeu_id")

            }
        }
    }
}