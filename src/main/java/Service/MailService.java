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
     * @param mailFrom E-Mail zur Authentifizierung des SMTP-Servers
     * @param pw Passwort zur Authentifizierung des SMTP-Servers
     * @param benutzername Benutzername des neu registrierten Benutzers
     * @param mailTo E-Mail-Adresse des neu registrierten Benutzers
     * @param pin Der zuvor zufällig erstellte Verifizierungspin
     * @throws IOException
     * @throws AddressException
     * @throws MessagingException
     * @throws InterruptedException
     */
    public void sendVerificationPin(String benutzername, String mailTo, int pin) throws IOException, AddressException, MessagingException, InterruptedException {

        String msg = "<h2>Sehr geehrte/r " + benutzername + ",</h2><p>vielen Dank für ihre Registrierung. Um Ihre Registrierung abzuschließen, brauchen Sie lediglich noch den folgenden Verifizierungscode einzugeben:</p>"
                + "</br>" + "<h2>" + pin + "</h2>"
                + "</br>"
                + "<h3>Mit freundlichen Grüßen</h3>";

        this.sendMail(mailTo, msg);

    }

    private void sendMail(String mailTo, String msg) throws IOException, AddressException, MessagingException, InterruptedException {
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

        message.setSubject("Bewerber-Registrierung");

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html;charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setFrom(new InternetAddress("innovationsaward2022@gymnasium-essen-werden.de"));

        message.setContent(multipart);

        Transport.send(message);
    }
}
