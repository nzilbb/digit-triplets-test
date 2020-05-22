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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.*;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Servlet that manages installation and upgrade.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/install",
   loadOnStartup = 0)
public class Install extends HttpServlet {
   
   // Attributes:

   protected DatabaseService db;

   // Methods:
   
   /**
    * Default constructor.
    */
   public Install() {
   } // end of constructor

   /** 
    * Initialise the servlet by loading the database connection settings.
    */
   public void init() {
      try {
         log("init...");

         db = new DatabaseService()
            .setContext(getServletContext());

         // get database connection info
         File contextXml = new File(getServletContext().getRealPath("META-INF/context.xml"));
         if (contextXml.exists()) { // get database connection configuration from context.xml
            Document doc = DocumentBuilderFactory.newInstance()
               .newDocumentBuilder().parse(new InputSource(new FileInputStream(contextXml)));
            
            // locate the node(s)
            XPath xpath = XPathFactory.newInstance().newXPath();
            db.setConnectionURL(xpath.evaluate("//Realm/@connectionURL", doc))
               .setConnectionName(xpath.evaluate("//Realm/@connectionName", doc))
               .setConnectionPassword(xpath.evaluate("//Realm/@connectionPassword", doc));

            // look for upgrades
            db.upgrade(
               new File(getServletContext().getRealPath("WEB-INF/sql")),
               new File(getServletContext().getRealPath("admin/upgrade/log.txt")));
            
         } else {
            log("Configuration file not found: " + contextXml.getPath());
            log("Webapp not installed yet.");
         }
      } catch (Exception x) {
         log("failed", x);
      } 
   }

