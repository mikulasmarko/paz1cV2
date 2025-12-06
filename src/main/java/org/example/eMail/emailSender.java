package org.example.eMail;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.example.eMail.emailConstants.*;

public class emailSender {

	private static final String emailFrom = APP_MAIL;
	private static final String emailTo = "5388467@upjs.sk";
	private static final String appPassword = EMAIL_APP_PASS;

	public static void main(String[] args) throws MessagingException, IOException {
        // Example usage
        // sendEmail("5388467@upjs.sk", "Registrácia", "Registrácia úspešná", "src/main/resources/qrCodesGenerated/540678.png");
	}

    public static void sendEmail(String to, String subject, String body, String attachmentPath) {
        try {
            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            if (attachmentPath != null) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                try {
                    attachmentPart.attachFile(new File(attachmentPath));
                    multipart.addBodyPart(attachmentPart);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("email sent");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

	private static Session getEmailSession() {
		return Session.getInstance(getEmailProperties(), new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(emailFrom, appPassword);
			}
		});
	}

	private static Properties getEmailProperties() {
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		return properties;
	}

}
