
import EJB.BewerberEJB;
import Entities.Bewerber;
import Service.Antwort;
import com.google.gson.Gson;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/bewerber")
@Stateless
@LocalBean
public class BewerberWS{

    @EJB
    private BewerberEJB bewerberEJB;

    private final Antwort response = new Antwort();

    private Gson parser = new Gson();

    /**
     * BEWERBERID INT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
     * NAME VARCHAR(64) NOT NULL,
     * VORNAME VARCHAR(64) NOT NULL,
     * EMAIL VARCHAR(64) NOT NULL,
     * TELEFON VARCHAR(64) NOT NULL,
     * GEBURTSTAG TIMESTAMP,
     * ADRESSE INT NOT NULL,
     * FOREIGN KEY(ADRESSE) REFERENCES ADRESSE(ADRESSEID) ON DELETE CASCADE
     *
     */
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(String daten){
        try{

            Bewerber newBewerber = parser.fromJson(daten, Bewerber.class);
            //return response.build(201, parser.toJson(nutzerprofilEJB.create(n)));
            return response.build(200, parser.toJson(bewerberEJB.add(newBewerber)));

        }catch(Exception e){
            return response.buildError(500, "Beim Laden der Nutzerprofile ist ein Fehler aufgetreten!");
        }
    }
}
