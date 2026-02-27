package org.steam2.plateforme.daos;

import org.steam2.plateforme.entites.JeuVideo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Data Access Object de JeuVideo
 * @author Wilhem
 */
public class JeuVideoDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public JeuVideoDAO(EntityManagerFactory emf){this.emf = emf;}

    /**
     * Persister un jeu vidéo en base
     * @param jeu Le jeu vidéo à persister
     * @author Wilhem
     */
    public void persister(JeuVideo jeu){
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(jeu);
            em.getTransaction().commit();
        }
    }

    /**
     * Modifie la note du jeu et appel la méthode qui met à jour le prix
     * @param jeu Le jeu dont il faut modifier la note
     * @author Wilhem
     */
    public void majNoteJeu(JeuVideo jeu){
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();

            //récupérer le jeu (vérifier s'il est en base)
            JeuVideo jeuAttach = em.find(JeuVideo.class, jeu.getId());
            if (jeuAttach == null){
                throw new RuntimeException("Jeu introuvable dans la base");
            }

            //calcul moyenne de la note
            Double moyenne = em.createQuery("SELECT AVG(c.note) FROM Commentaire c WHERE c.jeu.id = :jeuId",
                    Double.class)
                    .setParameter("jeuId", jeu.getId())
                    .getSingleResult();

            //si aucun commentaire, note null
            BigDecimal nouvelleNote = null;
            if (moyenne != null){
                nouvelleNote = BigDecimal.valueOf(moyenne).setScale(2, RoundingMode.HALF_UP);
            }




            //maj dans la bd
            em.createQuery("UPDATE JeuVideo j SET j.note =:note WHERE j.id = :jeuId")
                    .setParameter("note",nouvelleNote)
                    .setParameter("jeuId",jeu.getId())
                    .executeUpdate();

            em.getTransaction().commit();



            //maj de l'élément passé en param
            jeu.setNote(nouvelleNote);

            //lancer une maj du prix
            majPrixVenteJeu(jeu);
        }
    }

    /**
     * Met à jour le prix de vente d'un jeu
     * @param jeu Le jeu dont le prix doit être mis à jour
     * @author Wilhem
     */
    public void majPrixVenteJeu(JeuVideo jeu){
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();

            //récupérer le jeu (vérifier s'il est en base)
            JeuVideo jeuAttach = em.find(JeuVideo.class, jeu.getId());
            if (jeuAttach == null){
                throw new RuntimeException("Jeu introuvable dans la base");
            }

            BigDecimal prix_editeur = jeu.getPrix_editeur();

            //cas jeu gratuit
            if (prix_editeur.compareTo(BigDecimal.ZERO) == 0){
                jeu.setPrix_vente(BigDecimal.ZERO);
            }else{

                //La note
                BigDecimal note = jeu.getNote();
                if (note == null) note = BigDecimal.ZERO; //pour ne pas crash le calcul

                //récupérer le nombre d'incident concernant le jeu
                Long nb_incident = em.createQuery("SELECT COUNT(i) FROM Incident i WHERE i.jeu.id =:jeuId", Long.class)
                        .setParameter("jeuId", jeu.getId())
                        .getSingleResult();

                //Bonus note (+10% max)
                BigDecimal noteNormal = note.divide(new BigDecimal("10"),4,RoundingMode.HALF_UP);
                BigDecimal bonusNote = prix_editeur.multiply(new BigDecimal("0.10")).multiply(noteNormal);

                //Malus nombre incident (max -10%)
                BigDecimal facteurIncident = new BigDecimal(nb_incident).divide(new BigDecimal("10"),4,RoundingMode.HALF_UP);

                //si trop d'incident
                if (facteurIncident.compareTo(BigDecimal.ONE) > 0){
                    facteurIncident = BigDecimal.ONE;
                }
                BigDecimal malusIncident = prix_editeur.multiply(new BigDecimal("0.10")).multiply(facteurIncident);


                //Calcul prix final
                BigDecimal prix_vente = prix_editeur.add(bonusNote).subtract(malusIncident);

                if (prix_vente.compareTo(BigDecimal.ZERO) < 0) {
                    prix_vente = BigDecimal.ZERO;
                }

                //Maj du prix
                em.createQuery("UPDATE JeuVideo j SET prix_vente = :prixVente WHERE j.id = :jeuId")
                        .setParameter("prixVente", prix_vente)
                        .setParameter("jeuId", jeu.getId())
                        .executeUpdate();

                em.getTransaction().commit();

                //maj de l'objet courant
                jeu.setPrix_vente(prix_vente);

            }
        }
    }

    /**
     * Récupération d'un jeu à partir de son id
     * @param id l'identifiant du jeu
     * @return l'entité Jeu Vidéo correspondante
     * @author Wilhem
     */
    public JeuVideo getJeuVideoById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(JeuVideo.class, id);
        }
    }

    /**
     * Récupère la liste des jeux sans aucun filtre
     * @param page numéro de la page (0 = première page)
     * @return une liste de {@value #MAX_RESULTS} jeux
     * @author Wilhem
     *
     * TODO : Faire en sorte que si pas d'argument {page} -> =0 de base
     * TODO : Ici la table est "Jeu" avec majuscule mais dans le sql c'est "jeu"
     */
    public List<JeuVideo> getJeux(Integer page){
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT j FROM JeuVideo j ", JeuVideo.class)
                    .setFirstResult(page* MAX_RESULTS)
                    .setMaxResults(MAX_RESULTS)
                    .getResultList();
        }
    }
}
