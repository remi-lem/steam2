package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.client.entites.Commentaire;
import org.steam2.client.entites.Incident;

public class IncidentDAO {
    private final EntityManagerFactory emf;

    public IncidentDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void persister(Incident incident) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(incident);
            em.getTransaction().commit();
        }
    }
}
