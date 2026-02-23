package org.steam2.client.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.client.entites.*;

import java.util.List;

public class SessionDAO {
    private final EntityManagerFactory emf;

    public SessionDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void persister(Session session) {
        try (EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.persist(session);
            em.getTransaction().commit();
        }
    }

    public List<Session> getJoueurJeuSessions(Joueur joueur, JeuVideo jeuVideo){
        Integer jour_id = joueur.getId();
        Integer jeu_id = jeuVideo.getId();
        try (EntityManager em = emf.createEntityManager()){
            return em.createQuery("FROM Session as s WHERE s.id = :jeu_id AND s.id = :joueur_id", Session.class).getResultList();
        }
    }

    public Integer getTempsJoueTotal(Joueur joueur, JeuVideo jeu){
        List<Session> sessions;
        sessions = getJoueurJeuSessions(joueur, jeu);
        Integer temps_total_m = 0;
        for (Session session : sessions){
            temps_total_m += session.getTempsJoueM();
        }
        return temps_total_m;
    }
}
