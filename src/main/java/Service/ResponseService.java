/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.core.Response;

/**
 *
 * @author simon
 */
@Stateless
@LocalBean
public class ResponseService{

    /**
     * Diese Methode erstellt mit den übergebenen Daten ein Response-Objekt.
     *
     * @param status HTTP Status-Code
     * @param data übergebene Daten
     * @return Response-Objekt
     */
    public Response build(int status, String data){
        return Response
                .status(status)
                .entity(data)
                .build();
    }

    /**
     * Diese Methode erstellt ein Fehler-Response-Objekt mit dem Fehler als Status-Text,
     * sodass dieser Im Frontend angezeigt werden kann.
     *
     * @param status HTTP Status-Code
     * @param data übergebener Fehler
     * @return Response-Objekt
     */
    public Response buildError(int status, String data){
        return Response
                .status(status, data)
                .build();
    }

}
