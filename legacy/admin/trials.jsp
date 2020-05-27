<%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ page import = "java.sql.*" 
%><%
{
   Page pg = (Page) request.getAttribute("page");
   pg.setTitle("Trials");
  
   PreparedStatement sql = pg.getConnection().prepareStatement(
      "SELECT * FROM "+pg.getModule()+"_trial"
      +" WHERE instance_id = ? ORDER BY trial_number");
   sql.setInt(1, Integer.parseInt(request.getParameter("instance_id")));
   ResultSet rs = sql.executeQuery();
   pg.set("trials", Page.HashtableCollectionFromResultSet(rs));
   rs.close();
   sql.close();
   pg.setTemplatePage(null); // don't wrap the template around this page
   pg.setContentType("text/csv");
   pg.addResponseHeader("Content-Disposition", 
			"attachment; filename=instance_" + request.getParameter("instance_id") + ".csv;");

}
%>