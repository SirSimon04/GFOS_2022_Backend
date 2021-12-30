package EJB;

import Entities.Fachgebiet;
import Entities.Personaler;
import Service.Tokenizer;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
@LocalBean
public class PersonalerEJB{

    @PersistenceContext
    private EntityManager em;

    private Tokenizer tokenizer = new Tokenizer();

    public List<Personaler> getAll(){
        return em.createNamedQuery(Personaler.class.getSimpleName() + ".findAll").getResultList();
    }

    public Personaler add(Personaler b){
        em.persist(b);
        em.flush();
        return b;
    }

    public void delete(Personaler b){
        em.remove(b);
    }

    public Personaler getById(int id){
        return em.find(Personaler.class, id);
    }

    public Personaler getByMail(String mail){
        Query query = em.createNamedQuery(Personaler.class.getSimpleName() + ".findByEmail");
        query.setParameter("email", mail);
        try{
            Personaler b = (Personaler) query.getSingleResult();

            return b;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }

    public Personaler getByToken(String token){

        String mail = tokenizer.getMail(token);

        Query query = em.createNamedQuery(Personaler.class.getSimpleName() + ".findByEmail");
        query.setParameter("email", mail);
        try{
            Personaler b = (Personaler) query.getSingleResult();

            return b;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }

    public void setFachgebiet(Personaler p, Fachgebiet f){
        p.setFachgebiet(f);
    }

}
