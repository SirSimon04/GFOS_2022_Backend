package EJB;

import Entities.Bewerber;
import Entities.Bewerbereinstellungen;
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
public class BewerbereinstellungenEJB{

    @PersistenceContext
    private EntityManager em;

    public List<Datei> getAll(){
        return em.createNamedQuery(Bewerbereinstellungen.class.getSimpleName() + ".findAll").getResultList();
    }

    public Bewerbereinstellungen add(Bewerbereinstellungen b){
        em.persist(b);
        em.flush();
        return b;
    }

    public void remove(Bewerbereinstellungen b){
        em.remove(b);
    }

    public Bewerbereinstellungen getById(int id){
        try{
            return em.find(Bewerbereinstellungen.class, id);
        }catch(NoResultException e){
            return null;
        }
    }
}
