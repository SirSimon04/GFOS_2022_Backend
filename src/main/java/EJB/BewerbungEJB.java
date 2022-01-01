package EJB;

import Entities.Bewerber;
import Entities.Bewerbung;
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
public class BewerbungEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Bewerbung> getAll(){
        return em.createNamedQuery(Bewerbung.class.getSimpleName() + ".findAll").getResultList();
    }

    public Bewerbung add(Bewerbung b){
        em.persist(b);
        em.flush();
        return b;
    }

    public void remove(Bewerbung b){
        em.remove(b);
    }

    public Bewerbung getById(int id){
        try{
            return em.find(Bewerbung.class, id);
        }catch(NoResultException e){
            return null;
        }
    }
}
