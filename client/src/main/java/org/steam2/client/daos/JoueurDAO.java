package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.client.entites.*;

import java.time.Duration;
import java.util.List;

public class JoueurDAO {
    private final EntityManagerFactory emf;

    public JoueurDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Joueur getJoueurById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Joueur.class, id);
        }
    }

    public void persister(Joueur joueur){
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(joueur);
            em.getTransaction().commit();
        }
    }
}
