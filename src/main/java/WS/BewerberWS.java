package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.LebenslaufstationEJB;
import Entities.Adresse;
import Entities.Bewerber;
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

@Path("/bewerber")
@Stateless
@LocalBean
public class BewerberWS{

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private AdresseEJB adresseEJB;

    @EJB
    private LebenslaufstationEJB lebenslaufstationEJB;

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
    @Path("/testToken/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testToken(@PathParam("token") String token){
        if(this.verify(token)){

            return response.build(200, token);
        }else{
            return response.buildError(401, "Token ungueltig");
        }
    }

    @GET
    @Path("/getById/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int id, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                return response.build(200, parser.toJson(bewerberEJB.getById(id)));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten){
        try{

            Bewerber neuerBewerber = parser.fromJson(daten, Bewerber.class);

            //check if mail is registered
            Bewerber mailIsRegistered = bewerberEJB.getByMail(neuerBewerber.getEmail());

            if(mailIsRegistered != null){
                return response.buildError(400, "Diese E-Mail Adresse ist bereits registriert");
            }
            //set pw
            neuerBewerber.setPassworthash(hasher.checkPassword(neuerBewerber.getPassworthash()));

            Bewerber dbBewerber = bewerberEJB.add(neuerBewerber);//add to db

            JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

            //add adress
            Adresse neueAdresse = parser.fromJson((jsonObject.get("neueadresse")), Adresse.class);

            Adresse dbAdresse = adresseEJB.add(neueAdresse);

            dbBewerber.setAdresse(dbAdresse);

            //Lebenslaufstationen
            Type LebenslaufstationenListType = new TypeToken<List<Lebenslaufstation>>(){
            }.getType();

            List<Lebenslaufstation> stations = parser.fromJson(jsonObject.get("lebenslaufstationen"), LebenslaufstationenListType);

//            dbBewerber.getLebenslaufstationList().addAll(stations);
            for(Lebenslaufstation l : stations){
                Lebenslaufstation station = lebenslaufstationEJB.add(l);
                dbBewerber.getLebenslaufstationList().add(station);
            }

            //send verification pin
            Bewerber mailAuth = bewerberEJB.getById(1);
            String mailFrom = mailAuth.getEmail();
            String pw = mailAuth.getPassworthash();
            System.out.println(mailFrom + " " + pw);
            int min = 1000;
            int max = 9999;
            int random_int = (int) (Math.random() * (max - min + 1) + min);
            dbBewerber.setAuthcode(random_int);
            String neuerNutzername = dbBewerber.getVorname() + " " + dbBewerber.getName();
            String neueEmail = dbBewerber.getEmail();
            mail.sendVerificationPin(mailFrom, pw, neuerNutzername, neueEmail, random_int);

            return response.build(200, parser.toJson(tokenizer.createNewToken(neuerBewerber.getEmail())));

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
