package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbereinstellungenEJB;
import EJB.BewerbungEJB;
import EJB.BewerbungsnachrichtEJB;
import EJB.BlacklistEJB;
import EJB.FachgebietEJB;
import EJB.InteressenfelderEJB;
import EJB.JobangebotEJB;
import EJB.LebenslaufstationEJB;
import EJB.PersonalerEJB;
import Entitiy.Adresse;
import Entitiy.Bewerber;
import Entitiy.Bewerbereinstellungen;
import Entitiy.Bewerbung;
import Entitiy.Bewerbungsnachricht;
import Entitiy.Fachgebiet;
import Entitiy.Interessenfelder;
import Entitiy.Jobangebot;
import Entitiy.Lebenslaufstation;
import Entitiy.Personaler;
import Service.Antwort;
import Service.FileService;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.persistence.NoResultException;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;

/**
 * <h1>Webservice für Bewerber</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Bewerber bereit. Sie stellt somit
 * eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/bewerber")
@Stateless
@LocalBean
public class BewerberWS {

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private AdresseEJB adresseEJB;

    @EJB
    private LebenslaufstationEJB lebenslaufstationEJB;

    @EJB
    private FachgebietEJB fachgebietEJB;

    @EJB
    private BewerbereinstellungenEJB bewerbereinstellungenEJB;

    @EJB
    private JobangebotEJB jobangebotEJB;

    @EJB
    private BewerbungEJB bewerbungEJB;

    @EJB
    private BewerbungsnachrichtEJB bewerbungsnachrichtEJB;

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private final FileService fileService = new FileService();

    private final Tokenizer tokenizer = new Tokenizer();

    private final MailService mailService = new MailService();

    /**
     * Diese Methode verifiziert ein Token
     *
     * @param token Das Webtoken
     * @return Status des Tokens
     */
    public boolean verify(String token) {
        if (tokenizer.isOn()) {
            if (blacklistEJB.onBlacklist(token)) {
                return false;
            }
            return tokenizer.verifyToken(token) != null;
        } else {
            return true;
        }
    }

    /**
     * Diese Route gibt einen Bewerber anhand seiner Id wieder
     *
     * @param id Die Id
     * @param token Das Webtoken
     * @return Der Bewerber
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int id, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                return response.build(200, parser.toJson(bewerberEJB.getById(id).clone()));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt einen Bewerber anhand seines Tokens wieder.
     *
     * @param token Das Webtoken
     * @return Der Bewerber
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwnAccount(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (dbBewerber == null) {
                    return response.buildError(404, "Zu diesem Token wurde kein Account gefunden");
                } else {
                    return response.build(200, parser.toJson(dbBewerber.clone()));
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route fügt einen neuen Bewerber in das System ein. Dabei werden
     * Daten wie das Fachgebiet, der Lebenslauf, die Adresse und
     * Interessenfelder gesetzt. Der Bewerber erhält eine E-Mail zum
     * Freischalten seines Kontos.
     *
     * @param daten Die erforderlichen Daten
     * @return Response mit Bestätigung oder Fehler
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten) {
        try {

            Bewerber neuerBewerber = parser.fromJson(daten, Bewerber.class);

            //überprüfen, ob die Mail bereits registiert ist
            Bewerber mailIsRegistered = bewerberEJB.getByMail(neuerBewerber.getEmail());

            if (mailIsRegistered != null) {
                return response.buildError(400, "Diese E-Mail Adresse ist bereits registriert");
            }
            //Passwort hashen
            neuerBewerber.setPassworthash(hasher.checkPassword(neuerBewerber.getPassworthash()));

            //Bewerber in die Datenbank schreiben
            Bewerber dbBewerber = bewerberEJB.add(neuerBewerber);

            JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);
//            das Fachgebiet des Bewerbers setzen
            Fachgebiet fachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class));

            dbBewerber.setFachgebiet(fachgebiet);
//            Verifizierungspin senden
            String neuerNutzername = dbBewerber.getVorname() + " " + dbBewerber.getName();
            String neueEmail = dbBewerber.getEmail();
            int pin = mail.sendVerificationPin(neuerNutzername, neueEmail);
//            Wichtig: Wieder auskommentieren
            dbBewerber.setAuthcode(pin);
//
//            Einstellungen setzen
            Bewerbereinstellungen e = new Bewerbereinstellungen(true, false);// Mails erhalten, keine 2FA

            bewerbereinstellungenEJB.add(e);
            dbBewerber.setEinstellungen(e);

            return response.build(200, parser.toJson("Sie haben eine Bestätigunsmail zum Freischalten ihres Kontos erhalten."));

        } catch (Exception e) {
            System.out.println(e);
            return response.buildError(500, "Es ist ein Fehler aufgetreten!");
        }
    }

    /**
     * Diese Route verifiziert das Konto eines Bewerbers mithilfe eines
     * vierstelligen Codes. Ist dieser richtig, wird der Bewerber auch
     * eingeloggt.
     *
     * @param daten Die Daten zur Verifizierung
     * @return Das Webtoken des Bewerbers oder Fehler
     */
    @POST
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyAccount(String daten) {
        try {
            JsonObject loginUser = parser.fromJson(daten, JsonObject.class);
            String jsonMail = parser.fromJson((loginUser.get("mail")), String.class);
            String jsonPasswort = parser.fromJson((loginUser.get("passwort")), String.class);
            int jsonAuth = parser.fromJson(loginUser.get("authcode"), Integer.class);

            Bewerber dbBewerber = bewerberEJB.getByMail(jsonMail);
            if (dbBewerber == null) {
                return response.buildError(401, "Mit dieser E-Mailadresse ist kein Konto vorhanden");
            }

            if (dbBewerber.getPassworthash().equals(hasher.checkPassword(jsonPasswort))) {

                if (dbBewerber.getAuthcode() == jsonAuth) {
                    dbBewerber.setAuthcode(null);
                    return response.build(200, parser.toJson(tokenizer.createNewToken(jsonMail)));
                } else {
                    return response.buildError(401, "Falscher Authcode");
                }

            } else {
                return response.buildError(401, "Falsches Passwort");
            }

        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

    /**
     * Diese Route löscht einen Bewerber. Dabei werden auch alle getätigten
     * Bewerbungen gelöscht.
     *
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    //WICHTIG: GEHT NOCH NICHT
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBewerber(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);
                int bewerberId = dbBewerber.getBewerberid();

                //alle abgeschickten Bewerbungen löschen
                List<Bewerbung> bToRemove = new ArrayList<>();
                for (Bewerbung dbBewerbung : dbBewerber.getBewerbungList()) {

                    int bewerbungId = dbBewerbung.getBewerbungid();

                    bToRemove.add(dbBewerbung);
                    dbBewerbung.setBewerber(null);

                    fileService.deleteBewerbung(bewerbungId);

                    dbBewerbung.getJobangebot().getBewerbungList().remove(dbBewerbung);
                    dbBewerbung.setJobangebot(null);

                    for (Bewerbungsnachricht n : dbBewerbung.getBewerbungsnachrichtList()) {
                        bewerbungsnachrichtEJB.remove(n);
                    }
                    dbBewerbung.setBewerbungsnachrichtList(null);

                    for (Personaler p : dbBewerbung.getPersonalerList()) {
                        p.getBewerbungList().remove(dbBewerbung);
                    }
                    dbBewerbung.setPersonalerList(null);

                    bewerbungEJB.remove(dbBewerbung);
                }
                dbBewerber.getBewerbungList().removeAll(bToRemove);

                //Bewerber aus seinem Fachgebiet löschen
                if (dbBewerber.getFachgebiet() != null) {
                    Fachgebiet dbFachgebiet = dbBewerber.getFachgebiet();
                    dbFachgebiet.getBewerberList().remove(dbBewerber);
                    dbBewerber.setFachgebiet(null);
                }

                //Einstellungen löschen
                bewerbereinstellungenEJB.remove(dbBewerber.getEinstellungen());
                dbBewerber.setEinstellungen(null);

                //Interessenfelder entfernen, nicht löschen
                dbBewerber.setInteressenfelderList(null);

                //Lebenslaufstationen löschen, mit Referenz
                List<Lebenslaufstation> toRemove = new ArrayList<>();
                for (Lebenslaufstation lDB : dbBewerber.getLebenslaufstationList()) {

                    try {
                        fileService.deleteLebenslaufstation(lDB.getLebenslaufstationid());
                    } catch (FileNotFoundException e) {

                    }

                    toRemove.add(lDB);

                    lebenslaufstationEJB.remove(lDB);

                }
                dbBewerber.getLebenslaufstationList().removeAll(toRemove);

                //Adresse löschen
                if (dbBewerber.getAdresse() != null) {
                    adresseEJB.remove(dbBewerber.getAdresse());
                    dbBewerber.setAdresse(null);
                }

                //Lebenslauf löschen
                fileService.deleteLebenslauf(bewerberId);

                //Profilbild löschen
                fileService.deleteProfilbild(bewerberId);

                bewerberEJB.delete(dbBewerber);
                return response.build(200, "Ihr Account wurde erfolgreich gelöscht.");
            } catch (Exception e) {
                System.out.println(e);
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt die Bewerber aus, die sich auf ein bestimmtes Jobangebot
     * bewerben können. Das beinhaltet die Bewerber, die das gleiche Fachgebiet,
     * wie das Angebot besitzen und ein öffentliches Profil haben,
     *
     * @param token Das Webtoken
     * @param id Die Id des Jobangebots
     * @return Liste mit passenden Bewerbern
     */
    @GET
    @Path("/passender/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response passender(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler != null) {
                    Jobangebot jobangebot = jobangebotEJB.getById(id);
                    Fachgebiet fachgebiet = jobangebot.getFachgebiet();

                    List<Bewerber> bewerber = bewerberEJB.getByFachgebiet(fachgebiet);

                    return response.build(200, parser.toJson(bewerber));
                } else {
                    return response.buildError(403, "Es wurde kein Personaler gefunden.");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route aktualisiert das Profil eines Bewerbers
     *
     * @param daten Die neuen Daten des Bewerbes
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response udateProfile(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (dbBewerber == null) {
                    return response.buildError(404, "Es wurde kein Bewerber gefunden");
                }

                //Vorname, Nachname, E-Mail
                JsonObject updatedUser = parser.fromJson(daten, JsonObject.class);

                if (updatedUser.has("vorname")) {

                    String name = parser.fromJson(updatedUser.get("vorname"), String.class);

                    dbBewerber.setVorname(name);
                }

                if (updatedUser.has("nachname")) {

                    String name = parser.fromJson(updatedUser.get("nachname"), String.class);

                    dbBewerber.setName(name);
                }

                if (updatedUser.has("telefon")) {

                    String telefon = parser.fromJson(updatedUser.get("telefon"), String.class);

                    dbBewerber.setTelefon(telefon);
                }

                if (updatedUser.has("mail")) {
                    //Nutzer per Mail informieren
                    String newMail = parser.fromJson(updatedUser.get("mail"), String.class);

                    dbBewerber.setEmail(newMail);

                    String benutzername = dbBewerber.getVorname() + " " + dbBewerber.getName();
                    String mail = dbBewerber.getEmail();

                    mailService.sendMailChange(benutzername, mail, newMail);
                }

                return response.build(200, "Success");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
