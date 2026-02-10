package org.steam2.entites;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.steam2.exceptions.LoginException;

import java.util.List;

public class EditeurDAO {

    private EntityManagerFactory emf;

    public EditeurDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Editeur identifier(String nom, String hashPassword) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Editeur> q = em.createQuery(
                "SELECT e FROM Editeur e WHERE e.nom = :nom", Editeur.class);
            q.setParameter("nom", nom);

            List<Editeur> resultats = q.getResultList();

            if (resultats.isEmpty()) {
                throw new LoginException("Aucun éditeur trouvé avec le nom : " + nom);
            }

            Editeur e = resultats.get(0);

            if (e.getPassword().equals(hashPassword)) {
                return e;
            } else {
                throw new LoginException("Mot de passe incorrect pour l'éditeur " + nom);
            }
        } finally {
            em.close();
        }
    }
}
