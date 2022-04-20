package Service;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * <h1>Die Java-Klasse zum Versenden von E-Mails.</h1>
 * <p>
 * Diese Klasse versendet die E-Mails über einen konfigurierbaren
 * SMTP-Server.</p>
 *
 * @author Lukas Krinke, Florian Noje, Simon Engel
 */
public class MailService {

    final String mailFrom = "innovationsaward2022@gymnasium-essen-werden.de";
    final String password = "Kug30420";

    /**
     * Die folgende Methode versendet die E-Mails an die Bewerber, in der sie
     * ihren Verifizierungspin erhalten, um ihr Konto freizuschalten.
     *
     * @param benutzername Benutzername des neu registrierten Benutzers
     * @param mailTo E-Mail-Adresse des neu registrierten Benutzers
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public int sendVerificationPin(String benutzername, String mailTo) throws IOException, AddressException, MessagingException, InterruptedException {

        int pin = this.getRandomCode();

        String msg = "<h2>Sehr geehrte/r " + benutzername + ", </h2><p>vielen Dank für ihre Registrierung. Um Ihre Registrierung abzuschließen, brauchen Sie lediglich noch den folgenden Verifizierungscode einzugeben:</p>"
                + "</br>" + "<h2>" + pin + "</h2>"
                + "</br>"
                + "<h3>Mit freundlichen Grüßen</h3>";

        this.sendMail(mailTo, msg, "Anmeldung");

        return pin;

    }

    /**
     * Die folgende Methode versendet E-Mails an die Benutzer, um sie auf eine
     * Änderung ihres Passwortes aufmerksam zu machen.
     *
     * @param benutzername Benutzername
     * @param mailTo E-Mailadresse des Benutzers
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public void sendPasswordChangedMail(String benutzername, String mailTo) throws IOException, AddressException, MessagingException, InterruptedException {

        String msg = "<h2>Sehr geehrte/r " + benutzername + ", </h2><p>diese Mail dient dazu, Sie über eine Änderung Ihres Passwortes zu informieren. Wenn Sie diese Änderung durchgeführt haben, können Sie diese Mail ignorieren, wenn nicht, scheint es ein "
                + "Sicherheitsproblem mit ihrem Konto zu geben. Ändern Sie bitte Ihr Passwort oder kontaktieren Sie uns.</p>"
                + "</br>"
                + "<h3>Mit freundlichen Grüßen</h3>";

        this.sendMail(mailTo, msg, "Passwortänderung");
    }

    /**
     * Diese Methode vesendet eine E-Mail an Bewerber, um sie über ein neues
     * Jobangebot in ihrem Fachgebiet aufmerksam zu machen
     *
     * @param benutzername Benutzername des Bewerbers
     * @param mailTo E-Mailadresse des Bewerbers
     * @param fachgebiet Name des Fachgebiets
     * @param jobTitle Name des Jobangebots
     * @param description Kurzbeschreibung des Jobangebots
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public void sendNewJob(String benutzername, String mailTo, String fachgebiet, String jobTitle, String description) throws IOException, AddressException, MessagingException, InterruptedException {
        String msg = "<h2>Sehr geehrte/r " + benutzername + ", </h2>"
                + "<p>in Ihrem Fachgebiet " + fachgebiet + " gibt es ein neues Jobangebot, dass für Sie interessant sein könnte. Es ist unter dem folgenden Namen zu finden:</p>"
                + "</br>"
                + "<h2>" + jobTitle + "</h2>"
                + "</br>"
                + "<p>Die Kurzbeschreibung lautet: " + description + ".</p>"
                + "</br>"
                + "<p>Wir würden uns über Ihre Bewerbung freuen.</p>"
                + "<h3>Mit freundlichen Grüßen</h3>";

        this.sendMail(mailTo, msg, "Neues Jobangebot");
    }

    /**
     * Diese Methode versendet eine E-Mail an Personaler, wenn auf ein von ihnen
     * erstelltes Jobangebot eine neue Bewerbung vorliegt.
     *
     * @param benutzername Namre des Personalers
     * @param mailTo E-Mailadresse des Bewerbers
     * @param jobTitle Name des Jobangebots
     * @param applicantName Name des Bewerbers
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public void sendNewApplication(String benutzername, String mailTo, String jobTitle, String applicantName) throws IOException, AddressException, MessagingException, InterruptedException {
        String msg = "<h2>Sehr geehrte/r " + benutzername + ", </h2>"
                + "<p>auf das von Ihnen erstellte Jobangebot '" + jobTitle + "' gibt es eine neue Bewerbung "
                + "von " + applicantName + ".</p>"
                + "<h3>Mit freundlichen Grüßen</h3>";

        this.sendMail(mailTo, msg, "Neue Bewerbung");
    }

    /**
     * Diese Methode versendet eine E-Mail an neu eingestellte Personaler, um
     * sie zu begrüßen.
     *
     * @param benutzername Name des Personalers
     * @param mailTo E-Mailadresse des Bewerbers
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public void sendNewEmployer(String benutzername, String mailTo) throws IOException, AddressException, MessagingException, InterruptedException {
        String msg = "<h2>Sehr geehrte/r " + benutzername + ", </h2>"
                + "<p>Herzlich Willkommen in unserer Firma. Wir freuen uns auf die Arbeit mit Ihnen.</p>"
                + "</br>"
                + "<h3>Mit freundlichen Grüßen</h3>";

        this.sendMail(mailTo, msg, "Herzlich Willkommen");
    }

    /**
     * Dese Methode sendet eine E-Mail an Bewerber oder Personaler und schickt
     * ihnen den Pin für eine Zweifaktor-Authentifizierung.
     *
     * @param benutzername Der Name
     * @param mailTo Die E-Mail
     * @return
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public int send2Fa(String benutzername, String mailTo) throws IOException, AddressException, MessagingException, InterruptedException {

        int pin = this.getRandomCode();

        String msg = "<h2>Sehr geehrte/r " + benutzername + ", </h2>"
                + "<p>anbei finden Sie Ihren Code zur Zweifaktor-Authentifizierung: </p>"
                + "</br>" + "<h2>" + pin + "</h2>"
                + "</br>"
                + "<h3>Mit freundlichen Grüßen</h3>";

        this.sendMail(mailTo, msg, "Zweifaktor-Authentifizierung");

        return pin;
    }

    /**
     * Diese Methode versendet eine E-Mail an Bewerber oder Personaler, um sie
     * über eine Änderung ihrer E-Mailadresse zu informieren.
     *
     * @param benutzername Der Name
     * @param mailTo Die alte E-Mailadresse
     * @param newMail Die neue E-Mailadresse
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public void sendMailChange(String benutzername, String mailTo, String newMail) throws IOException, AddressException, MessagingException, InterruptedException {
        String msg = "<h2>Sehr geehrte/r " + benutzername + ", </h2>"
                + "<p>diese Mail dient dazu, Sie über eine Änderung ihrer E-Mailadresse zu informieren. SIe lautet nun: </p>"
                + "</br>" + "<h2>" + newMail + "</h2>"
                + "</br>"
                + "<h3>Mit freundlichen Grüßen</h3>";

        this.sendMail(mailTo, msg, "E-Mail-Änderung");
    }

    /**
     * Diese private Methode versendet eine E-Mail mit einem gegeben Inhalt an
     * eine E-Mailadresse
     *
     * @param mailTo Die Zieladresse
     * @param msg Der Inhalt der E-Mail, als HTML-Code formatiert
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    private void sendMail(String mailTo, String msg, String subject) throws IOException, AddressException, MessagingException, InterruptedException {
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.office365.com");
        prop.put("mail.smtp.ssl.trust", "smtp.office365.com");
        prop.put("mail.smtp.port", "587");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailFrom, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailTo));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(mailTo));

        message.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html;charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setFrom(new InternetAddress("innovationsaward2022@gymnasium-essen-werden.de"));

        message.setContent(multipart);

        Transport.send(message);
    }

    /**
     * Diese Methode generiert einen zufälligen vierstelligen Code, der über
     * Mails versendet wird, um das Registrieren, Einloggen oder eine
     * Passwortänderung zu genehmigen.
     *
     * @return Vierstelliger Code
     */
    public int getRandomCode() {
        int min = 1000;
        int max = 9999;
        int random_int = (int) (Math.random() * (max - min + 1) + min);
        return random_int;
    }
}
