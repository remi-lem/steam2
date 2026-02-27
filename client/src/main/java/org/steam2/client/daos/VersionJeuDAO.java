package org.steam2.client.daos;

import org.steam2.client.entites.VersionJeu;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Data Access Object pour les versions de jeu
 * @author Wilhem
 */
public class VersionJeuDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public VersionJeuDAO(EntityManagerFactory emf){this.emf = emf;}

    /**
     * Persister une version de jeu en base
     * @param versionJeu La version de jeu à persister
     * @author Wilhem
     */
    public void persister(VersionJeu versionJeu){
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(versionJeu);
            em.getTransaction().commit();
        }
    }
}