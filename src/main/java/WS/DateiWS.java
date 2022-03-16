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

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private final FileService fileService = new FileService();

    private Tokenizer tokenizer = new Tokenizer();

    public boolean verify(String token) {
        System.out.println("WS.BewerberWS.verifyToken()");
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

                File lebenslauf = fileService.getLebenslauf(id);

                return response.buildFile(lebenslauf);

            } catch (Exception e) {
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
    public Response getLebenslaufById(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getById(id);

                if (dbBewerber.getEinstellungen().getIspublic()) {

                    File lebenslauf = fileService.getLebenslauf(id);

                    return response.buildFile(lebenslauf);

                } else {
                    return response.buildError(400, "Dieser Bewerber hat sein Profil privat");
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

                return response.build(200, "Lebenslauf erfolgreich geändert");
            } catch (Exception e) {
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
    public Response löscheLebenslauf(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                int id = bewerberEJB.getByToken(token).getBewerberid();

                fileService.deleteLebenslauf(id);

                return response.build(200, "Lebenslauf wurde erfolgreich entfernt");
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
                        File bewerbung = fileService.getBewerbung(id);
                        return response.buildFile(bewerbung);
                    } else {
                        return response.buildError(403, "Sie haben diese Bewerbung nicht gestellt");
                    }
                } else if (dbPersonaler != null) {
                    if (dbBewerbung.getPersonalerList().contains(dbPersonaler)) {
                        File bewerbung = fileService.getBewerbung(id);
                        return response.buildFile(bewerbung);
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

        return response.build(200, "OK");
    }

//    @POST
//    @Path("/upload")
//    public void post(File file) throws FileNotFoundException, IOException{
//        System.out.println("method called");
//        String data = "";
//        StringBuilder stringBuilder = new StringBuilder();
//        try(InputStream in = new FileInputStream(file)){
//            System.out.println(file.getName());
//            int content;
//            while((content = in.read()) != -1){
//                stringBuilder.append((char) content);
//                data += (char) content;
//            }
//
//            FileService fileService = new FileService();
//            fileService.create("test/test.pdf");
//            fileService.write("test/test.pdf", data);
//        }catch(Exception e){
//        }
//
//    }
//    @POST
//    @Path("/upload")  //Your Path or URL to call this service
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response uploadFile(
//            @DefaultValue("true") @FormDataParam("enabled") boolean enabled,
//            @FormDataParam("file") InputStream uploadedInputStream,
//            @FormDataParam("file") FormDataContentDisposition fileDetail) {
//         //Your local disk path where you want to store the file
//        String uploadedFileLocation = "./uploadedFiles/" + fileDetail.getFileName();
//        System.out.println(uploadedFileLocation);
//        // save it
//        File  objFile=new File(uploadedFileLocation);
//        if(objFile.exists())
//        {
//            objFile.delete();
//
//        }
//
//        saveToFile(uploadedInputStream, uploadedFileLocation);
//
//        String output = "File uploaded via Jersey based RESTFul Webservice to: " + uploadedFileLocation;
//
//        return response.build(200, output);
//
//    }
//    private void saveToFile(InputStream uploadedInputStream,
//            String uploadedFileLocation) {
//
//        try {
//            OutputStream out = null;
//            int read = 0;
//            byte[] bytes = new byte[1024];
//
//            out = new FileOutputStream(new File(uploadedFileLocation));
//            while ((read = uploadedInputStream.read(bytes)) != -1) {
//                out.write(bytes, 0, read);
//            }
//            out.flush();
//            out.close();
//        } catch (IOException e) {
//
//            e.printStackTrace();
//        }
//
//    }
//    private static final String UPLOAD_FOLDER = "./uploadedFiles/";
//
//    @POST
//    @Path("/upload")
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    public Response uploadFile(
//            @FormDataParam("file") InputStream uploadedInputStream,
//            @FormDataParam("file") FormDataContentDisposition fileDetail) {
//        // check if all form parameters are provided
//        if (uploadedInputStream == null || fileDetail == null)
//            return Response.status(400).entity("Invalid form data").build();
//        // create our destination folder, if it not exists
//        try {
//            createFolderIfNotExists(UPLOAD_FOLDER);
//        } catch (SecurityException se) {
//            return Response.status(500)
//                    .entity("Can not create destination folder on server")
//                    .build();
//        }
//        String uploadedFileLocation = UPLOAD_FOLDER + fileDetail.getFileName();
//        try {
//            saveToFile(uploadedInputStream, uploadedFileLocation);
//        } catch (IOException e) {
//            return Response.status(500).entity("Can not save file").build();
//        }
//        return Response.status(200)
//                .entity("File saved to " + uploadedFileLocation).build();
//    }
//    /**
//     * Utility method to save InputStream data to target location/file
//     *
//     * @param inStream
//     *            - InputStream to be saved
//     * @param target
//     *            - full path to destination file
//     */
//    private void saveToFile(InputStream inStream, String target)
//            throws IOException {
//        OutputStream out = null;
//        int read = 0;
//        byte[] bytes = new byte[1024];
//        out = new FileOutputStream(new File(target));
//        while ((read = inStream.read(bytes)) != -1) {
//            out.write(bytes, 0, read);
//        }
//        out.flush();
//        out.close();
//    }
//    /**
//     * Creates a folder to desired location if it not already exists
//     *
//     * @param dirName
//     *            - full path to the folder
//     * @throws SecurityException
//     *             - in case you don't have permission to create the folder
//     */
//    private void createFolderIfNotExists(String dirName)
//            throws SecurityException {
//        File theDir = new File(dirName);
//        if (!theDir.exists()) {
//            theDir.mkdir();
//        }
//    }
}
