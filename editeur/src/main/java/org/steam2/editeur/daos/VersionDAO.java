package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import kotlin.Pair;
import org.hibernate.Hibernate;
import org.steam2.editeur.entites.DetailModifPatch;
import org.steam2.editeur.entites.JeuVideo;
import org.steam2.editeur.entites.VersionJeu;
import org.steam2.editeur.entites.type.TypeModificationPatch;

import java.util.ArrayList;
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
     * @return VersionJeu la nouvelle entité VersionJeu
     */
    public VersionJeu publierPatch(JeuVideo jeuVideo, List<Pair<TypeModificationPatch, String>> modifications, String commentaire) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            VersionJeu newVersion = new VersionJeu();

            newVersion.setListeDesModifications(new ArrayList<>());

            // changement de la version
            VersionJeu version = em.createQuery(
                "SELECT v FROM VersionJeu v WHERE v.jeu.id = :id ORDER BY v.id DESC", VersionJeu.class)
                .setParameter("id", jeuVideo.getId())
                .setMaxResults(1)
                .getSingleResult();

            newVersion.setJeu(version.getJeu());
            newVersion.setGeneration(version.getGeneration());
            newVersion.setRevision(version.getRevision());

            Integer correction = version.getCorrection();
            correction++;
            newVersion.setCorrection(correction);

            // ajout du commentaire
            newVersion.setCommentaireEditeur(commentaire);

            em.persist(newVersion);

            // enregistrement des modifications
            for (Pair<TypeModificationPatch, String> modif : modifications) {
                DetailModifPatch detail = new DetailModifPatch();
                detail.setTypeModificationPatch(modif.getFirst());
                detail.setCommentaire(modif.getSecond());
                detail.setVersion(newVersion);

                em.persist(detail);

                newVersion.getListeDesModifications().add(detail);
            }

            em.getTransaction().commit();

            return newVersion;
        }
    }
}
