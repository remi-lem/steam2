package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.editeur.entites.Incident;

import java.util.List;

public class IncidentDAO {

    public static final int MAX_RESULTS = 20;

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

    public List<Incident> getIncidents() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("FROM Incident ORDER BY date DESC, id DESC", Incident.class)
                     //.setMaxResults(MAX_RESULTS)
                     .getResultList();
        }
    }
}
