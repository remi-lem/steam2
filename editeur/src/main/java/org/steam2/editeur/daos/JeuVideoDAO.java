package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.editeur.entites.Genre;
import org.steam2.editeur.entites.JeuVideo;

import java.util.List;

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
     * Récupération des genres d'un JeuVidéo
     * @param jeuVideo le jeu concerné
     * @return la liste des genres du jeu
     */
    public List<Genre> getGenresByJeuVideo(JeuVideo jeuVideo) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                "SELECT g FROM JeuVideo j JOIN j.genres g WHERE j.id = :id ORDER BY g.nom ASC", Genre.class)
                .setParameter("id", jeuVideo.getId())
                .getResultList();
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
