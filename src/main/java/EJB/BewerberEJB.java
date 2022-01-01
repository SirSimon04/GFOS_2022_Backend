package EJB;

import Entities.Bewerber;
import Entities.Bewerbereinstellungen;
import Entities.Bewerbung;
import Entities.Datei;
import Entities.Fachgebiet;
import Entities.Foto;
import Entities.Interessenfelder;
import Entities.Lebenslaufstation;
import Service.Tokenizer;
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

    private Tokenizer tokenizer = new Tokenizer();

    public List<Bewerber> getAll(){
        return em.createNamedQuery(Bewerber.class.getSimpleName() + ".findAll").getResultList();
    }

    public Bewerber add(Bewerber b){
        em.persist(b);
        em.flush();
        return b;
    }

    public void delete(Bewerber b){
        em.remove(b);
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

    public Bewerber getByToken(String token){

        String mail = tokenizer.getMail(token);

        Query query = em.createNamedQuery(Bewerber.class.getSimpleName() + ".findByEmail");
        query.setParameter("email", mail);
        try{
            Bewerber b = (Bewerber) query.getSingleResult();

            return b;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }

    public void addLebenslaufstation(Bewerber b, Lebenslaufstation l){
        b.getLebenslaufstationList().add(l);
    }

    public void removeLebenslaufstation(Bewerber b, Lebenslaufstation l){
        b.getLebenslaufstationList().remove(l);
    }

    public void addInteressengebiet(Bewerber b, Interessenfelder f){
        b.getInteressenfelderList().add(f);
    }

    public void removeInteressengebiet(Bewerber b, Interessenfelder f){
        b.getInteressenfelderList().remove(f);
    }

    public void setFachgebiet(Bewerber b, Fachgebiet f){
        b.setFachgebiet(f);
    }

    public void setProfilbild(Bewerber b, Foto f){
        b.setProfilbild(f);
    }

    public void setLebenslauf(Bewerber b, Datei d){
        b.setLebenslauf(d);
    }

    public void setSettings(Bewerber b, Bewerbereinstellungen e){
        b.getEinstellungen().setGetmails(e.getGetmails());
        b.getEinstellungen().setIspublic(e.getIspublic());
    }

    public void addBewerbung(Bewerber b, Bewerbung bewerbung){
        b.getBewerbungList().add(bewerbung);
    }
}
