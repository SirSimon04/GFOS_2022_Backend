package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbereinstellungenEJB;
import EJB.BlacklistEJB;
import EJB.FachgebietEJB;
import EJB.InteressenfelderEJB;
import EJB.JobangebotEJB;
import EJB.LebenslaufstationEJB;
import EJB.PersonalerEJB;
import Entitiy.Adresse;
import Entitiy.Bewerber;
import Entitiy.Bewerbereinstellungen;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.persistence.NoResultException;
import javax.ws.rs.DELETE;

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
    private InteressenfelderEJB interessenfelderEJB;

    @EJB
    private FachgebietEJB fachgebietEJB;

    @EJB
    private BewerbereinstellungenEJB bewerbereinstellungenEJB;

    @EJB
    private JobangebotEJB jobangebotEJB;

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private final FileService fileService = new FileService();

    private Tokenizer tokenizer = new Tokenizer();

    public boolean verify(String token) {
        System.out.println("WS.BewerberWS.verifyToken()");
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

            //check if mail is registered
            Bewerber mailIsRegistered = bewerberEJB.getByMail(neuerBewerber.getEmail());

            if (mailIsRegistered != null) {
                return response.buildError(400, "Diese E-Mail Adresse ist bereits registriert");
            }
            //set pw
            neuerBewerber.setPassworthash(hasher.checkPassword(neuerBewerber.getPassworthash()));

            Bewerber dbBewerber = bewerberEJB.add(neuerBewerber);//add to db

            JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

            //add adress
            Adresse neueAdresse = parser.fromJson((jsonObject.get("neueadresse")), Adresse.class);

            Adresse dbAdresse = adresseEJB.add(neueAdresse);

            dbBewerber.setAdresse(dbAdresse);

            //TODO: Hier auch die Referenzen mit hochladen
            //Lebenslaufstationen
            if (jsonObject.has("lebenslaufstationen")) {
                System.out.println("stations");
                Type LebenslaufstationenListType = new TypeToken<List<Lebenslaufstation>>() {
                }.getType();

                List<Lebenslaufstation> stations = parser.fromJson(jsonObject.get("lebenslaufstationen"), LebenslaufstationenListType);

                for (Lebenslaufstation station : stations) {
                    String referenz = station.getReferenz(); //this has to be saved before adding to db, because its set to null because it should not be in the db

                    Lebenslaufstation dbStation = lebenslaufstationEJB.add(station);
                    dbBewerber.getLebenslaufstationList().add(dbStation);

                    //if a reference is sent, it gets saved
                    if (referenz != null) {
                        fileService.saveLebenslaufstation(station.getLebenslaufstationid(), referenz);
                    }
                }

            }

            //Interessenfelder
            if (jsonObject.has("neueinteressenfelder")) {
                Type interessenfelderListType = new TypeToken<List<String>>() {

                }.getType();

                List<String> interessenfelder = parser.fromJson(jsonObject.get("neueinteressenfelder"), interessenfelderListType);

                for (String interessenfeld : interessenfelder) {
                    Interessenfelder field = interessenfelderEJB.getByName(interessenfeld);
                    if (field == null) {
                        Interessenfelder feld = interessenfelderEJB.add(new Interessenfelder(interessenfeld));
                        dbBewerber.getInteressenfelderList().add(feld);
                    } else {
                        if (!dbBewerber.getInteressenfelderList().contains(field)) {
                            dbBewerber.getInteressenfelderList().add(field);
                        }
                    }
                }
            }

//            Fachgebiet, muss gesetzt werden
            Fachgebiet fachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class)); //Fachgebiete sind schon vorgegeben, deswegen kein null check nötig

            dbBewerber.setFachgebiet(fachgebiet);

            //Profilbild
            if (jsonObject.has("neuesprofilbild")) {

                String base64 = parser.fromJson(jsonObject.get("neuesprofilbild"), String.class);
                int id = dbBewerber.getBewerberid();
                fileService.saveProfilbild(id, base64);
            }
//            //Lebenslauf
            if (jsonObject.has("neuerlebenslauf")) {

                String base64 = parser.fromJson(jsonObject.get("neuerlebenslauf"), String.class);

                fileService.saveLebenslauf(dbBewerber.getBewerberid(), base64);
            }
            //Einstellungen
            Bewerbereinstellungen einstellungen = bewerbereinstellungenEJB.add(parser.fromJson(jsonObject.get("neueeinstellungen"), Bewerbereinstellungen.class));

            dbBewerber.setEinstellungen(einstellungen);

            //send verification pin
            int min = 1000;
            int max = 9999;
            int random_int = (int) (Math.random() * (max - min + 1) + min);
            dbBewerber.setAuthcode(random_int);
            String neuerNutzername = dbBewerber.getVorname() + " " + dbBewerber.getName();
            String neueEmail = dbBewerber.getEmail();
            mail.sendVerificationPin(neuerNutzername, neueEmail, random_int);

            return response.build(200, parser.toJson("Sie haben eine Bestätigunsmail zum Freischalten ihres Kontos erhalten."));

        } catch (Exception e) {
            System.out.println(e);
            return response.buildError(500, "Es ist ein Fehler aufgetreten!");
        }
    }

    /**
     * Diese Route verifiziert das Konto eines Bewerbers mithilfe eines
     * vierstelligen Codes. Ist dieser richtig wird der Bewerber auch
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
                //Methode implementieren
                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                bewerberEJB.delete(dbBewerber);

                //TODO: Alle Bewerbungen dieses Nutzers müssen gelöscht werden
                return response.build(200, "Ihr Account wurde erfolgreich gelöscht.");
            } catch (Exception e) {
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

}
