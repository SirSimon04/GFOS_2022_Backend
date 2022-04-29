package EJB;

import Entitiy.Bewerber;
import Entitiy.Blacklist;
import java.sql.Timestamp;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/**
 * <h1>EJB für die Blacklist der Tokens</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich der Blacklist für noch gültige Tokens
 * bereit, die allerdings nicht mehr genutzt werden dürfen. Sie stellt somit
 * eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author simon
 */
@Stateless
@LocalBean
public class BlacklistEJB {

    @PersistenceContext
    private EntityManager em;

    /**
     * Diese Methode prüft, ob das Token auf der Blacklist steht.
     *
     * @param token zu prüfendes Token
     * @return true, wenn es auf der BL steht, ansonsten false
     */
    public boolean onBlacklist(String token) {
        TypedQuery<Blacklist> q = em.createNamedQuery(Blacklist.class.getSimpleName() + ".findByToken", Blacklist.class);
        q.setParameter("token", token);

        return q.getResultList().size() > 0; //Wenn die List größer als 0 ist, ist das Token auf der Blacklist
    }

    /**
     * Diese Methode setzt ein Token mit Timestamp auf die Blacklist.
     *
     * @param token Token, das hinzugefügt werden soll
     */
    public void addToken(String token) {
        if (!onBlacklist(token)) {
            Blacklist t = new Blacklist(); //neues Blacklist-Objekt
            t.setToken(token); //enthält den Token
            t.setZeit(new Timestamp(System.currentTimeMillis())); //und wann es in die DB geschrieben wurde

            em.persist(t);
        }
    }

    /**
     * Diese Methode löscht in der Datenbank alle abgelaufenen Tokens auf der
     * Blacklist.
     */
    public void clear() {
        List<Blacklist> blacklist = em.createNamedQuery(Blacklist.class.getSimpleName() + ".findAll").getResultList();

        for (int i = 0; i < blacklist.size(); i++) {
            Blacklist token = blacklist.get(i);
            if (token.getZeit().getTime() <= new Timestamp(System.currentTimeMillis()).getTime() - 15 * 60 * 1000) {//löscht alle abgelaufenen Objkete auf der Blacklist
                em.remove(token); //löscht ein Token von der DB
            }
        }
    }

    /**
     * Diese Methode gibt ein Token anhand des Wert des Tokens wieder.
     *
     * @param token Wert des Tokens
     * @return Das Blacklist-Token
     */
    public Blacklist getToken(String token) {
        Query query = em.createNamedQuery(Blacklist.class.getSimpleName() + ".findByToken");
        query.setParameter("token", token);
        try {
            Blacklist b = (Blacklist) query.getSingleResult();

            return b;
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }

    /**
     * Diese Methode löscht ein Token von der Blacklist
     *
     * @param token Das zu löschende Token
     */
    public void removeToken(String token) {
        Blacklist b = this.getToken(token);
        if (b != null) {
            em.remove(b);
        }
    }
}
