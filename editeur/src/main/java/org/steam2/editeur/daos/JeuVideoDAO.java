package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.editeur.entites.JeuVideo;

/**
 * Data Access Object faisant les accès aux entités Jeu Vidéo
 * @author remi
 */
public class JeuVideoDAO {

    private final EntityManagerFactory emf;

    public JeuVideoDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Récupération d'un jeu à parirtr de son id
     * @see org.steam2.editeur.application.RecupererIncidents
     * @see org.steam2.editeur.application.RecupererCommentaires
     * @param id l'identifiant du jeu
     * @return l'entité Jeu Vidéo correspondante
     * @author remi
     */
    public JeuVideo getJeuVideoById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(JeuVideo.class, id);
        }
    }

    /**
     * Enregistrement d'une entité Jeu Vidéo en base
     * @param jeuVideo l'entité à persister
     * @author remi
     */
    public void persister(JeuVideo jeuVideo) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(jeuVideo);
            em.getTransaction().commit();
        }
    }
}
