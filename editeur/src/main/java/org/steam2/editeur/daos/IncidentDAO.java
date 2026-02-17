package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.editeur.entites.Incident;

import java.util.List;

/**
 * Data Access Object faisant les accès aux entités Incident
 * @author remi
 */
public class IncidentDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public IncidentDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Enregistrement d'une entité Incident en base
     * @param incident l'incident à persister
     * @author remi
     */
    public void persister(Incident incident) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(incident);
            em.getTransaction().commit();
        }
    }

    /**
     * Récupération des incidents
     * @param idEditeur filtre sur l'éditeur consultant les incidents
     * @return la liste d'entités correspondantes
     * @author remi
     */
    public List<Incident> getIncidents(Integer idEditeur) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT i FROM Incident i " +
                    "JOIN i.jeu j " +
                    "JOIN j.editeur e " +
                    "WHERE e.id = :idEditeur " +
                    "ORDER BY i.date DESC, i.id DESC", Incident.class)
                    .setParameter("idEditeur", idEditeur)
                     //.setMaxResults(MAX_RESULTS)
                     .getResultList();
        }
    }
}
