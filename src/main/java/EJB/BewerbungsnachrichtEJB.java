package EJB;

import Entities.Bewerbungsnachricht;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
public class BewerbungsnachrichtEJB{

    @PersistenceContext
    private EntityManager em;

    public Bewerbungsnachricht add(Bewerbungsnachricht b){
        em.persist(b);
        em.flush();
        return b;
    }

    public Bewerbungsnachricht getById(int id){
        return em.find(Bewerbungsnachricht.class, id);
    }

    public void remove(Bewerbungsnachricht b){
        em.remove(b);
    }
}
