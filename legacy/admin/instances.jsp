<%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ page import = "java.util.*" 
%><%@ page import = "java.sql.*" 
%><%@ page import = "java.text.*" 
%><%
{
   Page pg = (Page) request.getAttribute("page");
   nz.net.fromont.hexagon.Module module = pg.getModule();
   pg.setTitle("Test Instances");
  
   PreparedStatement sqlFields = pg.getConnection().prepareStatement(
      "SELECT * FROM "+pg.getModule()+"_instance_field"
      +" WHERE instance_id = ?");
   PreparedStatement sql = pg.getConnection().prepareStatement(
      "SELECT * FROM "+pg.getModule()+"_instance"
      +" ORDER BY start_time DESC, end_time DESC");
   ResultSet rs = sql.executeQuery();
   Vector vInstances = new Vector();
   pg.set("instances", vInstances);
   SimpleDateFormat dt = new SimpleDateFormat(
      "d MMM yyyy H:mma");
   while (rs.next())
   {
      Hashtable ht = Page.HashtableFromResultSet(rs);
      vInstances.add(ht);
      if (ht.get("start_time") != null)
      {
	 ht.put("start_time_formatted", 
		dt.format((java.util.Date)ht.get("start_time")));
      }
      if (ht.get("end_time") != null)
      {
	 ht.put("end_time_formatted", 
		dt.format((java.util.Date)ht.get("end_time")));
      }
      sqlFields.setInt(1, rs.getInt("instance_id"));
      ResultSet rsFields = sqlFields.executeQuery();
      Hashtable htFields = new Hashtable();
      while (rsFields.next())
      {
	 htFields.put(
	    rsFields.getString("field"), rsFields.getString("value"));
      } // next field value
      ht.put("fields", htFields);
      rsFields.close();
   }
   rs.close();
   sql.close();
   sqlFields.close();

   sql = pg.prepareStatement(
      "SELECT * FROM "+module+"_form_field"
      +" ORDER BY display_order"); 
   rs = sql.executeQuery();	
   Vector vFields = Page.HashtableCollectionFromResultSet(rs);
   pg.set("fields", vFields);
   rs.close();
   sql.close();

   if ("text/csv".equalsIgnoreCase(request.getParameter("content-type")))
   {
      pg.setTemplatePage(null); // don't wrap the template around this page
      pg.addResponseHeader("Content-Disposition", 
			   "attachment; filename=instances.csv;");
   }
}
%>