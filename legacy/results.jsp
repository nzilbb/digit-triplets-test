<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%><%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ page import = "java.io.*" 
%><%@ page import = "java.sql.*" 
%><%@ page import = "java.util.*" 
%><%
{
   int RESULT_INCOMPLETE = -2;
   int RESULT_POOR = -1;
   int RESULT_INCONCLUSIVE = 0;
   int RESULT_NORMAL = 1;
   Page pg = (Page) request.getAttribute("page");
   nz.net.fromont.hexagon.Module module = pg.getModule();
   pg.setTitle(pg.getModule().getName());
  
   String sInstanceId = request.getParameter("instance_id");
   if (sInstanceId == null) 
   {
      pg.sendRedirect(".");
      return;
   }
   String sLastInput = request.getParameter("LastInput");
   int iInstanceId = Integer.parseInt(sInstanceId);
   int iOtherInstanceId = iInstanceId;
   String sLanguage = ""+session.getAttribute("language");
   Vector<String> vResultSuffixes = new Vector<String>();


   // record last input
   PreparedStatement sql = pg.getConnection().prepareStatement(
      "SELECT MAX(trial_number) FROM "+pg.getModule()+"_trial WHERE instance_id = ?");
   sql.setInt(1, iInstanceId);
   ResultSet rs = sql.executeQuery();
   rs.next();
   int iLastTrial = rs.getInt(1);
   rs.close();
   sql.close();
   sql = pg.getConnection().prepareStatement(
      "UPDATE "+pg.getModule()+"_trial SET participant_answer = ?"
      +" WHERE instance_id = ? AND trial_number = ?");
   sql.setString(1, sLastInput);
   sql.setInt(2, iInstanceId);
   sql.setInt(3, iLastTrial);
   sql.executeUpdate();
   sql.close();

   // compute mean SNR, ignoring first few trials
   sql = pg.getConnection().prepareStatement(
      "SELECT AVG(decibels_signal) FROM "+pg.getModule()+"_trial"
      +" WHERE instance_id = ? AND trial_number >= ?");
   sql.setInt(1, iInstanceId);
   sql.setInt(2, Integer.parseInt(pg.localize("ignoretrials")));
   rs = sql.executeQuery();
   rs.next();
   double dMeanSNR = rs.getDouble(1);

   int iTestResult = RESULT_INCOMPLETE;
   if (dMeanSNR > Double.parseDouble(pg.localize("snrpoormin"))) iTestResult = RESULT_POOR;
   else if (dMeanSNR < Double.parseDouble(pg.localize("snrnormalmax"))) iTestResult = RESULT_NORMAL;
   else iTestResult = RESULT_INCONCLUSIVE;

   // check whether the instance already has a stop time
   // (i.e. the user hit the back button to run the test again)
   sql = pg.getConnection().prepareStatement(
      "SELECT other_instance_id FROM "+pg.getModule()+"_instance"
      +" WHERE instance_id = ? AND other_instance_id IS NOT NULL");
   sql.setInt(1, iInstanceId);
   rs = sql.executeQuery();
   if (rs.next())
   { // instance already finished
      iOtherInstanceId = rs.getInt(1);
   } // instance already finished
   rs.close();
   sql.close();

   sql = pg.getConnection().prepareStatement(
      "UPDATE "+pg.getModule()+"_instance"
      +" SET end_time = Now(), test_result = ?, mean_snr = ?"
      +" WHERE instance_id = ?");
   sql.setInt(1, iTestResult);
   sql.setDouble(2, dMeanSNR);
   sql.setInt(3, iInstanceId);
   sql.executeUpdate();
   sql.close();

   sql = pg.getConnection().prepareStatement(
      "SELECT language, mode, trial_set_number FROM "+pg.getModule()+"_instance"
      +" WHERE instance_id = ?");
   sql.setInt(1, iInstanceId);
   rs = sql.executeQuery();
   rs.next();
   String sMode = rs.getString("mode");

   String sMessage = "";
   sMessage += "InstanceId: " + iInstanceId;
   sMessage += "\nLanguage: " + rs.getString("language");
   sMessage += "\nMode: " + sMode;
   sMessage += "\nTrialSetNumber: " + rs.getString("trial_set_number");
   sMessage += "\nTestResult: " + iTestResult;
   sMessage += "\nMeanSNR: " + dMeanSNR;

   sql = pg.prepareStatement(
      "SELECT i.* FROM "+module+"_form_field f"
      +" INNER JOIN "+module+"_instance_field i ON f.field = i.field"
      +" WHERE i.instance_id = ?"
      +" ORDER BY f.display_order"); 
   sql.setInt(1, iInstanceId);
   rs = sql.executeQuery();
   while (rs.next())
   {
      sMessage += "\n" + rs.getString("field") 
	 + ": " + rs.getString("value");
   } // next field
   rs.close();
   sql.close();

//   sMessage += "\nTrialsCsv:\n" + sTrialsCsv;
   sMessage += "\nTrialsCsv: " + request.getSession().getAttribute("baseUrl") + "/" + module 
   + "/admin/trials?instance_id="+iInstanceId;

   // attach CSV file to message
   sql = pg.getConnection().prepareStatement(
      "SELECT * FROM "+pg.getModule()+"_trial"
      +" WHERE instance_id = ? ORDER BY trial_number");
   sql.setInt(1, Integer.parseInt(request.getParameter("instance_id")));
   rs = sql.executeQuery();
   File f = File.createTempFile("trials"+sMode+"_" + iInstanceId + "_", ".csv");
   PrintWriter fOut = new PrintWriter(f);
   fOut.println("\"Number\",\"Signal\",\"Triplet\",\"Entered\",\"Result\"");
   while (rs.next())
   {
      fOut.println(
	 rs.getString("trial_number") + ","
	 + rs.getString("decibels_signal") + ","
	 + "\"" + rs.getString("correct_answer") + "\","
	 + "\"" + rs.getString("participant_answer") + "\","
	 + rs.getString("correct_answer").equals(rs.getString("participant_answer"))
	 );
   }
   fOut.close();
   rs.close();
   sql.close();

   String sEmail = module.getProperties().getProperty("email");
   if (sEmail != null && sEmail.length() > 0)
   {
      try
      {
	 pg.getSite().sendEmail(sEmail, "DTT " + iInstanceId, sMessage, f);
      }
      catch(Exception exception)
      {
	 pg.set("mailError", exception.toString());
      }
   }

   vResultSuffixes.add(sMode + iTestResult);
   if (iInstanceId != iOtherInstanceId) // two tests were run
   {
      // display other result too
      sql = pg.prepareStatement(
	 "SELECT CONCAT(mode, test_result) FROM "+module+"_instance"
	 +" WHERE instance_id = ?"); 
      sql.setInt(1, iOtherInstanceId);
      rs = sql.executeQuery();
      if (rs.next())
      {
	 vResultSuffixes.insertElementAt(rs.getString(1), 0);
      } // next field
      rs.close();
      sql.close();
   }

   String sHtml = "";
   File fModulesDir 
   = new File(getServletContext().getRealPath(JSite.MODULES_ROOT));
   File fModuleDir = new File(fModulesDir, module.getModuleRoot());
   for (String sSuffix : vResultSuffixes)
   {
      File file = new File(fModuleDir, "result"+sSuffix+".html");
      try
      {
	 BufferedReader reader = new BufferedReader(new FileReader(file));
	 String sLine = reader.readLine();
	 while (sLine != null)
	 {
	    sHtml += sLine;
	    sLine = reader.readLine();
	 } // next line
	 reader.close();
      }
      catch(Exception exception) {}
   } // next suffix
   pg.set("htmlContent", sHtml, false);
   pg.set("suffixes", vResultSuffixes);

   if (sMode.equals("l"))
   {
      pg.sendRedirect("test?m=r&i="+iInstanceId);
   }
}
%>