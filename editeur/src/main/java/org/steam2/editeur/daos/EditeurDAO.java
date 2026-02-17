package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.steam2.editeur.entites.Editeur;
import org.steam2.editeur.exceptions.LoginException;

import java.util.List;

/**
 * Data Access Object faisant les accès aux entités Editeur
 * @author remi
 */
public class EditeurDAO {

    private final EntityManagerFactory emf;

    public EditeurDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Authentification d'un éditeur
     * @param nom login de l'éditeur
     * @param hashPassword mot de passe haché
     * @return l'éditeur identifié
     * @author remi
     */
    public Editeur identifier(String nom, String hashPassword) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<Editeur> q = em.createQuery(
                    "SELECT e FROM Editeur e WHERE e.nom = :nom", Editeur.class);
            q.setParameter("nom", nom);

            List<Editeur> resultats = q.getResultList();

            if (resultats.isEmpty()) {
                throw new LoginException("Aucun éditeur trouvé avec le nom : " + nom);
            }

            Editeur e = resultats.getFirst();

            if (e.getPassword().equals(hashPassword)) {
                return e;
            } else {
                throw new LoginException("Mot de passe incorrect pour l'éditeur " + nom);
            }
        }
    }

    /**
     * Rafraichissement de l'entité en base
     * @param editeur l'éditeur à rafraichir
     * @author remi
     */
    public Editeur refresh(Editeur editeur) {
        try (EntityManager em = emf.createEntityManager()) {
            editeur = em.merge(editeur);
            em.refresh(editeur);
            return editeur;
        }
    }
}
