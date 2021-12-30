package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FachgebietEJB;
import EJB.FotoEJB;
import EJB.PersonalerEJB;
import Entities.Datei;
import Entities.Bewerber;
import Entities.Fachgebiet;
import Entities.Foto;
import Entities.Personaler;
import Service.Antwort;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Objects;
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

@Path("/personaler")
@Stateless
@LocalBean
public class PersonalerWS{

    @EJB
    private DateiEJB dateiEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private PersonalerEJB personalerEJB;

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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Personaler self = personalerEJB.getByToken(token);

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                //Fachgebiet
                Fachgebiet fachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class)); //Fachgebiete sind schon vorgegeben, deswegen kein null check nötig

                if(self.getRang() == 0 || Objects.equals(fachgebiet.getFachgebietid(), self.getFachgebiet().getFachgebietid())){ //Wenn der Chef einen neuen Personaler erstellt, kann dieser jedem Fachgebeit angehören
                    Personaler newPersonaler = parser.fromJson(daten, Personaler.class);

                    if(personalerEJB.getByMail(newPersonaler.getEmail()) != null){
                        return response.buildError(400, "Es gibt schon einen Personaler mit dieser E-Mailadresse");
                    }

                    newPersonaler.setRang(self.getRang() + 1);

                    newPersonaler.setPassworthash(hasher.checkPassword(newPersonaler.getPassworthash()));

                    Personaler dbPersonaler = personalerEJB.add(newPersonaler);

                    personalerEJB.setFachgebiet(dbPersonaler, fachgebiet);
                    return response.build(200, "Erfolgreich den neuen Personaler erstellt");
                }else{
                    return response.buildError(400, "Es ist nur möglich, Personaler dem eigenen Fachgebeit hinzuzufügen");
                }

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSelf(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                return response.build(200, parser.toJson(personalerEJB.getByToken(token).clone()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Path("/team")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeam(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                return response.build(200, parser.toJson(personalerEJB.getTeam(dbPersonaler)));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    //getAbove in Hierarchie
    //getBelow in Hierarchie
    //getTeam
}
