package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.InteressenfelderEJB;
import EJB.LebenslaufstationEJB;
import Entities.Bewerber;
import Entities.Lebenslaufstation;
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.DELETE;

/**
 * <h1>Webservice für Lebenslaufstationen</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Lebenslaufstationen bereit.
 * Sie stellt somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/lebenslauf")
@Stateless
@LocalBean
public class LebenslaufstationWS{

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

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private Tokenizer tokenizer = new Tokenizer();

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
     * Diese Route gibt alle Lebenslaufstationen eines Bewerbers anhand des Tokens wieder.
     * Dabei wird überprüft, ob das Profil des Bewerbers öffentlich ist.
     *
     * @param token Das Webtoken
     * @return Die Lebenslaufstationen.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOwn(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                return response.build(200, parser.toJson(dbBewerber.getLebenslaufstationList()));

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Lebenslaufstationen anhand der Id des Bewerberswieder.
     * Dabei wird überprüft, ob das Profil des Bewerbers öffentlich ist.
     *
     * @param token Das Webtoken
     * @param id Die BewerberId
     * @return
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllById(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber b = bewerberEJB.getById(id);

                if(b.getEinstellungen().getIspublic()){
                    return response.build(200, parser.toJson(b.getLebenslaufstationList()));
                }else{
                    return response.buildError(400, "Der Bewerber hat ein privates Profil");
                }

            }catch(Exception e){
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
    public Response add(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Lebenslaufstation l = parser.fromJson(daten, Lebenslaufstation.class);

                Lebenslaufstation lDB = lebenslaufstationEJB.add(l);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                dbBewerber.getLebenslaufstationList().add(lDB);

                return response.build(200, "true");

            }catch(Exception e){
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
    public Response remove(@PathParam("id") int id, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Lebenslaufstation lDB = lebenslaufstationEJB.getById(id);

                Bewerber bewerberDB = bewerberEJB.getByToken(token);

                bewerberDB.getLebenslaufstationList().remove(lDB);

                return response.build(200, "true");

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
