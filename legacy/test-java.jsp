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
      pg.sendRedirect(".");
      return;
   }

   if (!"r".equals(sMode))
   {
      // create test instance
      PreparedStatement sql = pg.getConnection().prepareStatement(
	 "INSERT INTO "+pg.getModule()+"_instance (user_agent,ip,start_time,language)"
	 +" VALUES (?,?,Now(),?)");
      sql.setString(1, request.getHeader("User-Agent"));
      sql.setString(2, request.getRemoteHost());
      sql.setString(3, ""+session.getAttribute("language"));
      sql.executeUpdate();
      sql.close();
      sql = pg.getConnection().prepareStatement(
	 "SELECT LAST_INSERT_ID()");
      ResultSet rs = sql.executeQuery();
      rs.next();
      int iInstanceId = rs.getInt(1);
      pg.set("InstanceId", new Integer(iInstanceId));
      rs.close();
      sql.close();
      
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
   else // mode = r
   {
      pg.set("InstanceId", request.getParameter("i"));
   }

   File fJar = new File(pg.getModule().getRealPath(pg), "jar");
   fJar = new File(fJar, "digittripletstest"+sMode+".jar");
   pg.set("jarVersion", ""+fJar.lastModified());
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
}
%>