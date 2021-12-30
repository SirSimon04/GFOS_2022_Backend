package EJB;

import Entities.Bewerber;
import Entities.Bewerbungstyp;
import Entities.Jobangebot;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
@LocalBean
public class BewerbungstypEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Bewerbungstyp> getAll(){
        return em.createNamedQuery(Bewerbungstyp.class.getSimpleName() + ".findAll").getResultList();
    }

    public Bewerbungstyp add(Bewerbungstyp j){
        em.persist(j);
        em.flush();
        return j;
    }

    public void remove(Bewerbungstyp j){
        em.remove(j);
    }

    public Bewerbungstyp getById(int id){
        try{
            return em.find(Bewerbungstyp.class, id);
        }catch(NoResultException e){
            return null;
        }
    }

    public Bewerbungstyp getByMail(String art){
        Query query = em.createNamedQuery(Bewerbungstyp.class.getSimpleName() + ".findByArt");
        query.setParameter("art", art);
        try{
            Bewerbungstyp b = (Bewerbungstyp) query.getSingleResult();

            return b;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }
}
