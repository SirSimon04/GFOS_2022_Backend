package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BewerbungEJB;
import EJB.BewerbungsnachrichtEJB;
import EJB.BewerbungstypEJB;
import EJB.BlacklistEJB;
import EJB.FachgebietEJB;
import EJB.JobangebotEJB;
import EJB.PersonalerEJB;
import Entitiy.Adresse;
import Entitiy.Bewerber;
import Entitiy.Bewerbung;
import Entitiy.Bewerbungsnachricht;
import Entitiy.Bewerbungstyp;
import Entitiy.Fachgebiet;
import Entitiy.Jobangebot;
import Entitiy.Personaler;
import Service.ResponseService;
import Service.EntfernungsService;
import Service.FileService;
import Service.GeocodingService;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
 * <h1>Webservice für Jobangebote</h1>
 * <p>
 * Diese Klasse stellt Routen bezüglich der Jobangebote bereit. Sie stellt somit
 * eine Schnittstelle zwischen Frontend und Backend dar.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
@Path("/jobs")
@Stateless
@LocalBean
public class JobangebotWS {

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerbungEJB bewerbungEJB;

    @EJB
    private BewerbungsnachrichtEJB bewerbungsnachrichtEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private JobangebotEJB jobangebotEJB;

    @EJB
    private FachgebietEJB fachgebietEJB;

    @EJB
    private BewerbungstypEJB bewerbungstypEJB;

    @EJB
    private AdresseEJB adresseEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    private final ResponseService response = new ResponseService();

    private final Gson parser = new Gson();

    private final FileService fileService = new FileService();

    private Tokenizer tokenizer = new Tokenizer();

    private final GeocodingService geocodingService = new GeocodingService();

    private final EntfernungsService entfernungsService = new EntfernungsService();

    private final MailService mailService = new MailService();

    /**
     * Diese Methode verifiziert ein Token
     *
     * @param token Das Webtoken
     * @return Status des Tokens
     */
    public boolean verify(String token) {
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
     * Diese Route gibt alle Jobangebote zurück.
     *
     * @return Die Jobangebote
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        try {
            List<Jobangebot> allJobs = jobangebotEJB.getAll();
            List<Jobangebot> output = new ArrayList<>();

            for (Jobangebot j : allJobs) {
                output.add(j.clone());
            }

            return response.build(200, parser.toJson(output));
        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler beim Laden der Jobs aufgetreten");
        }
    }

    @GET
    @Path("/created")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreated(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler == null) {
                    return response.buildError(404, "Es wurde kein Personaler gefunden");
                }
                List<Jobangebot> output = new ArrayList<>();

                for (Jobangebot job : dbPersonaler.getJobangebotList()) {
                    output.add(job.clone());
                }

                return response.build(200, parser.toJson(output));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt ein Jobangebot anhand seiner Id zurück.
     *
     * @param id Die Id
     * @return Das Jobangebot
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") int id) {

        Jobangebot dbJobangebot = jobangebotEJB.getById(id);

