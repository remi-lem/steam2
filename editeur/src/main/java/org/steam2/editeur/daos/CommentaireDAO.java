package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.editeur.entites.Commentaire;

import java.util.List;

/**
 * Data Access Object faisant les accès aux entités Commentaires
 * @author remi
 */
public class CommentaireDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public CommentaireDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Persister un commentaire en base
     * @param commentaire l'entité à persister
     * @author remi
     */
    public void persister(Commentaire commentaire) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(commentaire);
            em.getTransaction().commit();
        }
    }

    /**
     * Récupération des commentaires
     * @param idEditeur filtre sur l'éditeur consultant les commentaires
     * @return la liste d'entités correspondantes
     * @author remi
     */
    public List<Commentaire> getCommentaires(Integer idEditeur) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT c FROM Commentaire c " +
                    "JOIN c.jeu j " +
                    "JOIN j.editeur e " +
                    "WHERE e.id = :idEditeur " +
                    "ORDER BY c.date DESC, c.id DESC", Commentaire.class)
                    .setParameter("idEditeur", idEditeur)
                     //.setMaxResults(MAX_RESULTS)
                     .getResultList();
        }
    }
}
