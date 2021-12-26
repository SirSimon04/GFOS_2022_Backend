package WS;

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

@Path("/adresse")
@Stateless
@LocalBean
public class AdresseWS{

}
