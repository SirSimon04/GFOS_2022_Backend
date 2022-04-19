package EJB;

import Entitiy.Todo;
import java.util.Comparator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * <h1>EJB für Fotos</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Fotos bereit. Sie stellt somit eine
 * Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class ToDoEJB {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private PersonalerEJB personalerEJB;

    /**
     * Diese Methode gibt alle Fotos zurück
     *
     * @return Liste mit allen Fotos
     */
    public List<Todo> getAll() {
        return em.createNamedQuery(Todo.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode gibt ein Todo anhand seiner Id wieder.
     *
     * @param id Id
     * @return Das Todo
     */
    public Todo getById(int id) {
        return em.find(Todo.class, id);
    }

    /**
     * Diese Methode löscht ein Todo aus der Datenbank
     *
     * @param t Das Todo
     */
    public void remove(Todo t) {
        em.remove(t);
    }

    /**
     * Diese Methode fügt ein Todo in die Datenbank ein.
     *
     * @param t das neue Todo
     * @return neues Todo mit generierter Id
     */
    public Todo add(Todo t) {
        em.persist(t);
        em.flush();
        return t;
    }

    /**
     * Diese Methode gibt alle Todos eines Personaler zurück. Diese werden dabei
     * der OderId nach geordnet, so dass der Personaler seine Todos neu
     * sortieren kann
     *
     * @param id Die Id des Personalers
     * @return Liste mit allen Todos
     */
    public List<Todo> getSortedByPersonaler(int id) {
        List<Todo> todos = personalerEJB.getById(id).getTodoList();

        //Sorting by orderId
        todos.sort(new Comparator<Todo>() {
            @Override
            public int compare(Todo t1, Todo t2) {
                return t1.getOrderid().compareTo(t2.getOrderid());
            }
        });

        return todos;
    }
}
