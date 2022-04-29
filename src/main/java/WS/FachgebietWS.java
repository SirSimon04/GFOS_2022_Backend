package WS;

import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.FachgebietEJB;
import EJB.PersonalerEJB;
import Entitiy.Bewerber;
import Entitiy.Fachgebiet;
import Entitiy.Personaler;
import Service.Antwort;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;

/**
 * <h1>Webservice für Fachgebiete</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Jobangebote bereit. Sie stellt somit
 * eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/fachgebiet")
@Stateless
@LocalBean
public class FachgebietWS {

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private FachgebietEJB fachgebietEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private Tokenizer tokenizer = new Tokenizer();

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
     * Diese Methode gibt das Fachgebiet eines Personalers oder eines Bewerbers
     * anhand seines Tokens wieder.
     *
     * @param token Das Webtoken
     * @return Das Fachgebiet
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwn(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (dbBewerber != null) {

                    if (dbBewerber.getFachgebiet() == null) {
                        return response.buildError(404, "Sie haben noch kein Fachgebiet");
                    }

                    return response.build(200, parser.toJson(dbBewerber.getFachgebiet().clone()));
                }

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler != null && dbPersonaler.getRang() == 0) {
                    return response.build(400, "Sie sind der Chef und haben deshalb kein Fachgebiet");
                }

                if (dbPersonaler != null) {
                    return response.build(200, parser.toJson(dbPersonaler.getFachgebiet().clone()));
                }

                return response.build(404, "Es wurde keine Person zu ihrem Token gefunden");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @PUT
    @Path("/bewerber")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateFachgebiet(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (dbBewerber != null) {
                    JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                    Fachgebiet dbFachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class));

                    if (dbFachgebiet != null) {
                        dbBewerber.setFachgebiet(dbFachgebiet);
                        dbFachgebiet.getBewerberList().add(dbBewerber);
                    } else {
                        return response.buildError(401, "Es wurde kein Fachgebiet gefunden");
                    }

                    return response.build(200, parser.toJson("Das Fachgebiet wurde erfolgreich geändert"));
                } else {
                    return response.buildError(404, "Es wurde kein Bewerber gefunden");
                }
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt das Fachgebiet eines Personalers anhand seiner Id
     * wieder.
     *
     * @param token Das Webtoken
     * @param id PersonalerId
     * @return Das Fachgebiet des Personalers
     */
    @GET
    @Path("/personaler/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPersonaler(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Personaler dbPersonaler = personalerEJB.getById(id);

                if (dbPersonaler != null) {
                    if (dbPersonaler.getRang() == 0) {
                        return response.build(400, "Der Chef hat kein Fachgebiet");
                    }
                    return response.build(200, parser.toJson(dbPersonaler.getFachgebiet().clone()));
                }

                return response.build(404, "Es wurde kein Personaler zu der ID gefunden");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt das Fachgebiet eines Bewerbers anhand seiner Id wieder.
     *
     * @param token Das Webtoken
     * @param id BewerberId
     * @return Das Fachgebiet des Bewerbers
     */
    @GET
    @Path("/bewerber/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBewerber(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getById(id);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler != null) {

                    if (dbBewerber.getFachgebiet() == null) {
                        return response.buildError(404, "Dieser Bewerber hat noch kein Fachgebiet");
                    }

                    return response.build(200, parser.toJson(dbBewerber.getFachgebiet().clone()));
                }

                return response.build(404, "Es wurde kein Bewerber zu der ID gefunden");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Fachgebiete zurück.
     *
     * @return Liste mit allen Fachgebieten
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {

        List<Fachgebiet> dbList = fachgebietEJB.getAll();

        List<Fachgebiet> output = new ArrayList<>();

        for (Fachgebiet f : dbList) {
            output.add(f.clone());
        }

        return response.build(200, parser.toJson(output));

    }

    /**
     * Diese Route gibt alle vom Chef angepinnten Fachgebiete zurück, damit
     * diese auf der Startseite angezeigt werden können. Dabei handelt es sich
     * um maximal 2 Fachgebiete.
     *
     * @return Die angepinnten Fachgebiete
     */
    @GET
    @Path("/pinned")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPinned() {
        try {
            List<Fachgebiet> pinned = fachgebietEJB.getPinnedByChef();

            List<Fachgebiet> output = new ArrayList<>();

            for (Fachgebiet f : pinned) {
                output.add(f.clone());
            }

            return response.build(200, parser.toJson(output));
        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

    /**
     * Mit dieser Route kann der Chef ein Fachgebiet anpinnen, damit es auf der
     * Startseite angezeigt wird. Sie kann nur vom Chef aufgerufen werden.
     *
     * @param token Das Webtoken
     * @param id FachgebietID
     * @return Response mit Fehler oder Bestätigung
     */
    @GET
    @Path("/admin/pin/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pinFachgebiet(@HeaderParam("Authorization") String token,
            @PathParam("id") int id
    ) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Fachgebiet fachgebiet = fachgebietEJB.getById(id);

                if (dbPersonaler.getRang() != 0) {
                    return response.buildError(403, "Sie sind nicht der Chef");
                } else if (fachgebietEJB.getPinnedByChef().size() >= 2) {
                    return response.buildError(403, "Es können maximal 2 Fachgebiete gepinnt werden");
                } else {

                    fachgebiet.setVonchefgepinnt(Boolean.TRUE);

                    return response.build(200, parser.toJson("Success"));
                }
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Mit dieser Route kann der Chef ein Fachgebiet entpinnen, damit es nicht
     * mehr auf der Startseite angezeigt wird. Sie kann nur vom Chef aufgerufen
     * werden.
     *
     * @param token Das Webtoken
     * @param id FachgebietId
     * @return Response mit Fehler oder Bestätigung
     */
    @GET
    @Path("/admin/unpin/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unpinFachgebiet(@HeaderParam("Authorization") String token,
            @PathParam("id") int id
    ) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler.getRang() != 0) {
                    return response.buildError(403, "Sie sind nicht der Chef");
                } else {

                    Fachgebiet fachgebiet = fachgebietEJB.getById(id);

                    fachgebiet.setVonchefgepinnt(Boolean.FALSE);

                    return response.build(200, parser.toJson("Success"));
                }
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    //TODO: Diese Route kann gelöscht werden
    /**
     * Mit dieser Route kann ein Fachgebiet hinzugefügt werden. Sie kann nur vom
     * Chef aufgerufen werden.
     *
     * @param daten Daten zum neuen Fachgebiet
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String name = parser.fromJson(jsonObject.get("name"), String.class);

                if (dbPersonaler.getRang() != 0) {
                    return response.buildError(403, "Sie sind nicht der Chef");
                } else if (fachgebietEJB.getByName(name) != null) {
                    return response.buildError(403, "Dieses Fachgebiet gibt es schon");
                } else {

//                    fachgebietEJB.add(new Fachgebiet(name, 0));
                    return response.build(200, parser.toJson("Success"));
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
