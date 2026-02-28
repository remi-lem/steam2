package org.steam2.plateforme.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.steam2.plateforme.entites.Joueur;

import javax.security.auth.login.LoginException;
import java.util.List;

/**
 * Data Access Object pour les joueurs
 * @author Wilhem
 */
public class JoueurDAO {

    public static final int MAX_RESULTS = 20;

    private final EntityManagerFactory emf;

    public JoueurDAO(EntityManagerFactory emf){this.emf = emf;}

    /**
     * Persister un joueur en base
     * @param joueur Le joueur à persister
     * @author Jules
     */
    public void persister(Joueur joueur){
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(joueur);
            em.getTransaction().commit();
        }
    }

    /**
     * Authentification d'un joueur
     * @param username login de l'éditeur
     * @param hashPassword mot de passe haché
     * @return l'éditeur identifié
     * @author Jules
     */
    public Joueur identifier(String username, String hashPassword) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Joueur> q = em.createQuery(
                    "SELECT e FROM Joueur e WHERE 'username' = :username", Joueur.class);
            q.setParameter("username", username);

            List<Joueur> resultats = q.getResultList();

            if (resultats.isEmpty()) {
                throw new LoginException("Aucun éditeur trouvé avec le nom : " + username);
            }

            Joueur e = resultats.getFirst();

            // TODO : Finir la requête de connexion
            /*
            if (e..equals(hashPassword)) {
                return e;
            } else {
                throw new LoginException("Mot de passe incorrect pour l'éditeur " + nom);
            }
             */
            return e;
        } catch (LoginException e) {
            throw new RuntimeException(e);
        }
    }

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

    /**
     * Fonction permettant d'ajouter une amitié entre deux joueurs dans la table amitie
     *
     * @param friendUsername, String, Pseudo de l'ami
     * @param userConnectedUsername, String, Pseudo de la personne actuellement connectée à la plateforme
     * @return résultat de la requête HQL
     *
     * @author Jules
     */
    public void addFriendship(String friendUsername, String userConnectedUsername){
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery(
                            "insert into amitie (joueur1_id, joueur2_id) values (:userConnectedUsername, :friendUsername)")
                    .setParameter("friendUsername", friendUsername)
                    .setParameter("userConnectedUsername", userConnectedUsername)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }

    /**
     * Fonction permettant de supprimer une amitié entre deux joueurs dans la table amitie
     *
     * @param friendUsername, String, Pseudo de l'ami
     * @param userConnectedUsername, String, Pseudo de la personne actuellement connectée à la plateforme
     *
     * @author Jules
     */
    public void deleteFriendship(String friendUsername, String userConnectedUsername){
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery(
                "delete from amitie where" +
                    " (joueur1_username = :userConnectedUsername and joueur2_username = :friendUsername)" +
                    " or (joueur1_username = :friendUsername and joueur2_username = :userConnectedUsername)")
                    .setParameter("friendUsername", friendUsername)
                    .setParameter("userConnectedUsername", userConnectedUsername)
                    .executeUpdate();
            em.getTransaction().commit();
        }
    }
}
