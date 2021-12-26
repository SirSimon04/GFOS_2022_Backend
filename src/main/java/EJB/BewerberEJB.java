package EJB;

import Entities.Bewerber;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
public class BewerberEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Bewerber> getAll(){
        return em.createNamedQuery(Bewerber.class.getSimpleName() + ".findAll").getResultList();
    }

    public Bewerber add(Bewerber b){
        em.persist(b);
        em.flush();
        return b;
    }
}
