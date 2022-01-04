package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BewerbungsnachrichtEJB;
import EJB.BewerbungstypEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FachgebietEJB;
import EJB.FotoEJB;
import EJB.JobangebotEJB;
import EJB.PersonalerEJB;
import Entities.Datei;
import Entities.Bewerber;
import Entities.Bewerbung;
import Entities.Bewerbungsnachricht;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private BewerbungEJB bewerbungEJB;

    @EJB
    private BewerbungsnachrichtEJB bewerbungsnachrichtEJB;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(){
        try{
            List<Jobangebot> allJobs = jobangebotEJB.getAll();
            List<Jobangebot> output = new ArrayList<>();

            for(Jobangebot j : allJobs){
                output.add(j.clone());
            }

            return response.build(200, parser.toJson(output));
        }catch(Exception e){
            return response.buildError(500, "Es ist ein Fehler beim Laden der Jobs aufgetreten");
        }
    }

    @GET
    @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewest(){
        try{
            List<Jobangebot> allJobs = jobangebotEJB.getAll();
            List<Jobangebot> output = new ArrayList<>();

            Collections.sort(allJobs);

            if(allJobs.size() >= 10){
                allJobs = allJobs.subList(0, 10);
            }else{
                allJobs = allJobs.subList(0, allJobs.size());
            }

            for(Jobangebot j : allJobs){
                output.add(j.clone());
            }

            return response.build(200, parser.toJson(output));
        }catch(Exception e){
            return response.buildError(500, "Es ist ein Fehler beim Laden der Jobs aufgetreten");
        }
    }

    @POST
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(String daten, @HeaderParam("Authorization") String token){

        try{

            JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

            Fachgebiet fachgebiet = fachgebietEJB.getCopyByName(parser.fromJson(jsonObject.get("fachgebiet"), String.class));

            List<Jobangebot> fachgebietJobs = fachgebiet.getJobangebotList();

            //Bewerbungstyp
            if(jsonObject.has("typ")){
                Bewerbungstyp bewerbungstyp = bewerbungstypEJB.getByName(parser.fromJson(jsonObject.get("typ"), String.class));
                List<Jobangebot> bewerbungstypJobs = bewerbungstyp.getJobangebotList();

                fachgebietJobs.retainAll(bewerbungstypJobs);
            }

            //istRemote
            if(jsonObject.has("istremote")){
                boolean istRemote = parser.fromJson(jsonObject.get("istremote"), Boolean.class);

                fachgebietJobs.removeIf(j -> (istRemote ? !j.getIstremote() : j.getIstremote()));
            }
            //istBefristet
            if(jsonObject.has("istbefristet")){
                boolean istBefristet = parser.fromJson(jsonObject.get("istbefristet"), Boolean.class);

                fachgebietJobs.removeIf(j -> (istBefristet ? !j.getIstbefristet() : j.getIstbefristet()));
            }
            //Jahresgehalt
            if(jsonObject.has("jahresgehalt")){
                int jahresgehalt = parser.fromJson(jsonObject.get("jahresgehalt"), Integer.class);
                fachgebietJobs.removeIf(j -> j.getJahresgehalt() < jahresgehalt);
            }
            //Urlaubstage
            if(jsonObject.has("urlaubstage")){
                int urlaubstage = parser.fromJson(jsonObject.get("urlaubstage"), Integer.class);
                fachgebietJobs.removeIf(j -> j.getUrlaubstage() < urlaubstage);
            }

            List<Jobangebot> returnList = new ArrayList<>();
            for(Jobangebot j : fachgebietJobs){
                returnList.add(j.clone());
            }

            return response.build(200, parser.toJson(returnList));
        }catch(Exception e){
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
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

                //Nur der Chef kann Jobangebote für andere Fachgebeite hinzufügen
                if(dbPersonaler.getRang() == 0){
                    fachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class)); //Fachgebiete sind schon vorgegeben, deswegen kein null check nötig
                }else{
                    fachgebiet = dbPersonaler.getFachgebiet();
                }

                jobangebotEJB.setFachgebiet(dbJobangebot, fachgebiet);

                fachgebietEJB.addJobangebot(dbJobangebot, fachgebiet);
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

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Jobangebot dbJobangebot = jobangebotEJB.getById(id);

                if(!dbJobangebot.getAnsprechpartner().equals(dbPersonaler)){
                    return response.buildError(400, "Sie haben dieses Jobangebot nicht erstellt");
                }else{

                    dbPersonaler.getJobangebotList().remove(dbJobangebot);
                    dbJobangebot.setAnsprechpartner(null);

                    //TODO: Alle Bewerbungen löschen, um Jobangebot löschen zu können
                    for(Bewerbung dbBewerbung : dbJobangebot.getBewerbungList()){

                        Bewerber dbBewerber = dbBewerbung.getBewerber();

                        dbBewerber.getBewerbungList().remove(dbBewerbung);
                        dbBewerbung.setBewerber(null);

                        dateiEJB.remove(dbBewerbung.getBewerbungschreiben());
                        dbBewerbung.setBewerbungschreiben(null);

                        dbBewerbung.getJobangebot().getBewerbungList().remove(dbBewerbung);
                        dbBewerbung.setJobangebot(null);

                        for(Bewerbungsnachricht n : dbBewerbung.getBewerbungsnachrichtList()){
                            bewerbungsnachrichtEJB.remove(n);
                        }
                        dbBewerbung.setBewerbungsnachrichtList(null);

                        for(Personaler p : dbBewerbung.getPersonalerList()){
                            p.getBewerbungList().remove(dbBewerbung);
                        }
                        dbBewerbung.setPersonalerList(null);

                        bewerbungEJB.remove(dbBewerbung);
                    }
                    dbJobangebot.setBewerbungList(null);

                    dbJobangebot.getFachgebiet().getJobangebotList().remove(dbJobangebot);
                    dbJobangebot.setFachgebiet(null);

                    dbJobangebot.getBewerbungstyp().getJobangebotList().remove(dbJobangebot);
                    dbJobangebot.setBewerbungstyp(null);

                    jobangebotEJB.remove(dbJobangebot);

                    return response.build(200, "Erfolgreich gelöscht");
                }

            }catch(Exception e){
                System.out.println(e);
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
