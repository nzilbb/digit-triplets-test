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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.HashSet;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that allows management of users.
 * @author Robert Fromont robert@fromont.net.nz
 */
public class ServletBase extends HttpServlet {
   
   // Attributes:

   /** Access to the relational database */
   protected DatabaseService db;

   // Methods:
   
   /** 
    * Initialise the servlet by loading the database connection settings.
    */
   public void init() {
      db = (DatabaseService)getServletContext().getAttribute("nzilbb.webapp.DatabaseService");
   }

   
   /**
    * Determines whether the request can continue. This depends on two factors:
    * <ul>
    *  <li> Whether the webapp has actually been installed yet, and</li>
    *  <li> If the servlet is annotated with {@link RequiredRole}, whether the user is in
    * that role. </li> 
    * </ul>
    * If this method returns false, a side-effect is that response.setStatus() has been
    * called with an appropriate status code.
    * @param request The request for identifying the user.
    * @param response The response for possibly setting the status.
    * @return true if the request is allowed, false otherwise.
    * @throws SQLException If a database error occurs.
    */
   protected boolean hasAccess(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
         return false;
      } else if (getServletContext().getInitParameter("dev-war") != null) {
         // dev builds don't require use roles
         return true;
      } else {
	 RequiredRole requiredRole = getClass().getAnnotation(RequiredRole.class);
         if (requiredRole != null) {
            try {
               if (!userRoles(request).contains(requiredRole.value())) {
                  response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                  return false;
               } else {
                  return true;
               }
            } catch(SQLException exception) {
               log("hasAccess: ERROR: " + exception);
               response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
               return false;
            }
         } else { // no role required
            return true;
         }
      }
   }
   
   /**
    * Determines the roles the user has. 
    * <p> This provides more flexibility than HttpServletRequest.isUserInRole() because it
    * allows authentication with an external source (e.g. LDAP), while keeping
    * authorization internal to this webapp's database (i.e. the LDAP database needn't
    * have "admin" in the "isMember" attribute for the user, because it's in our internal
    * role table instead).
    * @param request The request for identifying the user.
    * @return A set of role names that apply to the current user.
    * @throws SQLException If a database error occurs.
    */
   @SuppressWarnings("unchecked")
   protected Set<String> userRoles(HttpServletRequest request) throws SQLException {
      
      // empty set if the webapp isn't installed yet or the user is unknown 
      if (db == null || request.getRemoteUser() == null) return new HashSet<String>();
      
      Set<String> roles = (Set<String>)request.getSession().getAttribute("roles");
      if (roles == null) {
         // create the object
         roles = new HashSet<String>();
         request.getSession().setAttribute("roles", roles);
         
         // load roles from the database
         Connection connection = db.newConnection();
         PreparedStatement sqlUserRoles = connection.prepareStatement(
            "SELECT role FROM role WHERE user = ?");
         sqlUserRoles.setString(1, request.getRemoteUser());
         ResultSet rsUserRoles = sqlUserRoles.executeQuery();
         try {
            while (rsUserRoles.next()) {
               roles.add(rsUserRoles.getString("role"));
            } // next role
         } finally {
            rsUserRoles.close();
            sqlUserRoles.close();
            connection.close();
         }
      }
      return roles;
   } // end of userRoles()

   /**
    * Writes a JSON-formatted via the given response.
    * @param message The message to return.
    * @param response The response to write to.
    * @throws IOException
    */
   protected void returnMessage(String message, HttpServletResponse response) throws IOException {
      Json.createGenerator(response.getWriter())
         .writeStartObject()
         .write("message", message)
         .writeEnd()
         .close();
   } // end of returnMessage()

   /**
    * Get a configured instance of the email service.
    * @param connection A connection to the database.
    * @return A configured instance of the email service, or null if email is not configured.
    */
   protected EmailService getEmailService(Connection connection) throws SQLException {
      String smtpHost = getAttribute("EmailHost", connection);
      if (smtpHost == null || smtpHost.length() == 0) return null;
      return new EmailService()
         .setSMTPHost(smtpHost)
         .setSMTPUser(getAttribute("EmailUser", connection))
         .setSMTPPassword(getAttribute("EmailPassword", connection))
         .setFromAddress(getAttribute("EmailAddress", connection))
         .setContext(getServletContext());
   } // end of getEmailService()
   
   /**
    * Get a value from the attribute table.
    * @param attribute The attribute name.
    * @param connection A connection to the database.
    * @return The attribute value or null if the attribute isn't found.
    * @throws SQLException
    */
   protected String getAttribute(String attribute, Connection connection) throws SQLException {
      PreparedStatement sql = connection.prepareStatement(
         "SELECT value FROM attribute WHERE attribute = ?");
      sql.setString(1, attribute);
      ResultSet rs = sql.executeQuery();
      try {
         if (!rs.next()) return null;
         return rs.getString(1);
      } finally {
         try { rs.close(); } catch (SQLException x) {}
         try { sql.close(); } catch (SQLException x) {}
      }
   } // end of getAttribute()
  
   /**
    * Gets an integer value from the attribute table.
    * @param attribute
    * @param connection
    * @return The value of the attribute, or 0 if it's not found, or not parseable as an integer.
    * @throws SQLException
    */
   protected int getIntAttribute(String attribute, Connection connection) throws SQLException {
      String value = getAttribute(attribute, connection);
      try {
         return Integer.parseInt(value);
      } catch(Throwable t) {
         return 0;
      }
   } // end of getIntAttribute()

   /**
    * Gets a double value from the attribute table.
    * @param attribute
    * @param connection
    * @return The value of the attribute, or 0 if it's not found, or not parseable as a double.
    * @throws SQLException
    */
   protected double getDoubleAttribute(String attribute, Connection connection) throws SQLException {
      String value = getAttribute(attribute, connection);
      try {
         return Double.parseDouble(value);
      } catch(Throwable t) {
         return 0.0;
      }
   } // end of getIntAttribute()

   private static final long serialVersionUID = 1;
} // end of class ServletBase
