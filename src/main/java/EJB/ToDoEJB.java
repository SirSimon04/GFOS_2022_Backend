package EJB;

import Entitiy.Todo;
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
public class ToDoEJB{

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode gibt alle Fotos zurück
     *
     * @return Liste mit allen Fotos
     */
    public List<Todo> getAll(){
        return em.createNamedQuery(Todo.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt ein Todo in die Datenbank ein.
     *
     * @param t das neue Todo
     * @return neues Todo mit generierter Id
     */
    public Todo add(Todo t){
        em.persist(t);
        em.flush();
        return t;
    }
}
