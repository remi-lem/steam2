package org.steam2.plateforme.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.plateforme.entites.Incident;

/**
 * Data Access Objetc pour les incidents
 * @author Wilhem
 */
public class IncidentDAO {

    private final EntityManagerFactory emf;

    public IncidentDAO(EntityManagerFactory emf){this.emf = emf;}

    /**
     * Persister une entité Incident dans la base
     * @param incident l'entité à persister
     * @author Wilhem
     */
    public void persister(Incident incident) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(incident);
            em.getTransaction().commit();
        }
    }
}
