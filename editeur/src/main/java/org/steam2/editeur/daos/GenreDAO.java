package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.editeur.entites.Genre;

import java.util.List;

/**
 * Data Access Object faisant les accès aux entités Genre
 * @author remi
 */
public class GenreDAO {

    private final EntityManagerFactory emf;

    public GenreDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Récupération de tous les genres possibles
     * @return les entités genre enregistrées dans la base
     * @author remi
     */
    public List<Genre> getGenres() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("FROM Genre ORDER BY nom ASC", Genre.class).getResultList();
        }
    }

    /**
     * Persister une entité Genre dans la base
     * @param genre l'entité à persister
     * @author remi
     */
    public void persister(Genre genre) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(genre);
            em.getTransaction().commit();
        }
    }
}
