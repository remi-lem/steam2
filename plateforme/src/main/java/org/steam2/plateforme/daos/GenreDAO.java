package org.steam2.plateforme.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.steam2.plateforme.entites.Genre;

import java.util.List;

/**
 * Data Access Object pour les genres de Jeu
 * @author Wilhem
 */
public class GenreDAO {

    private final EntityManagerFactory emf;

    public GenreDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Persister une entité Genre dans la base
     * @param genre l'entité à persister
     * @author Wilhem
     */
    public void persister(Genre genre) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            em.persist(genre);
            em.getTransaction().commit();
        }
    }

    /**
     * Récupère un genre selon son nom exacte
     * @param nomGenre nom du genre
     * @return l'élément associé, null si non trouvé
     * @author Wilhem
     */
    public Genre getGenreByNom(String nomGenre){
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT g FROM Genre g WHERE g.nom = :nom",
                            Genre.class)
                    .setParameter("nom", nomGenre)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Récupère la liste des genres selon une liste des noms
     * @param listNoms liste des noms des genres
     * @return la liste des éléments associés
     * @author Wilhem
     */
    public List<Genre> getListGenreByNom(List<String> listNoms){
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery(
                            "SELECT g FROM Genre g WHERE g.nom IN :noms",
                            Genre.class)
                    .setParameter("noms", listNoms)
                    .getResultList();
        }
    }
}
