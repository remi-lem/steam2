package org.steam2.plateforme.plateforme.application

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import org.steam2.plateforme.daos.EditeurDAO
import org.steam2.plateforme.daos.GenreDAO
import org.steam2.plateforme.daos.JeuVideoDAO
import org.steam2.plateforme.entites.Genre
import org.steam2.plateforme.entites.JeuVideo
import org.steam2.plateforme.entites.type.TypeEditeur
import org.steam2.plateforme.plateforme.entites.type.Plateforme
import java.math.BigDecimal
import java.time.Duration

/**
 * Récupérer les jeux vidéos
 * @author Wilhem
 */
class RecupererJeuxVideos(private val consumer: KafkaConsumer<String, GenericRecord>,
                          private val jeuVideoDAO: JeuVideoDAO, private val editeurDAO: EditeurDAO, private val genreDAO: GenreDAO
) {

    companion object {
        const val DELAI_ATTENTE : Long = 5000; //5s
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private var isRunning = true;

    fun stop(){
        isRunning = false;
    }

    suspend fun launch(){
        try{
            while(isRunning){
                val records = consumer.poll(Duration.ofMillis(DELAI_ATTENTE))

                for (record in records){
                    log.info("Nouveau jeu reçu : ${record.value()}")
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
                        jeu.prix_vente = prix_editeur
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

                    //liste des genres
                    val genres = mutableListOf<Genre>()
                    var genre : Genre
                    for (nomGenre in listNomGenres){
                        genre = genreDAO.getGenreByNom(nomGenre)?: run{
                            //si genre pas trouvé
                            //créer le genre inexistant
                            log.info("Genre $nomGenre inexistent! Création du genre dans la base...")
                            val genre = Genre()
                            genre.nom = nomGenre
                            genreDAO.persister(genre)
                            log.info("Genre $nomGenre ajouté à la base")
                            genre
                        }
                        genres.add(genre)
                    }

                    //warn si des genres ont été perdu
                    if (genres.size != listNomGenres.size){
                        log.warn("Des genres ont été perdu dans le transfert car inexistant dans la base")
                    }
                    jeu.genres = genres

                    jeuVideoDAO.persister(jeu)

                    //Une fois persister on calcul son prix de vente
                    jeuVideoDAO.majPrixVenteJeu(jeu);

                    log.info("Jeu sauvegardé : id=$jeu_id")
                }
            }
        } finally {
            consumer.close()
        }
    }
}