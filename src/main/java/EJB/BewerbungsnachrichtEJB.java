package EJB;

import Entities.Bewerbungsnachricht;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * <h1>EJB für Bewerbungsnachrichten</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Bewerbungsnachrichten bereit.
 * Über diesen können Bewerber und Personaler über eine Bewerbung
 * kommunizieren.
 * Sie stellt somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class BewerbungsnachrichtEJB{

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode fügt eine Bewerbungsnachrichten in die Datenbank ein
     *
     * @param b Bewerbungsnachricht
     * @return Bewerbungsnachricht mit generierter Id
     */
    public Bewerbungsnachricht add(Bewerbungsnachricht b){
        em.persist(b);
        em.flush();
        return b;
    }

    /**
     * Diese Methode gibt eine Bewerbungsnachricht anhand der Id wieder
     *
     * @param id BewerbungsnachrichtId
     * @return Bewerbungsnachricht
     */
    public Bewerbungsnachricht getById(int id){
        return em.find(Bewerbungsnachricht.class, id);
    }

    /**
     * Diese Methode löscht eine Bewerbungsnachricht aus der Datenbank
     *
     * @param b
     */
    public void remove(Bewerbungsnachricht b){
        em.remove(b);
    }
}
