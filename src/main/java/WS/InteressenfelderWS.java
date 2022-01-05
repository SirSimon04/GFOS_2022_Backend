package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.InteressenfelderEJB;
import EJB.LebenslaufstationEJB;
import Entities.Adresse;
import Entities.Bewerber;
import Entities.Interessenfelder;
import Entities.Lebenslaufstation;
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
import javax.ws.rs.DELETE;

/**
 * <h1>Webservice für Interessenfelder</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Interessenfelder bereit.
 * Sie stellt somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/interessenfeld")
@Stateless
@LocalBean
public class InteressenfelderWS{

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
     * Diese Route gibt die Interessenfelder eines Bewerbers anhand des Tokens wieder.
     *
     * @param token Das Webtoken
     * @return Die Interessenfelderliste
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwn(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                return response.build(200, parser.toJson(bewerberEJB.getByToken(token).getInteressenfelderList()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt die Interessenfelder eines Bewerbers anhand der Id wieder.
     *
     * @param token Das Webtoken
     * @param id BewerberId
     * @return Die Interessenfelder
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Bewerber dbBewerber = bewerberEJB.getById(id);

                if(dbBewerber.getEinstellungen().getIspublic()){
                    return response.build(200, parser.toJson(dbBewerber.getInteressenfelderList()));
                }else{
                    return response.buildError(403, "Dieses Profil ist nicht öffentlich");
                }

            }catch(Exception e){
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
    public Response getAll(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                return response.build(200, parser.toJson(interessenfelderEJB.getAll()));

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route fügt ein Interessensfeld zu einem Nutzer hinzu.
     * Wenn das neue Interessenfeld noch nicht vorhanden ist, wird es
     * in die Datenbank geschrieben.
     *
     * @param daten Das neue Interessenfeld
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Bewerber bewerberDB = bewerberEJB.getByToken(token);

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String name = parser.fromJson(jsonObject.get("name"), String.class);

                Interessenfelder field = interessenfelderEJB.getByName(name);
                if(field == null){
                    Interessenfelder feld = interessenfelderEJB.add(new Interessenfelder(name));
                    bewerberEJB.addInteressengebiet(bewerberDB, feld);
                }else{
                    if(!bewerberDB.getInteressenfelderList().contains(field)){
                        bewerberEJB.addInteressengebiet(bewerberDB, field);
                    }
                }

                return response.build(200, "true");

            }catch(Exception e){
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
    public Response remove(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String name = parser.fromJson(jsonObject.get("name"), String.class);

                Interessenfelder fDB = interessenfelderEJB.getByName(name);

                Bewerber bewerberDB = bewerberEJB.getByToken(token);

                bewerberEJB.removeInteressengebiet(bewerberDB, fDB);

                return response.build(200, "true");

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
