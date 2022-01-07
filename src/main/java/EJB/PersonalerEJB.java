package EJB;

import Entitiy.Fachgebiet;
import Entitiy.Personaler;
import Service.Tokenizer;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * <h1>EJB für Personaler</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Personalern bereit.
 * Sie stellt somit eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class PersonalerEJB{

    @PersistenceContext
    private EntityManager em;

    private Tokenizer tokenizer = new Tokenizer();

    /**
     * Diese Methode gibt alle Personaler zurück
     *
     * @return Liste mit allen Personalern
     */
    public List<Personaler> getAll(){
        return em.createNamedQuery(Personaler.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt einen neuen Personaler in die Datenbank ein
     *
     * @param b Personaler
     * @return Personaler mit generierter Id
     */
    public Personaler add(Personaler b){
        em.persist(b);
        em.flush();
        return b;
    }

    /**
     * Diese Methode löscht einen Personaler aus der Datenbank
     *
     * @param b Personaler
     */
    public void delete(Personaler b){
        em.remove(b);
    }

    /**
     * Diese Methode gibt einen Personaler anhand seiner Id zurück
     *
     * @param id PersonalerId
     * @return Personaler
     */
    public Personaler getById(int id){
        return em.find(Personaler.class, id);
    }

    /**
     * Diese Methode gibt einen Personaler anhand seiner E-Mail wieder
     *
     * @param mail E-Mail
     * @return Personaler
     */
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

    /**
     * Diese Methode gibt den Chef der Firma zurück
     *
     * @return Chef
     */
    public Personaler getBoss(){
        Query query = em.createNamedQuery(Personaler.class.getSimpleName() + ".findByRang");
        query.setParameter("rang", 0);
        try{
            Personaler b = (Personaler) query.getResultList().get(0);

            return b;
        }catch(javax.persistence.NoResultException e){
            return null;
        }
    }

    /**
     * Diese Methdoe gibt einen Personaler anhand seines Tokens wieder
     *
     * @param token Das Webtoken
     * @return Personaler
     */
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

    /**
     * Diese Methode gibt das Team eines Personalers wieder.
     * Ein Team ist dadurch gekennzeichnet, dass es alle Mitarbeiter einer Ebene und eines Fachgebiets umfasst
     *
     * @param p Personaler
     * @return Liste mit Team
     */
    public List<Personaler> getTeam(Personaler p){
        List<Personaler> gleicheEbene = (List<Personaler>) em.createNamedQuery(Personaler.class.getSimpleName() + ".findByRang").setParameter("rang", p.getRang()).getResultList();
        gleicheEbene.remove(p);
        List<Personaler> returnList = new ArrayList<>();
        for(Personaler personaler : gleicheEbene){
            if(personaler.getFachgebiet().getName().equals(p.getFachgebiet().getName())){
                returnList.add(personaler.clone());
            }
        }
        return returnList;
    }

    /**
     * Diese Methode gibt das Team über einem Personaler wieder.
     * Ein Team ist dadurch gekennzeichnet, dass es alle Mitarbeiter einer Ebene und eines Fachgebiets umfasst
     *
     * @param p Personaler
     * @return Liste mit Personalern aus dem Team eine Ebene höher
     */
    public List<Personaler> getAboveTeam(Personaler p){
        List<Personaler> höhereEbene = (List<Personaler>) em.createNamedQuery(Personaler.class.getSimpleName() + ".findByRang").setParameter("rang", p.getRang() - 1).getResultList();
        List<Personaler> returnList = new ArrayList<>();
        for(Personaler personaler : höhereEbene){
            if(p.getRang() - 1 == 0 || personaler.getFachgebiet().getName().equals(p.getFachgebiet().getName())){//above only boss or samea fachgebiet
                returnList.add(personaler.clone());
            }
        }
        return returnList;
    }

    /**
     * Diese Methode gibt das Team unter einem Personaler wieder.
     * Ein Team ist dadurch gekennzeichnet, dass es alle Mitarbeiter einer Ebene und eines Fachgebiets umfasst
     *
     * @param p Personaler
     * @return Liste mit Personalern aus dem Team eine Ebene tiefer
     */
    public List<Personaler> getBelowTeam(Personaler p){
        List<Personaler> tiefereEbene = (List<Personaler>) em.createNamedQuery(Personaler.class.getSimpleName() + ".findByRang").setParameter("rang", p.getRang() + 1).getResultList();
        List<Personaler> returnList = new ArrayList<>();
        for(Personaler personaler : tiefereEbene){
            if(p.getRang() == 0 || personaler.getFachgebiet().getName().equals(p.getFachgebiet().getName())){
                returnList.add(personaler.clone());
            }
        }
        return returnList;
    }

    /**
     * Diese Methode ist nur für den Chef vorgesehen.
     * Dieser kann sich mit dieser Methode das Team eines bestimmten Fachgebiets auf der Ebene unter ihm anzeigen lassen.
     * Ein Team ist dadurch gekennzeichnet, dass es alle Mitarbeiter einer Ebene und eines Fachgebiets umfasst
     *
     * @param p Chef
     * @param f Fachgebiet
     * @return Liste mit Personalern aus dem Team eines bestimmten Fachgebiets eine Ebene tiefer
     */
    public List<Personaler> getBelowTeam(Personaler p, Fachgebiet f){
        List<Personaler> tiefereEbene = (List<Personaler>) em.createNamedQuery(Personaler.class.getSimpleName() + ".findByRang").setParameter("rang", p.getRang() + 1).getResultList();
        List<Personaler> returnList = new ArrayList<>();
        for(Personaler personaler : tiefereEbene){
            if(f.getName().equals(personaler.getFachgebiet().getName())){
                returnList.add(personaler.clone());
            }
        }
        return returnList;
    }

}
