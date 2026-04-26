package utils;

import modelos.Producto;
import modelos.Trato;
import modelos.Usuario;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;
import java.io.File;
import java.util.Properties;

// UTILIDAD: Gestión del envío de emails.
// Devuelve boolean para que la capa que llame decida qué mostrar al usuario.
public class EmailUtils {
    private static final String REMITENTE = "nachescalpro@gmail.com";
    private static final String CLAVE     = "ysep wwzd ygkk gafx";

    /**
     * Envía un email de texto plano.
     * @return true si el envío fue correcto, false si hubo algún error.
     */
    public static boolean enviarEmail(String destinatario, String asunto, String cuerpo) {
        try {
            Session session = crearSession();
            MimeMessage mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(REMITENTE));
            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
            mensaje.setSubject(asunto, "UTF-8");
            mensaje.setText(cuerpo, "UTF-8");

            Transport transport = session.getTransport("smtp");
            transport.connect(REMITENTE, CLAVE);
            transport.sendMessage(mensaje, mensaje.getAllRecipients());
            transport.close();
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    /**
     * Envía un email con un fichero PDF adjunto.
     * @return true si el envío fue correcto.
     */
    public static boolean enviarEmailConPDF(String destinatario, String asunto,
                                            String cuerpo, File adjuntoPDF) {
        try {
            Session session = crearSession();
            MimeMessage mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(REMITENTE));
            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
            mensaje.setSubject(asunto, "UTF-8");

            // Parte de texto
            MimeBodyPart textoParte = new MimeBodyPart();
            textoParte.setText(cuerpo, "UTF-8");

            // Parte del adjunto
            MimeBodyPart adjuntoParte = new MimeBodyPart();
            adjuntoParte.attachFile(adjuntoPDF);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textoParte);
            multipart.addBodyPart(adjuntoParte);
            mensaje.setContent(multipart);

            Transport transport = session.getTransport("smtp");
            transport.connect(REMITENTE, CLAVE);
            transport.sendMessage(mensaje, mensaje.getAllRecipients());
            transport.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Envía un email con un fichero Excel adjunto.
     */
    public static boolean enviarEmailConExcel(String destinatario, String asunto,
                                               String cuerpo, File adjuntoExcel) {
        return enviarEmailConPDF(destinatario, asunto, cuerpo, adjuntoExcel); // mismo mecanismo
    }

    // -------------------------------------------------------------------------
    private static Session crearSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host",             "smtp.gmail.com");
        props.put("mail.smtp.port",             "587");
        props.put("mail.smtp.auth",             "true");
        props.put("mail.smtp.starttls.enable",  "true");
        return Session.getInstance(props);
    }
}
