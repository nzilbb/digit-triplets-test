<%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ page import = "java.io.*" 
%><%@ page import = "java.sql.*" 
%><%@ page import = "java.util.*" 
%><%
{
   Page pg = (Page) request.getAttribute("page");
   nz.net.fromont.hexagon.Module module = pg.getModule();
   pg.setTitle(pg.getModule().getName());

   PreparedStatement sql = pg.prepareStatement(
      "SELECT * FROM "+module+"_form_field"
      +" ORDER BY display_order"); 
   ResultSet rs = sql.executeQuery();	
   Vector vFields = Page.HashtableCollectionFromResultSet(rs);
   pg.set("fields", vFields);
   
   sql.close();
   rs.close();
   sql = pg.prepareStatement(
      "SELECT * FROM "+module+"_form_field_option"
      +" WHERE field = ? ORDER BY display_order"); 

   // extra processing for each field...
   Enumeration enFields = vFields.elements();
   while (enFields.hasMoreElements())
   {
      Hashtable ht = (Hashtable) enFields.nextElement();
      
      // set options
      sql.setString(1, (String)ht.get("field"));
      rs.close();
      rs = sql.executeQuery();
      ht.put("options", Page.HashtableCollectionFromResultSet(rs));
      
      // set the size
      String sSize = (String)ht.get("size");
      if (sSize == null || sSize.length() == 0)
      {
	 sSize = "10";
      }
      else
      {
	 sSize = sSize.trim();
	 // look for non-numeric characters nxn or n n
	 String sFirstNumber = null;
	 String sSecondNumber = null;
	 StringTokenizer st = new StringTokenizer(sSize, " ,x");
	 sFirstNumber = st.nextToken();
	 
	 // if there are two numbers, it's probably a textarea...
	 try
	 {
	    sSecondNumber = st.nextToken();
	    ht.put("cols", sFirstNumber);
	    ht.put("rows", sSecondNumber);
	 }
	 catch(Exception exception)
	 {
	    sSize = sFirstNumber;
	 }
      } // check for two numbers
      ht.put("size", sSize); 
      
      // value?
      if (ht.get("value") == null)
      {
	 // if they've already submitted a value
	 if (request.getParameter((String)ht.get("field")) != null)
	 {
	    // that's the default value
	    ht.put("value", request.getParameter((String)ht.get("field")));
	 }
      }
   } // next field

   File fJar = new File(pg.getModule().getRealPath(pg), "jar");
   fJar = new File(fJar, "digittripletstest.jar");
   pg.set("jarVersion", ""+fJar.lastModified());

   String sHtml = "";
   File fModulesDir 
   = new File(getServletContext().getRealPath(JSite.MODULES_ROOT));
   File fModuleDir = new File(fModulesDir, module.getModuleRoot());
   File file = new File(fModuleDir, "introduction.html");
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
