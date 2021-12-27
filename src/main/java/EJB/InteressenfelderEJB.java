package EJB;

import Entities.Bewerber;
import Entities.Fachgebiet;
import Entities.Interessenfelder;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
@LocalBean
public class InteressenfelderEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Interessenfelder> getAll(){
        return em.createNamedQuery(Interessenfelder.class.getSimpleName() + ".findAll").getResultList();
    }

    public Interessenfelder add(Interessenfelder f){
        em.persist(f);
        em.flush();
        return f;
    }

    public void remove(Interessenfelder f){
        em.remove(f);
    }

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
