package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.client.entites.Genre;

import java.util.List;

public class GenreDAO {

    private final EntityManagerFactory emf;

    public GenreDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<Genre> getGenres() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("FROM Genre ORDER BY nom ASC", Genre.class).getResultList();
        }
    }

    public void persister(Genre genre) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(genre);
            em.getTransaction().commit();
        }
    }
}
