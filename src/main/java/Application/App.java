/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Application;

import EJB.BlacklistEJB;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJB;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * <h1>Grundgerüst der Application</h1>
 * <p>
 * Sie legt den Application-Path fest und sorgt für eine automatische Routine
 * bezüglich des Löschens von veralteten Daten.</p>
 *
 * @author Lukas Krinke, Simon Engel, Florian Noje
 */
@ApplicationPath("/")
public class App extends Application{

    @EJB
    private BlacklistEJB blacklistEJB;

    /**
     * Diese Methode löscht zum ersten Mal nach 30 Sekunden, dann jede Stunde veraltete Tokens auf
     * der Blacklist.
     *
     * @throws IOException
     */
    public App() throws IOException{
        ScheduledExecutorService exe = Executors.newScheduledThreadPool(5);
        exe.scheduleAtFixedRate(() -> {
            blacklistEJB.clear();
        }, 30, 3600, TimeUnit.SECONDS);
    }
}
