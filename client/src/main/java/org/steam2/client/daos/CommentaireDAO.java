package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.client.entites.Commentaire;

import java.util.List;

public class CommentaireDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public CommentaireDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void persister(Commentaire commentaire) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(commentaire);
            em.getTransaction().commit();
        }
    }

    public List<Commentaire> getRecentCommentaires() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("FROM Commentaire ORDER BY date DESC", Commentaire.class)
                     .setMaxResults(MAX_RESULTS)
                     .getResultList();
        }
    }
}
