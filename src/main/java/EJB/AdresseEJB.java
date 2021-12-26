package EJB;

import Entities.Adresse;
import Entities.Bewerber;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
@LocalBean
public class AdresseEJB{

    @PersistenceContext
    private EntityManager em;

    public Adresse add(Adresse a){
        em.persist(a);
        em.flush();
        return a;
    }
}
