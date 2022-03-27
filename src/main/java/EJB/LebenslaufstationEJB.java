package EJB;

import Entitiy.Lebenslaufstation;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * <h1>EJB für Lebenslaufstationen</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Lebenslaufstationen bereit. Sie stellt
 * somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class LebenslaufstationEJB {

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode fügt eine neue Lebenslaufstation in die Datenbank ein.
     * Außerdem wird die Referenz auf null gesetzt, da diese nicht in der
     * Datenbank gespeichert werden soll.
     *
     * @param l Lebenslaufstation
     * @return Lebenslaufstation mit generierter Id
     */
    public Lebenslaufstation add(Lebenslaufstation l) {
        l.setReferenz(null);
        em.persist(l);
        em.flush();
        return l;
    }

    /**
     * Diese Methode löscht eine Lebenslaufstation aus der Datenbank
     *
     * @param l Lebenslaufstation
     */
    public void remove(Lebenslaufstation l) {
        em.remove(l);
    }

    /**
     * Diese Methode gibt eine Lebenslaufstation anhand der Id zurück
     *
     * @param id LebenslaufstationId
     * @return Lebenslaufstation
     */
    public Lebenslaufstation getById(int id) {
        return em.find(Lebenslaufstation.class, id);
    }
}