        if (dbJobangebot != null) {
            return response.build(200, parser.toJson(dbJobangebot.clone()));
        } else {
            return response.buildError(404, "Es wurde kein Jobangebot gefunden");

        }

    }

    /**
     * Diese Route gibt die 10 neuesten Jobangebote wieder. Diese werden auf der
     * Startseite im Frontend angezeigt.
     *
     * @return Die Jobangebote
     */
    @GET
    @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewest() {
        try {
            List<Jobangebot> allJobs = jobangebotEJB.getAll();
            List<Jobangebot> output = new ArrayList<>();

            Collections.sort(allJobs);

            if (allJobs.size() >= 10) {
                allJobs = allJobs.subList(0, 10);
            } else {
                allJobs = allJobs.subList(0, allJobs.size());
            }

            for (Jobangebot j : allJobs) {
                output.add(j.clone());
            }

            return response.build(200, parser.toJson(output));
        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler beim Laden der Jobs aufgetreten");
        }
    }

    /**
     * Mit dieser Route kann nach Jobangeboten gesucht werden. Mögliche
     * Suchparamter sind dabei der Bewerbungstyp, das Fachgebiet, das Gehalt und
     * die Urlaubstage.
     *
     * @param daten Die erforderlichen Suchdaten
     * @param token Das Webtoken
     * @return Die gefunden Jobangebote
     */
    @POST
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response search(String daten, @HeaderParam("Authorization") String token) {

        try {

            JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

            //Kopie wichtig, damit Entfernung nicht in die DB geschrieben wird
            Fachgebiet fachgebiet = fachgebietEJB.getCopyByName(parser.fromJson(jsonObject.get("fachgebiet"), String.class));

            List<Jobangebot> fachgebietJobs = fachgebiet.getJobangebotList();

            //Bewerbungstyp
            if (jsonObject.has("typ")) {
                Bewerbungstyp bewerbungstyp = bewerbungstypEJB.getByName(parser.fromJson(jsonObject.get("typ"), String.class));
                List<Jobangebot> bewerbungstypJobs = bewerbungstyp.getJobangebotList();

                fachgebietJobs.retainAll(bewerbungstypJobs);
            }

            //istRemote
            if (jsonObject.has("istremote")) {
                boolean istRemote = parser.fromJson(jsonObject.get("istremote"), Boolean.class);

                fachgebietJobs.removeIf(j -> (istRemote ? !j.getIstremote() : j.getIstremote()));
            }
            //istBefristet
            if (jsonObject.has("istbefristet")) {
                boolean istBefristet = parser.fromJson(jsonObject.get("istbefristet"), Boolean.class);

                fachgebietJobs.removeIf(j -> (istBefristet ? !j.getIstbefristet() : j.getIstbefristet()));
            }
            //Jahresgehalt
            if (jsonObject.has("jahresgehalt")) {
                int jahresgehalt = parser.fromJson(jsonObject.get("jahresgehalt"), Integer.class);
                fachgebietJobs.removeIf(j -> j.getJahresgehalt() < jahresgehalt);
            }
            //Urlaubstage
            if (jsonObject.has("urlaubstage")) {
                int urlaubstage = parser.fromJson(jsonObject.get("urlaubstage"), Integer.class);
                fachgebietJobs.removeIf(j -> j.getUrlaubstage() < urlaubstage);
            }

            //Entfernung
            //Die Entfernungssuche funktioniert nicht, wenn das Programm auf Windows getestet wird
            //Wird aber der Docker-Container genutzt, so können die fehlenden Zertifikate installiert werden
            //funktioniert etwas nicht, ist beispielsweise die API nicht zu erreichen, wird dieser Teil ignoriert
            if (System.getProperty("os.name").toLowerCase().contains("win") == false) {
                if (jsonObject.has("entfernung") && jsonObject.has("lat") && jsonObject.has("lon")) {
                    int entfernung = parser.fromJson(jsonObject.get("entfernung"), Integer.class);

                    double lat = parser.fromJson(jsonObject.get("lat"), Double.class);
                    double lon = parser.fromJson(jsonObject.get("lon"), Double.class);

                    Double[] anfrageCords = new Double[2];
                    anfrageCords[0] = lon;
                    anfrageCords[1] = lat;

                    fachgebietJobs.removeIf(j -> {
                        Adresse jobAdresse = j.getAdresse();

                        try {
                            Double[] jobCords = geocodingService.getCoordinates(jobAdresse);
                            double distance = entfernungsService.berechneEntfernung(anfrageCords, jobCords);
                            j.setEntfernung(distance);
                            return distance > entfernung;
                        } catch (Exception e) {
                            //Falls etwas nicht funktioniert hat, einfach entfernen
                            return true;
                        }

                    });
                }
            }

            List<Jobangebot> returnList = new ArrayList<>();
            for (Jobangebot j : fachgebietJobs) {
                returnList.add(j.clone());
            }

            return response.build(200, parser.toJson(returnList));
        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }

    }

    /**
     * Diese Route fügt ein neues Jobangebot hinzu.
     *
     * @param daten Die Daten des Jobangebots
     * @param token Das Webtoken
     * @return Das neue Jobangebot
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addJobangebot(String daten, @HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler == null) {
                    return response.buildError(403, "Sie sind kein Personaler");
                }

                Jobangebot jobangebot = parser.fromJson(daten, Jobangebot.class);
                jobangebot.setStart((Date) jobangebot.getStart());
                jobangebot.setEnde((Date) jobangebot.getEnde());
                jobangebot.setEinstelldatum((Date) jobangebot.getEinstelldatum());

                Jobangebot dbJobangebot = jobangebotEJB.add(jobangebot);

                //Ansprechpartner
                dbJobangebot.setAnsprechpartner(dbPersonaler);
                dbPersonaler.getJobangebotList().add(dbJobangebot);

                JsonObject jsonObject = parser.fromJson(daten, JsonObject.class);

                //Fachgebiet
                Fachgebiet dbFachgebiet;

                //Nur der Chef kann Jobangebote für andere Fachgebeite hinzufügen
                if (dbPersonaler.getRang() == 0) {
                    dbFachgebiet = fachgebietEJB.getByName(parser.fromJson(jsonObject.get("neuesfachgebiet"), String.class)); //Fachgebiete sind schon vorgegeben, deswegen kein null check nötig
                } else {
                    dbFachgebiet = dbPersonaler.getFachgebiet();
                }

                dbJobangebot.setFachgebiet(dbFachgebiet);

                dbFachgebiet.getJobangebotList().add(dbJobangebot);

                dbFachgebiet.setAnzahljobs(dbFachgebiet.getAnzahljobs() + 1);
                //Bewerbungstyp
                Bewerbungstyp bewerbungstyp = bewerbungstypEJB.getByName(parser.fromJson(jsonObject.get("neuerbewerbungstyp"), String.class));

                dbJobangebot.setBewerbungstyp(bewerbungstyp);

                bewerbungstyp.getJobangebotList().add(dbJobangebot);

                //Adresse
                Adresse dbAdresse = adresseEJB.add(parser.fromJson(jsonObject.get("neueadresse"), Adresse.class));

                dbJobangebot.setAdresse(dbAdresse);

                //Mails verschicken
                String fachgebietName = dbJobangebot.getFachgebiet().getName();
                String jobTitle = dbJobangebot.getTitle();
                String description = dbJobangebot.getKurzbeschreibung();

                for (Bewerber b : bewerberEJB.getForNewMailSend(dbFachgebiet)) {
                    String userName = b.getVorname() + " " + b.getName();
                    mailService.sendNewJob(userName, b.getEmail(), fachgebietName, jobTitle, description);
                }

                return response.build(200, parser.toJson(dbJobangebot.clone()));
            } catch (Exception e) {
                return response.buildError(500, e.getMessage());
            }
        }
    }

    /**
     * Diese Route löscht ein Jobangebot. Das kann nur durch den Personaler
     * durchgeführt werden, der das Jobangebot erstellt hat. Um diese Aktion zu
     * vollführen, müssen dabei alle Bewerbungen auf dieses Jobangebot gelöscht
     * werden.
     *
     * @param token Das Webtoken
     * @param id Die JobangebotsId
     * @return Response mit Fehler oder Bestätigung
     */
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam("Authorization") String token, @PathParam("id") int id) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Jobangebot dbJobangebot = jobangebotEJB.getById(id);

                if (!dbJobangebot.getAnsprechpartner().equals(dbPersonaler)) {
                    return response.buildError(400, "Sie haben dieses Jobangebot nicht erstellt");
                } else {

                    dbPersonaler.getJobangebotList().remove(dbJobangebot);
                    dbJobangebot.setAnsprechpartner(null);

                    for (Iterator<Bewerbung> iterator = dbJobangebot.getBewerbungList().iterator(); iterator.hasNext();) {
                        Bewerbung dbBewerbung = iterator.next();
                        Bewerber dbBewerber = dbBewerbung.getBewerber();

                        dbBewerber.getBewerbungList().remove(dbBewerbung);
                        dbBewerbung.setBewerber(null);

                        fileService.deleteBewerbung(dbBewerbung.getBewerbungid());

                        iterator.remove();
                        dbBewerbung.setJobangebot(null);

                        for (Bewerbungsnachricht n : dbBewerbung.getBewerbungsnachrichtList()) {
                            bewerbungsnachrichtEJB.remove(n);
                        }
                        dbBewerbung.setBewerbungsnachrichtList(null);

                        for (Personaler p : dbBewerbung.getPersonalerList()) {
                            p.getBewerbungList().remove(dbBewerbung);
                        }
                        dbBewerbung.setPersonalerList(null);

                        bewerbungEJB.remove(dbBewerbung);
                    }
                    dbJobangebot.setBewerbungList(null);

                    Fachgebiet dbFachgebiet = dbJobangebot.getFachgebiet();

                    dbFachgebiet.setAnzahljobs(dbFachgebiet.getAnzahljobs() - 1);

                    dbFachgebiet.getJobangebotList().remove(dbJobangebot);
                    dbJobangebot.setFachgebiet(null);

                    dbJobangebot.getBewerbungstyp().getJobangebotList().remove(dbJobangebot);
                    dbJobangebot.setBewerbungstyp(null);

                    jobangebotEJB.remove(dbJobangebot);

                    return response.build(200, parser.toJson("Erfolgreich gelöscht"));
                }

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt alle Fachgebiete mit dem Durchschnittsgehalt der
     * Jobangebote aus diesem Fachgebiet wieder.
     *
     * @param token Das Webtoken
     * @return Liste aus Fachgebiet mit Durchschnittsgehalt
     */
    @GET
    @Path("/gehalt/durchschnitt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvergaSalary(@HeaderParam("Authorization") String token) {
        try {

            JsonObject output = new JsonObject();

            List<JsonObject> list = new ArrayList<>();

            for (Fachgebiet f : fachgebietEJB.getAll()) {
                List<Jobangebot> jobs = f.getJobangebotList();
                if (!jobs.isEmpty()) {
                    int sum = 0;

                    for (Jobangebot job : jobs) {
                        sum += job.getJahresgehalt();
                    }

                    int avg = sum / jobs.size();

                    JsonObject json = new JsonObject();

                    json.add("fachgebiet", parser.toJsonTree(f.clone()));
                    json.add("durchschnitt", parser.toJsonTree(avg));

                    list.add(json);
                }
            }

            return response.build(200, parser.toJson(list));
        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

    /**
     * Diese Route gibt alle vom Chef angepinnten Jobs zurück, damit diese auf
     * der Startseite angezeigt werden können. Dabei handelt es sich um maximal
     * 4 Jobangebote.
     *
     * @return Die angepinnten Jobs
     */
    @GET
    @Path("/pinned")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPinned() {
        try {
            List<Jobangebot> pinned = jobangebotEJB.getPinnedByChef();

            List<Jobangebot> output = new ArrayList<>();

            for (Jobangebot j : pinned) {
                output.add(j.clone());
            }

            return response.build(200, parser.toJson(output));
        } catch (Exception e) {
            return response.buildError(500, "Es ist ein Fehler aufgetreten");
        }
    }

    /**
     * Mit dieser Route kann der Chef ein Jobangebot anpinnen, damit es auf der
     * Startseite angezeigt wird. Sie kann nur vom Chef aufgerufen werden.
     *
     * @param token Das Webtoken
     * @param id JobangebotID
     * @return Response mit Fehler oder Bestätigung
     */
    @GET
    @Path("/admin/pin/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pinJobangebot(@HeaderParam("Authorization") String token,
            @PathParam("id") int id
    ) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Jobangebot job = jobangebotEJB.getById(id);

                if (dbPersonaler == null) {
                    return response.buildError(404, "Es wurde kein Personaler gefunden");
                }

                job.setVonchefgepinnt(Boolean.TRUE);

                return response.build(200, parser.toJson("Success"));

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Mit dieser Route kann der Chef ein Jobangebot entpinnen, damit es nicht
     * mehr auf der Startseite angezeigt wird. Sie kann nur vom Chef aufgerufen
     * werden.
     *
     * @param token Das Webtoken
     * @param id JobangebotID
     * @return Response mit Fehler oder Bestätigung
     */
    @GET
    @Path("/admin/unpin/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unpinJobangebot(@HeaderParam("Authorization") String token,
            @PathParam("id") int id
    ) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                if (dbPersonaler == null) {
                    return response.buildError(404, "Es wurde kein Personaler gefunden");
                }
                Jobangebot job = jobangebotEJB.getById(id);

                job.setVonchefgepinnt(Boolean.FALSE);

                return response.build(200, parser.toJson("Success"));

            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    /**
     * Diese Route gibt ein zufälliges Jobangebot zurück, dass zum Fachgebiet
     * des Bewerbers passt, der diese Route aufruft.
     *
     * @param token Das Webtoken
     * @return Ein zufälliges Jobangebot
     */
    @GET
    @Path("/passend")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFittingJob(@HeaderParam("Authorization") String token) {
        if (!verify(token)) {
            return response.buildError(401, "Ungueltiges Token");
        } else {
            try {

                Bewerber dbBewerber = bewerberEJB.getByToken(token);

                if (dbBewerber == null) {
                    return response.buildError(404, "Es wurde kein Bewerber gefunden");
                }

                if (dbBewerber.getFachgebiet() == null) {
                    return response.buildError(403, "Dieser Bewerber hat noch kein Fachgebiet");
                }

                List<Jobangebot> fittingJobs = jobangebotEJB.getByFachgebiet(dbBewerber.getFachgebiet());

                if (fittingJobs.isEmpty()) {
                    return response.buildError(401, "Es gibt noch keine Jobs in diesem Fachgebiet");
                }

                Random rand = new Random();
                Jobangebot dbJobangebot = fittingJobs.get(rand.nextInt(fittingJobs.size()));

                return response.build(200, parser.toJson(dbJobangebot.clone()));
            } catch (Exception e) {
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
