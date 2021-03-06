package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BlacklistEJB;
import EJB.FachgebietEJB;
import EJB.PersonalerEJB;
import Entitiy.Bewerber;
import Entitiy.Bewerbung;
import Entitiy.Fachgebiet;
import Entitiy.Personaler;
import Service.ResponseService;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
 * <h1>Webservice für Personaler</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Personaler bereit. Sie stellt somit
 * eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/personaler")
@Stateless
@LocalBean
public class PersonalerWS {

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerbungEJB bewerbungEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private FachgebietEJB fachgebietEJB;

    private final ResponseService response = new ResponseService();

    private final Gson parser = new Gson();

    private final Hasher hasher = new Hasher();

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
     * Diese Route fügt einen neuen Personaler hinzu. Wenn diese Aktion vom Chef
     * ausgeführt wird, kann dieser das Fachgebiet des neuen Personalers
     * aussuchen, sonst wird das Fachgebiet des Personalers übernommen, der den
     * Neuen hinzufügt.
     *
     * @param daten Die Daten des neuen Personalers
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler self = personalerEJB.getByToken(token);

                if (!self.getIschef()) {
                    return response.buildError(401, "Sie sind kein Chef");
                }

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                //Fachgebiet
                Fachgebiet fachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class)); //Fachgebiete sind schon vorgegeben, deswegen kein null check nötig

                if (self.getRang() == 0 || Objects.equals(fachgebiet.getFachgebietid(), self.getFachgebiet().getFachgebietid())) { //Wenn der Chef einen neuen Personaler erstellt, kann dieser jedem Fachgebeit angehören
                    Personaler newPersonaler = parser.fromJson(daten, Personaler.class);

                    if (personalerEJB.getByMail(newPersonaler.getEmail()) != null) {
                        return response.buildError(400, "Es gibt schon einen Personaler mit dieser E-Mailadresse");
                    }

                    newPersonaler.setRang(self.getRang() + 1);

                    newPersonaler.setPassworthash(hasher.checkPassword(newPersonaler.getPassworthash()));

                    Personaler dbPersonaler = personalerEJB.add(newPersonaler);

                    dbPersonaler.setFachgebiet(fachgebiet);

                    if (personalerEJB.getTeam(dbPersonaler).isEmpty()) {
                        dbPersonaler.setIschef(true);
                    } else {
                        dbPersonaler.setIschef(false);
                    }

                    //send mail
                    String userName = dbPersonaler.getVorname() + " " + dbPersonaler.getName();
                    String mail = dbPersonaler.getEmail();
                    mailService.sendNewEmployer(userName, mail);

                    return response.build(200, parser.toJson("Erfolgreich den neuen Personaler erstellt"));
                } else {
                    return response.buildError(400, "Es ist nur möglich, Personaler dem eigenen Fachgebiet hinzuzufügen");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt eine Personaler anhand seines Tokens wieder.
     *
     * @param token Das Webtoken
     * @return Der Personaler
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSelf(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler != null) {
                    return response.build(200, parser.toJson(dbPersonaler.clone()));
                } else {
                    return response.buildError(404, "Zum Token wurde kein Nutzer gefunden.");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Methode gibt das Team eines Personalers wieder. Zu einem Team
     * gehören immer die Personaler, die sich auf einer Ebene befinden und dem
     * gleichen Fachgebiet angehören.
     *
     * @param token Das Webtoken
     * @return Das Team
     */
    @GET
    @Path("/team")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeam(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                return response.build(200, parser.toJson(personalerEJB.getTeam(dbPersonaler)));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt das Team über einem Personaler wieder. Zu einem Team
     * gehören immer die Personaler, die sich auf einer Ebene befinden und dem
     * gleichen Fachgebiet angehören.
     *
     * @param token Das Webtoken
     * @return Das Team eine Ebene höher
     */
    @GET
    @Path("/team/above")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAboveTeam(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler.getRang() == 0) {
                    return response.buildError(400, "Du bist bereits der Chef");
                }

                return response.build(200, parser.toJson(personalerEJB.getAboveTeam(dbPersonaler)));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt das Team unter einem Personaler wieder. Zu einem Team
     * gehören immer die Personaler, die sich auf einer Ebene befinden und dem
     * gleichen Fachgebiet angehören.
     *
     * @param token Das Webtoken
     * @return Das Team eine Ebene niederiger
     */
    @GET
    @Path("/team/below")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBelowTeam(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                return response.build(200, parser.toJson(personalerEJB.getBelowTeam(dbPersonaler)));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Mitarbeiter weiter, an die eine Bewerbung
     * weitergeleitet werden kann. Das sind Mitarbeiter, die im gleichen Team
     * sind.
     *
     * @param token Das Webtoken
     * @param id Die BewerbungId
     * @return Liste mit weiterleitbaren Mitarbeitern
     */
    @GET
    @Path("/weiterleitbar/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWeiterleitbar(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Bewerbung dbBewerbung = bewerbungEJB.getById(id);

                List<Personaler> personaler = new ArrayList<>();

                personaler.addAll(personalerEJB.getTeam(dbPersonaler));

                List<Personaler> output = new ArrayList<>();

                for (Personaler p : personaler) {
                    if (!dbBewerbung.getPersonalerList().contains(p)) {
                        output.add(p.clone());
                    }

                }
                return response.build(200, parser.toJson(output));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt den Mitarbeiter wieder, an den eine Bewerbung delegiert
     * werden kann. Das ist der Chef der Ebene unter dem anfragenden Personaler.
     *
     * @param token Das Webtoken
     * @param id Die BewerbungId
     * @return Der delegierbare Mitarbeiter
     */
    @GET
    @Path("/delegierbar/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDelegierbar(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Bewerbung dbBewerbung = bewerbungEJB.getById(id);

                if (dbBewerbung == null) {
                    return response.buildError(405, "Diese Bewerbung wurde nicht gefunden");
                } else if (!dbBewerbung.getPersonalerList().contains(dbPersonaler)) {
                    return response.buildError(403, "Sie arbeiten nicht an dieser Bewerbung");
                } else if (!dbPersonaler.getIschef()) {
                    return response.buildError(401, "Du bist keine Chef");
                } else {

                    List<Personaler> personaler = personalerEJB.getBelowTeam(dbPersonaler, dbBewerbung.getJobangebot().getFachgebiet());

                    List<Personaler> output = new ArrayList<>();

                    for (Personaler p : personaler) {
                        if (!dbBewerbung.getPersonalerList().contains(p) && p.getIschef()) {
                            output.add(p.clone());
                        }
                    }

                    return response.build(200, parser.toJson(output));

                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
