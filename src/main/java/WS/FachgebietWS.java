package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.FachgebietEJB;
import EJB.InteressenfelderEJB;
import EJB.LebenslaufstationEJB;
import EJB.PersonalerEJB;
import Entities.Adresse;
import Entities.Bewerber;
import Entities.Fachgebiet;
import Entities.Interessenfelder;
import Entities.Jobangebot;
import Entities.Lebenslaufstation;
import Entities.Personaler;
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
import java.util.ArrayList;

@Path("/fachgebiet")
@Stateless
@LocalBean
public class FachgebietWS{

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private FachgebietEJB fachgebietEJB;

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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwn(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if(dbBewerber != null){
                    return response.build(200, parser.toJson(dbBewerber.getFachgebiet().clone()));
                }

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if(dbPersonaler.getRang() == 0){
                    return response.build(400, "Sie sind der Chef und haben deshalb kein Fachgebiet");
                }

                if(dbPersonaler != null){
                    return response.build(200, parser.toJson(dbPersonaler.getFachgebiet().clone()));
                }

                return response.build(404, "Es wurde keine Person zu ihrem Token gefunden");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Path("/personaler/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPersonaler(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Personaler dbPersonaler = personalerEJB.getById(id);

                if(dbPersonaler != null){
                    if(dbPersonaler.getRang() == 0){
                        return response.build(400, "Der Chef hat kein Fachgebiet");
                    }
                    return response.build(200, parser.toJson(dbPersonaler.getFachgebiet().clone()));
                }

                return response.build(404, "Es wurde kein Personaler zu der ID gefunden");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Path("/bewerber/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBewerber(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getById(id);

                if(dbBewerber != null){
                    return response.build(200, parser.toJson(dbBewerber.getFachgebiet().clone()));
                }

                return response.build(404, "Es wurde kein Personaler zu der ID gefunden");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                List<Fachgebiet> dbList = fachgebietEJB.getAll();

                List<Fachgebiet> output = new ArrayList<>();

                for(Fachgebiet f : dbList){
                    output.add(f.clone());
                }

                return response.build(200, parser.toJson(output));

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle vom Chef angepinnten Fachgebiete zurück,
     * damit diese auf der Startseite angezeigt werden können.
     * Dabei handelt es sich um maximal 2 Fachgebiete.
     *
     * @return Die angepinnten Fachgebiete
     */
    @GET
    @Path("/pinned")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPinned(){
        try{
            List<Fachgebiet> pinned = fachgebietEJB.getPinnedByChef();

            List<Fachgebiet> output = new ArrayList<>();

            for(Fachgebiet f : pinned){
                output.add(f.clone());
            }

            return response.build(200, parser.toJson(output));
        }catch(Exception e){
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

    /**
     * Mit dieser Route kann der Chef ein Fachgebiet anpinnen,
     * damit es auf der Startseite angezeigt wird.
     * Sie kann nur vom Chef aufgerufen werden.
     *
     * @param token Das Webtoken
     * @param id FachgebietID
     * @return Response mit Fehler oder Bestätigung
     */
    @GET
    @Path("/admin/pin/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pinFachgebiet(@HeaderParam("Authorization") String token,
            @PathParam("id") int id
    ){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Fachgebiet fachgebiet = fachgebietEJB.getById(id);

                if(dbPersonaler.getRang() != 0){
                    return response.buildError(403, "Sie sind nicht der Chef");
                }else if(fachgebietEJB.getPinnedByChef().size() >= 2){
                    return response.buildError(403, "Es können maximal 2 Fachgebiete gepinnt werden");
                }else{

                    fachgebiet.setVonchefgepinnt(Boolean.TRUE);

                    return response.build(200, "Success");
                }
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Mit dieser Route kann der Chef ein Fachgebiet entpinnen,
     * damit es nicht mehr auf der Startseite angezeigt wird.
     * Sie kann nur vom Chef aufgerufen werden.
     *
     * @param token Das Webtoken
     * @param id FachgebietId
     * @return Response mit Fehler oder Bestätigung
     */
    @GET
    @Path("/admin/unpin/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unpinFachgebiet(@HeaderParam("Authorization") String token,
            @PathParam("id") int id
    ){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if(dbPersonaler.getRang() != 0){
                    return response.buildError(403, "Sie sind nicht der Chef");
                }else{

                    Fachgebiet fachgebiet = fachgebietEJB.getById(id);

                    fachgebiet.setVonchefgepinnt(Boolean.FALSE);

                    return response.build(200, "Success");
                }
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @POST
    @Path("/admin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String name = parser.fromJson(jsonObject.get("name"), String.class);

                if(dbPersonaler.getRang() != 0){
                    return response.buildError(403, "Sie sind nicht der Chef");
                }else if(fachgebietEJB.getByName(name) != null){
                    return response.buildError(403, "Dieses Fachgebiet gibt es schon");
                }else{

                    fachgebietEJB.add(new Fachgebiet(name));

                    return response.build(200, "Success");
                }

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
