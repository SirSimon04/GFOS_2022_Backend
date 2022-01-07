package EJB;

import Entitiy.Fachgebiet;
import Entitiy.Jobangebot;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * <h1>EJB für Fachgebiete</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Fachgebieten bereit.
 * Sie stellt somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class FachgebietEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Fachgebiet> getAll(){
        return em.createNamedQuery(Fachgebiet.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt ein neues Fachgebiet in die Datenbank ein
     *
     * @param b Fachgebiet
     * @return Fachgebiet mit generierter Id
     */
    public Fachgebiet add(Fachgebiet b){
        em.persist(b);
        em.flush();
        return b;
    }

    /**
     * Diese Methode gibt ein Fachgebiet anhand der Id zurück
     *
     * @param id FachgebietId
     * @return Fachgebiet
     */
    public Fachgebiet getById(int id){
        return em.find(Fachgebiet.class, id);
    }

    /**
     * Diese Methode gibt ein Fachgebiet anhand des Namens zurück
     *
     * @param name name
     * @return Fachgebiet
     */
    public Fachgebiet getByName(String name){
        Query query = em.createNamedQuery(Fachgebiet.class.getSimpleName() + ".findByName");
        query.setParameter("name", name);
        try{
            Fachgebiet f = (Fachgebiet) query.getSingleResult();

            return f;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }

    /**
     * Diese Methode gibt die Kopie eines Fachgebiets anhand des Namens wieder.
     * Das bedeutet, dass die Datenbankverbindung des Fachgebiets und aller damit
     * verbunden Jobs getrennt wird
     *
     * @param name Name
     * @return Kopie des Fachgebiets
     */
    public Fachgebiet getCopyByName(String name){
        Query query = em.createNamedQuery(Fachgebiet.class.getSimpleName() + ".findByName");
        query.setParameter("name", name);
        try{
            Fachgebiet f = (Fachgebiet) query.getSingleResult();
            em.detach(f);
            for(Jobangebot j : f.getJobangebotList()){
                em.detach(j);
            }
            return f;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }

    /**
     * Diese Methode gibt alle Fachgebiete zurück, die vom Chef angepinnt wurden
     *
     * @return angepinnte Fachgebiete
     */
    public List<Fachgebiet> getPinnedByChef(){
        return em.createNamedQuery("Fachgebiet.findByVonchefgepinnt").setParameter("vonchefgepinnt", true).getResultList();
    }
}
