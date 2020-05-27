<%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ page import = "java.io.*" 
%><%@ page import = "java.sql.*" 
%><%@ page import = "java.util.*" 
%><%
{
   Page pg = (Page) request.getAttribute("page");
   nz.net.fromont.hexagon.Module module = pg.getModule();
   pg.setTitle(pg.getModule().getName());

   String sMode = request.getParameter("m");
   if (sMode == null) 
   {
      pg.sendRedirect("/" + module + "/");
      return;
   }

   // TrialSets is a semicolon-separated list of comma-separated lists of trials
   StringTokenizer stTrialSets = new StringTokenizer(pg.localize("TrialSets"), ";");
   Vector<Vector<String>> vTrialSets = new Vector<Vector<String>>();
   while (stTrialSets.hasMoreTokens())
   {
      StringTokenizer stTrialSet = new StringTokenizer(stTrialSets.nextToken(), ",");
      Vector<String> vTrialSet = new Vector<String>();
      while (stTrialSet.hasMoreTokens())
      {
	 vTrialSet.add(stTrialSet.nextToken());
      } // next trial
      vTrialSets.add(vTrialSet);
   } // next trial set
   
   // randomly pick a trial set
   Random random = new Random();
   int iTrialSetNumber = random.nextInt(vTrialSets.size());
   Vector<String> vTrialSet = vTrialSets.elementAt(iTrialSetNumber);
   pg.set("trialSetNumber", iTrialSetNumber);
   pg.set("numTrials", vTrialSet.size());
   
   // create test instance
   PreparedStatement sql = pg.getConnection().prepareStatement(
      "INSERT INTO "+pg.getModule()+"_instance (user_agent,ip,start_time,language,mode,trial_set_number)"
      +" VALUES (?,?,Now(),?,?,?)");
   sql.setString(1, request.getHeader("User-Agent"));
   sql.setString(2, request.getRemoteHost());
   sql.setString(3, ""+session.getAttribute("language"));
   sql.setString(4, sMode);
   sql.setInt(5, iTrialSetNumber);
   sql.executeUpdate();
   sql.close();
   sql = pg.getConnection().prepareStatement(
      "SELECT LAST_INSERT_ID()");
   ResultSet rs = sql.executeQuery();
   rs.next();
   int iInstanceId = rs.getInt(1);
   pg.set("instance_id", new Integer(iInstanceId));
   rs.close();
   sql.close();
   
   // create trials
   sql = pg.getConnection().prepareStatement(
      "INSERT INTO "+pg.getModule()+"_trial (instance_id,trial_number,correct_answer)"
      +" VALUES (?,?,?)");
   sql.setInt(1, iInstanceId);
   int iTrial = 0;
   while (vTrialSet.size() > 0) 
   {
      // pick a random element
      int iRandomElement = random.nextInt(vTrialSet.size());
      String sTrial = vTrialSet.elementAt(iRandomElement);
      
      // remove the element so it won't be reused
      vTrialSet.removeElementAt(iRandomElement);
      
      // insert it in the list
      sql.setInt(2, iTrial++);
      sql.setString(3, sTrial);	 
      sql.executeUpdate();
      
   } // next trial
   sql.close();

   if (request.getParameter("i") == null)
   {
      // form answers
      sql = pg.prepareStatement(
	 "SELECT * FROM "+module+"_form_field"
	 +" ORDER BY display_order"); 
      rs = sql.executeQuery();	
      Vector vFields = Page.HashtableCollectionFromResultSet(rs);
      pg.set("fields", vFields);
      
      String sMessage = "";
      // validate fields (in case client-side javascript is turned off)
      Enumeration enFields = vFields.elements();
      boolean bAllValid = true;
      while (enFields.hasMoreElements())
      {
	 Hashtable ht = (Hashtable) enFields.nextElement();
	 String sValue = request.getParameter((String)ht.get("field"));
	 
	 if (ht.get("required").toString().equals("1"))
	 {
	    if (sValue == null || sValue.trim().length() == 0)
	    {
	       pg.addError(
		  pg.localize("Please provide a value for ") 
		  + ht.get("name"));
	       bAllValid = false;
	    }
	 }
	 if (ht.get("type").equals("email"))
	 {
	    if (sValue.indexOf('@') < 0)
	    {
	       pg.addError(
		  pg.localize("Please enter an email address for ") 
		  + ht.get("name"));
	       bAllValid = false;
	    }
	 }
	 if (ht.get("type").equals("number"))
	 {
	    if (
	       ht.get("required").toString().equals("1")
	       || sValue.length()  > 0)
	       try
	       {
		  sValue = "" + Integer.parseInt(sValue);
	       }
	       catch(NumberFormatException exception)
	       {
		  try
		  {
		     sValue = "" + Double.parseDouble(sValue);
		  }
		  catch(NumberFormatException exception2)
		  {
		     pg.addError(
			pg.localize("Please enter a number for ") 
			+ ht.get("name"));
		     bAllValid = false;
		  }
	       }
	 }
	 ht.put("value", sValue);
	 
	 sMessage += "" + ht.get("field") + ": " + sValue + "\n";
	 
      } // next field   
      sql.close();
      rs.close();
   
      //if (bAllValid)
      {
	 // save the values
	 enFields = vFields.elements();
	 sql = pg.prepareStatement(
	    "INSERT INTO "+module+"_instance_field (instance_id, field, value)"
	    +" VALUES (?,?,?)"); 
	 sql.setInt(1, iInstanceId);
	 while (enFields.hasMoreElements())
	 {
	    Hashtable ht = (Hashtable) enFields.nextElement();
	    sql.setString(2, (String)ht.get("field"));
	    sql.setString(3, request.getParameter((String)ht.get("field")));
	    sql.executeUpdate();
	 } // next field
	 rs.close();
	 sql.close();
      }	 
   }
   else // based on another instance
   {
      int iOtherInstanceId = Integer.parseInt(request.getParameter("i"));
      sql = pg.getConnection().prepareStatement(
	 "UPDATE "+pg.getModule()+"_instance"
	 +" SET other_instance_id = ?"
	 +" WHERE instance_id = ?");
      sql.setInt(1, iOtherInstanceId);
      sql.setInt(2, iInstanceId);
      sql.executeUpdate();
      sql.close();
      
      // copy field values
      sql = pg.getConnection().prepareStatement(
	 "INSERT INTO "+pg.getModule()+"_instance_field"
	 +" (instance_id, field, value)"
	 +" SELECT ?, field, value FROM "+pg.getModule()+"_instance_field"
	 +" WHERE instance_id = ?");
      sql.setInt(1, iInstanceId);
      sql.setInt(2, iOtherInstanceId);
      sql.executeUpdate();
      sql.close();
   }

   pg.set("Mode", sMode);

   String sHtml = "";
   File fModulesDir 
   = new File(getServletContext().getRealPath(JSite.MODULES_ROOT));
   File fModuleDir = new File(fModulesDir, module.getModuleRoot());
   File file = new File(fModuleDir, "test"+sMode+".html");
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
   pg.set("htmlContent", sHtml, false);
   pg.set("volumeCheckFile", pg.localize("volumecheckfile" + sMode));
}
%>
