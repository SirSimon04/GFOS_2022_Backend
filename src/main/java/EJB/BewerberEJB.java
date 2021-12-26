package EJB;

import Entities.Bewerber;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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

    public Bewerber getById(int id){
        return em.find(Bewerber.class, id);
    }

    public Bewerber getByMail(String mail){
        Query query = em.createNamedQuery(Bewerber.class.getSimpleName() + ".findByEmail");
        query.setParameter("email", mail);
        try{
            Bewerber b = (Bewerber) query.getSingleResult();

            return b;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }
}
