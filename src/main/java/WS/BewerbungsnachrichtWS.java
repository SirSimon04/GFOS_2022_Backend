package WS;

import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BewerbungsnachrichtEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.PersonalerEJB;
import Entities.Bewerber;
import Entities.Bewerbung;
import Entities.Bewerbungsnachricht;
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
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/bewerbungsnachricht")
@Stateless
@LocalBean
public class BewerbungsnachrichtWS{

    @EJB
    private DateiEJB dateiEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private BewerbungsnachrichtEJB bewerbungsnachrichtEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private BewerbungEJB bewerbungEJB;

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

    //add
    //getByBewerbung
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBewerbungsnachricht(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Bewerbungsnachricht nachricht = bewerbungsnachrichtEJB.add(parser.fromJson(daten, Bewerbungsnachricht.class));

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                int bewerbungId = parser.fromJson(jsonObject.get("bewerbungid"), Integer.class);

                Bewerbung bewerbung = bewerbungEJB.getById(bewerbungId);

                if(bewerbung == null){
                    return response.buildError(500, "Die Bewerbung wurde nicht gefunden");
                }

                //nachschauen, ob es sich um einen Bewerber oder einen Personaler handelt
                Bewerber bewerber = bewerberEJB.getByToken(token);

                Personaler personaler = personalerEJB.getByToken(token);

                if(bewerber == null && personaler == null){
                    return response.buildError(401, "Es wurde kein Bewerber oder Personaler zu ihrem Token gefunden");
                }else if(bewerber != null){

                    nachricht.setVonbewerber(Boolean.TRUE);

                    if(!bewerbung.getBewerber().equals(bewerber)){
                        return response.build(400, "Sie haben diese Bewerbung nicht gestellt");
                    }else{
                        bewerbung.getBewerbungsnachrichtList().add(nachricht);
                    }

                }else if(personaler != null){

                    nachricht.setVonbewerber(Boolean.FALSE);

                    if(!bewerbung.getPersonalerList().contains(personaler)){
                        return response.buildError(400, "Sie sind nicht autorisiert, diese Bewerbung zu bearbeiten");
                    }else{
                        bewerbung.getBewerbungsnachrichtList().add(nachricht);
                    }
                }

                return response.build(200, parser.toJson(nachricht.clone()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
