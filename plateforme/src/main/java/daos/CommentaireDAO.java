package daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import entites.Commentaire;

/**
 * Data Access Object de Commentaire
 * @author Wilhem
 */
public class CommentaireDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public CommentaireDAO(EntityManagerFactory emf){this.emf = emf;}


    /**
     * Récupérer les commentaires d'un jeu
     * @param idJeu l'identifiant du jeu dont l'on veut les commentaires
     * @param page numéro de la page (0 = première page)
     * @return une liste de {@value #MAX_RESULTS} Commentaires du jeu
     * @author Wilhem
     */
    public List<Commentaire> getCommentairesJeu(Integer idJeu, Integer page){
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT c FROM Commentaire c " +
                            "JOIN c.jeu j " +
                            "WHERE j.id = :idJeu " +
                            "ORDER BY c.date DESC, c.id DESC", Commentaire.class)
                    .setParameter("idJeu", idJeu)
                    .setFirstResult(page* MAX_RESULTS)
                    .setMaxResults(MAX_RESULTS)
                    .getResultList();
        }
    }
}
