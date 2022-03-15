package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import Entitiy.Bewerber;
import Service.Antwort;
import Service.FileService;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <h1>Webservice für Fotos</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Fotos bereit. Sie stellt somit eine
 * Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/foto")
@Stateless
@LocalBean
public class FotoWS {

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

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
     * Diese Route gibt das Profilbild eines Bewerbers anhand des Tokens wieder.
     *
     * @param token Das Webtoken
     * @return Das Profilbild
     */
    @GET
    @Path("/profilbild")
    public Response getProfilbild(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                int id = bewerberEJB.getByToken(token).getBewerberid();

                File profilbild = fileService.getProfilbild(id);

                if (profilbild == null) {
                    return response.buildError(404, "Das Profilbild wurde nicht gefunden");
                }

                return response.buildFile(profilbild);
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt das Profilbild eines Bewerbers anhand der Id wieder.
     *
     * @param token Das Webtoken
     * @param id BewerberId
     * @return Das Profilbild
     */
    @GET
    @Path("/profilbild/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfilbildByID(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                return response.build(200, parser.toJson("TODO"));
//                return response.build(200, parser.toJson(bewerberEJB.getById(id).getProfilbild()));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route ändert das Profilbild eines Bewerbers.
     *
     * @param daten Das neue Profilbild
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @POST
    @Path("/profilbild")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProfilbild(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                String base64 = parser.fromJson(jsonObject.get("string"), String.class);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                String name = dbBewerber.getBewerberid().toString() + ".jpg";

                fileService.saveProfilbild(name, base64);
//                Foto profilbild = bewerberEJB.getByToken(token).getProfilbild();
                //
                //                profilbild.setString(parser.fromJson(jsonObject.get("string"), String.class));
                return response.build(200, "Profilbild erfolgreich geändert");
            } catch (Exception e) {
                System.out.println(e);
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route löscht das Profilbild eines Bewerbers.
     *
     * @param token Das Webtoken
     * @return Response mit Fehler oder Bestätigung
     */
    @DELETE
    @Path("/profilbild")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProfilbild(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

//                bewerberEJB.getByToken(token).setProfilbild(null);
                return response.build(200, "Das Profilbild wurde erfolgreich entfernt");
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
