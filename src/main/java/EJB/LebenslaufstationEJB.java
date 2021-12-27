package EJB;

import Entities.Lebenslaufstation;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
public class LebenslaufstationEJB{

    @PersistenceContext
    private EntityManager em;

    public Lebenslaufstation add(Lebenslaufstation l){
        em.persist(l);
        em.flush();
        return l;
    }

    public void remove(Lebenslaufstation l){
        em.remove(l);
    }

    public Lebenslaufstation getById(int id){
        return em.find(Lebenslaufstation.class, id);
    }
}
