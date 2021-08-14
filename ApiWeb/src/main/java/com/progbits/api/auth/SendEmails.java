package com.progbits.api.auth;

import com.sun.mail.imap.IMAPProvider;
import com.sun.mail.smtp.SMTPProvider;
import com.sun.mail.smtp.SMTPSSLProvider;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceConfigurationError;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
public class SendEmails {

	private static final Logger log = LoggerFactory.getLogger(SendEmails.class);

	private Map<String, String> _params;

	public void configure(Map<String, String> params) {
		_params = params;
	}

	public void sendEmail(String to, String subject, String body) {
		String from = _params.get("EMAIL_FROM");

		Properties properties = System.getProperties();

		properties.setProperty("mail.smtp.host", _params.get("EMAIL_HOST"));
		properties.setProperty("mail.smtp.port", _params.get("EMAIL_PORT"));
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.user", _params.get("EMAIL_USER"));
		properties.setProperty("mail.password", _params.get("EMAIL_PASSWORD"));
		properties.put("mail.smtp.starttls.enable", "true");

		Authenticator auth = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(from, _params.get("EMAIL_PASSWORD"));
			}
		};

		Session session = Session.getInstance(properties, auth);

		SMTPSSLProvider smtpSslProvider = new SMTPSSLProvider();
		SMTPProvider smtpProvider = new SMTPProvider();
		IMAPProvider imapProvider = new IMAPProvider();
		
		session.addProvider(imapProvider);
		session.addProvider(smtpSslProvider);
		session.addProvider(smtpProvider);
		
		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			//set message headers
//			message.addHeader("Content-type", "text/HTML; charset=UTF-8");
//			message.addHeader("format", "flowed");
//			message.addHeader("Content-Transfer-Encoding", "8bit");

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// Set Subject: header field
			message.setSubject(subject);

			// Now set the actual message
			message.setText(body, "UTF-8");

			// Send message
			Transport.send(message);
			log.info("Sent Email to <" + to + "> Successfully");
		} catch (Exception | ServiceConfigurationError mex) {
			log.error("Email to <" + to + "> Failed: " + mex.getMessage());
		}
	}
}
