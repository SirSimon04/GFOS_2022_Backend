package EJB;

import Entities.Interessenfelder;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * <h1>EJB für Interessenfelder</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Interessenfelder bereit.
 * Sie stellt somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class InteressenfelderEJB{

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode gibt alle Interessenfelder zurück
     *
     * @return
     */
    public List<Interessenfelder> getAll(){
        return em.createNamedQuery(Interessenfelder.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt ein Interessenfeld in die Datenbank ein
     *
     * @param f Interessenfelder
     * @return Interessenfelder mit generierter Id
     */
    public Interessenfelder add(Interessenfelder f){
        em.persist(f);
        em.flush();
        return f;
    }

    /**
     * Diese Methode löscht ein Interessenfeld aus der Datenbank
     *
     * @param f Interessenfeld
     */
    public void remove(Interessenfelder f){
        em.remove(f);
    }

    /**
     * Diese Methode gibt ein Interessenfeld anhand der Id wieder
     *
     * @param name InteressenfeldId
     * @return Interessenfeld
     */
    public Interessenfelder getByName(String name){
        Query query = em.createNamedQuery(Interessenfelder.class.getSimpleName() + ".findByName");
        query.setParameter("name", name);
        try{
            Interessenfelder f = (Interessenfelder) query.getSingleResult();

            return f;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }
}
