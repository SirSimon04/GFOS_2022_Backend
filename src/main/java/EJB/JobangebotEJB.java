package EJB;

import Entities.Bewerbungstyp;
import Entities.Fachgebiet;
import Entities.Jobangebot;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * <h1>EJB für Jobangebote</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Jobangeboten bereit.
 * Sie stellt somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class JobangebotEJB{

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode gibt alle Jobangebote zurück
     *
     * @return Liste mit allen Jobangeboten
     */
    public List<Jobangebot> getAll(){
        return em.createNamedQuery(Jobangebot.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt ein Jobangebot in die Datenbank ein
     *
     * @param j Jobangebote
     * @return Jobangebote mit generierter Id
     */
    public Jobangebot add(Jobangebot j){
        em.persist(j);
        em.flush();
        return j;
    }

    /**
     * Diese Methode löscht ein Jobangebot aus der Datenbank
     *
     * @param j Jobangebot
     */
    public void remove(Jobangebot j){
        em.remove(j);
    }

    /**
     * Diese Methode gibt ein Jobangebot anhand der Id wieder
     *
     * @param id JobangebotId
     * @return Jobangebot
     */
    public Jobangebot getById(int id){
        try{
            return em.find(Jobangebot.class, id);
        }catch(NoResultException e){
            return null;
        }
    }

    /**
     * Diese Methode gibt alle vom Chef angepinnten Jobangebote zurück
     *
     * @return angepinnte Jobangebote
     */
    public List<Jobangebot> getPinnedByChef(){
        return em.createNamedQuery("Jobangebot.findByVonchefgepinnt").setParameter("vonchefgepinnt", true).getResultList();
    }
}
