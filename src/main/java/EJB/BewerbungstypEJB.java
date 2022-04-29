package EJB;

import Entitiy.Bewerber;
import Entitiy.Bewerbungstyp;
import Entitiy.Jobangebot;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * <h1>EJB für Bewerbungstypen</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Bewerbungstypen bereit. Sie stellt
 * somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class BewerbungstypEJB {

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode gibt alle Bewerbungstypen zurück.
     *
     * @return Liste mit allen Bewerbungstypen
     */
    public List<Bewerbungstyp> getAll() {
        return em.createNamedQuery(Bewerbungstyp.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt einen neuen Bewerbungstyp in die Datenbank ein
     *
     * @param t Bewerbungstyp
     * @return Bewerbungstyp
     */
    public Bewerbungstyp add(Bewerbungstyp t) {
        em.persist(t);
        em.flush();
        return t;
    }

    /**
     * Diese Methode löscht einen Bewerbungstyp
     *
     * @param t Bewerbungstyp
     */
    public void remove(Bewerbungstyp t) {
        em.remove(t);
    }

    /**
     * Diese Methode gibt einen Bewerbungstyp anhand seiner Id zurück
     *
     * @param id BewerbungstypId
     * @return Bewerbungstyp
     */
    public Bewerbungstyp getById(int id) {
        try {
            return em.find(Bewerbungstyp.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Diese Methode gibt einen Bewerbungstyp anhand des Namens zurück
     *
     * @param art Name
     * @return Bewerbungstyp
     */
    public Bewerbungstyp getByName(String art) {
        Query query = em.createNamedQuery("Bewerbungstyp.findByArt");
        query.setParameter("art", art);
        try {
            Bewerbungstyp b = (Bewerbungstyp) query.getSingleResult();

            return b;
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }
}
