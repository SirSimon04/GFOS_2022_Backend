package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BlacklistEJB;
import EJB.PersonalerEJB;
import Entitiy.Bewerber;
import Entitiy.Bewerbung;
import Entitiy.Personaler;
import Service.Antwort;
import Service.FileService;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.core.header.FormDataContentDisposition;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
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
import com.sun.jersey.multipart.FormDataParam;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import sun.misc.BASE64Decoder;

/**
 * <h1>Webservice für Dateien</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Dateien bereit. Sie stellt somit
 * eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/datei")
@Stateless
@LocalBean
public class DateiWS {

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

    private final FileService fileService = new FileService();

    private Tokenizer tokenizer = new Tokenizer();

    /**
     * Diese Methode verifiziert ein Token
     *
     * @param token Das Webtoken
     * @return Status des Tokens
     */
    public boolean verify(String token) {
        if (tokenizer.isOn()) {
            if (blacklistEJB.onBlacklist(token)) {
                return false;
            }
            return tokenizer.verifyToken(token) != null;
        } else {
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
    public Response getOwnLebenslauf(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                int id = bewerberEJB.getByToken(token).getBewerberid();

                String lebenslauf = fileService.getLebenslauf(id);

                return response.build(200, parser.toJson(lebenslauf));

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt den Lebenslauf einer Bewerbers anhand seiner Id wieder.
     * Das ist nur für den Bewerber, dem der Lebenslauf gehört, oder allen
     * Personalern möglich.
     *
     * @param token Das Webtoken
     * @param id BewerberId
     * @return Der Lebenslauf
     */
    @GET
    @Path("/lebenslauf/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLebenslaufById(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber bewerber = bewerberEJB.getById(id);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (Objects.equals(bewerber, dbBewerber) || dbPersonaler != null) {
                    String lebenslauf = fileService.getLebenslauf(id);

                    return response.build(200, parser.toJson(lebenslauf));
                } else {
                    return response.buildError(403, "Sie haben nicht die nötige Berechtigung.");
                }

            } catch (Exception e) {
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
    public Response setLebenslauf(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String base64 = parser.fromJson(jsonObject.get("string"), String.class);

                int id = bewerberEJB.getByToken(token).getBewerberid();

                fileService.saveLebenslauf(id, base64);

                return response.build(200, parser.toJson("Lebenslauf erfolgreich geändert"));
            } catch (Exception e) {
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
    public Response löscheLebenslauf(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                int id = bewerberEJB.getByToken(token).getBewerberid();

                fileService.deleteLebenslauf(id);

                return response.build(200, parser.toJson("Lebenslauf wurde erfolgreich entfernt"));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt das Bewerbungsschreiben einer Bewerbung wieder. Dabei
     * wird überprüft, ob der Personaler an der Bewerbung arbeitet oder der
     * Bewerber die Bewerbung gestellt hat.
     *
     * @param token Das Webtoken
     * @param id BewerbungId
     * @return Das Bewerbungsschreiben
     */
    @GET
    @Path("/bewerbung/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBewerbung(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Bewerbung dbBewerbung = bewerbungEJB.getById(id);
                if (dbBewerber != null) {
                    if (dbBewerbung.getBewerber().equals(dbBewerber)) {
                        String bewerbung = fileService.getBewerbung(id);
                        return response.build(200, parser.toJson(bewerbung));
                    } else {
                        return response.buildError(403, "Sie haben diese Bewerbung nicht gestellt");
                    }
                } else if (dbPersonaler != null) {
                    if (dbBewerbung.getPersonalerList().contains(dbPersonaler)) {
                        String bewerbung = fileService.getBewerbung(id);
                        return response.build(200, parser.toJson(bewerbung));
                    } else {
                        return response.buildError(403, "Sie arbeiten nicht an dieser Bewerbung");
                    }
                } else {
                    return response.buildError(404, "Kein Bewerber oder Personaler gefunden");
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
//    This is the code to make the file download possible
    @Inject
    ServletContext context;

    //root for downloading is the same as for upload–
    //hier wird die Dateiendung mit übergeben
    //um es mal zusammen zu fassen:
    //wenn man dateien anfragen möchte, muss aus irgendeinem grund der komplette dateiname
    //als pathparam übergeben werden, einfach strings zusammenbauen funktioniert nicht
    //mit dieser methode wird die dateiendung.pdf übergeben, sonst nicht (int oder so)
    @GET
    @Path("{path:.*}")
    public Response staticResources(@PathParam("path") final String path) {

        File file = new File("./projectFiles/lebenslaeufe/" + path);

        return Objects.isNull(file)
                ? Response.status(NOT_FOUND).build()
                : Response.ok().type(MediaType.MULTIPART_FORM_DATA).entity(file).build();
    }

    //file upload now works with base64 string, just put in base64 field
    //root for saving is /glassfish/domains/domain1/config/
    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadTest(String daten) throws IOException {

        JsonObject json = parser.fromJson(daten, JsonObject.class);

        String base64 = parser.fromJson(json.get("base64"), String.class);
        String name = parser.fromJson(json.get("name"), String.class);

        BASE64Decoder decoder = new BASE64Decoder();

        //Base64 decoding, Base64 decoding of byte array string and generating file
        byte[] byt = decoder.decodeBuffer(base64);
        for (int i = 0, len = byt.length; i < len; ++i) {
            //Adjust abnormal data
            if (byt[i] < 0) {
                byt[i] += 256;
            }
        }
        OutputStream out = null;
        InputStream input = new ByteArrayInputStream(byt);
        try {
            //Generate files in the specified format
            out = new FileOutputStream("./lebenslaeufe/" + name);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = input.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }

        return response.build(200, parser.toJson("OK"));
    }

}
