package EJB;

import Entities.Jobangebot;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Stateless
@LocalBean
public class JobangebotEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Jobangebot> getAll(){
        return em.createNamedQuery(Jobangebot.class.getSimpleName() + ".findAll").getResultList();
    }

    public Jobangebot add(Jobangebot j){
        em.persist(j);
        em.flush();
        return j;
    }

    public void remove(Jobangebot j){
        em.remove(j);
    }

    public Jobangebot getById(int id){
        try{
            return em.find(Jobangebot.class, id);
        }catch(NoResultException e){
            return null;
        }
    }
}
