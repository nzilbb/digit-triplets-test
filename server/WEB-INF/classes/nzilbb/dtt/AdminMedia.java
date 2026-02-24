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
import java.nio.file.Files;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * Servlet that manages installation of media files - i.e. mp3s of digit triplets.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/admin/media",
   loadOnStartup = 30)
@RequiredRole("admin")
public class AdminMedia extends ServletBase {
   
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
         writer.println("  <title>Digit Triplets Test Install Media</title>");
         writer.println("  <link rel=\"shortcut icon\" href=\"../logo.png\" />");
         writer.println("  <link rel=\"stylesheet\" href=\"../css/install.css\" type=\"text/css\" />");
         writer.println(" </head>");
         writer.println(" <body>");
         writer.println("  <h1>Digit Triplets Test Install Media</h1>");
         writer.println("  <p>You can upload the digit triplets recordings here.</p>");
         writer.println("  <p>They must be all in a .zip file, with the following folder structure</p>");
         writer.println("  <ul>");
         writer.println("   <li><i>dtt</i> - mp3 recordings of stereo digits named <var>{triplet}</var>_<var>{db}</var>.mp3</li>");
         writer.println("   <li><i>dtta</i> - antiphasic mp3 recordings of stereo digits named <var>{triplet}</var>_<var>{db}</var>.mp3</li>");
         writer.println("   <li><i>dttl</i> - mp3 recordings of left-channel digits named <var>{triplet}</var>_<var>{db}</var>l.mp3</li>");
         writer.println("   <li><i>dttr</i> - mp3 recordings of right-channel digits named <var>{triplet}</var>_<var>{db}</var>r.mp3</li>");
         writer.println("  </ul>");
         writer.println("  <p>Where:</p>");
         writer.println("  <ul>");
         writer.println("   <li><var>{triplet}</var> is the digits represented by the recording (3 characters), and</li>");
         writer.println("   <li><var>{db}</var> is the decibel level of the voice (3 characters).</li>");
         writer.println("  </ul>");
         writer.println("  <p>If <i>dtta</i> files are specified, these are used for headphones-on tests, and <i>dttl</i>/<i>dttr</i> files are ignored.</p>");
         writer.println("  <p>If no <i>dtta</i>, <i>dttl</i>, or <i>dttr</i> files are specified, no option for headphones-on tests is presented.</p>");
         writer.println("  <p>If no <i>dtt</i> files are specified, no option for speakers-on tests is presented.</p>");
         writer.println("  <form method=\"POST\" enctype=\"multipart/form-data\"><table>");

         // WAR file
         writer.println("   <tr title=\"The .zip file containing the recordings.\">");
         writer.println("    <td><label for=\"war\">.zip file</label></td>");
         writer.println("    <td><input id=\"zip\" name=\"zip\" type=\"file\""
                        +" onchange=\"if (!this.files[0].name.match('\\.zip$'))"
                        +" { alert('Please choose a .zip file'); this.value = null; }\""
                        +"/></td></tr>");
         
         writer.println("    <tr><td><input type=\"submit\" value=\"Upload\"></td></tr>");
         
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

      File mp3Root = new File(getServletContext().getRealPath("/mp3")); 
      File soundCheckMp3 = new File(mp3Root, "sound-check.mp3");
     
      // are we a new installation?
      if (hasAccess(request, response)) {
         log("AdminMedia from war...");
         PrintWriter writer = response.getWriter();
         writer.println("<!DOCTYPE html>");
         writer.println("<html>");
         writer.println(" <head>");
         writer.println("  <title>Digit Triplets Test Install Media</title>");
         writer.println("  <link rel=\"shortcut icon\" href=\"../logo.png\" />");
         writer.println("  <link rel=\"stylesheet\" href=\"../css/install.css\" type=\"text/css\" />");
         writer.println(" </head>");
         writer.println(" <body>");
         writer.println("  <h1>Digit Triplets Test Install Media</h1>");
         writer.println("  <pre>");
         boolean fileFound = false;
         ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
         try {
            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
               if (!item.isFormField()
                   && item.getName() != null && item.getName().endsWith(".zip")) { // it's a zip file
                  log("File: " + item.getName());
                  writer.println("File: " + item.getName());
                  fileFound = true;
                  
                  // save file
                  File zip = File.createTempFile(item.getName(), ".zip");
                  try {
                     item.write(zip);
                     log("Saved: " + zip.getPath());
                     
                     // unpack it
                     JarFile jar = new JarFile(zip);
                     Enumeration<JarEntry> entries = jar.entries();
                     Pattern validateDigitFile = Pattern.compile(
                        "dtt[lra]?/([0-9]{3}_[-0-9][0-9]{2}[lra]?|sound-check)\\.mp3");
                     while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        
                        if (entry.isDirectory()) continue;
                        if (!validateDigitFile.matcher(entry.getName()).matches()) {
                           writer.println("<span class='warning'>Ignoring: "+entry.getName()+"</span>");
                           continue;
                        }
                        
                        // unpack file 
                        File parent = mp3Root;
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

                        if (!soundCheckMp3.exists()) {
                          writer.println("Using " + sFileName + " for sound-check.");
                          Files.copy(file.toPath(), soundCheckMp3.toPath());
                        }
                        
                     } // next entry
                  } finally {
                     zip.delete();
                  }
               } // .zip file
            } // next item
            
            if (!fileFound) {
               writer.print("<span class=\"error\">No file uploaded.</span>");
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
               writer.println("</pre>");
               writer.println("<p><em>Installation complete</em></p>");
               writer.println("<p>Click <a href=\".\" target=\"admin\">here</a> to continue...</p>");
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
} // end of class AdminMedia
