package org.steam2.plateforme.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.plateforme.entites.Joueur;

/**
 * Data Access Object pour les joueurs
 * @author Wilhem
 */
public class JoueurDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public JoueurDAO(EntityManagerFactory emf){this.emf = emf;}

    /**
     * Récupérer une entité joueur à l'aide de son username
     * @param joueur_username l'identifiant du joueur que l'on cherche
     * @return l'entité correspondante
     * @author Wilhem
     */
    public Joueur getJoueurByUsername(String joueur_username){
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Joueur.class, joueur_username);
        }
    }
}
