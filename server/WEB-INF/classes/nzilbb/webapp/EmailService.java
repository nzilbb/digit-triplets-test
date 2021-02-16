//
// Copyright 2020 New Zealand Institute of Language, Brain and Behaviour, 
// University of Canterbury
// Written by Robert Fromont - robert.fromont@canterbury.ac.nz
//
//    This file is part of digit-triplets-test.
//
//    This is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This software is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this software; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package nzilbb.webapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.servlet.ServletContext;

/**
 * Provides access to email services.
 * @author Robert Fromont robert@fromont.net.nz
 */
public class EmailService {
   
   // Attributes:
   
   /**
    * The servlet context of the service.
    * @see #getContext()
    * @see #setContext(ServletContext)
    */
   protected ServletContext context;
   /**
    * Getter for {@link #context}: The servlet context of the service.
    * @return The servlet context of the service.
    */
   public ServletContext getContext() { return context; }
   /**
    * Setter for {@link #context}: The servlet context of the service.
    * <p> This method also sets the "nzilbb.webapp.DatabaseService" attribute of the servlet
    * context to be this object. 
    * @param newContext The servlet context of the service.
    */
   public EmailService setContext(ServletContext newContext) {
      context = newContext;
      if (context != null) {
         context.setAttribute("nzilbb.webapp.DatabaseService", this);
      }
      return this;
   }
   
   /**
    * STMP user name.
    * @see #getSMTPUser()
    * @see #setSMTPUser(String)
    */
   protected String SMTPUser;
   /**
    * Getter for {@link #SMTPUser}: STMP user name.
    * @return STMP user name.
    */
   public String getSMTPUser() { return SMTPUser; }
   /**
    * Setter for {@link #SMTPUser}: STMP user name.
    * @param newSMTPUser STMP user name.
    */
   public EmailService setSMTPUser(String newSMTPUser) {
      SMTPUser = newSMTPUser;
      if (SMTPUser != null && SMTPUser.length() == 0) SMTPUser = null;
      return this;
   }
   
   /**
    * STMP password.
    * @see #getSMTPPassword()
    * @see #setSMTPPassword(String)
    */
   protected String SMTPPassword;
   /**
    * Getter for {@link #SMTPPassword}: STMP password.
    * @return STMP password.
    */
   public String getSMTPPassword() { return SMTPPassword; }
   /**
    * Setter for {@link #SMTPPassword}: STMP password.
    * @param newSMTPPassword STMP password.
    */
   public EmailService setSMTPPassword(String newSMTPPassword) {
      SMTPPassword = newSMTPPassword;
      if (SMTPPassword != null && SMTPPassword.length() == 0) SMTPPassword = null;
      return this;
   }
   
   /**
    * SMTP server host name.
    * @see #getSMTPHost()
    * @see #setSMTPHost(String)
    */
   protected String SMTPHost;
   /**
    * Getter for {@link #SMTPHost}: SMTP server host name.
    * @return SMTP server host name.
    */
   public String getSMTPHost() { return SMTPHost; }
   /**
    * Setter for {@link #SMTPHost}: SMTP server host name.
    * @param newSMTPHost SMTP server host name.
    */
   public EmailService setSMTPHost(String newSMTPHost) {
      SMTPHost = newSMTPHost;
      if (SMTPHost != null && SMTPHost.length() == 0) SMTPHost = null;
      return this;
   }

   /**
    * Address messages are from.
    * @see #getFromAddress()
    * @see #setFromAddress(String)
    */
   protected String fromAddress;
   /**
    * Getter for {@link #fromAddress}: Address messages are from.
    * @return Address messages are from.
    */
   public String getFromAddress() { return fromAddress; }
   /**
    * Setter for {@link #fromAddress}: Address messages are from.
    * @param newFromAddress Address messages are from.
    */
   public EmailService setFromAddress(String newFromAddress) {
      fromAddress = newFromAddress;
      if (fromAddress != null && fromAddress.length() == 0) fromAddress = null;
      return this;
   }
   
   // Methods:
   
   /**
    * Default constructor.
    */
   public EmailService() {
   } // end of constructor
   
   /**
    * Sends an email.
    * @param to Email address of recipient. Multiple recipients must be delimited by ';'.
    * @param subject Subject line.
    * @param message Message body.
    * @param html Whether the message is HTML (true) or plain text (false).
    */
   public void send(String to, String subject, String message, boolean html)
      throws javax.mail.internet.AddressException, javax.mail.MessagingException {
      send(fromAddress, to, subject, message, html, null);
   }

   /**
    * Sends an email.
    * @param to Email address of recipient. Multiple recipients must be delimited by ';'.
    * @param subject Subject line.
    * @param message Message body.
    * @param html Whether the message is HTML (true) or plain text (false).
    */
   public void send(String to, String subject, String message)
      throws javax.mail.internet.AddressException, javax.mail.MessagingException {
      send(fromAddress, to, subject, message, false, null);
   }

