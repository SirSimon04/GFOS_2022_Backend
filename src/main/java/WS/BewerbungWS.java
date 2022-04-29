package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BewerbungsnachrichtEJB;
import EJB.BlacklistEJB;
import EJB.JobangebotEJB;
import EJB.PersonalerEJB;
import Entitiy.Bewerber;
import Entitiy.Bewerbung;
import Entitiy.Bewerbungsnachricht;
import Entitiy.Jobangebot;
import Entitiy.Personaler;
import Service.Antwort;
import Service.FileService;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
 * <h1>Webservice für Bewerbungen</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Bewerbungen bereit. Sie stellt somit
 * eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/bewerbung")
@Stateless
@LocalBean
public class BewerbungWS {

    @EJB
    private BewerbungEJB bewerbungEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private JobangebotEJB jobangebotEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private BewerbungsnachrichtEJB bewerbungsnachrichtEJB;

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final FileService fileService = new FileService();

    private final MailService mailService = new MailService();

    private final Tokenizer tokenizer = new Tokenizer();

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
     * Diese Route fügt eine neue Bewerbung in das System ein. Dabei werden alle
     * für die Bewerbung wichtigen Daten gesetzt.
     *
     * @param daten Die Daten zu der neuen Bewerbung
     * @param token Das Webtoken
     * @return Response mit neuer Bewerbung oder Fehler
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBewerbung(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                int jobangebotId = parser.fromJson(jsonObject.get("jobangebotid"), Integer.class);
                Jobangebot dbJobangebot = jobangebotEJB.getById(jobangebotId);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (dbJobangebot == null) {
                    return response.buildError(404, "Das Jobgenbot wurde nicht gefunden");
                }

                if (dbBewerber == null) {
                    return response.buildError(404, "Es wurde kein Bewerber gefunden");
                }

                //Überprüfen, ob sich der Bewerber schon einmal auf eine Stelle beworben hat
                for (Bewerbung b : dbJobangebot.getBewerbungList()) {
                    if (b.getBewerber().equals(dbBewerber)) {
                        return response.buildError(400, "Sie haben sich bereits auf diese Stelle beworben");
                    }
                }

                //Bewerbung
                Bewerbung bewerbung = parser.fromJson(daten, Bewerbung.class);
                bewerbung.setDatum((Date) bewerbung.getDatum());

                Bewerbung dbBewerbung = bewerbungEJB.add(bewerbung);

                dbBewerber.getBewerbungList().add(dbBewerbung);

                dbBewerbung.setBewerber(dbBewerber);

                //Bewerbungsstatus
                dbBewerbung.setStatus(0);

                //Jobangebot
                dbJobangebot.getBewerbungList().add(dbBewerbung);

                dbBewerbung.setJobangebot(dbJobangebot);

                //Bewerbungsschreiben
                String base64 = parser.fromJson(jsonObject.get("neuesbewerbungsschreiben"), String.class);

                int id = dbBewerbung.getBewerbungid();

                fileService.saveBewerbung(id, base64);

                //der Ansprechpartner ist der Personaler, der das Jobangebot erstellt hat
                Personaler dbPersonaler = dbJobangebot.getAnsprechpartner();

                dbBewerbung.getPersonalerList().add(dbPersonaler);
                dbPersonaler.getBewerbungList().add(dbBewerbung);

                //Mail an den Ansprechpartner verschicken
                String userName = dbPersonaler.getVorname() + " " + dbPersonaler.getName();
                String mail = dbPersonaler.getEmail();
                String jobTitle = dbJobangebot.getTitle();
                String applicantName = dbBewerber.getVorname() + " " + dbBewerber.getName();

                mailService.sendNewApplication(userName, mail, jobTitle, applicantName);
                return response.build(200, parser.toJson(dbBewerbung.clone()));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route löscht eine Bewerbung aus dem System. Dafür muss die
     * Bewerbung entweder ablehnt oder zurückgezogen worden sein. Dieser Vorgang
     * kann von Bewerbern und Personalern durchgefüghrt werden, sie müssen aber
     * an der Bewerbung beteilligt sein.
     *
     * @param token Das Webtoken
     * @param id BewerbungId
     * @return Response mit Bestätigung oder Fehler
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Bewerbung dbBewerbung = bewerbungEJB.getById(id);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbBewerber != null && !dbBewerbung.getBewerber().equals(dbBewerber)) {
                    return response.buildError(400, "Sie haben diese Bewerbung nicht gestellt");
                } else if (dbPersonaler != null) {

                    dbBewerber = dbBewerbung.getBewerber();

                    dbBewerber.getBewerbungList().remove(dbBewerbung);
                    dbBewerbung.setBewerber(null);

                    fileService.deleteBewerbung(id);

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

                    return response.build(200, parser.toJson("Die Bewerbung wurde erfolgreich gelöscht"));

                } else if (dbBewerber != null) {

                    dbBewerber.getBewerbungList().remove(dbBewerbung);
                    dbBewerbung.setBewerber(null);

                    fileService.deleteBewerbung(id);

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

                    return response.build(200, parser.toJson("Die Bewerbung wurde erfolgreich gelöscht"));
                } else {
                    return response.buildError(404, "Es wurde keine Person zu Ihrem Token gefunden");
                }
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle abgeschickten Bewerbungen eines Bewerbers zurück.
     * Sie dient dem Bewerber als Übersicht, auf welche Stellen er sich beworben
     * hat.
     *
     * @param token Das Webtoken
     * @return Die Bewerbungen
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAbgeschickte(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                List<Bewerbung> output = new ArrayList<>();

                for (Bewerbung b : dbBewerber.getBewerbungList()) {
                    output.add(b.clone());
                }

                return response.build(200, parser.toJson(output));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Bewerbungen eines Jobangebots wieder. Sie kann nur
     * von Personalern aufgerufen werden
     *
     * @param token Das Webtoken
     * @param id JobangebotId
     * @return Liste mit Bewerbungen des Jobangebots
     */
    @GET
    @Path("/{jobangebotid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByJobangebot(@HeaderParam("Authorization") String token, @PathParam("jobangebotid") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler == null) {
                    return response.buildError(401, "Es wurde kein Personaler gefunden");
                }

                Jobangebot dbJobangebot = jobangebotEJB.getById(id);

                if (dbJobangebot == null) {
                    return response.buildError(401, "Es wurde kein Jobangebot gefunden");
                }

                List<Bewerbung> bewerbungList = dbJobangebot.getBewerbungList();

                List<Bewerbung> output = new ArrayList<>();

                for (Bewerbung b : bewerbungList) {
                    output.add(b.clone());
                }

                return response.build(200, parser.toJson(output));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Bewerbungen zurück, denen der Personaler zugewiesen
     * ist. Sie dient dem Personaler als Übersicht, welche Bewerbungen er zu
     * bearbeiten hat.
     *
     * @param token Das Webtoken
     * @return Die Bewerbungen
     */
    @GET
    @Path("/zubearbeiten")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getZuBearbeitende(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                List<Bewerbung> zuBearbeitende = bewerbungEJB.getEditable(dbPersonaler);

                List<Bewerbung> output = new ArrayList<>();

                //wenn die Bewerbung abgelehnt oder zurückgezogen wurde, aber noch nicht gelöscht ist,
                //muss sie nicht mehr angezeigt werden
                for (Bewerbung b : zuBearbeitende) {

                    if (b.getStatus() == 0 || b.getStatus() == 1 || b.getStatus() == 3) {
                        output.add(b.clone());
                    }
                }

                return response.build(200, parser.toJson(output));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route leitet eine Bewerbung an einen anderen Bewerber weiter. Das
     * bedeutet, dass dem anderen Bewerber nun diese Bewerbung zugewiesen ist.
     * Der Personaler, der die Anfrage stellt, ist daraufhin auch noch für diese
     * Bewerbung verantwortlich.
     *
     * @param daten Die Daten zu Bewerbung und Bewerber
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/weiterleiten")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response leiteWeiter(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                //Bewerbung
                Bewerbung dbBewerbung = bewerbungEJB.getById(parser.fromJson(jsonObject.get("bewerbungid"), Integer.class));

                //Personaler
                Personaler dbPersonaler = personalerEJB.getById(parser.fromJson(jsonObject.get("personalerid"), Integer.class));

                dbBewerbung.getPersonalerList().add(dbPersonaler);

                dbPersonaler.getBewerbungList().add(dbBewerbung);

                return response.build(200, parser.toJson("Erfolgreich weitergeleitet"));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route delegiert eine Bewerbung an einen anderen Bewerber. Das
     * bedeutet, dass dem anderen Bewerber nun diese Bewerbung zugewiesen ist.
     * Der Personaler, der die Anfrage stellt, ist daraufhin nicht mehr für
     * diese Bewerbung verantwortlich. Außerdem wird die Bewerbung auch für alle
     * Mitarbeiter des Teams entfernt.
     *
     * @param daten Die Daten zu Bewerbung und Bewerber
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/delegiere")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delegiere(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Personaler self = personalerEJB.getByToken(token);

                if (self == null) {
                    return response.buildError(404, "Es wurde kein Personaler gefunden");
                } else if (!self.getIschef()) {
                    return response.buildError(403, "Sie sind kein Chef");
                }

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                //Bewerbung
                Bewerbung dbBewerbung = bewerbungEJB.getById(parser.fromJson(jsonObject.get("bewerbungid"), Integer.class));

                //Personaler
                Personaler dbPersonaler = personalerEJB.getById(parser.fromJson(jsonObject.get("personalerid"), Integer.class));

                dbBewerbung.getPersonalerList().add(dbPersonaler);

                dbPersonaler.getBewerbungList().add(dbBewerbung);

                //Selbst entfernen, deswegen delegieren
                self.getBewerbungList().remove(dbBewerbung);

                dbBewerbung.getPersonalerList().remove(self);

                //alle Personaler aus dem eigenen Team entfernen
                for (Personaler p : personalerEJB.getTeamNotCloned(self)) {
                    p.getBewerbungList().remove(dbBewerbung);

                    dbBewerbung.getPersonalerList().remove(p);
                }

                return response.build(200, parser.toJson("Erfolgreich delegiert"));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route setzt den Status einer Bewerbung. Die möglichen setzbaren
     * Status sind abhängig davon, ob die Anfrage von einem Bewerber oder
     * Personaler stammt.
     *
     * @param daten Die Daten zu Bewerbung und Status
     * @param token Das Webtoken
     * @return Response mit Bestätigung oder Fehler
     */
    @POST
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setStatus(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                Bewerbung dbBewerbung = bewerbungEJB.getById(parser.fromJson(jsonObject.get("bewerbung"), Integer.class));

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                int status = parser.fromJson(jsonObject.get("status"), Integer.class);

                if (dbBewerber == null && dbPersonaler == null) {
                    return response.buildError(404, "Keine Person gefunden");
                } else if (dbBewerber != null) {
                    if (!dbBewerbung.getBewerber().equals(dbBewerber)) {
                        return response.buildError(403, "Sie haben nicht die nötige Berechtigung");
                    } else {
                        //Bewerber kann Bewerbung nur zurückziehen
                        if (status == 4) {
                            dbBewerbung.setStatus(status);
                            return response.build(200, parser.toJson("Status erfolgreich gesetzt"));
                        } else {
                            return response.buildError(403, "Dieser Status darf nicht gesetzt werden");
                        }
                    }
                } else if (dbPersonaler != null) {
                    if (!dbBewerbung.getPersonalerList().contains(dbPersonaler)) {
                        return response.buildError(403, "Sie haben nicht die nötige Berechtigung");
                    } else {
                        if (status == 1 || status == 2 || status == 3) {
                            dbBewerbung.setStatus(status);
                            return response.build(200, parser.toJson("Status erfolgreich gesetzt"));
                        } else {
                            return response.buildError(403, "Dieser Status darf nicht gesetzt werden");
                        }
                    }
                }

                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
