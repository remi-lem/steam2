package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.client.entites.JeuVideo;
import java.util.List;

public class JeuVideoDAO {

    private final EntityManagerFactory emf;

    public JeuVideoDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public JeuVideo getJeuVideoById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(JeuVideo.class, id);
        }
    }

    public void persister(JeuVideo jeuVideo) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(jeuVideo);
            em.getTransaction().commit();
        }
    }

    public List<JeuVideo> recupListeJeux() {
        try (EntityManager em = emf.createEntityManager()){
            return em.createQuery("FROM JeuVideo ORDER BY nom DESC", JeuVideo.class)
                    .getResultList();
        }
    }
}
