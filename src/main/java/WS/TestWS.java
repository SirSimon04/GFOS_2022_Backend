package WS;

import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import Entities.Bewerber;
import Service.Antwort;
import Service.GeocodingService;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Diese Klasse dient nur zum Testen von Funktionen, die
 * keiner anderen WS-Klasse eindeutig zuzuordnen sind.
 * Sie wird vom Frontend nicht aufgerufen.
 *
 * @author simon
 */
@Path("/test")
@Stateless
@LocalBean
public class TestWS{

    @EJB
    private DateiEJB dateiEJB;

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
    @Path("/mail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMail(){
        try{

            Bewerber mailAuth = bewerberEJB.getById(1);
            String mailFrom = mailAuth.getEmail();
            String pw = mailAuth.getPassworthash();

            mail.sendVerificationPin(mailFrom, pw, "Simon", "simi@engelnetz.de", 0);

            return response.build(200, "Success");
        }catch(Exception e){
            System.out.println("ErrorError");
            System.out.println(e);
            return response.buildError(500, e.toString());
        }
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendGet(){
        try{

            GeocodingService geo = new GeocodingService();

            geo.sendGet();

            return response.build(200, "Success");
        }catch(Exception e){
            System.out.println("ErrorError");
            System.out.println(e);
            return response.buildError(500, e.toString());
        }
    }

}
