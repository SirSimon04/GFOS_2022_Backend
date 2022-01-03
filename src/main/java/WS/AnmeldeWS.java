package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FotoEJB;
import EJB.PersonalerEJB;
import Entities.Datei;
import Entities.Bewerber;
import Entities.Foto;
import Entities.Personaler;
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

@Path("/anmeldung")
@Stateless
@LocalBean
public class AnmeldeWS{

    private DateiEJB dateiEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private PersonalerEJB personalerEJB;

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
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(String daten){
        try{
            JsonObject loginUser = parser.fromJson(daten, JsonObject.class);
            String jsonMail = parser.fromJson((loginUser.get("mail")), String.class);
            String jsonPasswort = parser.fromJson((loginUser.get("passwort")), String.class);

            Bewerber dbBewerber = bewerberEJB.getByMail(jsonMail);

            Personaler dbPersonaler = personalerEJB.getByMail(jsonMail);

            if(dbBewerber != null){
                if(dbBewerber.getAuthcode() != null){
                    return response.buildError(401, "Du musst zuerst deinen Account bestätigen");
                }

                if(dbBewerber.getPassworthash().equals(hasher.checkPassword(jsonPasswort))){

                    String newToken = tokenizer.createNewToken(jsonMail);

                    blacklistEJB.removeToken(newToken); //In case the user logins while his token his still active, it has to be removed from bl

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.add("token", parser.toJsonTree(newToken));
                    jsonObject.add("ispersonaler", parser.toJsonTree(false));

                    return response.build(200, parser.toJson(jsonObject));
                }else{
                    return response.buildError(401, "Falsches Passwort");
                }
            }

            if(dbPersonaler != null){
                if(dbPersonaler.getPassworthash().equals(hasher.checkPassword(jsonPasswort))){
                    String newToken = tokenizer.createNewToken(jsonMail);

                    blacklistEJB.removeToken(newToken); //In case the user logins while his token his still active, it has to be removed from bl

                    JsonObject jsonObject = new JsonObject();

                    jsonObject.add("token", parser.toJsonTree(newToken));
                    jsonObject.add("ispersonaler", parser.toJsonTree(true));

                    return response.build(200, parser.toJson(jsonObject));
                }else{
                    return response.buildError(401, "Falsches Passwort");
                }
            }
            return response.buildError(401, "Mit dieser E-Mailadresse ist kein Konto vorhanden");

        }catch(Exception e){
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Bereits ausgeloggt");
        }else{
            try{

                blacklistEJB.addToken(token);

                return response.build(200, "Logout erfolgreich");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