   /**
    * GET handler
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      // are we a new installation?
      assert db != null : "db != null";
      if (db.getVersion() != null) { // already installed
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         // send the installation form
         response.setContentType("text/html");
         response.setCharacterEncoding("UTF-8");

         PrintWriter writer = response.getWriter();
         writer.println("<!DOCTYPE html>");
         writer.println("<html>");
         writer.println(" <head>");
         writer.println("  <title>Digit Triplets Test Install</title>");
         writer.println("  <link rel=\"shortcut icon\" href=\"logo.png\" />");
         writer.println("  <link rel=\"stylesheet\" href=\"css/install.css\" type=\"text/css\" />");
         writer.println(" </head>");
         writer.println(" <body>");
         writer.println("  <h1>Digit Triplets Test Install</h1>");
         writer.println("  <form method=\"POST\"><table>");

         // MySQL server
         writer.println("   <tr title=\"The host that the MySQL server is running on\">");
         writer.println("    <td><label for=\"mysql-host\">MySQL host name</label></td>");
         writer.println("    <td><input id=\"mysql-host\" name=\"mysql-host\" type=\"text\" value=\"localhost\"/></td></tr>");

         // root user credentials
         writer.println("   <tr title=\"Does the database already exist? Or should it be created now?\">");
         writer.println("    <td><label for=\"create-db\">Database exists</label></td>");
         writer.println("    <td><input id=\"create-db\" name=\"create-db\" type=\"checkbox\""
                        +" onchange=\"document.getElementById('root-user').disabled"
                        +" = document.getElementById('root-password').disabled"
                        +" = this.checked;\" autofocus/></td></tr>");
         writer.println("   <tr title=\"The MySQL user that can create databases and users\">");
         writer.println("    <td><label for=\"root-user\">MySQL root user (blank if the database already exists)</label></td>");
         writer.println("    <td><input id=\"root-user\" name=\"root-user\" type=\"text\" value=\"root\"/></td></tr>");
         writer.println("   <tr title=\"The password for the root user, or blank if the database already exists\">");
         writer.println("    <td><label for=\"root-password\">MySQL root password</label></td>");
         writer.println("    <td><input id=\"root-password\" name=\"root-password\" type=\"password\"/></td></tr>");
         
         // database access
         writer.println("   <tr title=\"The name of the database to connect to or create\">");
         writer.println("    <td><label for=\"db-name\">Database name</label></td>");
         writer.println("    <td><input id=\"db-name\" name=\"db-name\" type=\"text\" value=\"dtt\"/></td></tr>");
         writer.println("   <tr title=\"The MySQL user name to create for this web application\">");
         writer.println("    <td><label for=\"db-user\">Database user</label></td>");
         writer.println("    <td><input id=\"db-user\" name=\"db-user\" type=\"text\" value=\"dtt\"/></td></tr>");
         writer.println("   <tr title=\"The MySQL password to use for this web application\">");
         writer.println("    <td><label for=\"db-password\">Database password</label></td>");
         writer.println("    <td><input id=\"db-password\" name=\"db-password\" type=\"password\" value=\""
                        +randomString(20)
                        +"\"/></td></tr>");
         writer.println("    <tr><td><input type=\"submit\" value=\"Install\"></td></tr>");
         
         writer.println("  </table></form>");
         writer.println(" </body>");
         writer.println("</html>");
         writer.flush();
      } // need to install
   }

   /**
    * POST handler for installer form.
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      
      // are we a new installation?
      assert db != null : "db != null";
      if (db.getVersion() != null) { // already installed
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         log("Installing...");
         PrintWriter writer = response.getWriter();
         writer.println("<!DOCTYPE html>");
         writer.println("<html>");
         writer.println(" <head>");
         writer.println("  <title>Digit Triplets Test Install</title>");
         writer.println("  <link rel=\"shortcut icon\" href=\"logo.png\" />");
         writer.println("  <link rel=\"stylesheet\" href=\"css/install.css\" type=\"text/css\" />");
         writer.println(" </head>");
         writer.println(" <body><pre>");
         
         String mysqlHost = request.getParameter("mysql-host");
         String rootUser = request.getParameter("root-user");
         String rootPassword = request.getParameter("root-password");
         String dbName = request.getParameter("db-name");
         String dbUser = request.getParameter("db-user");
         String dbPassword = request.getParameter("db-password");
         boolean databaseExists = rootUser == null || rootUser.length() == 0;

         try {

            if (mysqlHost == null || mysqlHost.trim().length() == 0) {
               writer.println("<span class=\"error\">No MySQL host specified</span>");
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
               return;
            }
            if (!databaseExists) {
               if (rootUser == null || rootUser.trim().length() == 0) {
                  writer.println("<span class=\"error\">No MySQL root user specified</span>");
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  return;
               }
               if (rootPassword == null || rootPassword.trim().length() == 0) {
                  writer.println("<span class=\"error\">No MySQL root password specified</span>");
                  response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                  return;
               }
            }
            if (dbName == null || dbName.trim().length() == 0) {
               writer.println("<span class=\"error\">No database name specified</span>");
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
               return;
            }
            if (dbUser == null || dbUser.trim().length() == 0) {
               writer.println("<span class=\"error\">No database user specified</span>");
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
               return;
            }
            if (dbPassword == null || dbPassword.trim().length() == 0) {
               writer.println("<span class=\"error\">No database password specified</span>");
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
               return;
            }

            writer.println("mysqlHost: "+mysqlHost);
            writer.println("dbName: "+dbName);	 
            writer.println("dbUser: "+dbUser);

            if (!databaseExists) {
               log("Root user details provided, creating database...");
               writer.println("Root user details provided, creating database...");

               db.createDatabase(
                  mysqlHost, rootUser, rootPassword, dbName,  dbUser, dbPassword, writer);
            } else {
               log("Root user details not provided, assuming database already exists...");
               writer.println("Root user details not provided, assuming database already exists...");
            }
            db.setConnectionURL(mysqlHost, dbName, dbUser, dbPassword);
            
            log("Installing database schema...");
            writer.print("Installing database schema...");
            db.upgrade(
               new File(getServletContext().getRealPath("WEB-INF/sql")),
               new File(getServletContext().getRealPath("admin/upgrade/log.txt")));
            writer.println("OK");
            
            String adminUser = "admin";
            String adminPassword = "admin";
            log("Set '"+adminUser+"' user password...");
            writer.print("Set '"+adminUser+"' user password to '"+adminPassword+"'...");
            if (db.setUserPassword(adminUser, adminPassword)) {
               writer.println("OK");
            } else {
               writer.println("FAILED");
            }

            log("Creating context.xml from context-template.xml...");
            writer.print("Creating context.xml from context-template.xml...");
            File contextXmlTemplate
               = new File(getServletContext().getRealPath("/WEB-INF/context-template.xml"));      
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
               .parse(new InputSource(new FileInputStream(contextXmlTemplate)));            
            XPathSearchReplace(
               doc, "//Realm/@connectionURL", db.getConnectionURL());
            XPathSearchReplace(
               doc, "//Realm/@connectionName", db.getConnectionName());
            XPathSearchReplace(
               doc, "//Realm/@connectionPassword", db.getConnectionPassword());
            File contextXmlFile
               = new File(getServletContext().getRealPath("/META-INF/context.xml"));
            // save the result
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(new DOMSource(doc), new StreamResult(contextXmlFile));
            writer.println("OK");
            log("context.xml saved.");
            
            writer.println("</pre>");
            writer.println("<p><em>Installation complete.</em> Now:</p>");
            writer.println("<ol>");
            writer.println("<li>Restart Tomcat</li>");
            writer.println("<li>Click <a href=\"admin/\" target=\"admin\">here</a></li>");
            writer.println("<li>Log in with username: <em>"+adminUser+"</em>"
                           +" password: <em>"+adminPassword+"</em></li>");
            writer.print("</ol><pre>");
            
         } catch(Throwable x) {
            // reset state
            db.setVersion(null);
            db.setConnectionURL(null);
            db.setConnectionName(null);
            db.setConnectionPassword(null);
            
            log("ERROR: " + x);
            writer.println("\n<span class=\"error\">ERROR: " + x + "</span>");
            x.printStackTrace(writer);
            
         } finally {
            writer.println("</pre></body></html>");
         }
         
         log("Finished request");
      } // need to install
   }
      
   private static void XPathSearchReplace(Document doc, String query, String value)
      throws XPathExpressionException, IOException {
      
      // locate the node(s)
      XPath xpath = XPathFactory.newInstance().newXPath();
      NodeList nodes = (NodeList)xpath.evaluate(query, doc, XPathConstants.NODESET);
      
      // make the change
      for (int idx = 0; idx < nodes.getLength(); idx++)
      {
         nodes.item(idx).setTextContent(value);
      }
   }   
   
   /**
    * Generates a random string.
    * @return A new random string.
    */
   public String randomString(int length) {
      String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      String lower = upper.toLowerCase(Locale.ROOT);
      String digits = "0123456789";
      String alphanum = upper + lower + digits;
      Random random = new Random();
      char[] symbols = alphanum.toCharArray();
      char[] buf = new char[length];
      for (int idx = 0; idx < buf.length; ++idx) {
         buf[idx] = symbols[random.nextInt(symbols.length)];
      }
      return new String(buf);
   } // end of randomString()
   
   private static final long serialVersionUID = 1;
} // end of class Install
