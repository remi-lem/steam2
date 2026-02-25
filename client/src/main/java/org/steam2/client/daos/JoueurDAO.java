package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.steam2.client.entites.Joueur;
import org.steam2.client.exceptions.LoginException;

import java.util.List;

public class JoueurDAO {
    private final EntityManagerFactory emf;

    public JoueurDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Joueur getJoueurById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Joueur.class, id);
        }
    }

    public void persister(Joueur joueur){
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(joueur);
            em.getTransaction().commit();
        }
    }

    public Joueur identifier(String username, String passhash){
        try (EntityManager em = emf.createEntityManager()){
            TypedQuery<Joueur> q = em.createQuery(
                    "SELECT e FROM Joueur e WHERE e.username = :username", Joueur.class);
            q.setParameter("username", username);

            List<Joueur> resultats = q.getResultList();

            if (resultats.isEmpty()) {
                throw new LoginException("Aucun aucun joueur trouvé avec le nom : " + username);
            }

            Joueur e = resultats.getFirst();

            if (e.getPassword().equals(passhash)) {
                return e;
            } else {
                throw new LoginException("Mot de passe incorrect pour le joueur " + username);
            }

        }
    }
}
