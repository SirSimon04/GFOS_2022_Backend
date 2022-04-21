package EJB;

import Entitiy.Adresse;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * <h1>EJB für Adressen</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Adressen bereit. Sie stellt somit eine
 * Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class AdresseEJB {

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode fügt eine neue Adresse in die Datenbank ein
     *
     * @param a neue Adresse
     * @return die eingefügte Adresse mit generierter Id
     */
    public Adresse add(Adresse a) {
        em.persist(a);
        em.flush();
        return a;
    }

    /**
     * Diese Methode gibt eine Adresse anhand ihrer Id zurück
     *
     * @param id AdresseId
     * @return Adresse
     */
    public Adresse getById(int id) {
        return em.find(Adresse.class, id);
    }

    public void remove(Adresse a) {
        em.remove(a);
    }
}
