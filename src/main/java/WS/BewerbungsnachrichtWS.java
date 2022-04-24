package WS;

import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BewerbungsnachrichtEJB;
import EJB.BlacklistEJB;
import EJB.PersonalerEJB;
import Entitiy.Bewerber;
import Entitiy.Bewerbung;
import Entitiy.Bewerbungsnachricht;
import Entitiy.Personaler;
import Service.Antwort;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
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

/**
 * <h1>Webservice für Bewerbungsnachrichten</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Bewerbungsnachrichten bereit. Sie
 * stellt somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/bewerbungsnachricht")
@Stateless
@LocalBean
public class BewerbungsnachrichtWS {

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private BewerbungsnachrichtEJB bewerbungsnachrichtEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private BewerbungEJB bewerbungEJB;

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
     * Diese Route fügt einer Bewerbung eine Nachricht hinzu. Dabei wird
     * überprüft, ob der Personaler an der Bewerbung arbeitet oder der Bewerber
     * die Bewerbung gestellt hat.
     *
     * @param daten Die Daten zu Nachricht und Bewerbung
     * @param token Das Webtoken
     * @return Die neue Nachricht
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBewerbungsnachricht(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Bewerbungsnachricht nachricht = bewerbungsnachrichtEJB.add(parser.fromJson(daten, Bewerbungsnachricht.class));

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                int bewerbungId = parser.fromJson(jsonObject.get("bewerbungid"), Integer.class);

                Bewerbung bewerbung = bewerbungEJB.getById(bewerbungId);

                if (bewerbung == null) {
                    return response.buildError(500, "Die Bewerbung wurde nicht gefunden");
                }

                //nachschauen, ob es sich um einen Bewerber oder einen Personaler handelt
                Bewerber bewerber = bewerberEJB.getByToken(token);

                Personaler personaler = personalerEJB.getByToken(token);

                if (bewerber == null && personaler == null) {
                    return response.buildError(401, "Es wurde kein Bewerber oder Personaler zu ihrem Token gefunden");
                } else if (bewerber != null) {

                    nachricht.setVonbewerber(Boolean.TRUE);

                    if (!bewerbung.getBewerber().equals(bewerber)) {
                        return response.build(400, "Sie haben diese Bewerbung nicht gestellt");
                    } else {
                        bewerbung.getBewerbungsnachrichtList().add(nachricht);
                        nachricht.setBewerbung(bewerbung);
                    }

                    //E-Mail an Ansprechpartner senden
                    Personaler dbPersonaler = bewerbung.getJobangebot().getAnsprechpartner();

                    String benutzername = dbPersonaler.getVorname() + " " + dbPersonaler.getName();

                    String mailTo = dbPersonaler.getEmail();

                    String jobTitle = bewerbung.getJobangebot().getTitle();

                    String sender = bewerber.getVorname() + " " + bewerber.getName();

                    String msg = nachricht.getText();

                    mailService.sendApplicationMessage(benutzername, mailTo, sender, jobTitle, msg);

                } else if (personaler != null) {

                    nachricht.setVonbewerber(Boolean.FALSE);

                    if (!bewerbung.getPersonalerList().contains(personaler)) {
                        return response.buildError(400, "Sie sind nicht autorisiert, diese Bewerbung zu bearbeiten");
                    } else {
                        bewerbung.getBewerbungsnachrichtList().add(nachricht);
                        nachricht.setBewerbung(bewerbung);
                    }

                    //E-Mail an Bewerber senden senden
                    Bewerber dbBewerber = bewerbung.getBewerber();

                    String benutzername = dbBewerber.getVorname() + " " + dbBewerber.getName();

                    String mailTo = dbBewerber.getEmail();

                    String jobTitle = bewerbung.getJobangebot().getTitle();

                    String sender = personaler.getVorname() + " " + personaler.getName();

                    String msg = nachricht.getText();

                    mailService.sendApplicationMessage(benutzername, mailTo, sender, jobTitle, msg);

                }

                return response.build(200, parser.toJson(nachricht.clone()));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Nachrichten einer Bewerbung zurück. Dabei wird
     * überprüft, ob der Personaler an der Bewerbung arbeitet oder der Bewerber
     * die Bewerbung gestellt hat.
     *
     * @param token Das Webtoken
     * @param id BewerbungId
     * @return Die Nachrichten
     */
    @GET
    @Path("/{bewerbungid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByBewerbung(@HeaderParam("Authorization") String token, @PathParam("bewerbungid") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerbung bewerbung = bewerbungEJB.getById(id);

                if (bewerbung == null) {
                    return response.buildError(500, "Die Bewerbung wurde nicht gefunden");
                }

                //nachschauen, ob Bewerber oder Personaler
                Bewerber bewerber = bewerberEJB.getByToken(token);

                Personaler personaler = personalerEJB.getByToken(token);

                if (bewerber == null && personaler == null) {
                    return response.buildError(401, "Es wurde kein Bewerber oder Personaler zu ihrem Token gefunden");
                } else if (bewerber != null && !bewerbung.getBewerber().equals(bewerber)) {

                    return response.build(400, "Sie haben diese Bewerbung nicht gestellt");

                } else if (personaler != null && !bewerbung.getPersonalerList().contains(personaler)) {

                    return response.buildError(400, "Sie sind nicht autorisiert, diese Bewerbung zu bearbeiten");
                }

                List<Bewerbungsnachricht> nachrichten = bewerbung.getBewerbungsnachrichtList();

                List<Bewerbungsnachricht> output = new ArrayList<>();

                for (Bewerbungsnachricht b : nachrichten) {
                    output.add(b.clone());
                }

                return response.build(200, parser.toJson(output));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
