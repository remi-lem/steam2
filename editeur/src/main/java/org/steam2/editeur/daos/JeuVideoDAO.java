package org.steam2.editeur.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Version;
import kotlin.Pair;
import org.steam2.editeur.entites.DetailModifPatch;
import org.steam2.editeur.entites.JeuVideo;
import org.steam2.editeur.entites.VersionJeu;
import org.steam2.editeur.entities.type.TypeModificationPatch;

import java.util.List;

public class JeuVideoDAO {

    private final EntityManagerFactory emf;

    public JeuVideoDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public List<JeuVideo> getAllJeuxVideos() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT j FROM JeuVideo j", JeuVideo.class).getResultList();
        }
    }

    public JeuVideo getJeuVideoById(Integer id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(JeuVideo.class, id);
        }
    }

    public void persister(JeuVideo jeuVideo) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(jeuVideo);
            em.getTransaction().commit();
        }
    }
}
