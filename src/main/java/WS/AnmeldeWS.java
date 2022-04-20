package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.PersonalerEJB;
import Entitiy.Bewerber;
import Entitiy.Personaler;
import Service.Antwort;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <h1>Webservice für die Anmeldung</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Anmeldung bereit. Sie stellt somit
 * eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/anmeldung")
@Stateless
@LocalBean
public class AnmeldeWS {

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private Tokenizer tokenizer = new Tokenizer();

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
     * Diese Route stellt den Login für Bewerber und Personaler dar
     *
     * @param daten Die Anmeldedaten
     * @return Response mit Token und, ob es sich um einen Personaler oder
     * Bewerber handelt
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(String daten) {
        try {
            JsonObject loginUser = parser.fromJson(daten, JsonObject.class);
            String jsonMail = parser.fromJson((loginUser.get("mail")), String.class);
            String jsonPasswort = parser.fromJson((loginUser.get("passwort")), String.class);

            Bewerber dbBewerber = bewerberEJB.getByMail(jsonMail);

            Personaler dbPersonaler = personalerEJB.getByMail(jsonMail);

            if (dbBewerber != null) {
                if (dbBewerber.getAuthcode() != null) {
                    return response.buildError(401, "Du musst zuerst deinen Account bestätigen");
                }

                if (dbBewerber.getPassworthash().equals(hasher.checkPassword(jsonPasswort))) {

                    String newToken = tokenizer.createNewToken(jsonMail);

                    blacklistEJB.removeToken(newToken); //In case the user logins while his token his still active, it has to be removed from bl

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.add("ispersonaler", parser.toJsonTree(false));

                    if (dbBewerber.getEinstellungen().getTwofa()) {

                        String benutzername = dbBewerber.getVorname() + " " + dbBewerber.getName();
                        String email = dbBewerber.getEmail();

                        int pin = mailService.send2Fa(benutzername, email);

                        dbBewerber.setTwofacode(pin);

                        jsonObject.add("2fa", parser.toJsonTree(true));
                    } else {
                        jsonObject.add("2fa", parser.toJsonTree(false));
                        jsonObject.add("token", parser.toJsonTree(newToken));
                    }

                    return response.build(200, parser.toJson(jsonObject));
                } else {
                    return response.buildError(401, "Falsches Passwort");
                }
            }

            if (dbPersonaler != null) {
                if (dbPersonaler.getPassworthash().equals(hasher.checkPassword(jsonPasswort))) {

                    String benutzerName = dbPersonaler.getVorname() + " " + dbPersonaler.getName();
                    String email = dbPersonaler.getEmail();

                    int pin = mailService.send2Fa(benutzerName, email);

                    dbPersonaler.setTwofacode(pin);

//                    String newToken = tokenizer.createNewToken(jsonMail);
//                    blacklistEJB.removeToken(newToken); //In case the user logins while his token his still active, it has to be removed from bl
                    JsonObject jsonObject = new JsonObject();

//                    jsonObject.add("token", parser.toJsonTree(newToken));
                    jsonObject.add("ispersonaler", parser.toJsonTree(true));
                    jsonObject.add("2fa", parser.toJsonTree(true));

                    return response.build(200, parser.toJson(jsonObject));
                } else {
                    return response.buildError(401, "Falsches Passwort");
                }
            } else {
                return response.buildError(401, "Mit dieser E-Mailadresse ist kein Konto vorhanden");
            }

        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

    @POST
    @Path("/2fa")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response twoFALogin(String daten) {
        try {

            JsonObject loginUser = parser.fromJson(daten, JsonObject.class);
            String jsonMail = parser.fromJson((loginUser.get("mail")), String.class);
            int pin = parser.fromJson((loginUser.get("pin")), Integer.class);

            Personaler dbPersonaler = personalerEJB.getByMail(jsonMail);
            Bewerber dbBewerber = bewerberEJB.getByMail(jsonMail);

            if (dbPersonaler != null) {
                if (dbPersonaler.getTwofacode() == pin) {
                    dbPersonaler.setTwofacode(null);

                    String newToken = tokenizer.createNewToken(jsonMail);
                    blacklistEJB.removeToken(newToken); //In case the user logins while his token his still active, it has to be removed from bl

                    return response.build(200, parser.toJson(newToken));
                } else {
                    return response.buildError(403, "Falscher Pin");
                }
            } else if (dbBewerber != null) {
                if (dbBewerber.getTwofacode() == pin) {
                    dbBewerber.setTwofacode(null);

                    String newToken = tokenizer.createNewToken(jsonMail);
                    blacklistEJB.removeToken(newToken); //In case the user logins while his token his still active, it has to be removed from bl

                    return response.build(200, parser.toJson(newToken));
                } else {
                    return response.buildError(403, "Falscher Pin");
                }
            } else {
                return response.buildError(404, "Kein Nutzer gefunden");
            }
        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }

    }

    /**
     * Diese Route stellt den Logout aus dem System für Bewerber und Personaler
     * dar. Dafür wird das Token des Nutzers auf die Blacklist geschrieben
     *
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Bereits ausgeloggt");
        } else {
            try {

                blacklistEJB.addToken(token);

                return response.build(200, "Logout erfolgreich");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestPasswordchange(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbBewerber != null) {

                    String benutzername = dbBewerber.getVorname() + " " + dbBewerber.getName();
                    String mail = dbBewerber.getEmail();

                    int pin = mailService.sendPasswordChangeMail(benutzername, mail);

                    dbBewerber.setPasswordresetcode(pin);

                } else if (dbPersonaler != null) {
                    String benutzername = dbPersonaler.getVorname() + " " + dbPersonaler.getName();
                    String mail = dbPersonaler.getEmail();

                    int pin = mailService.sendPasswordChangeMail(benutzername, mail);

                    dbPersonaler.setPasswordresetcode(pin);
                } else {
                    return response.buildError(404, "Es wurde keine Person gefunden");
                }

                return response.build(200, "Eine Mail wurde versandt.");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route bietet die Möglichkeit, das Passwort zurückzusetzen. Dabei
     * wird erkannt, ob es sich um einen Bewerber oder Personaler handelt.
     *
     * @param daten Das alte und neue Passwort des Benutzers
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/password")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                int pin = parser.fromJson(jsonObject.get("pin"), Integer.class);
                String newPassword = parser.fromJson(jsonObject.get("new"), String.class);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbBewerber != null) {
                    if (pin == dbBewerber.getPasswordresetcode()) {

                        bewerberEJB.changePassword(dbBewerber, newPassword);

                    } else {
                        return response.buildError(403, "Der Pin ist falsch.");
                    }
                } else if (dbPersonaler != null) {
                    if (pin == dbPersonaler.getPasswordresetcode()) {

                        personalerEJB.changePassword(dbPersonaler, newPassword);

                    } else {
                        return response.buildError(403, "Der Pin ist falsch.");
                    }

                } else {
                    return response.buildError(404, "Es wurde kein Nutzer zum eingebenen Token gefunden.");
                }

                return response.build(200, "Das Passwort wurde erfolgreich geändert.");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
