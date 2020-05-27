<%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ page import = "java.io.*" 
%><%@ page import = "java.util.*" 
%><%@ page import = "java.text.SimpleDateFormat" 
%><%
{
   Page pg = (Page)request.getAttribute("page");
   nz.net.fromont.hexagon.Module module = pg.getModule();

   // what path has been called, relative to the module root?
   String sFile = request.getParameter("file");
   if (sFile == null) sFile = "introduction";
   pg.setTitle("Edit Page: " + sFile);
   pg.addBreadCrumb("Edit Page: " + sFile);
   pg.set("file", sFile);

   // get the file to edit
   File fModulesDir 
   = new File(getServletContext().getRealPath(JSite.MODULES_ROOT));
   File fModuleDir = new File(fModulesDir, module.getModuleRoot());
   File file = new File(fModuleDir, sFile + ".html");

   // save the file contents?
   if (request.getParameter("htmlContent") != null)
   {
      if (file.exists())
      { // take a backup
	 File backup = new File(fModuleDir, sFile + (new SimpleDateFormat(".yyyy-MM-dd_HH-mm-ss").format(new Date())) + ".html");
	 if (!file.renameTo(backup)) pg.addError("Could not backup original version");
      }
      FileWriter writer = new FileWriter(file);
      writer.write(request.getParameter("htmlContent"));
      writer.close();
      pg.addMessage("File saved");
   }

   // get the contents
   String sHtml = "";
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
   catch(Exception exception)
   {
      pg.addError(exception);
   }
   pg.set("htmlContent", sHtml, false);
}
%>
