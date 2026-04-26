package utils;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtils {
    // Cambia estos datos por los de tu cuenta de Gmail
    private static final String REMITENTE = "nachescalpro@gmail.com";
    private static final String CLAVE = "ysep wwzd ygkk gafx"; // Usa clave de app de Gmail

    public static void enviarEmail(String destinatario, String asunto, String cuerpo) {

        // Configuración del servidor SMTP de Gmail
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props);

        try {
            MimeMessage mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(REMITENTE));
            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo); // Texto plano

            Transport transport = session.getTransport("smtp");
            transport.connect(REMITENTE, CLAVE);
            transport.sendMessage(mensaje, mensaje.getAllRecipients());
            transport.close();

            System.out.println("Correo enviado correctamente a " + destinatario);

        } catch (MessagingException e) {
            System.out.println("Error al enviar el correo a " + destinatario);
            e.printStackTrace();
        }
    }
}
