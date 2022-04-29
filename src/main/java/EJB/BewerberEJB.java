package EJB;

import Entitiy.Bewerber;
import Entitiy.Fachgebiet;
import Service.Hasher;
import Service.Tokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * <h1>EJB für Bewerbern</h1>
 * <p>
 * Diese Klasse stellt Methoden bezüglich Bewerbern bereit. Sie stellt somit
 * eine Schnittstelle zwischen Webservice und Datenbank dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Stateless
@LocalBean
public class BewerberEJB {

    @PersistenceContext
    private EntityManager em;

    private final Tokenizer tokenizer = new Tokenizer();

    private final Hasher hasher = new Hasher();

    /**
     * Diese Methode gibt alle Bewerber zurück.
     *
     * @return Liste mit allen Bewerbern
     */
    public List<Bewerber> getAll() {
        return em.createNamedQuery(Bewerber.class.getSimpleName() + ".findAll").getResultList();
    }

    /**
     * Diese Methode fügt einen neuen Bewerber in das System ein.
     *
     * @param b neuer Bewerber
     * @return Der Bewerber mit generierter Id
     */
    public Bewerber add(Bewerber b) {
        em.persist(b);
        em.flush();
        return b;
    }

    /**
     * Diese Methode löscht einen Bewerber aus der Datenbank
     *
     * @param b zu löschender Bewerber
     */
    public void delete(Bewerber b) {
        em.remove(b);
    }

    /**
     * Diese Methode gibt einen Bewerber anhand seiner Id zurück
     *
     * @param id BewerberId
     * @return Der Bewerber
     */
    public Bewerber getById(int id) {
        return em.find(Bewerber.class, id);
    }

    /**
     * Diese Methode gibt einen Bewerber anhand seiner E-Mailadresse zurück
     *
     * @param mail Die E-Mailadresse
     * @return Der Bewerber
     */
    public Bewerber getByMail(String mail) {
        Query query = em.createNamedQuery(Bewerber.class.getSimpleName() + ".findByEmail");
        query.setParameter("email", mail);
        try {
            Bewerber b = (Bewerber) query.getSingleResult();

            return b;
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }

    /**
     * Diese Methode gibt einen Bewerber anhand seines Tokens wieder
     *
     * @param token Das Webtoken
     * @return Der Bewerber
     */
    public Bewerber getByToken(String token) {

        String mail = tokenizer.getMail(token);

        Query query = em.createNamedQuery(Bewerber.class.getSimpleName() + ".findByEmail");
        query.setParameter("email", mail);
        try {
            Bewerber b = (Bewerber) query.getSingleResult();

            return b;
        } catch (javax.persistence.NoResultException e) {
            return null;
        }
    }

    /**
     * Diese Methode aktualisiert den Passworthash eines Bewerbers
     *
     * @param dbBewerber Der Bewerber
     * @param newPassword Das neue Passwort
     */
    public void changePassword(Bewerber dbBewerber, String newPassword) {

        dbBewerber.setPassworthash(hasher.checkPassword(newPassword));
    }

    /**
     * Diese Methode gibt alle Bewerber zurück, die über die ein neues
     * Jobangebot in ihrem Fachgebiet informiert werden wollen. Das ist der
     * Fall, wenn die Einstellung zum Erhalten von Mails gesetzt ist.
     *
     * @param f Das Fachgebiet
     * @return Liste mit allen passenden Bewerbern
     */
    public List<Bewerber> getForNewMailSend(Fachgebiet f) {
        List<Bewerber> returnList = new ArrayList<>();

        for (Bewerber b : this.getAll()) {
            if (Objects.equals(f, b.getFachgebiet()) && b.getEinstellungen().getGetmails()) {
                returnList.add(b);
            }
        }

        return returnList;
    }

    /**
     * Diese Methode gibt alle Bewerber zurück, die zu einem Fachgebiet gehören.
     *
     * @param f Das Fachgebiet
     * @return Liste mit Bewerbern
     */
    public List<Bewerber> getByFachgebiet(Fachgebiet f) {
        List<Bewerber> returnList = new ArrayList<>();

        System.out.println(this.getAll());

        for (Bewerber b : this.getAll()) {
            if (b.getFachgebiet() != null && b.getFachgebiet().getFachgebietid() == f.getFachgebietid()) {
                returnList.add(b.clone());
            }
        }

        System.out.println(returnList);

        return returnList;
    }

}
