package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.steam2.client.entites.*;
import org.steam2.client.entites.Editeur;
import org.steam2.client.exceptions.LoginException;

import java.util.List;

public class EditeurDAO {

    private final EntityManagerFactory emf;

    public EditeurDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

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
