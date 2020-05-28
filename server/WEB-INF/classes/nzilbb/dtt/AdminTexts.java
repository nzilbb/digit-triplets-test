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
 * Servlet that allows management of prompts and results texts.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/admin/texts/*",
   loadOnStartup = 30)
public class AdminTexts extends ServletBase {
   
   /**
    * GET handler lists all rows.
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      try {Thread.sleep(4000);} catch(Exception exception) {}

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         try {
            Connection connection = db.newConnection();
            try {
               if (request.getPathInfo() != null
                   && request.getPathInfo().contains("/")) { // return a particular row
                  String id = request.getPathInfo().replaceAll("^/","");
                  PreparedStatement sql = connection.prepareStatement(
                     "SELECT * FROM text WHERE id = ?");
                  sql.setString(1, id);
                  ResultSet rs = sql.executeQuery();
                  try {
                     if (!rs.next()) { // row not found
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        returnMessage("Text doesn't exist: " + id, response);
                     } else { // row found
                        JsonGenerator json = Json.createGenerator(response.getWriter());
                        json.writeStartObject();
                        json.write("id", rs.getString("id"));
                        json.write("label", rs.getString("label"));
                        json.write("html", rs.getString("html"));
                        json.writeEnd();
                        json.close();
                     } // row found
                  } finally {                  
                     rs.close();
                     sql.close();
                  }
               } else { // list all rows
                  PreparedStatement sql = connection.prepareStatement(
                     "SELECT id, label FROM text ORDER BY display_order");
                  ResultSet rs = sql.executeQuery();
                  JsonGenerator json = Json.createGenerator(response.getWriter());
                  json.writeStartArray();
                  try {
                     while (rs.next()) {
                        json.writeStartObject();
                        json.write("id", rs.getString("id"));
                        json.write("label", rs.getString("label"));
                        json.writeEnd();
                     } // next user
                  } finally {
                     json.writeEnd();
                     json.close();
                     
                     rs.close();
                     sql.close();
                  }
               } // list all rows
            } finally {
               connection.close();
            }
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("AdminTexts GET: ERROR: " + exception);
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
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         // read the incoming object
         JsonReader reader = Json.createReader(request.getReader());
         JsonObject jsonUser = reader.readObject();
         String id = jsonUser.getString("id");
         String label = jsonUser.getString("label");
         String html = jsonUser.getString("html");
         
         try {
            Connection connection = db.newConnection();
            
            // insert the user
            PreparedStatement sql = connection.prepareStatement(
               "UPDATE text SET html = ?, update_date = Now(), update_user_id = ? WHERE id = ?");
            sql.setString(1, html);
            sql.setString(2, request.getRemoteUser());
            sql.setString(3, id);
            int rows = sql.executeUpdate();
            if (rows == 0) {
               response.setStatus(HttpServletResponse.SC_NOT_FOUND);
               returnMessage("Text not found: " + id, response);
            } else {
               
               // row updated, so return it
               JsonGenerator json = Json.createGenerator(response.getWriter());
               json.writeStartObject();
               try {
                  json.write("id", id);
                  json.write("label", label);
                  json.write("html", html);
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
            log("AdminTexts POST: ERROR: " + exception);
         }
      } 
   }

   private static final long serialVersionUID = 1;
} // end of class AdminTexts
