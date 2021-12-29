package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FotoEJB;
import Entities.Datei;
import Entities.Bewerber;
import Entities.Bewerbereinstellungen;
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

@Path("/bewerbereinstellungen")
@Stateless
@LocalBean
public class BewerbereinstellungenWS{

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

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setSettings(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerbereinstellungen einstellungen = parser.fromJson(daten, Bewerbereinstellungen.class);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                bewerberEJB.setSettings(dbBewerber, einstellungen);

                return response.build(200, "Einstellungen erfolgreich verändert");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
