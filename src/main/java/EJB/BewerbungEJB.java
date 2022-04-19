package EJB;

import Entitiy.Bewerbung;
import Entitiy.Personaler;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
public class BewerbungEJB {

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode gibt alle Bewerbungen zurück
     *
     * @return Liste mit allen Bewerbungen
     */
    public List<Bewerbung> getAll() {
        return em.createNamedQuery(Bewerbung.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt eine Bewerbung in die Datenbank ein
     *
     * @param b Die Bewerbung
     * @return Die Bewerbung mit automatisch generierter Id
     */
    public Bewerbung add(Bewerbung b) {
        em.persist(b);
        em.flush();
        return b;
    }

    /**
     * Diese Methode löscht eine Bewerbung aus der Datenbank.
     *
     * @param b Die zu köschende Bewerbung
     */
    public void remove(Bewerbung b) {
        em.remove(b);
    }

    /**
     * Diese Methode gibt eine Bewerbung anhand der Id wieder
     *
     * @param id Die Id
     * @return Die Bewerbung oder null
     */
    public Bewerbung getById(int id) {
        try {
            return em.find(Bewerbung.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Diese Methode gibt alle Bewerbungen zurück, an denen ein Personaler
     * arbeitet.
     *
     * @param p Der Personaler
     * @return Liste mit zu bearbeitenden Bewerbungen
     */
    public List<Bewerbung> getEditable(Personaler p) {
        List<Bewerbung> allAplications = this.getAll();
        List<Bewerbung> myApplications = new ArrayList<>();

        for (Bewerbung b : allAplications) {
            if (b.getPersonalerList().contains(p)) {
                myApplications.add(b);
            }
        }
        return myApplications;
    }
}
