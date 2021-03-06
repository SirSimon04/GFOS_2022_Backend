/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import java.io.File;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <h1>Service für das Erstellen von Response-Objektes</h1>
 * <p>
 * Diese Klasse stellt Methoden für das Erstellen von Response-Objekten zur
 * Kommuniktion mit dem Frontend bereit.</p>
 *
 * @author Lukas Krinke, Simon Engel, Florian Noje
 */
@Stateless
@LocalBean
public class ResponseService {

    /**
     * Diese Methode erstellt mit den übergebenen Daten ein Response-Objekt.
     *
     * @param status HTTP Status-Code
     * @param data übergebene Daten
     * @return Response-Objekt
     */
    public Response build(int status, String data) {
        return Response
                .status(status)
                .entity(data)
                .build();
    }

    /**
     * Diese Methode erstellt ein Responseobjekt mit einer Datei.
     *
     * @param file Die Datei
     * @return Response-Objekt
     */
    public Response buildFile(File file) {
        return Response
                .status(200)
                .type(MediaType.MULTIPART_FORM_DATA)
                .entity(file)
                .build();
    }

    /**
     * Diese Methode erstellt ein Fehler-Response-Objekt mit dem Fehler als
     * Status-Text, sodass dieser Im Frontend angezeigt werden kann.
     *
     * @param status HTTP Status-Code
     * @param data übergebener Fehler
     * @return Response-Objekt
     */
    public Response buildError(int status, String data) {
        return Response
                .status(status, data)
                .build();
    }

}
