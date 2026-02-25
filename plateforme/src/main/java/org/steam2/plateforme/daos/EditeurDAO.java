package org.steam2.plateforme.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.plateforme.entites.Editeur;

/**
 * Data Access Object des éditeurs
 * @author Wilhem
 */
public class EditeurDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public EditeurDAO(EntityManagerFactory emf){this.emf = emf;}

    /**
     * Récupération d'un éditeur à partir de son id
     * @param id l'identifiant de l'éditeur
     * @return l'entité Jeu Vidéo correspondante
     * @author Wilhem
     */
    public Editeur getEditeurById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Editeur.class, id);
        }
    }
}
