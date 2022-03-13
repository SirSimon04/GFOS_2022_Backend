package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FotoEJB;
import EJB.PersonalerEJB;
import Entitiy.Datei;
import Entitiy.Bewerber;
import Entitiy.Bewerbung;
import Entitiy.Foto;
import Entitiy.Personaler;
import Service.Antwort;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletContext;
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
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import sun.misc.IOUtils;

/**
 * <h1>Webservice für Dateien</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Dateien bereit.
 * Sie stellt somit eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/datei")
@Stateless
@LocalBean
public class DateiWS{

    @EJB
    private DateiEJB dateiEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private BewerbungEJB bewerbungEJB;

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

    /**
     * Diese Route gibt den Lebenslauf eines Nutzers anhand des Tokens wieder.
     *
     * @param token Das Webtoken
     * @return Der Lebenslauf
     */
    @GET
    @Path("/lebenslauf")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwnLebenslauf(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                return response.build(200, parser.toJson(bewerberEJB.getByToken(token).getLebenslauf()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt den Lebenslauf einer Bewerbers anhand seiner Id wieder.
     * Das ist nur möglich, wenn das Profil des Bewerbers öffentlich ist.
     *
     * @param token Das Webtoken
     * @param id BewerberId
     * @return Der Lebenslauf
     */
    @GET
    @Path("/lebenslauf/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLebenslaufById(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getById(id);

                if(dbBewerber.getEinstellungen().getIspublic()){
                    return response.build(200, parser.toJson(dbBewerber.getLebenslauf()));
                }else{
                    return response.buildError(400, "Dieser Bewerber hat sein Profil privat");
                }

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Rotue lädt einen neuen Lebenslauf für einen Bewerber hoch.
     *
     * @param daten Die erforderlichen Daten
     * @param token Das Webtoken
     * @return Response mit Bestätigung oder Fehler
     */
    @POST
    @Path("/lebenslauf")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setLebenslauf(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                Datei lebenslauf = bewerberEJB.getByToken(token).getLebenslauf();

                lebenslauf.setString(parser.fromJson(jsonObject.get("string"), String.class));

                return response.build(200, "Lebenslauf erfolgreich geändert");
            }catch(Exception e){
                System.out.println(e);
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Methode löscht den Lebenslauf eines Bewerbers.
     *
     * @param token Das Webtoken
     * @return Response mit Bestätigung oder Fehler
     */
    @DELETE
    @Path("/lebenslauf")
    @Produces(MediaType.APPLICATION_JSON)
    public Response löscheLebenslauf(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Datei lebenslauf = dbBewerber.getLebenslauf();

                if(lebenslauf != null){
                    dateiEJB.delete(bewerberEJB.getByToken(token).getLebenslauf());
                }

                dbBewerber.setLebenslauf(null);

                return response.build(200, "Lebenslauf wurde erfolgreich entfernt");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt das Bewerbungsschreiben einer Bewerbung wieder. Dabei wird überprüft, ob der
     * Personaler an der Bewerbung arbeitet oder der Bewerber die Bewerbung gestellt hat.
     *
     * @param token Das Webtoken
     * @param id BewerbungId
     * @return Das Bewerbungsschreiben
     */
    @GET
    @Path("/bewerbung/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBewerbung(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Bewerbung dbBewerbung = bewerbungEJB.getById(id);

                if(dbBewerber != null){
                    if(dbBewerbung.getBewerber().equals(dbBewerber)){
                        return response.build(200, parser.toJson(dbBewerbung.getBewerbungschreiben()));
                    }else{
                        return response.buildError(403, "Sie haben diese Bewerbung nicht gestellt");
                    }
                }else if(dbPersonaler != null){
                    if(dbBewerbung.getPersonalerList().contains(dbPersonaler)){
                        return response.build(200, parser.toJson(dbBewerbung.getBewerbungschreiben()));
                    }else{
                        return response.buildError(403, "Sie arbeiten nicht an dieser Bewerbung");
                    }
                }else{
                    return response.buildError(404, "Kein Bewerber oder Personaler gefunden");
                }

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
//    This is the code to make the file download possible
//    @Inject
//    ServletContext context;
//
//    @GET
//    @Path("{path:.*}")
//    public Response staticResources(@PathParam("path") final String path){
//
//        InputStream resource = context.getResourceAsStream(String.format("/WEB-INF/%s", path));
//
//        return Objects.isNull(resource)
//                ? Response.status(NOT_FOUND).build()
//                : Response.ok().type(MediaType.MULTIPART_FORM_DATA).entity(resource).build();
//    }

    @POST
    @Path("/upload")
    public void post(File file) throws FileNotFoundException, IOException{
        System.out.println("method called");
//        String content = null;
//        try{
//            content = readFile(file);
//        }catch(IOException e){
//        }
//
//        System.out.println(content);
        try(InputStream in = new FileInputStream(file)){
            int content;
            while((content = in.read()) != -1){
                System.out.print((char) content);
            }
        }catch(Exception e){
        }
    }

    public static String readFile(File file) throws IOException{
        StringBuilder sb = new StringBuilder();
        InputStream in = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String line;
        while((line = br.readLine()) != null){
            sb.append(line + System.lineSeparator());
        }

        return sb.toString();
    }

}
