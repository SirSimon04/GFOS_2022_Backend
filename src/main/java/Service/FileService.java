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

    public File getLebenslauf(int id) {
        String path = "./projectFiles/lebenslaeufe/" + id + ".pdf";

        File lebenslauf = new File(path);

        return lebenslauf;
    }

    public boolean saveLebenslauf(int id, String base64) throws IOException {
        String path = "./projectFiles/lebenslaeufe/" + id + ".pdf";

        base64 = base64.split(",")[1];

        return this.saveFile(path, base64);
    }

    public void deleteLebenslauf(int id) {
        String path = "./projectFiles/lebenslaeufe/" + id + ".pdf";

        File lebenslauf = new File(path);

        lebenslauf.delete();
    }

    public File getProfilbild(int id) {
        String path = "./projectFiles/profileimages/" + id + ".jpg";
        File profilbild = new File(path);

        return profilbild;
    }

    public boolean saveProfilbild(int id, String base64) throws IOException {
        String path = "./projectFiles/profileimages/" + id + ".jpg";

        base64 = base64.split(",")[1];

        return this.saveFile(path, base64);
    }

    public void deleteProfilbild(int id) {
        String path = "./projectFiles/profileimages/" + id + ".jpg";

        File profilbild = new File(path);

        profilbild.delete();
    }

    public File getBewerbung(int name) {
        String path = "./projectFiles/bewerbungen/" + name + ".pdf";

        File bewerbung = new File(path);
        return bewerbung;
    }

    public void saveBewerbung(int id, String base64) throws IOException {
        String path = "./projectFiles/bewerbungen/" + id + ".pdf";

        base64 = base64.split(",")[1];

        this.saveFile(path, base64);
    }

    public void deleteBewerbung(int id) {
        String path = "./projectFiles/bewerbungen/" + id + ".pdf";
        File bewerbung = new File(path);

        bewerbung.delete();
    }

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
