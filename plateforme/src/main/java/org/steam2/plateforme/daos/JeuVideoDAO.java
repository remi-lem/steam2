package org.steam2.plateforme.daos;

import org.steam2.plateforme.entites.JeuVideo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

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
     */
    public List<JeuVideo> getJeux(Integer page){
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT j FROM Jeu j ", JeuVideo.class)
                    .setFirstResult(page* MAX_RESULTS)
                    .setMaxResults(MAX_RESULTS)
                    .getResultList();
        }
    }
}
