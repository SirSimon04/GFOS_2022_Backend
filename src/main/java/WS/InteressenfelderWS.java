package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.InteressenfelderEJB;
import EJB.LebenslaufstationEJB;
import EJB.PersonalerEJB;
import Entitiy.Adresse;
import Entitiy.Bewerber;
import Entitiy.Interessenfelder;
import Entitiy.Lebenslaufstation;
import Entitiy.Personaler;
import Service.ResponseService;
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
import java.util.Objects;
import javax.ws.rs.DELETE;

/**
 * <h1>Webservice für Interessenfelder</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Interessenfelder bereit. Sie stellt
 * somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/interessenfeld")
@Stateless
@LocalBean
public class InteressenfelderWS {

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
    private PersonalerEJB personalerEJB;

    private final ResponseService response = new ResponseService();

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
     * Diese Route gibt die Interessenfelder eines Bewerbers anhand des Tokens
     * wieder.
     *
     * @param token Das Webtoken
     * @return Die Interessenfelderliste
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwn(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                return response.build(200, parser.toJson(bewerberEJB.getByToken(token).getInteressenfelderList()));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt die Interessenfelder eines Bewerbers anhand der Id
     * wieder. Das ist nur für den Bewerber, dem die Interessenfelder zugeordnet
     * ist, oder allen Personalern möglich.
     *
     * @param token Das Webtoken
     * @param id BewerberId
     * @return Die Interessenfelder
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber bewerber = bewerberEJB.getById(id);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (Objects.equals(bewerber, dbBewerber) || dbPersonaler != null) {
                    return response.build(200, parser.toJson(bewerber.getInteressenfelderList()));
                } else {
                    return response.buildError(403, "Sie haben nicht die nötige Berechtigung");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Methode gibt alle Interessenfelder wieder.
     *
     * @param token Das Webtoken
     * @return Die Interessenfelder
     */
    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                return response.build(200, parser.toJson(interessenfelderEJB.getAll()));

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route fügt ein Interessensfeld zu einem Nutzer hinzu. Wenn das neue
     * Interessenfeld noch nicht vorhanden ist, wird es in die Datenbank
     * geschrieben.
     *
     * @param daten Das neue Interessenfeld
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String name = parser.fromJson(jsonObject.get("name"), String.class);

                Interessenfelder field = interessenfelderEJB.getByName(name);
                if (field == null) {
                    Interessenfelder feld = interessenfelderEJB.add(new Interessenfelder(name));
                    dbBewerber.getInteressenfelderList().add(feld);
                } else {
                    if (!dbBewerber.getInteressenfelderList().contains(field)) {
                        dbBewerber.getInteressenfelderList().add(field);
                    }
                }

                return response.build(200, parser.toJson("Success"));

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route löscht ein Interessensfeld von einem Nutzer.
     *
     * @param daten Das Interessenfeld
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String name = parser.fromJson(jsonObject.get("name"), String.class);

                Interessenfelder fDB = interessenfelderEJB.getByName(name);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);
                dbBewerber.getInteressenfelderList().remove(fDB);

                return response.build(200, parser.toJson("Success"));

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
