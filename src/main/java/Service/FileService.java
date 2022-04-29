/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import javax.imageio.ImageIO;
import sun.misc.BASE64Decoder;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
 * <h1>Die Klasse zum Verwalten von Dateien, die von Nutzern gesendet
 * werden.</h1>
 *
 * @author simon
 */
public class FileService {

    /**
     * Die Methode erstellt den FileService.
     */
    public FileService() {
    }

    /**
     * Diese Methode gibt einen Lebenslauf anhand seiner Id zurück
     *
     * @param id Id
     * @return Lebenslauf
     */
    public String getLebenslauf(int id) {
        String path = "./projectFiles/lebenslaeufe/" + id + ".pdf";

        File lebenslauf = new File(path);

        return "data:application/pdf;base64," + this.encodeFileToBase64(lebenslauf);
    }

    /**
     * Diese Methode speichert einen Lebenslauf
     *
     * @param id Id
     * @param base64 Lebenslauf als Base64
     * @return boolean, ob erfolgreich
     * @throws IOException
     */
    public boolean saveLebenslauf(int id, String base64) throws IOException {
        String path = "./projectFiles/lebenslaeufe/" + id + ".pdf";

        base64 = base64.split(",")[1];

        return this.saveFile(path, base64);
    }

    /**
     * Diese Methode löscht einen Lebenslauf
     *
     * @param id Id
     */
    public void deleteLebenslauf(int id) {
        String path = "./projectFiles/lebenslaeufe/" + id + ".pdf";

        File lebenslauf = new File(path);

        lebenslauf.delete();
    }

    /**
     * Diese Methode gibt ein Profilbild anhand seiner Id zurück.
     *
     * @param id Id
     * @return Profilbild
     */
    public String getProfilbild(int id) {
        String path = "./projectFiles/profileimages/" + id + ".jpg";
        File profilbild = new File(path);

        return "data:image/jpeg;base64," + this.encodeFileToBase64(profilbild);
    }

    /**
     * Diese Methode speichert ein Profilbild
     *
     * @param id Id
     * @param base64 Profilbild als Base64
     * @return boolean, ob erfolgreich
     * @throws IOException
     */
    public boolean saveProfilbild(int id, String base64) throws IOException {
        String path = "./projectFiles/profileimages/" + id + ".jpg";

        base64 = base64.split(",")[1];

        return this.saveFile(path, base64);
    }

    /**
     * Diese Methode löscht ein Profilbild
     *
     * @param id Id
     */
    public void deleteProfilbild(int id) {
        String path = "./projectFiles/profileimages/" + id + ".jpg";

        File profilbild = new File(path);

        profilbild.delete();
    }

    /**
     * Diese Methode gibt eine Bewerbung anhand seiner Id zurück
     *
     * @param id Id
     * @return Bewerbungsschreiben
     */
    public String getBewerbung(int id) {
        String path = "./projectFiles/bewerbungen/" + id + ".pdf";

        File bewerbung = new File(path);

        return "data:application/pdf;base64," + this.encodeFileToBase64(bewerbung);
    }

    /**
     * Diese Methode speichert eine Bewerbung
     *
     * @param id Id
     * @param base64 Bewerbung als Base64
     * @throws IOException
     */
    public void saveBewerbung(int id, String base64) throws IOException {
        String path = "./projectFiles/bewerbungen/" + id + ".pdf";

        base64 = base64.split(",")[1];

        this.saveFile(path, base64);
    }

    /**
     * Diese Methode löscht eine Bewerbung
     *
     * @param id Id
     */
    public void deleteBewerbung(int id) {
        String path = "./projectFiles/bewerbungen/" + id + ".pdf";
        File bewerbung = new File(path);

        bewerbung.delete();
    }

    /**
     * Diese Methode gibt eine Lebenslaufstation anhand seiner Id zurück.
     *
     * @param id Id Id
     * @return Lebenslaufstation
     */
    public String getLebenslaufstation(int id) {
        String path = "./projectFiles/lebenslaufstationen/" + id + ".pdf";

        File station = new File(path);

        return "data:application/pdf;base64," + this.encodeFileToBase64(station);
    }

    /**
     * Diese Methode speichert eine Lebenslaufstation
     *
     * @param id Id
     * @param base64 Lebenslaufstation als Base64
     * @throws IOException
     */
    public void saveLebenslaufstation(int id, String base64) throws IOException {
        String path = "./projectFiles/lebenslaufstationen/" + id + ".pdf";

        base64 = base64.split(",")[1];

        this.saveFile(path, base64);
    }

    /**
     * Diese Methode löscht eine Lebenslaufsstation
     *
     * @param id Id
     * @throws FileNotFoundException
     */
    public void deleteLebenslaufstation(int id) throws FileNotFoundException {
        String path = "./projectFiles/lebenslaufstationen/" + id + ".pdf";

        File station = new File(path);

        station.delete();
    }

    public String encodeFileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }

    /**
     * Diese Methode speichert eine Datei im System ab. Sie wird von allen
     * anderen Methoden dieser Klasse aufgerufen, die eine Datei jeder Art
     * speichern.
     *
     * @param path neuer Pfad
     * @param base64 Datei als Base64
     * @return boolean, ob erfolgreich‚
     * @throws IOException
     */
    private boolean saveFile(String path, String base64) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();

        //Base64 decoding, Base64 decoding of byte array string and generating file
        byte[] byt = decoder.decodeBuffer(base64);
        for (int i = 0, len = byt.length; i < len; ++i) {
            //Adjust abnormal data
            if (byt[i] < 0) {
                byt[i] += 256;
            }
        }
        OutputStream out = null;
        InputStream input = new ByteArrayInputStream(byt);
        try {
            //Generate files in the specified format
            out = new FileOutputStream(path);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = input.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            out.flush();
            out.close();
            return false;
        } catch (Exception e) {
            out.flush();
            out.close();
            return false;
        } finally {
            out.flush();
            out.close();
            return true;
        }
    }
}
