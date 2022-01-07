package EJB;

import Entitiy.Datei;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * <h1>EJB für Dateien</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Dateien bereit.
 * Sie stellt somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class DateiEJB{

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode gibt alle Datein zurück
     *
     * @return Liste mit allen Dateien
     */
    public List<Datei> getAll(){
        return em.createNamedQuery(Datei.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt eine neue Datei in die Datenbank ein
     *
     * @param d Datei
     * @return Datei mit generierter Id
     */
    public Datei add(Datei d){
        em.persist(d);
        em.flush();
        return d;
    }

    /**
     * Diese Methode löscht eine Datei
     *
     * @param d Datei
     */
    public void delete(Datei d){
        em.remove(d);
    }

    /**
     * Diese Methode gibt eine Datei anhand ihrer Id zurück
     *
     * @param id DateId
     * @return Datei
     */
    public Datei getById(int id){
        try{
            return em.find(Datei.class, id);
        }catch(NoResultException e){
            return null;
        }
    }
}
