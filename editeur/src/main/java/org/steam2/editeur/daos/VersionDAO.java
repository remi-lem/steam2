package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import kotlin.Pair;
import org.steam2.editeur.entites.DetailModifPatch;
import org.steam2.editeur.entites.JeuVideo;
import org.steam2.editeur.entites.VersionJeu;
import org.steam2.editeur.entites.type.TypeModificationPatch;

import java.util.List;

/**
 * Data Access Object faisant les accès aux entités Version
 * @author remi
 */
public class VersionDAO {

    private final EntityManagerFactory emf;

    public VersionDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Enregistrer une entité Version en base
     * @param version l'entiter à persister
     * @author remi
     */
    public void persister(VersionJeu version) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(version);
            em.getTransaction().commit();
        }
    }

    /**
     * Publication d'un patch (ajout de version et enregistrement des détails)
     * @param jeuVideo le jeu concerné
     * @param modifications la liste des modifications contenues dans le patch
     * @param commentaire le commentaire de l'éditeur sur le patch
     */
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
