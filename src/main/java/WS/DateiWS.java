package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FotoEJB;
import Entities.Datei;
import Entities.Bewerber;
import Entities.Foto;
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

@Path("/datei")
@Stateless
@LocalBean
public class DateiWS{

    @EJB
    private DateiEJB dateiEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

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

    @POST
    @Path("/lebenslauf")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProfilbild(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Datei datei = new Datei();
                datei.setString(parser.fromJson(jsonObject.get("string"), String.class));
                Datei dateiDB = dateiEJB.add(datei);
                System.out.println(dbBewerber);
                bewerberEJB.setLebenslauf(dbBewerber, dateiDB);

                return response.build(200, "Lebenslauf erfolgreich geändert");
            }catch(Exception e){
                System.out.println(e);
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @DELETE
    @Path("/lebenslauf")
    @Produces(MediaType.APPLICATION_JSON)
    public Response asdf(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Datei lebenslauf = dbBewerber.getLebenslauf();

                if(lebenslauf != null){
                    dateiEJB.remove(bewerberEJB.getByToken(token).getLebenslauf());
                }

                dbBewerber.setLebenslauf(null);

                return response.build(200, "Lebenslauf wurde erfolgreich entfernt");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
