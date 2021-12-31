package EJB;

import Entities.Bewerber;
import Entities.Fachgebiet;
import Entities.Jobangebot;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
@LocalBean
public class FachgebietEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Bewerber> getAll(){
        return em.createNamedQuery(Fachgebiet.class.getSimpleName() + ".findAll").getResultList();
    }

    public Fachgebiet add(Fachgebiet b){
        em.persist(b);
        em.flush();
        return b;
    }

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

    public void addJobangebot(Jobangebot j, Fachgebiet f){
        f.getJobangebotList().add(j);
    }
}
