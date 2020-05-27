<%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ page import = "java.io.*" 
%><%@ page import = "java.sql.*" 
%><%@ page import = "java.util.*" 
%><%@ page import = "java.text.*" 
%><%
{
   // receives an answer, stores it, and returns the audio for the next trial
   Page pg = (Page) request.getAttribute("page");
   nz.net.fromont.hexagon.Module module = pg.getModule();

   File dirModuleRoot = Module.GetRealPath(pg);
   File dirMedia = new File(dirModuleRoot, "mp3");

   String sInstanceId = request.getParameter("i");
   String sAnswer = request.getParameter("a");
   if (sAnswer == null) sAnswer = "";

   PreparedStatement sql = pg.getConnection().prepareStatement(
      "SELECT mode FROM "+pg.getModule()+"_instance WHERE instance_id = ?");
   sql.setInt(1, Integer.parseInt(sInstanceId));
   ResultSet rs = sql.executeQuery();
   String sMode = "";
   try
   {
      if (!rs.next()) throw new Exception("Invalid test instance: " + sInstanceId);
      sMode = rs.getString("mode"); 
   }
   finally
   {
      rs.close();
      sql.close();
   }

   // get next unanswered trial
   int iAnswerTrialNumber = -2;
   try
   {
      sql = pg.getConnection().prepareStatement(
	 "SELECT MIN(trial_number) FROM "+pg.getModule()+"_trial"
	 +" WHERE instance_id = ? AND participant_answer IS NULL");
      sql.setInt(1, Integer.parseInt(sInstanceId));
      rs = sql.executeQuery();
      if (rs.next() && rs.getString(1) != null) iAnswerTrialNumber = rs.getInt(1);
   }
   finally
   {
      rs.close();
      sql.close();
   }

   File fPrompt = new File(dirMedia, "silence.mp3"); // silence by default
   sql = pg.getConnection().prepareStatement(
      "SELECT correct_answer, decibels_signal"
      +" FROM "+pg.getModule()+"_trial"
      +" WHERE instance_id = ? AND trial_number = ?");
   sql.setInt(1, Integer.parseInt(sInstanceId));
   sql.setInt(2, iAnswerTrialNumber);
   rs = sql.executeQuery();
   String sCorrectAnswer = "";
   int iDecibelsSignal = new Integer(pg.localize("signallevelstart"));
   try
   {
      if (rs.next())
      {
          sCorrectAnswer = rs.getString("correct_answer");
          iDecibelsSignal = rs.getInt("decibels_signal");
      }
   }
   finally
   {
      rs.close();
      sql.close();
   }
   
   int iNextTrialNumber = iAnswerTrialNumber + 1;
   if (iNextTrialNumber >= 0) // not silence
   {
      Integer iNextDecibelsSignal = new Integer(pg.localize("signallevelstart"));
      // if it's the first playback (first trial, no answer posted)
      if (iAnswerTrialNumber == 0
	  && request.getParameter("a") == null)
      {
	 iNextTrialNumber = 0; // play the first prompt
      }
      else
      { // after first trial prompt
	 // Safari sends two requests, the first for range "bytes=0-1", and then the rest
	 // so if we're on the first of those, don't record anything, 
	 // so that the second request works correctly
	 if (!"bytes=0-1".equals(request.getHeader("range"))) 
	 {
	    // register their answer
	    sql = pg.getConnection().prepareStatement(
	       "UPDATE "+pg.getModule()+"_trial SET participant_answer = ?"
	       +" WHERE instance_id = ? AND trial_number = ?");
	    try
	    {
	       sql.setString(1, sAnswer);
	       sql.setInt(2, Integer.parseInt(sInstanceId));
	       sql.setInt(3, iAnswerTrialNumber);
	       sql.executeUpdate();
	    }
	    finally
	    {
	       sql.close();
	    }
	 } // recording answer
	 
	 // decide which prompt to play next
	 int iSignalLevelIncrement = new Integer(pg.localize("signallevelincrement"));
	 if (sAnswer.equals(sCorrectAnswer))
	 {   
	    iNextDecibelsSignal = iDecibelsSignal + iSignalLevelIncrement;
	 }
	 else
	 {
	    iNextDecibelsSignal = iDecibelsSignal - iSignalLevelIncrement;
	 }
	 int iSignalLevelMinimum = new Integer(pg.localize("signallevelminimum"));
	 if (iNextDecibelsSignal < iSignalLevelMinimum) iNextDecibelsSignal = iSignalLevelMinimum;
	 int iSignalLevelMaximum = new Integer(pg.localize("signallevelmaximum"));
	 if (iNextDecibelsSignal > iSignalLevelMaximum) iNextDecibelsSignal = iSignalLevelMaximum;
      } // given an answer
      
      // record decibel level
      sql = pg.getConnection().prepareStatement(
	 "UPDATE "+pg.getModule()+"_trial SET decibels_signal = ?"
	 +" WHERE instance_id = ? AND trial_number = ?");
      try
      {
	 sql.setInt(1, iNextDecibelsSignal);
	 sql.setInt(2, Integer.parseInt(sInstanceId));
	 sql.setInt(3, iNextTrialNumber);
	 sql.executeUpdate();
      }
      finally
      {
	 sql.close();
      }
      
      // get next triplet
      sql = pg.getConnection().prepareStatement(
	 "SELECT correct_answer FROM "+pg.getModule()+"_trial"
	 +" WHERE instance_id = ? AND trial_number = ?");
      sql.setInt(1, Integer.parseInt(sInstanceId));
      sql.setInt(2, iNextTrialNumber);
      rs = sql.executeQuery();
      if (rs.next())
      {
	 String sNextTriplet = rs.getString("correct_answer");
	 
	 // return media
	 File dirMode = new File(dirMedia, pg.localize("mediadirectory"+sMode));
	 MessageFormat fmtFileName = new MessageFormat(pg.localize("filenameformat"+sMode));
	 Object[] oArgs = {sNextTriplet, iNextDecibelsSignal};
	 fPrompt = new File(dirMode, fmtFileName.format(oArgs));
      }
      rs.close();
      sql.close();
   } // not silence
   
   if (!fPrompt.exists())
   {
      System.out.println("TRIAL not found: " + fPrompt.getPath());
      throw new Exception("Sorry, media file not found.");
   }
 
   pg.setTemplatePage(null); // don't wrap the template around this page
   pg.setContentType("audio/mpeg");
   pg.set("prompt", fPrompt);
pg.addResponseHeader("Pragma", "no-cache"); 
pg.addResponseHeader("Cache-Control", "no-cache");

}
// gotcha! make sure there's no blank line after this one %>