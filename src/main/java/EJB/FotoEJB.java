package EJB;

import Entities.Bewerber;
import Entities.Fachgebiet;
import Entities.Foto;
import Entities.Interessenfelder;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
@LocalBean
public class FotoEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Foto> getAll(){
        return em.createNamedQuery(Foto.class.getSimpleName() + ".findAll").getResultList();
    }

    public Foto add(Foto f){
        em.persist(f);
        em.flush();
        return f;
    }

    public void remove(Foto f){
        em.remove(f);
    }

    public Foto getById(int id){
        try{
            return em.find(Foto.class, id);
        }catch(NoResultException e){
            return null;
        }
    }
}
