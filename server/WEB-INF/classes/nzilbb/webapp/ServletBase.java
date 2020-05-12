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
   
   protected DatabaseService db;
   
   // Methods:
   
   /** 
    * Initialise the servlet by loading the database connection settings.
    */
   public void init() {
      db = (DatabaseService)getServletContext().getAttribute("nzilbb.webapp.DatabaseService");
   }
   
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
