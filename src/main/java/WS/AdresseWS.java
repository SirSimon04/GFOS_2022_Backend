package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import Entities.Adresse;
import Entities.Bewerber;
import Service.Antwort;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
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
 * <h1>Webservice für die Adresse</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Adresse der Bewerber bereit.
 * Sie stellt somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/adresse")
@Stateless
@LocalBean
public class AdresseWS{

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
     * Diese Route verifiziert ein Token
     *
     * @param token Das Webtoken
     * @return Status des Tokens
     */
    public boolean verify(String token){
        System.out.println("WS.BewerberWS.verifyToken()");
        if(tokenizer.isOn()){
            if(blacklistEJB.onBlacklist(token)){
                return false;
            }
            return tokenizer.verifyToken(token) != null;
        }else{
            return true;
        }
    }

    /**
     * Diese Route gibt die Adresse eines Bewerber anhand seines Tokens zurück
     *
     * @param token Das Webtoken
     * @return Die Adresse des Bewerbers
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwn(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                return response.build(200, parser.toJson(bewerberEJB.getByToken(token).getAdresse()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Methode aktualisiert die Adresse eines Nutzers
     *
     * @param daten Die neue Adresse
     * @param token Das Webtoken
     * @return Response mit Bestätigung oder Fehler
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAddress(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{

            try{
                Adresse dbAdresse = bewerberEJB.getByMail(tokenizer.getMail(token)).getAdresse();

                Adresse neueAdresse = parser.fromJson(daten, Adresse.class);

                dbAdresse.setHausnummer(neueAdresse.getHausnummer());
                dbAdresse.setLand(neueAdresse.getLand());
                dbAdresse.setPlz(neueAdresse.getPlz());
                dbAdresse.setStadt(neueAdresse.getStadt());
                dbAdresse.setStrasse(neueAdresse.getStrasse());

                return response.build(200, parser.toJson("Adresse geändert"));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
