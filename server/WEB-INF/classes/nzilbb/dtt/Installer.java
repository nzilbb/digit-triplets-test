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
package nzilbb.dtt;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.io.FileFilter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
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
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import org.apache.catalina.realm.MessageDigestCredentialHandler;

/**
 * Servlet that manages installation and upgrade.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/install",
   loadOnStartup = 0)
public class Installer extends HttpServlet {
   
   // Attributes:

   protected String version;
   protected String driverName = "com.mysql.cj.jdbc.Driver";
   protected String connectionURL;
   protected String connectionName;
   protected String connectionPassword;

   // Methods:
   
   /**
    * Default constructor.
    */
   public Installer() {
   } // end of constructor

   /** 
    * Initialise the servlet by loading the database connection settings.
    */
   public void init() {
      try {
         log("init...");

         // ensure JDBC driver registered with the driver manager
         Class.forName(driverName).getConstructor().newInstance();

         // get database connection info
         File contextXml = new File(getServletContext().getRealPath("META-INF/context.xml"));
         if (contextXml.exists()) { // get database connection configuration from context.xml
            Document doc = DocumentBuilderFactory.newInstance()
               .newDocumentBuilder().parse(new InputSource(new FileInputStream(contextXml)));
            
            // locate the node(s)
            XPath xpath = XPathFactory.newInstance().newXPath();
            connectionURL = xpath.evaluate("//Realm/@connectionURL", doc);
            connectionName = xpath.evaluate("//Realm/@connectionName", doc);
            connectionPassword = xpath.evaluate("//Realm/@connectionPassword", doc);

            // look for upgrades
            upgrade();
            
         } else {
            log("Configuration file not found: " + contextXml.getPath());

            // trigger a new installation
            version = null;
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
      if (version != null) { // already installed
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         // send the installation form
         response.setContentType("text/html");
         response.setCharacterEncoding("UTF-8");

         PrintWriter writer = response.getWriter();
         writer.println("<!DOCTYPE html>");
         writer.println("<html>");
         writer.println(" <head>");
         writer.println("  <title>Digit Triplets Test Installer</title>");
         writer.println("  <link rel=\"shortcut icon\" href=\"logo.png\" />");
         writer.println("  <link rel=\"stylesheet\" href=\"css/install.css\" type=\"text/css\" />");
         writer.println(" </head>");
         writer.println(" <body>");
         writer.println("  <h1>Digit Triplets Test Installer</h1>");
         writer.println("  <form method=\"POST\"><table>");

         // MySQL server
         writer.println("   <tr title=\"The host that the MySQL server is running on\">");
         writer.println("    <td><label for=\"mysql-host\">MySQL host name</label></td>");
         writer.println("    <td><input id=\"mysql-host\" name=\"mysql-host\" type=\"text\" value=\"localhost\"/></td></tr>");

         // root user credentials
         writer.println("   <tr title=\"The MySQL user that can create databases and users\">");
         writer.println("    <td><label for=\"root-user\">MySQL root user</label></td>");
         writer.println("    <td><input id=\"root-user\" name=\"root-user\" type=\"text\" value=\"root\"/></td></tr>");
         writer.println("   <tr title=\"The password for the root user\">");
         writer.println("    <td><label for=\"root-password\">MySQL root password</label></td>");
         writer.println("    <td><input id=\"root-password\" name=\"root-password\" type=\"password\" autofocus/></td></tr>");
         
         // database access
         writer.println("   <tr title=\"The name of the database to create\">");
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
      if (version != null) { // already installed
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         log("Installing...");
         PrintWriter writer = response.getWriter();
         writer.println("<!DOCTYPE html>");
         writer.println("<html>");
         writer.println(" <head>");
         writer.println("  <title>Digit Triplets Test Installer</title>");
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

         try {

            if (mysqlHost == null || mysqlHost.trim().length() == 0) {
               writer.println("<span class=\"error\">No MySQL host specified</span>");
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
               return;
            }
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

            String dbUserHosts = mysqlHost.equals("localhost")?"localhost":"*";
         
            writer.println("mysqlHost: "+mysqlHost);
            writer.println("dbName: "+dbName);	 
            writer.println("dbUser: "+dbUser);
            
            String dbConnectString =
               "jdbc:mysql://" + mysqlHost + "/mysql"
               + "?dontTrackOpenResources=true" // this prevents 'leakage'
               + "&characterEncoding=UTF-8" // this ensures non-roman data is correctly saved
               + "&useSSL=false&allowPublicKeyRetrieval=true"; // this disables SSL, avoiding log errors
            writer.println("connect string: "+dbConnectString);

            Connection connection = DriverManager.getConnection(
               dbConnectString, rootUser, rootPassword);

            writer.print("Creating database...");
            log("Creating database...");
            PreparedStatement sql = connection.prepareStatement(
               "CREATE DATABASE IF NOT EXISTS " + dbName
               + " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
            sql.executeUpdate();
            sql.close();
            writer.println("OK.");
            
            writer.print("Creating user...");
            sql = connection.prepareStatement(
               "CREATE USER '" + dbUser.replaceAll("'", "\\'") + "'@'"+dbUserHosts+"'"
               +" IDENTIFIED BY '" + dbPassword.replaceAll("'", "\\'") + "'");
            sql.executeUpdate();
            sql.close();
            writer.println("OK.");

            writer.print("Setting database user access...");
            sql = connection.prepareStatement(
               "GRANT ALL PRIVILEGES ON " + dbName + ".* TO '" 
               + dbUser.replaceAll("'", "\\'") + "'@'"+dbUserHosts+"';");
            sql.executeUpdate();
            sql.close();
            writer.println("OK.");
            
            log("Database/user created.");
            connection.close();

            // set attributes so that SQL scripts can run
            dbConnectString =
               "jdbc:mysql://" + mysqlHost + "/" + dbName // now with new database
               + "?dontTrackOpenResources=true" // this prevents 'leakage'
               + "&characterEncoding=UTF-8" // this ensures non-roman data is correctly saved
               + "&useSSL=false&allowPublicKeyRetrieval=true"; // this disables SSL, avoiding log errors
            connectionURL = dbConnectString;
            connectionName = dbName;
            connectionPassword = dbPassword;
            
            log("Installing database schema...");
            writer.print("Installing database schema...");
            upgrade();
            writer.println("OK");
            
            String adminUser = "admin";
            String adminPassword = "admin";
            log("Set '"+adminUser+"' user password...");
            writer.print("Set '"+adminUser+"' user password to '"+adminPassword+"'...");
            MessageDigestCredentialHandler credentials = new MessageDigestCredentialHandler();
            credentials.setAlgorithm("SHA-256");
            credentials.setEncoding("UTF-8");
            credentials.setIterations(1000);
            credentials.setSaltLength(8);
            String credential = credentials.mutate("admin");
            connection = newConnection();
            try {
               sql = connection.prepareStatement(
                  "UPDATE user SET password = ? WHERE user = ?");
               sql.setString(1, credential);
               sql.setString(2, adminUser);
               sql.executeUpdate();
               sql.close();
               writer.println("OK");
            } finally {
               connection.close();
            }

            log("Creating context.xml from context-template.xml...");
            writer.print("Creating context.xml from context-template.xml...");
            File contextXmlTemplate
               = new File(getServletContext().getRealPath("/WEB-INF/context-template.xml"));      
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
               .parse(new InputSource(new FileInputStream(contextXmlTemplate)));            
            XPathSearchReplace(
               doc, "//Realm/@connectionURL", dbConnectString);
            XPathSearchReplace(
               doc, "//Realm/@connectionName", dbUser);
            XPathSearchReplace(
               doc, "//Realm/@connectionPassword", dbPassword);
            File contextXmlFile
               = new File(getServletContext().getRealPath("/META-INF/context.xml"));
            // save the result
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(new DOMSource(doc), new StreamResult(contextXmlFile));
            writer.println("OK");
            log("context.xml saved.");
            
            writer.println();
            writer.println("Installation complete, please click <a href=\"admin/\">here</a>");
            writer.println("and log in with username: <em>"+adminUser+"</em>"
                           +" password: <em>"+adminPassword+"</em>");
         } catch(Exception x) {
            // reset state
            version = null;
            connectionURL = null;
            connectionName = null;
            connectionPassword = null;
            
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
    * Creates a new database connection object
    * @return A connected connection object
    * @throws Exception
    */
   protected Connection newConnection()
      throws SQLException { 
      return DriverManager.getConnection(connectionURL, connectionName, connectionPassword);
   } // end of newDatabaseConnection()
   
   /**
    * Run SQL scripts to upgrade the database schema.
    */
   protected void upgrade() throws SQLException {
      Connection connection = newConnection();
      connection.setAutoCommit(false);

      PreparedStatement sql = connection.prepareStatement("SELECT version FROM version");
      try {
         ResultSet rs = sql.executeQuery();
         if (rs.next()) version = rs.getString("version");
         rs.close();
      } catch(SQLException exception) {} // the table might not exist yet
      sql.close();

      log("upgrade - current version: " + version);
      String originalVersion = version;

      // list files in WEB-INF/sql in order
      File dir = new File(getServletContext().getRealPath("WEB-INF/sql"));
      File[] scripts = dir.listFiles(new FileFilter() {
            public boolean accept(File f) {
               return f.getName().endsWith(".sql");
            }});
      Arrays.sort(scripts);

      // execute scripts greater than our current version
      if (version == null) version = "";
      try {
         for (File script : scripts) {
            String scriptVersion = script.getName().replace(".sql","");
            if (version.compareTo(scriptVersion) < 0) {
               
               // execute this script...
               log("upgrade : " + version + " -> " + scriptVersion);
               
               // read the file
               StringBuilder content = new StringBuilder();
               try {
                  BufferedReader reader = new BufferedReader(new FileReader(script));
                  String line = reader.readLine();
                  while (line != null) {
                     content.append(line);
                     content.append("\n");
                     line = reader.readLine();
                  } // next line
                  
               } catch(IOException exception) {
                  log("ERROR: " + exception);
               }
               // execute each statement in it
               String[] statements = content.toString().split(";");
               for (String statement : statements) {
                  if (statement.trim().length() == 0) continue;
                  
                  sql = connection.prepareStatement(statement);
                  try {
                     
                     sql.executeUpdate();
                     
                  } catch(SQLException exception) {
                     log("ERROR upgrade " + scriptVersion + ": " + exception.getMessage());
                     // rollback if possible
                     try { connection.rollback(); } catch(SQLException x) {}
                     throw exception;
                  } finally {
                     sql.close();
                  }
               } // next statement
               connection.commit();
               version = scriptVersion;
            } // version < scriptVersion
         } // next script
      } finally {
         if (version != null && !version.equals(originalVersion)) {
            // save version
            sql = connection.prepareStatement(
               "REPLACE INTO version (version) VALUES (?)");
            sql.setString(1, version);
            try {
               sql.executeUpdate();
            } catch(SQLException exception) {
               log("ERROR updating version: " + exception.getMessage());
            } finally {
               sql.close();
            }
         }
         log("upgrade finished - version now: " + version);
         
         connection.close();
      }
   } // end of upgrade()
   
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
} // end of class Installer
