package EJB;

import Entitiy.Bewerbereinstellungen;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * <h1>EJB für Bewerbereinstellungen</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Bewerbereinstellungen bereit. Sie
 * stellt somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class BewerbereinstellungenEJB {

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode gibt alle Bewerbereinstellungen zurück
     *
     * @return Liste mit allen Bewerbereinstellungen
     */
    public List<Bewerbereinstellungen> getAll() {
        return em.createNamedQuery(Bewerbereinstellungen.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt Bewerbereinstellungen in die Datenbank ein
     *
     * @param b Bewerbereinstellungen
     * @return Bewerbereinstellungen mit generierter Id
     */
    public Bewerbereinstellungen add(Bewerbereinstellungen b) {
        em.persist(b);
        em.flush();
        return b;
    }

    /**
     * Diese Methode entfernt Bewerbereinstellungen
     *
     * @param b Bewerbereinstellungen
     */
    public void remove(Bewerbereinstellungen b) {
        em.remove(b);
    }

    /**
     * Diese Methode gibt Bewerbereinstellungen anhand der Id zurück
     *
     * @param id Bewerbereinstellungsid
     * @return Bewerbereinstellungen
     */
    public Bewerbereinstellungen getById(int id) {
        try {
            return em.find(Bewerbereinstellungen.class, id);
        } catch (NoResultException e) {
            return null;
        }
    }
}
