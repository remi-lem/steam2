package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import kotlin.Pair;
import org.steam2.editeur.entites.DetailModifPatch;
import org.steam2.editeur.entites.JeuVideo;
import org.steam2.editeur.entites.VersionJeu;
import org.steam2.editeur.entities.type.TypeModificationPatch;

import java.util.List;

public class VersionDAO {

    private final EntityManagerFactory emf;

    public VersionDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void persister(VersionJeu version) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(version);
            em.getTransaction().commit();
        }
    }

    public void publierPatch(JeuVideo jeuVideo, List<Pair<TypeModificationPatch, String>> modifications, String commentaire) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // changement de la version
            VersionJeu version = em.createQuery(
                "SELECT v FROM VersionJeu v WHERE v.jeu.id = :id ORDER BY v.id DESC", VersionJeu.class)
                .setParameter("id", jeuVideo.getId())
                .setMaxResults(1)
                .getSingleResult();
            Integer correction = version.getCorrection();
            correction++;
            version.setCorrection(correction);

            // ajout du commentaire
            version.setCommentaireEditeur(commentaire);

            // enregistrement des modifications
            for (Pair<TypeModificationPatch, String> modif : modifications) {
                DetailModifPatch detail = new DetailModifPatch();
                detail.setTypeModificationPatch(modif.getFirst());
                detail.setCommentaire(modif.getSecond());
                detail.setVersion(version);

                em.persist(detail);
            }

            em.getTransaction().commit();
        }
    }
}
