package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BewerbungsnachrichtEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FotoEJB;
import EJB.JobangebotEJB;
import EJB.PersonalerEJB;
import Entities.Datei;
import Entities.Bewerber;
import Entities.Bewerbung;
import Entities.Bewerbungsnachricht;
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

/**
 * <h1>Webservice für Bewerbungen</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Bewerbungen bereit.
 * Sie stellt somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/bewerbung")
@Stateless
@LocalBean
public class BewerbungWS{

    @EJB
    private DateiEJB dateiEJB;

    @EJB
    private BewerbungEJB bewerbungEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private JobangebotEJB jobangebotEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private BewerbungsnachrichtEJB bewerbungsnachrichtEJB;

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private Tokenizer tokenizer = new Tokenizer();

    public boolean verify(String token){
        if(tokenizer.isOn()){
            if(blacklistEJB.onBlacklist(token)){
                return false;
            }
            return tokenizer.verifyToken(token) != null;
        }else{
            return true;
        }
    }

    /**
     * Diese Route fügt eine neue Bewerbung in das System ein.
     * Dabei werden alle für die Bewerbung wichtigen Daten gesetzt.
     *
     * @param daten Die Daten zu der neuen Bewerbung
     * @param token Das Webtoken
     * @return Response mit neuer Bewerbung oder Fehler
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBewerbung(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                int jobangebotId = parser.fromJson(jsonObject.get("jobangebotid"), Integer.class);
                Jobangebot jobangebot = jobangebotEJB.getById(jobangebotId);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                //Überprüfen, ob sich der Bewerber schon einmal auf eine Stelle beworben hat
                for(Bewerbung b : jobangebot.getBewerbungList()){
                    if(b.getBewerber().equals(dbBewerber)){
                        return response.buildError(400, "Sie haben sich bereits auf diese Stelle beworben");
                    }
                }

                //Bewerbung
                Bewerbung dbBewerbung = bewerbungEJB.add(parser.fromJson(daten, Bewerbung.class));

                dbBewerber.getBewerbungList().add(dbBewerbung);

                dbBewerbung.setBewerber(dbBewerber);

                //Bewerbungsstatus
                dbBewerbung.setStatus(0);

                //Jobangebot
                jobangebot.getBewerbungList().add(dbBewerbung);

                dbBewerbung.setJobangebot(jobangebot);

                //Bewerbungsschreiben
                Datei datei = new Datei();
                datei.setString(parser.fromJson(jsonObject.get("neuesbewerbungsschreiben"), String.class));
                Datei bewerbungsSchreiben = dateiEJB.add(datei);

                dbBewerbung.setBewerbungschreiben(bewerbungsSchreiben);

                //Wird zuerst nur vom Chef "bearbeitet", der diese dann an seine Mitarbeiter delegiert
                dbBewerbung.getPersonalerList().add(personalerEJB.getBoss());

                personalerEJB.getBoss().getBewerbungList().add(dbBewerbung);

                return response.build(200, parser.toJson(dbBewerbung.clone()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route löscht eine Bewerbung aus dem System. Dafür muss die Bewerbung entweder
     * ablehnt oder zurückgezogen worden sein. Dieser Vorgang kann von Bewerbern und
     * Personalern durchgefüghrt werden, sie müssen aber an der Bewerbung beteilligt sein.
     *
     * @param token Das Webtoken
     * @param id BewerbungId
     * @return Response mit Bestätigung oder Fehler
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Bewerbung dbBewerbung = bewerbungEJB.getById(id);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if(!dbBewerbung.getBewerber().equals(dbBewerber)){
                    return response.buildError(400, "Sie haben diese Bewerbung nicht gestellt");
                }else if(dbBewerbung.getStatus() == 0 || dbBewerbung.getStatus() == 1 || dbBewerbung.getStatus() == 3){
                    return response.buildError(403, "Diese Bewerbung muss erst zurückgezogen oder abgelehnt worden sein, bevor Sie sie löschen können.");
                }else if(dbPersonaler != null){

                    dbBewerber = dbBewerbung.getBewerber();

                    dbBewerber.getBewerbungList().remove(dbBewerbung);
                    dbBewerbung.setBewerber(null);

                    dateiEJB.delete(dbBewerbung.getBewerbungschreiben());
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

                    return response.build(200, "Die Bewerbung wurde erfolgreich gelöscht");

                }else if(dbBewerber != null){

                    dbBewerber.getBewerbungList().remove(dbBewerbung);
                    dbBewerbung.setBewerber(null);

                    dateiEJB.delete(dbBewerbung.getBewerbungschreiben());
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

                    return response.build(200, "Die Bewerbung wurde erfolgreich gelöscht");
                }else{
                    return response.buildError(404, "Es wurde keine Person zu Ihrem Token gefunden");
                }
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle abgeschickten Bewerbungen eines Bewerbers zurück.
     * Sie dient dem Bewerber als Übersicht, auf welche Stellen er sich beworben hat.
     *
     * @param token Das Webtoken
     * @return Die Bewerbungen
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAbgeschickte(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                List<Bewerbung> output = new ArrayList<>();

                for(Bewerbung b : dbBewerber.getBewerbungList()){
                    output.add(b.clone());
                }

                return response.build(200, parser.toJson(output));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Bewerbungen zurück, denen der Personaler zugewiesen ist.
     * Sie dient dem Personaler als Übersicht, welche Bewerbungen er zu bearbeiten hat.
     *
     * @param token Das Webtoken
     * @return Die Bewerbungen
     */
    @GET
    @Path("/zubearbeiten")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getZuBearbeitende(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                List<Bewerbung> zuBearbeitende = bewerbungEJB.getEditable(dbPersonaler);

                List<Bewerbung> output = new ArrayList<>();

                //wenn die Bewerbung abgelehnt oder zurückgezogen wurde, aber noch nicht gelöscht ist,
                //muss sie nicht mehr angezeigt werden
                for(Bewerbung b : zuBearbeitende){
                    System.out.println(b.getStatus());
                    if(b.getStatus() == 0 || b.getStatus() == 1 || b.getStatus() == 3){
                        System.out.println("Add to output");
                        output.add(b.clone());
                    }
                }

                return response.build(200, parser.toJson(output));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route leitet eine Bewerbung an einen anderen Bewerber weiter.
     * Das bedeutet, dass dem anderen Bewerber nun diese Bewerbung zugewiesen ist.
     * Der Personaler, der die Anfrage stellt, ist daraufhin auch noch für diese
     * Bewerbung verantwortlich.
     *
     * @param daten Die Daten zu Bewerbung und Bewerber
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/weiterleiten")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response leiteWeiter(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                //Bewerbung
                Bewerbung dbBewerbung = bewerbungEJB.getById(parser.fromJson(jsonObject.get("bewerbungid"), Integer.class));

                //Personaler
                Personaler dbPersonaler = personalerEJB.getById(parser.fromJson(jsonObject.get("personalerid"), Integer.class));

                dbBewerbung.getPersonalerList().add(dbPersonaler);

                dbPersonaler.getBewerbungList().add(dbBewerbung);

                return response.build(200, "Erfolgreich weitergeleitet");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route delegiert eine Bewerbung an einen anderen Bewerber.
     * Das bedeutet, dass dem anderen Bewerber nun diese Bewerbung zugewiesen ist.
     * Der Personaler, der die Anfrage stellt, ist daraufhin nicht mehr für diese
     * Bewerbung verantwortlich.
     *
     * @param daten Die Daten zu Bewerbung und Bewerber
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/delegiere")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delegiere(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                //Bewerbung
                Bewerbung dbBewerbung = bewerbungEJB.getById(parser.fromJson(jsonObject.get("bewerbungid"), Integer.class));

                //Personaler
                Personaler dbPersonaler = personalerEJB.getById(parser.fromJson(jsonObject.get("personalerid"), Integer.class));

                dbBewerbung.getPersonalerList().add(dbPersonaler);

                dbPersonaler.getBewerbungList().add(dbBewerbung);

                //Selbst entfernen, deswegen delegieren
                Personaler self = personalerEJB.getByToken(token);

                self.getBewerbungList().remove(dbBewerbung);

                dbBewerbung.getPersonalerList().remove(self);

                return response.build(200, "Erfolgreich delegiert");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route setzt den Status einer Bewerbung.
     * Die möglichen setzbaren Status sind abhängig davon, ob die Anfrage
     * von einem Bewerber oder Personaler stammt.
     *
     * @param daten Die Daten zu Bewerbung und Status
     * @param token Das Webtoken
     * @return Response mit Bestätigung oder Fehler
     */
    @POST
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setStatus(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                Bewerbung dbBewerbung = bewerbungEJB.getById(parser.fromJson(jsonObject.get("bewerbung"), Integer.class));

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if(dbBewerber == null && dbPersonaler == null){
                    return response.buildError(404, "Keine Person gefunden");
                }

                if(dbPersonaler == null && !dbBewerbung.getBewerber().equals(dbBewerber)){
                    return response.buildError(403, "Sie haben nicht die nötige Berechtigung");
                }

                if(dbBewerber == null && !dbBewerbung.getPersonalerList().contains(dbPersonaler)){
                    return response.buildError(403, "Sie haben nicht die nötige Berechtigung");
                }

                dbBewerbung.setStatus(parser.fromJson(jsonObject.get("status"), Integer.class));

                return response.build(200, "Status erfolgreich gesetzt");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
