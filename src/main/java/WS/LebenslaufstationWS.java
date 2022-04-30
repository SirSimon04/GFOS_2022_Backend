package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.InteressenfelderEJB;
import EJB.LebenslaufstationEJB;
import EJB.PersonalerEJB;
import Entitiy.Bewerber;
import Entitiy.Lebenslaufstation;
import Entitiy.Personaler;
import Service.ResponseService;
import Service.FileService;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
import javax.ws.rs.DELETE;

/**
 * <h1>Webservice für Lebenslaufstationen</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Lebenslaufstationen bereit. Sie
 * stellt somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/lebenslauf")
@Stateless
@LocalBean
public class LebenslaufstationWS {

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private AdresseEJB adresseEJB;

    @EJB
    private LebenslaufstationEJB lebenslaufstationEJB;

    @EJB
    private InteressenfelderEJB interessenfelderEJB;

    @EJB
    private final PersonalerEJB personalerEJB = new PersonalerEJB();

    private final ResponseService response = new ResponseService();

    private final Gson parser = new Gson();

    private final Gson nullParser = new GsonBuilder().serializeNulls().create();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private final FileService fileService = new FileService();

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
     * Diese Route gibt alle Lebenslaufstationen eines Bewerbers anhand des
     * Tokens wieder. Dabei wird überprüft, ob das Profil des Bewerbers
     * öffentlich ist.
     *
     * @param token Das Webtoken
     * @return Die Lebenslaufstationen.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOwn(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (dbBewerber == null) {
                    return response.buildError(404, "Es wurde kein bewerber gefunden");
                }

                List<Lebenslaufstation> lebenslaufstationList = lebenslaufstationEJB.getByBewerberDetached(dbBewerber);

                Collections.sort(lebenslaufstationList);

                for (Lebenslaufstation l : lebenslaufstationList) {
                    try {
                        String base64 = fileService.getLebenslaufstation(l.getLebenslaufstationid());
                        l.setReferenz(base64);
                    } catch (Exception e) {
                        l.setReferenz(null);
                    }

                }

                return response.build(200, nullParser.toJson(lebenslaufstationList));

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Lebenslaufstationen anhand der Id des
     * Bewerberswieder. Dabei wird überprüft, ob das Profil des Bewerbers
     * öffentlich ist.
     *
     * @param token Das Webtoken
     * @param id Die BewerberId
     * @return Liste mit Lebenslaufstationen
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllById(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Bewerber b = bewerberEJB.getById(id);

                List<Lebenslaufstation> lebenslaufstationList = lebenslaufstationEJB.getByBewerberDetached(b);

                Collections.sort(lebenslaufstationList);

                for (Lebenslaufstation l : lebenslaufstationList) {
                    try {
                        String base64 = fileService.getLebenslaufstation(l.getLebenslaufstationid());
                        l.setReferenz(base64);
                    } catch (Exception e) {
                        l.setReferenz(null);
                    }

                }

                if (Objects.equals(dbBewerber, b)) {
                    return response.build(200, nullParser.toJson(lebenslaufstationList));
                } else if (dbPersonaler != null) {
                    return response.build(200, nullParser.toJson(lebenslaufstationList));
                } else {
                    return response.buildError(403, "Kein Personaler oder Bewerber gefunden");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt die Referenz zu einer Lebenslaufstation anhand der Id
     * wieder. Darauf haben nur der Bewerber Zugriff, zu dem die
     * Lebenslaufstation gehört und alle Personaler
     *
     * @param token Das Webtoken
     * @param id Id
     * @return Lebenslaufstation als PDF
     */
    @GET
    @Path("/referenz/{lebenslaufstationid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReferenzById(@HeaderParam("Authorization") String token, @PathParam("lebenslaufstationid") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Lebenslaufstation lebenslaufstation = lebenslaufstationEJB.getById(id);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbBewerber != null) {
                    if (dbBewerber.getLebenslaufstationList().contains(lebenslaufstation)) {

                        String station = fileService.getLebenslaufstation(id);

                        return response.build(200, parser.toJson(station));
                    } else {
                        return response.buildError(403, "Diese Lebenslaufstation ist nicht von dir");
                    }
                } else if (dbPersonaler != null) {

                    String station = fileService.getLebenslaufstation(id);

                    return response.build(200, parser.toJson(station));
                } else {
                    return response.buildError(404, "Es wurde kein Personaler oder Bewerber gefunden");
                }
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Mit dieser Route kann ein Bewerber eine Referenz zu einer
     * Lebenslaufstation hinzufügen.
     *
     * @param daten Referenz als Base64
     * @param token Das Webtoken
     * @param id Id der Lebenslaufstation
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/referenz/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadReference(String daten, @HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String base64 = parser.fromJson(jsonObject.get("string"), String.class);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Lebenslaufstation lebenslaufstation = lebenslaufstationEJB.getById(id);

                if (dbBewerber.getLebenslaufstationList().contains(lebenslaufstation)) {
                    fileService.saveLebenslaufstation(id, base64);
                    return response.build(200, parser.toJson("Referenz erfolgreich geändert"));
                } else {
                    return response.buildError(403, "Diese Station gehört nicht Ihnen");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Mit dieser Route kann ein Bewerber die Referenz zu einer
     * Lebenslaufstation löschen
     *
     * @param token Das Webtoken
     * @param id Id der Lebenslaufstation
     * @return Response mit Bestätigung oder Fehler
     */
    @DELETE
    @Path("/referenz/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteReferenz(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Lebenslaufstation lebenslaufstation = lebenslaufstationEJB.getById(id);

                if (dbBewerber.getLebenslaufstationList().contains(lebenslaufstation)) {
                    fileService.deleteLebenslaufstation(id);
                    return response.build(200, parser.toJson("Erfolgreich geändert"));
                } else {
                    return response.buildError(403, "Diese Station gehört nicht Ihnen");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route fügt einem Bewerber eine Lebenslaufstation hinzu.
     *
     * @param daten Die neue Lebenslaufstation
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Lebenslaufstation l = parser.fromJson(daten, Lebenslaufstation.class);
                l.setStart((Date) l.getStart());
                l.setEnde((Date) l.getEnde());
                Lebenslaufstation lDB = lebenslaufstationEJB.add(l);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);
                dbBewerber.getLebenslaufstationList();

                if (dbBewerber.getLebenslaufstationList() == null) {
                    dbBewerber.setLebenslaufstationList(new ArrayList<>());
                }

                dbBewerber.getLebenslaufstationList().add(lDB);

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                if (jsonObject.has("string")) {
                    String base64 = parser.fromJson(jsonObject.get("string"), String.class);

                    fileService.saveLebenslaufstation(lDB.getLebenslaufstationid(), base64);
                }

                return response.build(200, parser.toJson("Success"));

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route entfernt eine Lebenslaufstation.
     *
     * @param id LebenslaufstationId
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("id") int id, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Lebenslaufstation lDB = lebenslaufstationEJB.getById(id);

                Bewerber bewerberDB = bewerberEJB.getByToken(token);

                if (bewerberDB.getLebenslaufstationList().contains(lDB)) {
                    bewerberDB.getLebenslaufstationList().remove(lDB);

                    try {
                        fileService.deleteLebenslaufstation(id);
                    } catch (FileNotFoundException e) {

                    }

                    lebenslaufstationEJB.remove(lDB);

                    return response.build(200, parser.toJson("Success"));
                } else {
                    return response.buildError(403, "Diese Station gehört nicht dir");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
