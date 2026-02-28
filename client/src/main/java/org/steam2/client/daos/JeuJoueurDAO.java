package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.steam2.client.entites.JeuJoueur;
import org.steam2.client.entites.JeuVideo;
import org.steam2.client.entites.Joueur;

import java.util.List;

public class JeuJoueurDAO {
    private final EntityManagerFactory emf;

    public JeuJoueurDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void persister(JeuJoueur jeuJoueur) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(jeuJoueur);
            em.getTransaction().commit();
        }
    }
    public Boolean possede(Joueur joueur, JeuVideo jeu){
        try (EntityManager em = emf.createEntityManager()){
            TypedQuery<JeuJoueur> query = em.createQuery("SELECT e from JeuJoueur e WHERE e.jeuVideo = :jeu and e.joueur = :joueur", JeuJoueur.class)
                    .setParameter("jeu", jeu)
                    .setParameter("joueur", joueur);
            List<JeuJoueur> result = query
                    .getResultList();
            return !result.isEmpty();
        }
    }
    public List<JeuVideo> JeuxPossedes(Joueur joueur){
        try (EntityManager em = emf.createEntityManager()){
            TypedQuery<JeuVideo> query = em.createQuery("SELECT jeu from JeuVideo jeu JOIN JeuJoueur jj ON jeu = jj.jeuVideo WHERE jj.joueur = :joueur AND jeu.jeuParent IS NULL", JeuVideo.class)
                    .setParameter("joueur",joueur);
            return query.getResultList();
        }
    }
}
