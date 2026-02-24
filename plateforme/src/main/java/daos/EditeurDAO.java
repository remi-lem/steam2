package daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

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
    public entites.Editeur getEditeurById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(entites.Editeur.class, id);
        }
    }
}
