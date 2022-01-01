package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbereinstellungenEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FachgebietEJB;
import EJB.FotoEJB;
import EJB.InteressenfelderEJB;
import EJB.LebenslaufstationEJB;
import Entities.Adresse;
import Entities.Bewerber;
import Entities.Bewerbereinstellungen;
import Entities.Datei;
import Entities.Fachgebiet;
import Entities.Foto;
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
import javax.persistence.NoResultException;
import javax.ws.rs.DELETE;

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

    @EJB
    private FotoEJB fotoEJB;

    @EJB
    private DateiEJB dateiEJB;

    @EJB
    private BewerbereinstellungenEJB bewerbereinstellungenEJB;

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
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int id, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                return response.build(200, parser.toJson(bewerberEJB.getById(id).clone()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwnAccount(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if(dbBewerber == null){
                    return response.buildError(404, "Zu diesem Token wurde kein Account gefunden");
                }else{
                    return response.build(200, parser.toJson(dbBewerber.clone()));
                }

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @POST
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
            if(jsonObject.has("lebenslaufstationen")){
                Type LebenslaufstationenListType = new TypeToken<List<Lebenslaufstation>>(){
                }.getType();

                List<Lebenslaufstation> stations = parser.fromJson(jsonObject.get("lebenslaufstationen"), LebenslaufstationenListType);

                for(Lebenslaufstation l : stations){
                    Lebenslaufstation station = lebenslaufstationEJB.add(l);
                    bewerberEJB.addLebenslaufstation(dbBewerber, station);
                }
            }

            //Interessenfelder
            if(jsonObject.has("neueinteressenfelder")){
                Type interessenfelderListType = new TypeToken<List<String>>(){

                }.getType();

                List<String> interessenfelder = parser.fromJson(jsonObject.get("neueinteressenfelder"), interessenfelderListType);

                for(String interessenfeld : interessenfelder){
                    Interessenfelder field = interessenfelderEJB.getByName(interessenfeld);
                    if(field == null){
                        Interessenfelder feld = interessenfelderEJB.add(new Interessenfelder(interessenfeld));
                        bewerberEJB.addInteressengebiet(dbBewerber, feld);
                    }else{
                        if(!dbBewerber.getInteressenfelderList().contains(field)){
                            bewerberEJB.addInteressengebiet(dbBewerber, field);
                        }
                    }
                }
            }

//            Fachgebiet, muss gesetzt werden
            Fachgebiet fachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class)); //Fachgebiete sind schon vorgegeben, deswegen kein null check nötig

            bewerberEJB.setFachgebiet(dbBewerber, fachgebiet);

            //Profilbild
            if(jsonObject.has("neuesprofilbild")){

                Foto foto = new Foto();
                foto.setString(parser.fromJson(jsonObject.get("neuesprofilbild"), String.class));
                Foto fotoDB = fotoEJB.add(foto);
                bewerberEJB.setProfilbild(dbBewerber, fotoDB);
            }

            //Lebenslauf
            if(jsonObject.has("neuerlebenslauf")){
                Datei datei = new Datei();
                datei.setString(parser.fromJson(jsonObject.get("neuerlebenslauf"), String.class));
                Datei lebenslauf = dateiEJB.add(datei);
                bewerberEJB.setLebenslauf(dbBewerber, lebenslauf);
            }

            //Einstellungen
            Bewerbereinstellungen einstellungen = bewerbereinstellungenEJB.add(parser.fromJson(jsonObject.get("neueeinstellungen"), Bewerbereinstellungen.class));

            dbBewerber.setEinstellungen(einstellungen);

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

            return response.build(200, parser.toJson("Sie haben eine Bestätigunsmail zum Freischalten ihres Kontos erhalten."));

        }catch(Exception e){
            System.out.println(e.getMessage());
            return response.buildError(500, "Es ist ein Fehler aufgetreten!");
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

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBewerber(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                //Methode implementieren
                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                bewerberEJB.delete(dbBewerber);

                //TODO: Alle Bewerbungen dieses Nutzers müssen gelöscht werden
                return response.build(200, "Ihr Account wurde erfolgreich gelöscht.");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
