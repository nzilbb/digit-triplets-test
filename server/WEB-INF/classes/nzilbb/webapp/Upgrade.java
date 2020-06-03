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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;
import javax.servlet.ServletException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.File;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

/**
 * Servlet that manages installation and upgrade.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/admin/upgrade",
   loadOnStartup = 10)
@RequiredRole("admin")
public class Upgrade extends ServletBase {
   
   /**
    * GET handler
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      if (hasAccess(request, response)) {
         // send upgrade form
         response.setContentType("text/html");
         response.setCharacterEncoding("UTF-8");

         PrintWriter writer = response.getWriter();
         writer.println("<!DOCTYPE html>");
         writer.println("<html>");
         writer.println(" <head>");
         writer.println("  <title>Digit Triplets Test Upgrade</title>");
         writer.println("  <link rel=\"shortcut icon\" href=\"../logo.png\" />");
         writer.println("  <link rel=\"stylesheet\" href=\"../css/install.css\" type=\"text/css\" />");
         writer.println(" </head>");
         writer.println(" <body>");
         writer.println("  <h1>Digit Triplets Test Upgrade</h1>");
         writer.println("  <form method=\"POST\" enctype=\"multipart/form-data\"><table>");

         // WAR file
         writer.println("   <tr title=\"The new version of the web application archive (.war file)\">");
         writer.println("    <td><label for=\"war\">digit-triplets-test.war file</label></td>");
         writer.println("    <td><input id=\"war\" name=\"war\" type=\"file\""
                        +" onchange=\"if (!this.files[0].name.match('\\.war$'))"
                        +" { alert('Please choose a .war file'); this.value = null; }\""
                        +"/></td></tr>");
         
         writer.println("    <tr><td><input type=\"submit\" value=\"Upgrade\"></td></tr>");
         
         writer.println("  </table></form>");
         writer.println(" </body>");
         writer.println("</html>");
         writer.flush();
      } // send upgrade form
   }

   /**
    * POST handler for installer form.
    */
   @Override
   @SuppressWarnings("unchecked")
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      File webappRoot = new File(getServletContext().getRealPath("/"));
      
      if (hasAccess(request, response)) {
         log("Upgrade from war...");
         PrintWriter writer = response.getWriter();
         writer.println("<!DOCTYPE html>");
         writer.println("<html>");
         writer.println(" <head>");
         writer.println("  <title>Digit Triplets Test Install</title>");
         writer.println("  <link rel=\"shortcut icon\" href=\"../logo.png\" />");
         writer.println("  <link rel=\"stylesheet\" href=\"../css/install.css\" type=\"text/css\" />");
         writer.println(" </head>");
         writer.println(" <body>");
         writer.println("  <h1>Digit Triplets Test Upgrade</h1>");
         writer.println("  <pre>");
         boolean fileFound = false;
         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
         try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
               if (!item.isFormField()
                   && item.getName() != null && item.getName().endsWith(".war")) { // it's a war file
                  log("File: " + item.getName());
                  writer.println("File: " + item.getName());
                  fileFound = true;
                  
                  // save file
                  File war = File.createTempFile(item.getName(), ".war");
                  try {
                     item.write(war);
                     log("Saved: " + war.getPath());
                     
                     // TODO somehow validate it's actually this webapp before unpacking it
                     
                     // unpack it
                     JarFile jar = new JarFile(war);
                     String buildVersion = jar.getComment();
                     if (buildVersion == null
                         || !buildVersion.startsWith("digit-triplets-test version ")) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        writer.println(
                           "<span class=\"error\">No build version information found."
                           +" Please upload a .war file built from the source code.</span>");
                        log("ERROR: No build version information found."
                            +" Please upload a .war file built from the source code.");
                     } else { // build version found
                        writer.println("Build: " + buildVersion);
                        log("Build " + buildVersion);
                        
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                           JarEntry entry = entries.nextElement();
                           
                           // don't replace WEB-INF/web.xml, which might have been customized
                           if ("WEB-INF/web.xml".equals(entry.getName())) continue;
                           
                           if (!entry.isDirectory()) {
                              
                              // unpack file 
                              File parent = webappRoot;
                              String sFileName = entry.getName();
                              writer.print("Unpacking: "+sFileName+" ...");
                              log("Unpacking: "+sFileName+" ...");
                              String[] pathParts = entry.getName().split("/");
                              for (int p = 0; p < pathParts.length - 1; p++) {
                                 // ensure that the required directories exist
                                 parent = new File(parent, pathParts[p]);
                                 if (!parent.exists()) {
                                    parent.mkdir();
                                 }		     
                              } // next part
                              sFileName = pathParts[pathParts.length - 1];
                              File file = new File(parent, sFileName);
                              
                              // get input stream
                              InputStream in = jar.getInputStream(entry);
                              
                              // get output stream
                              FileOutputStream fout = new FileOutputStream(file);
                              
                              // pump data from one stream to the other
                              byte[] buffer = new byte[1024];
                              int bytesRead = in.read(buffer);
                              while(bytesRead >= 0) {
                                 fout.write(buffer, 0, bytesRead);
                                 bytesRead = in.read(buffer);
                              } // next chunk of data
                              
                              // close streams
                              in.close();
                              fout.close();
                              writer.println("OK");
                              
                           } // not a directory
                        
                        } // next entry
                     } // build version found
                  } finally {
                     war.delete();
                  }
               } // .war file
            } // next item

            if (!fileFound) {
               writer.print("<span class=\"error\">No file uploaded.</span>");
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
               writer.println("</pre>");
               writer.println("<p><em>Upload complete</em></p>");
               writer.println("<p>The web-app should automatically reload and upgrade.</p>");
               writer.println("<p>Click <a href=\".\" target=\"admin\">here</a> to continue...</p>");
               writer.println("<p>A log of the upgrade is available <a href=\"upgrade/log.txt\" target=\"log\">here</a>.</p>");
               writer.print("<pre>");
            }
         } catch (Exception x) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log("ERROR: " + x);
            writer.println("\n<span class=\"error\">ERROR: " + x + "</span>");
            x.printStackTrace(writer);            
         } finally {
            writer.println("</pre></body></html>");
         }
      } // process upgrade
   }

   private static final long serialVersionUID = 1;
} // end of class Upgrade
