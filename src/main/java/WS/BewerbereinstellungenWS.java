package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import Entitiy.Bewerber;
import Entitiy.Bewerbereinstellungen;
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
 * <h1>Webservice für die Bewerbereinstellungen</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Einstellungen der Bewerber bereit.
 * Sie stellt somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/bewerbereinstellungen")
@Stateless
@LocalBean
public class BewerbereinstellungenWS {

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

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
     * Diese Route gibt die Einstellungen eines Bewerbers anhand seines Tokens
     * wieder
     *
     * @param token Das Webtoken
     * @return Die Einstellungen
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwn(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                return response.build(200, parser.toJson(bewerberEJB.getByToken(token).getEinstellungen()));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route aktualisiert die Einstellungen eines Bewerbers
     *
     * @param daten Die neuen Einstellungen
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setSettings(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerbereinstellungen einstellungen = parser.fromJson(daten, Bewerbereinstellungen.class);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (einstellungen.getGetmails() != null) {
                    dbBewerber.getEinstellungen().setGetmails(einstellungen.getGetmails());
                }
                if (einstellungen.getTwofa() != null) {
                    dbBewerber.getEinstellungen().setTwofa(einstellungen.getTwofa());
                }

                return response.build(200, parser.toJson("Einstellungen erfolgreich verändert"));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
