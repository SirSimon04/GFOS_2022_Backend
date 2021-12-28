package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.FachgebietEJB;
import EJB.InteressenfelderEJB;
import EJB.LebenslaufstationEJB;
import Entities.Adresse;
import Entities.Bewerber;
import Entities.Fachgebiet;
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

    @EJB
    private InteressenfelderEJB interessenfelderEJB;

    @EJB
    private FachgebietEJB fachgebietEJB;

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

            for(Lebenslaufstation l : stations){
                Lebenslaufstation station = lebenslaufstationEJB.add(l);
                bewerberEJB.addLebenslaufstation(dbBewerber, station);
            }
//            Interessenfelder
            Type interessenfelderListType = new TypeToken<List<Interessenfelder>>(){
            }.getType();

            List<Interessenfelder> fields = parser.fromJson(jsonObject.get("interessenfelder"), interessenfelderListType);

            for(Interessenfelder f : fields){
                Interessenfelder field = interessenfelderEJB.getByName(f.getName());
                if(field != null){
                    bewerberEJB.addInteressengebiet(dbBewerber, field);
                }
            }

//            Fachgebiet
            Fachgebiet fachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class)); //Fachgebiete sind schon vorgegeben, deswegen kein null check nötig

            bewerberEJB.setFachgebiet(dbBewerber, fachgebiet);

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
            if(dbBewerber == null){
                return response.buildError(401, "Mit dieser E-Mailadresse ist kein Konto vorhanden");
            }

            if(dbBewerber.getAuthcode() != null){
                return response.buildError(401, "Du musst zuerst deinen Account bestätigen");
            }

            if(dbBewerber.getPassworthash().equals(hasher.checkPassword(jsonPasswort))){
                return response.build(200, parser.toJson(tokenizer.createNewToken(jsonMail)));
            }else{
                return response.buildError(401, "Falsches Passwort");
            }

        }catch(Exception e){
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

    @POST
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyAccount(String daten){
        try{
            JsonObject loginUser = parser.fromJson(daten, JsonObject.class);
            String jsonMail = parser.fromJson((loginUser.get("mail")), String.class);
            String jsonPasswort = parser.fromJson((loginUser.get("passwort")), String.class);
            int jsonAuth = parser.fromJson(loginUser.get("authcode"), Integer.class);

            Bewerber dbBewerber = bewerberEJB.getByMail(jsonMail);
            if(dbBewerber == null){
                return response.buildError(401, "Mit dieser E-Mailadresse ist kein Konto vorhanden");
            }

            if(dbBewerber.getPassworthash().equals(hasher.checkPassword(jsonPasswort))){

                if(dbBewerber.getAuthcode() == jsonAuth){
                    dbBewerber.setAuthcode(null);
                    return response.build(200, parser.toJson(tokenizer.createNewToken(jsonMail)));
                }else{
                    return response.buildError(401, "Falscher Authcode");
                }

            }else{
                return response.buildError(401, "Falsches Passwort");
            }

        }catch(Exception e){
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

}
