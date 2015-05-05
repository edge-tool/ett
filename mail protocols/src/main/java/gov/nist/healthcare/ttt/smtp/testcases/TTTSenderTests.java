package gov.nist.healthcare.ttt.smtp.testcases;

import gov.nist.healthcare.ttt.smtp.TestInput;
import gov.nist.healthcare.ttt.smtp.TestResult;
import gov.nist.healthcare.ttt.smtp.TestResult.CriteriaStatus;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.log4j.Logger;

public class TTTSenderTests {

	public static Logger log = Logger.getLogger("TTTSenderTests");
	Properties config;

	/**
	 * Implements Testcase #9. Sends a mail from TTT James to SUT.
	 * 
	 * @return
	 */

	public TestResult testSendMail(TestInput ti) {

		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.TRUE); // proctored are true unless exception happens
		HashMap<String, String> result = tr.getTestRequestResponses();


		// Create a mail session
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", ti.useTLS ? "true" : "false");
		properties.put("mail.smtp.quitwait", "false");
		properties.put("mail.smtp.userset", "true");
		properties.put("mail.smtp.ssl.trust", "*");
		try {
			Session session = Session.getInstance(properties, null);

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutUserName));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));

			message.setSubject("Email from TTT (Test Case 9)");
			message.setText("This is a mail from JAMES Server");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			Multipart multipart = new MimeMultipart();
			String aName = "";
			for (Map.Entry<String, byte[]> e : ti.getAttachments().entrySet()) {

				DataSource source = new ByteArrayDataSource(e.getValue(),
						"text/html");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(e.getKey());
				aName += e.getKey();
				multipart.addBodyPart(messageBodyPart);

				// Send the complete message parts
				message.setContent(multipart);
			}
			Transport transport = session.getTransport("smtp");
			transport.connect (ti.tttSmtpAddress, ti.useTLS ? ti.startTlsPort : ti.tttSmtpPort, ti.tttUserName, ti.tttPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			log.info("SENDING FIRST EMAIL");
			result.put("1","SENDING FIRST EMAIL TO " + ti.sutEmailAddress + " FROM " + ti.tttEmailAddress + " WITH ATTACHMENT " + aName);
			result.put("2","Email sent Successfully");
			System.out.println("Email sent successfully");

		} catch (MessagingException e) {
			e.printStackTrace();
			log.info("Error in Testcase 9" );
			result.put("1", "Error Sending Email " +  e.getLocalizedMessage() + new String(e.getMessage()));
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	/**
	 * Implements Testcase #16. Authenticates with SUT and sends a mail from SUT Server to a user on SUT using STARTTLS.
	 * 
	 * @return
	 */

	public TestResult testStarttls(TestInput ti) {

		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.MANUAL);
		HashMap<String, String> result = tr.getTestRequestResponses();

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable",true);
		props.put("mail.smtp.starttls.required", true);
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");


		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.sutEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing STARTTLS & PLAIN SASL AUTHENTICATION (Test Case 9,16,20)!");
			message.setText("This is a message to test STARTTLS Security!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");
			String aName = "";

			Multipart multipart = new MimeMultipart();
			for (Map.Entry<String, byte[]> e : ti.getAttachments().entrySet()) {

				DataSource source = new ByteArrayDataSource(e.getValue(),
						"text/html");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(e.getKey());
				aName += e.getKey();
				multipart.addBodyPart(messageBodyPart);

				// Send the complete message parts
				message.setContent(multipart);
			}

			log.info("Sending Message");
			System.setProperty("java.net.preferIPv4Stack", "true");

			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();

			System.out.println("Done");
			log.info("Message Sent");
			result.put("\n1","SENDING STARTTLS & PLAIN SASL AUTHENTICATION EMAIL TO " + ti.sutEmailAddress + " WITH ATTACHMENT " + aName);
			result.put("\n2","Email sent Successfully");

		} catch (SendFailedException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe weren't able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);

		} catch (AddressException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage() + "\nWe weren't able to find the vendor's domain. Please check for any spelling errors, and make sure you didn't enter any spaces, periods, or other punctuation after the vendor's email address.");
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}
		catch (MessagingException e) {
			log.info("Error in testStarttls");
			result.put("\nERROR ", e.getLocalizedMessage());
			// throw new RuntimeException(e);
			e.printStackTrace();
			tr.setCriteriamet(CriteriaStatus.FALSE);
		}

		return tr;
	}

	/**
	 * Implements Testcase #20 and #22. Authenticates with SUT(good/bad password) and sends a mail from SUT Server to a user on SUT.
	 * 
	 * @return
	 */
	public TestResult testPlainSasl(TestInput ti, boolean useBadPassWord) {

		TestResult tr = new TestResult();
		tr.setProctored(true);
		tr.setCriteriamet(CriteriaStatus.TRUE);
		HashMap<String, String> result = tr.getTestRequestResponses();
		Properties props = new Properties();

		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", ti.useTLS ? "true" : "false");
		props.put("mail.smtp.auth.mechanisms", "PLAIN");
		props.put("mail.smtp.ssl.trust", "*");

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing PLAIN SASL (Test Case 20)");
			message.setText("This is a message to test PLAIN SASL!");

			BodyPart messageBodyPart = new MimeBodyPart();

			messageBodyPart.setText("This is message body");

			Multipart multipart = new MimeMultipart();
			for (Map.Entry<String, byte[]> e : ti.getAttachments().entrySet()) {

				DataSource source = new ByteArrayDataSource(e.getValue(),
						"text/html");
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(e.getKey());
				multipart.addBodyPart(messageBodyPart);

				// Send the complete message parts
				message.setContent(multipart);
			}

			log.info("Authenticating....");
			Transport transport = session.getTransport("smtp");

			transport.connect(ti.sutSmtpAddress, ti.useTLS ? ti.startTlsPort
					: ti.sutSmtpPort, ti.sutUserName,
					useBadPassWord ? "badpassword" : ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			log.info("Authenticated Succefully");
			result.put("\n1","SENDING STARTTLS EMAIL TO " + ti.sutEmailAddress);
			result.put("\n2","Email sent Successfully");

			System.out.println("Email Sent.");

		} catch (MessagingException e) {
			if (e instanceof AuthenticationFailedException) {
				log.info("Authentication Failed. SUT rejects user/pass");
				result.put("SUCCESS", "Vendor rejects bad Username/Password combination :" + e.getLocalizedMessage());
				if(useBadPassWord){
					tr.setCriteriamet(CriteriaStatus.TRUE);
				}
				else {
					tr.setCriteriamet(CriteriaStatus.FALSE);
				}
			} else {
				tr.setCriteriamet(CriteriaStatus.FALSE);
				e.printStackTrace();
				log.info("error in PLAIN SASL");
				result.put("ERROR", e.getLocalizedMessage());
			}
		}


		return tr;
	}

	public void testDigestMd5(TestInput ti) {

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", ti.useTLS);
		props.put("mail.smtp.host", ti.sutSmtpAddress);
		props.put("mail.smtp.port", ti.startTlsPort);
		props.put("mail.smtp.auth.mechanisms", "DIGEST-MD5");

		Session session = Session.getInstance(props, null);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(ti.tttEmailAddress));

			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(ti.sutEmailAddress));
			message.setSubject("Testing DIGEST-MD5");
			message.setText("This is a message to test DIGEST-MD5!");

			log.info("Sending Message");
			Transport transport = session.getTransport("smtp");
			transport.connect(ti.sutSmtpAddress, ti.sutSmtpPort,
					ti.sutUserName, ti.sutPassword);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			System.out.println("Done");

		} catch (MessagingException e) {
			log.info("Error in DigestMD5");
			throw new RuntimeException(e);
		}
	}


}