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
import nzilbb.webapp.DatabaseService;

/**
 * Servlet that allows management of meta-data fields.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/admin/fields/*",
   loadOnStartup = 30)
public class AdminFields extends HttpServlet {
   
   // Attributes:
   
   protected DatabaseService db;
   
   // Methods:
   
   /**
    * Default constructor.
    */
   public AdminFields() {
   } // end of constructor
   
   /** 
    * Initialise the servlet by loading the database connection settings.
    */
   public void init() {
      db = (DatabaseService)getServletContext().getAttribute("nzilbb.webapp.DatabaseService");
   }
   
   /**
    * GET handler lists all rows.
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
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
            log("AdminFields GET: ERROR: " + exception);
         }
      } 
   }

   /**
    * POST handler - add a new row.
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         // read the incoming object
         JsonReader reader = Json.createReader(request.getReader());
         JsonObject jsonUser = reader.readObject();
         String field = jsonUser.getString("field");
         String name = jsonUser.getString("name");
         String description = jsonUser.getString("description");
         String type = jsonUser.getString("type");
         String size = jsonUser.getString("size");
         int display_order = jsonUser.getInt("display_order");
         boolean required = jsonUser.getBoolean("required");
         // TODO read options

         // validate
         if (field == null || field.trim().length() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnMessage("No field ID specified.", response);
            return;
         }
         if (type.trim().length() == 0) type = "string";

         try {
            Connection connection = db.newConnection();
            
            // default order
            if (display_order == 0) {
               // put it at the end of the list
               PreparedStatement sqlDefault = connection.prepareStatement(
                  "SELECT COALESCE(MAX(display_order) + 10, 10) FROM form_field");
               ResultSet rs = sqlDefault.executeQuery();
               try {
                  rs.next();
                  display_order = rs.getInt(1);
               } finally {
                  rs.close();
                  sqlDefault.close();
               }
            }
            
            // insert the user
            PreparedStatement sql = connection.prepareStatement(
               "INSERT INTO form_field"
               +" (field, name, description, type, size, required, display_order,"
               +" update_date, update_user_id) VALUES (?,?,?,?,?,?,?,Now(),?)");
            sql.setString(1, field);
            sql.setString(2, name);
            sql.setString(3, description);
            sql.setString(4, type);
            sql.setString(5, size);
            sql.setInt(6, required?1:0);
            sql.setInt(7, display_order);
            sql.setString(8, ""+request.getRemoteUser());
            int rows = sql.executeUpdate();
            if (rows == 0) {
               response.setStatus(HttpServletResponse.SC_CONFLICT);
               returnMessage("Field not added: " + field, response);
            } else {

               // TODO add options
               
               // row added, so return it
               JsonGenerator json = Json.createGenerator(response.getWriter());
               json.writeStartObject();
               try {
                  json.write("field", field);
                  json.write("name", name);
                  json.write("description", description);
                  json.write("type", type);
                  json.write("size", size);
                  json.write("required", required);
                  json.write("display_order", display_order);
               } finally {
                  json.writeEnd();
                  json.close();
                  
                  sql.close();
                  connection.close();
               }
            } // user added
         } catch(SQLIntegrityConstraintViolationException exception) {
            // user is already there
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            returnMessage("Field already exists: " + field, response);
         } catch(SQLException exception) {
            // TODO return JSON-encoded informative message 
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("AdminFields POST: ERROR: " + exception);
         }
      } 
   }

   /**
    * PUT handler - update an existing row.
    */
   @Override
   protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      
      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         // read the incoming object
         JsonReader reader = Json.createReader(request.getReader());
         JsonObject jsonUser = reader.readObject();
         String field = jsonUser.getString("field");
         String name = jsonUser.getString("name");
         String description = jsonUser.getString("description");
         String type = jsonUser.getString("type");
         if (type.trim().length() == 0) type = "string";
         String size = jsonUser.getString("size");
         int display_order = jsonUser.getInt("display_order");
         boolean required = jsonUser.getBoolean("required");
         
         // options
         JsonArray options = jsonUser.containsKey("options") && !jsonUser.isNull("options")
            ?jsonUser.getJsonArray("options"):null;
         
         try {
            Connection connection = db.newConnection();

            // insert the user
            PreparedStatement sql = connection.prepareStatement(
               "UPDATE form_field SET name = ?, description = ?, type = ?, size = ?, required = ?,"
               +" display_order = ?, update_date = Now(), update_user_id = ? WHERE field = ?");
            sql.setString(1, name);
            sql.setString(2, description);
            sql.setString(3, type);
            sql.setString(4, size);
            sql.setInt(5, required?1:0);
            sql.setInt(6, display_order);
            sql.setString(7, request.getRemoteUser());
            sql.setString(8, field);
            int rows = sql.executeUpdate();
            if (rows == 0) {
               response.setStatus(HttpServletResponse.SC_NOT_FOUND);
               returnMessage("Field not found: " + field, response);
            } else {
               // update options
               sql = connection.prepareStatement(
                  "DELETE FROM form_field_option WHERE field = ?");
               sql.setString(1, field);
               sql.executeUpdate();
               if (options != null && "select".equals(type)) {
                  // add given options
                  sql = connection.prepareStatement(
                     "INSERT INTO form_field_option"
                     +" (field, value, description, display_order, update_date, update_user_id)"
                     +" VALUES (?,?,?,?,Now(),?)");
                  sql.setString(1, field);
                  for (int o = 0; o < options.size(); o++) {
                     JsonObject jsonOption = options.getJsonObject(o);
                     sql.setString(2, jsonOption.getString("value"));
                     sql.setString(3, jsonOption.getString("description"));
                     sql.setInt(4, o);
                     sql.setString(5, request.getRemoteUser());
                     try {
                        sql.executeUpdate();
                     } catch(SQLException exception) {
                        log("AdminFields POST: options ERROR: " + exception);
                     }
                  } // next option
               }

               // row updated, so return it
               JsonGenerator json = Json.createGenerator(response.getWriter());
               json.writeStartObject();
               try {
                  json.write("field", field);
                  json.write("name", name);
                  json.write("description", description);
                  json.write("type", type);
                  json.write("size", size);
                  json.write("required", required);
                  json.write("display_order", display_order);
                  if (options != null && "select".equals(type)) {
                     json.writeStartArray("options");
                     for (int o = 0; o < options.size(); o++) {
                        JsonObject jsonOption = options.getJsonObject(o);
                        json.writeStartObject();
                        json.write("value", jsonOption.getString("value"));
                        json.write("description", jsonOption.getString("description"));
                        json.writeEnd();
                     } // next option
                     json.writeEnd();
                  } // send options
               } finally {
                  json.writeEnd();
                  json.close();
                  
                  sql.close();
                  connection.close();
               }
            } // user updated
         } catch(SQLException exception) {
            // TODO return JSON-encoded informative message 
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("AdminFields POST: ERROR: " + exception);
         }
      } 
   }

   /**
    * DELETE handler - remove existing row.
    */
   @Override
   protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
      
      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         try {
            String field = request.getPathInfo().replaceAll("^/","");
            
            // delete the user
            Connection connection = db.newConnection();
            PreparedStatement sql = connection.prepareStatement(
               "DELETE FROM form_field WHERE field = ?");
            sql.setString(1, field);
            int rows = sql.executeUpdate();
            if (rows == 0) {
               response.setStatus(HttpServletResponse.SC_NOT_FOUND);
               returnMessage("Field doesn't exist: " + field, response);
            } 
            sql = connection.prepareStatement(
               "DELETE FROM form_field_option WHERE field = ?");
            sql.setString(1, field);
            sql.executeUpdate();
            
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
         }
      }
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

   private static final long serialVersionUID = 1;
} // end of class AdminFields
