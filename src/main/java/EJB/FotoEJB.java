package EJB;

import Entities.Foto;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * <h1>EJB für Fotos</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Fotos bereit.
 * Sie stellt somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class FotoEJB{

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode gibt alle Fotos zurück
     *
     * @return Liste mit allen Fotos
     */
    public List<Foto> getAll(){
        return em.createNamedQuery(Foto.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt ein Foto in die Datenbank ein
     *
     * @param f Foto
     * @return Foto mit generierter Id
     */
    public Foto add(Foto f){
        em.persist(f);
        em.flush();
        return f;
    }

    /**
     * Diese Methode löscht ein Foto
     *
     * @param f Foto
     */
    public void remove(Foto f){
        em.remove(f);
    }

    /**
     * Diese Methode gibt ein Foto anhand der Id wieder
     *
     * @param id FotoId
     * @return Foto
     */
    public Foto getById(int id){
        try{
            return em.find(Foto.class, id);
        }catch(NoResultException e){
            return null;
        }
    }
}
