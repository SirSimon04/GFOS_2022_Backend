package EJB;

import Entities.Bewerber;
import Entities.Datei;
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
public class DateiEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Datei> getAll(){
        return em.createNamedQuery(Datei.class.getSimpleName() + ".findAll").getResultList();
    }

    public Datei add(Datei d){
        em.persist(d);
        em.flush();
        return d;
    }

    public void remove(Datei d){
        em.remove(d);
    }

    public Datei getById(int id){
        try{
            return em.find(Datei.class, id);
        }catch(NoResultException e){
            return null;
        }
    }
}
