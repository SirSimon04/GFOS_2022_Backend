package WS;

import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import Entities.Bewerber;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/bewerber")
@Stateless
@LocalBean
public class BewerberWS{

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private Tokenizer tokenizer = new Tokenizer();

    public boolean verifyToken(String token){
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
    @Path("/testToken/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testToken(@PathParam("token") String token){
        if(this.verifyToken(token)){

            return response.build(200, token);
        }else{
            return response.buildError(401, "Token ungueltig");
        }
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten){
        try{

            Bewerber newBewerber = parser.fromJson(daten, Bewerber.class);

            //check if mail is registered
            Bewerber mailIsRegistered = bewerberEJB.getByMail(newBewerber.getEmail());

            if(mailIsRegistered != null){
                return response.buildError(400, "Diese E-Mail Adresse ist bereits registriert");
            }

            newBewerber.setPassworthash(hasher.checkPassword(newBewerber.getPassworthash()));

            //send mail with verification pin
            Bewerber neuerBewerber = bewerberEJB.add(newBewerber);

            Bewerber mailAuth = bewerberEJB.getById(1);
            String mailFrom = mailAuth.getEmail();
            String pw = mailAuth.getPassworthash();
            System.out.println(mailFrom + " " + pw);
            int min = 1000;
            int max = 9999;
            int random_int = (int) (Math.random() * (max - min + 1) + min);
            neuerBewerber.setAuthcode(random_int);
            String neuerNutzername = neuerBewerber.getVorname() + " " + neuerBewerber.getName();
            String neueEmail = neuerBewerber.getEmail();
            mail.sendVerificationPin(mailFrom, pw, neuerNutzername, neueEmail, random_int);

            return response.build(200, parser.toJson(tokenizer.createNewToken(newBewerber.getEmail())));

        }catch(Exception e){
            System.out.println(e.getMessage());
            return response.buildError(500, "Es ist ein Fehler aufgetreten!");
        }
    }

    //login
//    JsonObject loginUser = parser.fromJson(Daten, JsonObject.class);
//    String jsonUsername = parser.fromJson((loginUser.get("benutzername")), String.class);
//    String jsonPasswort = parser.fromJson((loginUser.get("passwort")), String.class);
}
