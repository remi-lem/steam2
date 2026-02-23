package daos;

import entites.JeuVideo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

/**
 * Data Access Object de JeuVideo
 * @author Wilhem
 */
public class JeuVideoDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public JeuVideoDAO(EntityManagerFactory emf){this.emf = emf;}

    /**
     * Récupère la liste des jeux sans aucun filtre
     * @param page numéro de la page (0 = première page)
     * @return une liste de {@value #MAX_RESULTS} jeux
     * @author Wilhem
     */
    public List<JeuVideo> getJeux(Integer page){
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT j FROM Jeu j ", JeuVideo.class)
                    .setFirstResult(page* MAX_RESULTS)
                    .setMaxResults(MAX_RESULTS)
                    .getResultList();
        }
    }
}
