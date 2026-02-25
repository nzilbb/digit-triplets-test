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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nzilbb.webapp.ServletBase;

/**
 * Servlet that provides definitions for meta-data fields.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/fields",
   loadOnStartup = 30)
public class Fields extends ServletBase {
      
   /**
    * GET handler lists all rows.
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         try {
            // return a list of rows
            Connection connection = db.newConnection();
            PreparedStatement sql = connection.prepareStatement(
               "SELECT * FROM form_field ORDER BY display_order");
            PreparedStatement sqlOptions = connection.prepareStatement(
               "SELECT * FROM form_field_option WHERE field = ? ORDER BY display_order");
            ResultSet rs = sql.executeQuery();
            JsonGenerator json = Json.createGenerator(response.getWriter());
            json.writeStartArray();
            try {
               while (rs.next()) {
                  json.writeStartObject();
                  json.write("field", rs.getString("field"));
                  json.write("name", rs.getString("name"));
                  json.write("description", rs.getString("description"));
                  json.write("postscript", rs.getString("postscript"));
                  json.write("type", rs.getString("type"));
                  json.write("size", rs.getString("size"));
                  json.write("required", rs.getInt("required") != 0);
                  json.write("display_order", rs.getInt("display_order"));
                  
                  // return options if type=select
                  if ("select".equals(rs.getString("type"))) {
                     sqlOptions.setString(1, rs.getString("field"));
                     ResultSet rsOptions = sqlOptions.executeQuery();
                     json.writeStartArray("options");
                     try {
                        while(rsOptions.next()) {
                           json.writeStartObject();
                           json.write("value", rsOptions.getString("value"));
                           json.write("description", rsOptions.getString("description"));
                           json.writeEnd();
                        } // next option
                     } finally {
                        json.writeEnd();
                        rsOptions.close();
                     }
                  } // send options
                  json.writeEnd();
               } // next user
            } finally {
               json.writeEnd();
               json.close();
               
               rs.close();
               sql.close();
               sqlOptions.close();
               connection.close();
            }
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("Fields GET: ERROR: " + exception);
         }
      } 
   }
   
   private static final long serialVersionUID = 1;
} // end of class Fields
