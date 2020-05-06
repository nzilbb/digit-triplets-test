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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.io.PrintWriter;
import java.io.File;
import java.nio.file.Files;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.io.FileFilter;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import org.apache.catalina.realm.MessageDigestCredentialHandler;
import java.lang.reflect.InvocationTargetException;

/**
 * Provides access to and information about the database.
 * @author Robert Fromont robert@fromont.net.nz
 */
public class DatabaseService {
   
   // Attributes:

   PrintWriter logFile;
   
   /**
    * Current webapp version, of the form <i>YYYYMMDD</i>.<i>hhmm</i>
    * @see #getVersion()
    * @see #setVersion(String)
    */
   protected String version;
   /**
    * Getter for {@link #version}: Current webapp version, of the form <i>YYYYMMDD</i>.<i>hhmm</i>
    * @return Current webapp version.
    */
   public String getVersion() { return version; }
   /**
    * Setter for {@link #version}: Current webapp version, of the form <i>YYYYMMDD</i>.<i>hhmm</i>
    * @param newVersion Current webapp version.
    */
   public DatabaseService setVersion(String newVersion) { version = newVersion; return this; }

   /**
    * JDBC driver class name.
    * @see #getDriverName()
    * @see #setDriverName(String)
    */
   protected String driverName;
   /**
    * Getter for {@link #driverName}: JDBC driver class name. 
    * <p> Default is "com.mysql.cj.jdbc.Driver".
    * @return JDBC driver class name.
    */
   public String getDriverName() { return driverName; }
   /**
    * Setter for {@link #driverName}: JDBC driver class name.
    * @param newDriverName JDBC driver class name.
    */
   public DatabaseService setDriverName(String newDriverName)
      throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
      driverName = newDriverName;
      if (driverName != null && driverName.length() != 0) {
         Class.forName(driverName).getConstructor().newInstance();
      }
      return this;
   }

   /**
    * JDBC connection URL.
    * @see #getConnectionURL()
    * @see #setConnectionURL(String)
    */
   protected String connectionURL;
   /**
    * Getter for {@link #connectionURL}: JDBC connection URL.
    * @return JDBC connection URL.
    */
   public String getConnectionURL() { return connectionURL; }
   /**
    * Setter for {@link #connectionURL}: JDBC connection URL.
    * @param newConnectionURL JDBC connection URL.
    */
   public DatabaseService setConnectionURL(String newConnectionURL) { connectionURL = newConnectionURL; return this; }

   /**
    * Database user name.
    * @see #getConnectionName()
    * @see #setConnectionName(String)
    */
   protected String connectionName;
   /**
    * Getter for {@link #connectionName}: Database user name.
    * @return Database user name.
    */
   public String getConnectionName() { return connectionName; }
   /**
    * Setter for {@link #connectionName}: Database user name.
    * @param newConnectionName Database user name.
    */
   public DatabaseService setConnectionName(String newConnectionName) { connectionName = newConnectionName; return this; }

   /**
    * Database user password.
    * @see #getConnectionPassword()
    * @see #setConnectionPassword(String)
    */
   protected String connectionPassword;
   /**
    * Getter for {@link #connectionPassword}: Database user password.
    * @return Database user password.
    */
   public String getConnectionPassword() { return connectionPassword; }
   /**
    * Setter for {@link #connectionPassword}: Database user password.
    * @param newConnectionPassword Database user password.
    */
   public DatabaseService setConnectionPassword(String newConnectionPassword) { connectionPassword = newConnectionPassword; return this; }

   /**
    * The servlet context of the service.
    * @see #getContext()
    * @see #setContext(ServletContext)
    */
   protected ServletContext context;
   /**
    * Getter for {@link #context}: The servlet context of the service.
    * @return The servlet context of the service.
    */
   public ServletContext getContext() { return context; }
   /**
    * Setter for {@link #context}: The servlet context of the service.
    * <p> This method also sets the "nzilbb.webapp.DatabaseService" attribute of the servlet
    * context to be this object. 
    * @param newContext The servlet context of the service.
    */
   public DatabaseService setContext(ServletContext newContext) {
      context = newContext;
      if (context != null) {
         context.setAttribute("nzilbb.webapp.DatabaseService", this);
      }
      return this;
   }
   
   // Methods:
   
   /**
    * Default constructor.
    */
   public DatabaseService() throws ClassNotFoundException {
      try {
       setDriverName("com.mysql.cj.jdbc.Driver");
      } catch(Throwable t) {
         System.err.println(t.toString());
         t.printStackTrace(System.err);
      }
   } // end of constructor
   
   /**
    * Creates the database.
    * @param mysqlHost MySql host name
    * @param rootUser Database user with privileges for creating databases and users.
    * @param rootPassword Password for <var>rootUser</var>
    * @param dbName Name of the database to create.
    * @param dbUser Database user to create for the app.
    * @param dbPassword Password for <var>dbUser</var>
    * @param writer For writing status messages to.
    * @throws SQLException If any SQL statements fail.
    */
   public void createDatabase(
      String mysqlHost,
      String rootUser,
      String rootPassword,
      String dbName,
      String dbUser,
      String dbPassword,
      PrintWriter writer) throws SQLException {

      String dbUserHosts = mysqlHost.equals("localhost")?"localhost":"*";

      String dbConnectString =
         "jdbc:mysql://" + mysqlHost + "/mysql"
         + "?dontTrackOpenResources=true" // this prevents 'leakage'
         + "&characterEncoding=UTF-8" // this ensures non-roman data is correctly saved
         + "&useSSL=false&allowPublicKeyRetrieval=true"; // this disables SSL, avoiding log errors

      Connection connection = DriverManager.getConnection(
         dbConnectString, rootUser, rootPassword);
      try {
         log("Creating database...");
         writer.print("Creating database...");
         PreparedStatement sql = connection.prepareStatement(
            "CREATE DATABASE IF NOT EXISTS " + dbName
            + " CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
         sql.executeUpdate();
         sql.close();
         writer.println("OK.");
         
         log("Creating user...");
         writer.print("Creating user...");
         sql = connection.prepareStatement(
            "CREATE USER '" + dbUser.replaceAll("'", "\\'") + "'@'"+dbUserHosts+"'"
            +" IDENTIFIED BY '" + dbPassword.replaceAll("'", "\\'") + "'");
         sql.executeUpdate();
         sql.close();
         writer.println("OK.");
         
         log("Setting database user access...");
         writer.print("Setting database user access...");
         sql = connection.prepareStatement(
            "GRANT ALL PRIVILEGES ON " + dbName + ".* TO '" 
            + dbUser.replaceAll("'", "\\'") + "'@'"+dbUserHosts+"';");
         sql.executeUpdate();
         sql.close();
         writer.println("OK.");

      } finally {
         connection.close();
      }

      // set attributes so that SQL scripts can run
      dbConnectString =
         "jdbc:mysql://" + mysqlHost + "/" + dbName // now with new database
         + "?dontTrackOpenResources=true" // this prevents 'leakage'
         + "&characterEncoding=UTF-8" // this ensures non-roman data is correctly saved
         + "&useSSL=false&allowPublicKeyRetrieval=true"; // this disables SSL, avoiding log errors
      connectionURL = dbConnectString;
      connectionName = dbName;
      connectionPassword = dbPassword;

      log("createDatabase finished.");
   } // end of createDatabase()

   /**
    * Run SQL scripts to upgrade the database schema.
    * @param upgradeScriptDirectory Directory where the upgrade scripts are. These have
    * names of the form <tt><i>YYYYMMDD</i>.<i>hhmm</i>.sql</tt>, and only those that are
    * <em>greater than</em> the current {@link #version} are executed.
    * @return true if upgrade scripts were executed, false otherwise.
    */
   protected boolean upgrade(File upgradeScriptDirectory, File log) throws SQLException {
      
      boolean upgradesExecuted = false;
      
      Connection connection = newConnection();
      connection.setAutoCommit(false);

      PreparedStatement sql = connection.prepareStatement(
         "SELECT value FROM attribute WHERE attribute = 'version'");
      try {
         ResultSet rs = sql.executeQuery();
         if (rs.next()) version = rs.getString(1);
         rs.close();
      } catch(SQLException exception) {} // the table might not exist yet
      sql.close();

      try {
         Files.createDirectories(log.getParentFile().toPath());
         logFile = new PrintWriter(log);
      }
      catch(Exception exception) {
         log("Can't log to " + log.getPath() + ": " + exception);
      }

      log("upgrade - current version: " + version);
      String originalVersion = version;
      
      // list files in WEB-INF/sql in order
      File[] scripts = upgradeScriptDirectory.listFiles(new FileFilter() {
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
                     upgradesExecuted = true;
                     
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
               "UPDATE attribute SET value = ?, update_date = Now() WHERE attribute = 'version'");
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
         if (logFile != null) {
            logFile.close();
            logFile = null;
         }
      }
      return upgradesExecuted;
   } // end of upgrade()

   
   /**
    * Sets the password of the given web-app user.
    * @param user Web-app user.
    * @param password Their new password.
    * @return true if the user password was set, false otherwise.
    * @throws SQLException
    */
   public boolean setUserPassword(String user, String password) throws SQLException {
      MessageDigestCredentialHandler credentials = new MessageDigestCredentialHandler();
      try {
         credentials.setAlgorithm("SHA-256");
      } catch(Exception exception) {
         log(exception.toString());
      }
      credentials.setEncoding("UTF-8");
      credentials.setIterations(1000);
      credentials.setSaltLength(8);
      String credential = credentials.mutate(password);
      Connection connection = newConnection();
      try {
         PreparedStatement sql = connection.prepareStatement(
            "UPDATE user SET password = ? WHERE user = ?");
         sql.setString(1, credential);
         sql.setString(2, user);
         try {
            return sql.executeUpdate() > 0;
         } finally {
            sql.close();
         }
      } finally {
         connection.close();
      }
   } // end of setUserPassword()


   /**
    * Print a log message
    * @param message
    */
   public void log(String message) {
      if (context != null) context.log(message);
      if (logFile != null) {
         logFile.println(new java.util.Date().toString() + ": " + message);
         logFile.flush();
      }
   } // end of log()
   
   /**
    * Creates a new database connection object
    * @return A connected connection object
    * @throws Exception
    */
   public Connection newConnection()
      throws SQLException { 
      return DriverManager.getConnection(connectionURL, connectionName, connectionPassword);
   } // end of newDatabaseConnection()

} // end of class DatabaseService
