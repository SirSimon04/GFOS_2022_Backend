package Filter;

import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import java.io.IOException;
import javax.ws.rs.ext.Provider;

/**
 * <h1>CORS-Filter</h1>
 * <p>
 * Dieser Filter fügt jeder Request Header hinzu, die CORS erlauben. Ansonsten
 * blockiert der Browser die Requests des Frontends.</p>
 *
 * @author Lukas Krinke, Simon Engel, Florian Noje
 */
@Provider
public class Filter implements ContainerResponseFilter {

    /**
     * Hier werden der Response die Header hinzugefügt, die CORS-Fehler
     * vermeiden.
     *
     * @param requestContext eingehende Request
     * @param responseContext ausgehende Response
     * @throws IOException
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
