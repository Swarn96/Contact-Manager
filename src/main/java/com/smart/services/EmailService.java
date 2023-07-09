package com.smart.services;

import java.util.Properties;



import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

	

	public boolean sendEmail(String subject, String message, String to) {
		// TODO Auto-generated method stub
		
		// rest code
				boolean flag=false;
				
				String from="swarnkamal96@gmail.com";

				// variable for gmail
				String host = "smtp.gmail.com";

				// get the system properties
				Properties properties = new Properties();
				System.out.println("PROPERTIES" + properties);

				// setting important information to properties object

				// set host
				properties.put("mail.smtp.auth", true);
				properties.put("mail.smtp.starttls.enable", true);
				properties.put("mail.smtp.port", "587");
				properties.put("mail.smtp.host", host);
			
				// STEP1 get session object
				Session session = Session.getInstance(properties, new Authenticator() {

					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						// TODO Auto-generated method stub
						return new PasswordAuthentication("swarnkamal96@gmail.com","rthxshpjkmqhxzdi");
					}
				
				});
				
				try {
					
					// STEP2 compose the message[text,multi,media]
					
					Message m = new MimeMessage(session);
					m.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
					m.setFrom(new InternetAddress(from));
					m.setSubject(subject);
					//m.setText(message);
					m.setContent(message,"text/html");
					
					
					//STEP3 send the message through Transport class
					Transport.send(m);
					
					flag=true;
					
				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
				return flag;
	}
	
}
