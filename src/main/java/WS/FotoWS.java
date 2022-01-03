package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.FotoEJB;
import Entities.Adresse;
import Entities.Bewerber;
import Entities.Foto;
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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/foto")
@Stateless
@LocalBean
public class FotoWS{

    @EJB
    private FotoEJB fotoEJB;

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

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
    @Path("/profilbild")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfilbild(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                return response.build(200, parser.toJson(bewerberEJB.getByToken(token).getProfilbild()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Path("/profilbild/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProfilbildByID(@HeaderParam("Authorization") String token, @PathParam("id") int id){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                return response.build(200, parser.toJson(bewerberEJB.getById(id).getProfilbild()));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @POST
    @Path("/profilbild")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProfilbild(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                Foto profilbild = bewerberEJB.getByToken(token).getProfilbild();

                profilbild.setString(parser.fromJson(jsonObject.get("string"), String.class));

                return response.build(200, "Profilbild erfolgreich geändert");
            }catch(Exception e){
                System.out.println(e);
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @DELETE
    @Path("/profilbild")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProfilbild(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                bewerberEJB.getByToken(token).setProfilbild(null);

                return response.build(200, "Das Profilbild wurde erfolgreich entfernt");
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
