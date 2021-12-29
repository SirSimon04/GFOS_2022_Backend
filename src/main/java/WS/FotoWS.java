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

    @POST
    @Path("/setProfilbild")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProfilbild(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                //fotoEJB.remove(fotoEJB.getById(dbBewerber.getProfilbild().getFotoid())); //delete old image from db //deletes user somehow
                Foto foto = new Foto();
                foto.setString(parser.fromJson(jsonObject.get("string"), String.class));
                Foto fotoDB = fotoEJB.add(foto);
                bewerberEJB.setProfilbild(dbBewerber, fotoDB);
                return response.build(200, "Profilbild erfolgreich geändert");
            }catch(Exception e){
                System.out.println(e);
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Path("/removeProfilbild")
    @Produces(MediaType.APPLICATION_JSON)
    public Response asdf(@HeaderParam("Authorization") String token){
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
