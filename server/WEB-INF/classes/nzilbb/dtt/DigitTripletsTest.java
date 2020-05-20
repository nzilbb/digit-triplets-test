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
package nzilbb.dtt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nzilbb.webapp.EmailService;
import nzilbb.webapp.ServletBase;

/**
 * Servlet that manages instances of the digit triplets test.
 * <p>
 * The cycle of requests for a test run is:
 * <ol>
 *  <li> POST to <tt>/test</tt> to generate get a new test <q>id</q> and
 *        <q>numTrials</q>. </li>  
 *  <li> POST to <tt>/test/<i>{id}</i></tt> to set a field value as entered by the
 *        participant. </li>
 *  <li> GET from <tt>/test/<i>{id}</i></tt> get the audio of the first trial </li>
 *  <li> GET from <tt>/test/<i>{id}</i>?a=<i>{answer}</i></tt> to record the
 *        answer for the previous trial, and get the audio for the next. </li>
 *  <li> Repeat the above until <i>{numTrials}</i> trials have been completed.</li>
 *  <li> GET from <tt>/test/<i>{id}</i>/result</i> to get the ID of the result
 *        text to display, and the next test mode, if any.</li>
 * </li>
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/test/*",
   loadOnStartup = 30)
public class DigitTripletsTest extends ServletBase {

   Random random = new Random();
   MessageFormat nameFormat = new MessageFormat(
      "{0}_{1,choice,-1#{1,number,00}|0#{1,number,000}}{2}.mp3");

   /**
    * POST handler - add a new instance, or a new field value.
    * <p>Post to <tt>/test</tt> to generate a new test instance. The response is a JSON
    * object with the following attributes:
    * <ul>
    *  <li><q>id</q> : used to form subsequent request URLs.</li>
    *  <li><q>numTrials</q> : the number of trials for this instance.</li>
    *  <li><q>mode</q> : the mode for the test.</li>
    * </ul>
    * <p>Post to <tt>/test/<i>{id}</i></tt> to set a field value as entered by the
    * participant. The request body should be a JSON object with the following attributes:
    * <ul>
    *  <li><q>field</q> : the name of the field to set the value of.</li>
    *  <li><q>value</q> : the value to store.</li>
    * </ul> 
    * In both cases, a response status of 200 indicates success. Any other status
    * indicates an error, and a JSON object with a <q>message</q> attribute indicates the reason.
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      
      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         try {
            if (request.getPathInfo() == null) { // create an instance
               createInstance(request, response);
            } else { // add something to an existing trial
               // PathInfo something like /123e4567-e89b-12d3-a456-426652340000
               String instanceId = request.getPathInfo().replaceAll("^/","");
               createFieldValue(request, response, instanceId);
            }
         } catch (SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("ERROR: " + exception.getMessage());
         }
      }
   }

   /** Creates a new test instance */
   protected void createInstance(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {

      Connection connection = db.newConnection();
      try {
       
         // read the incoming object
         JsonReader reader = Json.createReader(request.getReader());
         JsonObject json = reader.readObject();
         
         if (!json.containsKey("mode")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnMessage("No mode provided.", response);
            return;
         }
         String mode = json.getString("mode");
         
         String otherInstanceId = json.containsKey("otherInstanceId")
            ?json.getString("otherInstanceId"):null;      
         
         // pick a trial set at random
         PreparedStatement sql = connection.prepareStatement("SELECT COUNT(*) FROM trial_set");
         ResultSet rs = sql.executeQuery();
         rs.next();
         int trialSetCount = rs.getInt(1);
         rs.close();
         sql.close();
         sql = connection.prepareStatement("SELECT * FROM trial_set LIMIT ?, 1");      
         sql.setInt(1, random.nextInt(trialSetCount));
         rs = sql.executeQuery();
         rs.next();
         int trialSetId = rs.getInt("id");
         String trialsString = rs.getString("trials"); // a comma-separated list
         String[] trialsArray = trialsString.split(",");
         rs.close();
         sql.close();
         
         // shuffle the trials
         List<String> trials = Arrays.asList(trialsArray);
         Collections.shuffle(trials);
         
         // create an instance
         String instanceId = UUID.randomUUID().toString();
         sql = connection.prepareStatement(
            "INSERT INTO instance (instance_id,user_agent,ip,start_time,mode,trial_set_id)"
            +" VALUES (?,?,?,Now(),?,?)");
         sql.setString(1, instanceId);
         sql.setString(2, request.getHeader("User-Agent"));
         sql.setString(3, request.getRemoteHost());
         sql.setString(4, mode);
         sql.setInt(5, trialSetId);
         sql.executeUpdate();
         sql.close();
         
         // create the trial records
         sql = connection.prepareStatement(
            "INSERT INTO trial (instance_id,trial_number,correct_answer) VALUES (?,?,?)");
         sql.setString(1, instanceId);
         int iTrial = 0;
         for (String digits : trials) {      
            // insert it in the list
            sql.setInt(2, iTrial++);
            sql.setString(3, digits);	 
            sql.executeUpdate();
         } // next trial
         sql.close();
         
         // copy details from the other instance? (i.e. mode=r when before mode=l)
         if (otherInstanceId != null) {
            sql = connection.prepareStatement(
               "UPDATE instance SET other_instance_id = ? WHERE instance_id = ?");
            sql.setString(1, otherInstanceId);
            sql.setString(2, instanceId);
            sql.executeUpdate();
            sql.close();
            
            // copy field values
            sql = connection.prepareStatement(
               "INSERT INTO instance_field (instance_id, field, value)"
               +" SELECT ?, field, value FROM instance_field"
               +" WHERE instance_id = ?");
            sql.setString(1, instanceId);
            sql.setString(2, otherInstanceId);
            sql.executeUpdate();
            sql.close();
         } // otherInstanceId is specified
         
         JsonGenerator jsonResponse = Json.createGenerator(response.getWriter());
         jsonResponse.writeStartObject();
         jsonResponse.write("id", instanceId);
         jsonResponse.write("mode", mode);
         jsonResponse.write("numTrials", trials.size());
         jsonResponse.writeEnd();
         jsonResponse.close();
      } catch (Throwable t) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + t.getMessage(), response);
            log("ERROR: " + t.getMessage());
      } finally {
         connection.close();
      }
   }

   /** Records a meta-data field value */
   protected void createFieldValue(HttpServletRequest request, HttpServletResponse response, String instanceId)
      throws ServletException, IOException, SQLException {

      Connection connection = db.newConnection();
      try {
         // read the incoming object
         JsonReader reader = Json.createReader(request.getReader());
         JsonObject json = reader.readObject();
         
         if (!json.containsKey("field")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnMessage("No field provided.", response);
            return;
         }
         String field = json.getString("field");
         
         if (!json.containsKey("value")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnMessage("No value provided.", response);
            return;
         }
         String value = json.getString("value"); // TODO should validate incoming values
         
         PreparedStatement sql = connection.prepareStatement(
            "REPLACE INTO instance_field (instance_id, field, value) VALUES (?,?,?)"); 
         sql.setString(1, instanceId);
         sql.setString(2, field);
         sql.setString(3, value);
         int rowCount = sql.executeUpdate();
         sql.close();
         
         if (rowCount == 0) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            returnMessage("Instance doesn't exist: " + instanceId, response);
         } else {
            response.setStatus(HttpServletResponse.SC_OK);
         }
      } catch (Throwable t) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + t.getMessage(), response);
            log("ERROR: " + t.getMessage());
      } finally {
         connection.close();
      }
   }
   
   /**
    * GET handler for trial audio and test results. 
    * <p> GET from <tt>/test/<i>{id}</i>?a=<i>{answer}</i></tt> to record the
    * answer for the previous trial, and get the audio for the next.
    * <p> If the answer parameter <q>a</q> is not present, and no previous answers have
    * been submitted, audio for the first trial in the test instance is returned.
    * <p> Or GET from <tt>/test/<i>{id}/result</i></tt> to get the test result for
    * the instance. 
    * <p> In this case the result body is a JSON object with the following attributes:
    * <ul>
    *  <li><q>textId</q> : The ID of the text to present to the participant.</li>
    *  <li><q>mode</q> : The mode to use in the next test, if any (i.e. after mode
    *       <q>l</q> for the left ear, the participant will do the text again with mode
    *       <q>r</q> for the right ear.</li>
    * </ul>
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      
      if (db == null || db.getVersion() == null // not installed yet
          || request.getPathInfo() == null) { // no instanceId
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         try {

            // PathInfo something like /123e4567-e89b-12d3-a456-426652340000
            String instanceId = request.getPathInfo().replaceAll("^/","");
            
            if (instanceId.endsWith("result")) {
               instanceId = instanceId.replaceAll("/.*","");
               getResult(request, response, instanceId);
            } else {
               getTrial(request, response, instanceId);
            }
         } catch (SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log("ERROR: " + exception.getMessage());
         }
      }
   }

   /** Record last trial answer and get next trial audio */
   protected void getTrial(HttpServletRequest request, HttpServletResponse response, String instanceId)
      throws ServletException, IOException, SQLException {
      log("getTrial: " + instanceId);
      
      // GET returnd mp3
      response.setContentType("audio/mpeg");
      // no caching please
      response.addHeader("Pragma", "no-cache"); 
      response.addHeader("Cache-Control", "no-cache");

      Connection connection = db.newConnection();
      try {
         String answer = request.getParameter("a");
         
         PreparedStatement sql = connection.prepareStatement(
            "SELECT mode FROM instance WHERE instance_id = ?");
         sql.setString(1, instanceId);
         ResultSet rs = sql.executeQuery();
         String mode = "";
         try {
            if (!rs.next()) {
               response.setStatus(HttpServletResponse.SC_NOT_FOUND);
               log("Instance doesn't exist: " + instanceId);
               return;
            }
            mode = rs.getString("mode"); 
         } finally {
            rs.close();
            sql.close();
         }
         
         int answerTrialNumber = -2;
         try {
            sql = connection.prepareStatement(
               "SELECT MIN(trial_number) FROM trial"
               +" WHERE instance_id = ? AND participant_answer IS NULL");
            sql.setString(1, instanceId);
            rs = sql.executeQuery();
            if (rs.next() && rs.getString(1) != null) answerTrialNumber = rs.getInt(1);
         } finally {
            rs.close();
            sql.close();
         }
         
         File dirMedia = new File(request.getServletContext().getRealPath("/mp3"));
         File fPrompt = new File(dirMedia, "silence.mp3"); // silence by default
         sql = connection.prepareStatement(
            "SELECT correct_answer, decibels_signal FROM trial"
            +" WHERE instance_id = ? AND trial_number = ?");
         sql.setString(1, instanceId);
         sql.setInt(2, answerTrialNumber);
         rs = sql.executeQuery();
         String correctAnswer = "";
         int decibelsSignal = getIntAttribute("signallevelstart", connection);
         try {
            if (rs.next()) {
               correctAnswer = rs.getString("correct_answer");
               decibelsSignal = rs.getInt("decibels_signal");
            }
         } finally {
            rs.close();
            sql.close();
         }   
         
         int nextTrialNumber = answerTrialNumber + 1;
         if (nextTrialNumber >= 0) {
            int nextDecibelsSignal = getIntAttribute("signallevelstart", connection);
            // if it's the first playback (first trial, no answer posted)
            if (answerTrialNumber == 0 && answer == null) {
               nextTrialNumber = 0; // play the first prompt
            } else { // after first trial prompt
               // Safari sends two requests, the first for range "bytes=0-1", and then the rest
               // so if we're on the first of those, don't record anything, 
               // so that the second request works correctly
               if (!"bytes=0-1".equals(request.getHeader("range"))) {
                  
                  // register their answer
                  sql = connection.prepareStatement(
                     "UPDATE trial SET participant_answer = ?"
                     +" WHERE instance_id = ? AND trial_number = ?");
                  try {
                     sql.setString(1, answer);
                     sql.setString(2, instanceId);
                     sql.setInt(3, answerTrialNumber);
                     sql.executeUpdate();
                  } finally {
                     sql.close();
                  }
               } // recording answer
               
               // decide which prompt to play next
               int signalLevelIncrement = getIntAttribute("signallevelincrement", connection);
               if (answer.equals(correctAnswer)) {
                  nextDecibelsSignal = decibelsSignal + signalLevelIncrement;
               } else {
                  nextDecibelsSignal = decibelsSignal - signalLevelIncrement;
               }
               int signalLevelMinimum = getIntAttribute("signallevelminimum", connection);
               if (nextDecibelsSignal < signalLevelMinimum) nextDecibelsSignal = signalLevelMinimum;
               int signalLevelMaximum = getIntAttribute("signallevelmaximum", connection);
               if (nextDecibelsSignal > signalLevelMaximum) nextDecibelsSignal = signalLevelMaximum;
            } // given an answer
            
            // record decibel level
            sql = connection.prepareStatement(
               "UPDATE trial SET decibels_signal = ?"
               +" WHERE instance_id = ? AND trial_number = ?");
            try {
               sql.setInt(1, nextDecibelsSignal);
               sql.setString(2, instanceId);
               sql.setInt(3, nextTrialNumber);
               sql.executeUpdate();
            } finally {
               sql.close();
            }
            
            // get next triplet
            sql = connection.prepareStatement(
               "SELECT correct_answer FROM trial WHERE instance_id = ? AND trial_number = ?");
            sql.setString(1, instanceId);
            sql.setInt(2, nextTrialNumber);
            rs = sql.executeQuery();
            if (rs.next()) {
               String nextTriplet = rs.getString("correct_answer");
               
               // return media
               File dirMode = new File(dirMedia, "dtt"+mode);
               Object[] args = { nextTriplet, nextDecibelsSignal, mode };
               String name = nameFormat.format(args);
               fPrompt = new File(dirMode, name);
            }
            rs.close();
            sql.close();
         } // not silence
         
         log("getTrial: prompt " + fPrompt.getPath());
         if (!fPrompt.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         } else {
            InputStream i = new FileInputStream(fPrompt);
            OutputStream o = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead = i.read(buffer);
            // safari asks for the first byte sometimes
            if ("bytes=0-1".equals(request.getHeader("range"))) {
               response.setHeader(
                  "Content-Range", "bytes 0-1/"+fPrompt.length());
               o.write(buffer, 0, 2);
            } else {
               response.setHeader(
                  "Content-Range", "bytes 0-"+fPrompt.length()+"/"+fPrompt.length());
               // send all the content
               while(bytesRead >= 0)
               {
                  o.write(buffer, 0, bytesRead);
                  bytesRead = i.read(buffer);
               } // next chunk of data
            }
            i.close();
         }
         
      } finally {
         connection.close();
      }
   }   

   /** Record last trial answer and get next trial audio */
   protected void getResult(HttpServletRequest request, HttpServletResponse response, String instanceId)
      throws ServletException, IOException, SQLException {

      Connection connection = db.newConnection();
      try {
         // get mode
         PreparedStatement sql = connection.prepareStatement(
            "SELECT mode, other_instance_id, trial_set_id FROM instance WHERE instance_id = ?");
         sql.setString(1, instanceId);
         ResultSet rs = sql.executeQuery();
         String mode = "";
         String otherInstanceId = null;
         int trialSetId = -1;
         try {
            if (!rs.next()) {
               response.setStatus(HttpServletResponse.SC_NOT_FOUND);
               log("Instance doesn't exist: " + instanceId);
               return;
            }
            mode = rs.getString("mode"); 
            otherInstanceId = rs.getString("other_instance_id");
            trialSetId = rs.getInt("trial_set_id");
         } finally {
            rs.close();
            sql.close();
         }

         // compute mean SNR, ignoring first few trials
         sql = connection.prepareStatement(
            "SELECT AVG(decibels_signal) FROM trial"
            +" WHERE instance_id = ? AND trial_number >= ?");
         sql.setString(1, instanceId);
         sql.setInt(2, getIntAttribute("ignoretrials", connection));
         rs = sql.executeQuery();
         rs.next();
         double dMeanSNR = rs.getDouble(1);
         
         int RESULT_INCOMPLETE = -2;
         int RESULT_POOR = -1;
         int RESULT_INCONCLUSIVE = 0;
         int RESULT_NORMAL = 1;
         
         int iTestResult = RESULT_INCOMPLETE;
         if (dMeanSNR > getDoubleAttribute("snrpoormin", connection)) iTestResult = RESULT_POOR;
         else if (dMeanSNR < getDoubleAttribute("snrnormalmax", connection)) iTestResult = RESULT_NORMAL;
         else iTestResult = RESULT_INCONCLUSIVE;
         
         sql = connection.prepareStatement(
            "UPDATE instance SET end_time = Now(), test_result = ?, mean_snr = ?"
            +" WHERE instance_id = ?");
         sql.setInt(1, iTestResult);
         sql.setDouble(2, dMeanSNR);
         sql.setString(3, instanceId);
         sql.executeUpdate();
         sql.close();
         
         // email results
         StringBuilder body = new StringBuilder();
         body.append("InstanceId: " + instanceId);
         body.append("\nMode: " + mode);
         body.append("\nTrialSetNumber: " + trialSetId);
         body.append("\nTestResult: " + iTestResult);
         body.append("\nMeanSNR: " + dMeanSNR);
         
         sql = connection.prepareStatement(
            "SELECT i.* FROM form_field f"
            +" INNER JOIN instance_field i ON f.field = i.field"
            +" WHERE i.instance_id = ?"
            +" ORDER BY f.display_order"); 
         sql.setString(1, instanceId);
         rs = sql.executeQuery();
         while (rs.next()) {
            body.append("\n" + rs.getString("field") + ": " + rs.getString("value"));
         } // next field
         rs.close();
         sql.close();

         // attach CSV file to message
         sql = connection.prepareStatement(
            "SELECT * FROM trial WHERE instance_id = ? ORDER BY trial_number");
         sql.setString(1, instanceId);
         rs = sql.executeQuery();
         final File f = File.createTempFile("trials"+mode+"_"+instanceId+"_", ".csv");
         PrintWriter fOut = new PrintWriter(f);
         fOut.println("\"trial_number\",\"decibels_signal\",\"correct_answer\",\"participant_answer\",\"correct\"");
         while (rs.next()) {
            fOut.println(
               rs.getString("trial_number") + ","
               + rs.getString("decibels_signal") + ","
               + "\"" + rs.getString("correct_answer") + "\","
               + "\"" + rs.getString("participant_answer") + "\","
               + rs.getString("correct_answer").equals(rs.getString("participant_answer")));
         }
         fOut.close();
         rs.close();
         sql.close();

         // email to all users with an email address
         sql = connection.prepareStatement(
            "SELECT GROUP_CONCAT(DISTINCT email SEPARATOR ';')"
            +" FROM user WHERE email LIKE '%@%'");
         rs = sql.executeQuery();
         rs.next();
         final String recipients = rs.getString(1);
         final String subject = "DTT " + instanceId;
         final String message = body.toString();
         if (recipients != null && recipients.length() > 0) {
            final EmailService emailService = getEmailService(connection);
            if (emailService != null) {
               // send email in its own thread, so sending delays don't make the participant wait
               new Thread(new Runnable() {
                     public void run() {
                        try {
                           log("Email to " + recipients + ": " + body);
                           emailService.send(recipients, subject, message, f);
                        } catch(Exception exception) {
                           log("ERROR cannot sent email: " + exception.toString());
                        } finally {
                           f.delete();
                        }
                     }
                  }).start();
            }
         }
         
         JsonGenerator jsonResponse = Json.createGenerator(response.getWriter());
         jsonResponse.writeStartObject();
         jsonResponse.write("textId", "result" + mode + iTestResult);
         if (mode.equals("l")) { // after left, do right
            jsonResponse.write("mode", "r");
         }
         jsonResponse.writeEnd();
         jsonResponse.close();
      } finally {
         connection.close();
      }
   }
   private static final long serialVersionUID = 1;
} // end of class DigitTripletsTest