   /**
    * Sends an email.
    * @param to Email address of recipient. Multiple recipients must be delimited by ';'.
    * @param subject Subject line.
    * @param message Message body.
    * @param html Whether the message is HTML (true) or plain text (false).
    * @param fAttachment A file to attach, or null.
    */
   public void send(String to, String subject, String message, File fAttachment)
      throws javax.mail.internet.AddressException, javax.mail.MessagingException {
      send(fromAddress, to, subject, message, false, fAttachment);
   }
   
   /**
    * Sends an email.
    * @param to Email address of recipient. Multiple recipients must be delimited by ';'.
    * @param subject Subject line.
    * @param message Message body.
    * @param html Whether the message is HTML (true) or plain text (false).
    * @param fAttachment A file to attach, or null.
    */
   public void send(String to, String subject, String message, boolean html, File fAttachment)
      throws javax.mail.internet.AddressException, javax.mail.MessagingException {
      send(fromAddress, to, subject, message, html, fAttachment);
   }
   
   /**
    * Sends an email.
    * @param from Email address, or null to default to correctionsEmail system attribute.
    * @param to Email address of recipient. Multiple recipients must be delimited by ';'.
    * @param subject Subject line.
    * @param message Message body.
    * @param html Whether the message is HTML (true) or plain text (false).
    * @param fAttachment A file to attach, or null.
    */
   public void send(String from, String to, String subject, String message, boolean html, File fAttachment)
      throws javax.mail.internet.AddressException, javax.mail.MessagingException {
      
      // Set the host smtp address
      Properties props = new Properties();
      props.put("mail.smtp.host", SMTPHost);
      
      if (SMTPUser != null) {
         if (SMTPPassword != null) { // password is set too
            props.put("mail.smtp.auth", "true");
         } else { // no password
            // treat SMTPUser as the sending host instead
            // otherwise, set mail.smtp.localhost
            props.put("mail.smtp.localhost", SMTPUser);
         }
         props.put("mail.smtp.starttls.enable","true");	 
         props.put("mail.smtp.EnableSSL.enable","true");
	 
         props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");   
         props.setProperty("mail.smtp.socketFactory.fallback", "false");   
         props.setProperty("mail.smtp.port", "465");   
         props.setProperty("mail.smtp.socketFactory.port", "465");
      }
      
      // create some properties and get the default Session
      Session session = Session.getDefaultInstance(props, null);
      
      // create a message
      Message msg = new MimeMessage(session);
      
      // set the from and to address
      if (from == null) from = fromAddress;
      InternetAddress addressFrom = new InternetAddress(from);
      msg.setFrom(addressFrom);
      
      for (String recipient : to.split(";")) {
         InternetAddress addressTo = new InternetAddress(recipient); 
         msg.addRecipient(Message.RecipientType.TO, addressTo);
      } // next recipient
      
      // Setting the Subject and Content Type
      msg.setSubject(subject);
      if (fAttachment == null) {
         msg.setContent(message, "text/" + (html?"html":"plain") + "; charset=UTF-8");
      } else { // there's an attachment
         // Create the message part 
         BodyPart messageBodyPart = new MimeBodyPart();
	 
         // Fill the message
         messageBodyPart.setContent(message, "text/" + (html?"html":"plain") + "; charset=UTF-8");
         
         // Create a multipart message
         Multipart multipart = new MimeMultipart();
	 
         // Set text message part
         multipart.addBodyPart(messageBodyPart);
	 
         // Part two is attachment
         messageBodyPart = new MimeBodyPart();
         String filename = fAttachment.getPath();
         DataSource source = new FileDataSource(filename);
         messageBodyPart.setDataHandler(new DataHandler(source));
         messageBodyPart.setFileName(fAttachment.getName());
         multipart.addBodyPart(messageBodyPart);
         
         // Send the complete message parts
         msg.setContent(multipart);
      } // attachment
      
      if (SMTPUser != null && SMTPPassword != null) {
         try {
            Transport tr = session.getTransport("smtp");
            tr.connect(SMTPHost, SMTPUser, SMTPPassword);
            msg.saveChanges();
            tr.sendMessage(msg, msg.getAllRecipients());
            tr.close();
         } catch(javax.mail.MessagingException t) {
            System.err.println("send: " + t);
            log("EmailService: " + t);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            System.err.println(sw.toString());
            log(sw.toString());
            throw t;
         } catch(Throwable t) {
            System.err.println("EmailService: " + t);
            log("EmailService: " + t);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            System.err.println(sw.toString());
            log(sw.toString());
         }
      } else {
         Transport.send(msg);
      }
   } // end of send()
   
   /**
    * Print a log message
    * @param message
    */
   public void log(String message) {
      if (context != null) context.log(message);
   } // end of log()

} // end of class EmailService
