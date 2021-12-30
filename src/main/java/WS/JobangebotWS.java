package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbungstypEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FachgebietEJB;
import EJB.FotoEJB;
import EJB.JobangebotEJB;
import EJB.PersonalerEJB;
import Entities.Datei;
import Entities.Bewerber;
import Entities.Bewerbungstyp;
import Entities.Fachgebiet;
import Entities.Foto;
import Entities.Jobangebot;
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

@Path("/jobs")
@Stateless
@LocalBean
public class JobangebotWS{

    @EJB
    private DateiEJB dateiEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private JobangebotEJB jobangebotEJB;

    @EJB
    private FachgebietEJB fachgebietEJB;

    @EJB
    private BewerbungstypEJB bewerbungstypEJB;

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
    public Response addJobangebot(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Jobangebot dbJobangebot = jobangebotEJB.add(parser.fromJson(daten, Jobangebot.class));

                //Ansprechpartner
                dbJobangebot.setAnsprechpartner(dbPersonaler);

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                //Fachgebiet
                Fachgebiet fachgebiet;

                if(dbPersonaler.getRang() == 0){
                    fachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class)); //Fachgebiete sind schon vorgegeben, deswegen kein null check nötig
                }else{
                    fachgebiet = dbPersonaler.getFachgebiet();
                }

                jobangebotEJB.setFachgebiet(dbJobangebot, fachgebiet);
                //Bewerbungstyp
                Bewerbungstyp bewerbungstyp = bewerbungstypEJB.getByName(parser.fromJson(jsonObject.get("neuerbewerbungstyp"), String.class));

                jobangebotEJB.setBewerbungstyp(dbJobangebot, bewerbungstyp);

                bewerbungstypEJB.addJobangebot(dbJobangebot, bewerbungstyp);

                return response.build(200, parser.toJson(dbJobangebot.clone()));
            }catch(Exception e){
                return response.buildError(500, e.getMessage());
            }
        }
    }
}
