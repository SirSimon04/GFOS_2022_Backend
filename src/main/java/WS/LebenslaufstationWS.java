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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllOwn(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber b = bewerberEJB.getByToken(token);

                return response.build(200, parser.toJson(b.getLebenslaufstationList()));

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Lebenslaufstation l = parser.fromJson(daten, Lebenslaufstation.class);

                Lebenslaufstation lDB = lebenslaufstationEJB.add(l);

                Bewerber bewerberDB = bewerberEJB.getByToken(token);

                bewerberEJB.addLebenslaufstation(bewerberDB, lDB);

                return response.build(200, "true");

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Path("/remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response remove(@PathParam("id") int id, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Lebenslaufstation lDB = lebenslaufstationEJB.getById(id);

                Bewerber bewerberDB = bewerberEJB.getByToken(token);

                bewerberEJB.removeLebenslaufstation(bewerberDB, lDB);

                return response.build(200, "true");

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
