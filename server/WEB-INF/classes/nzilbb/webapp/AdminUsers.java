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
 * Servlet that manages installation and upgrade.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/admin/users",
   loadOnStartup = 10)
public class AdminUsers extends HttpServlet {
   
   // Attributes:
   
   protected DatabaseService db;
   
   // Methods:
   
   /**
    * Default constructor.
    */
   public AdminUsers() {
   } // end of constructor
   
   /** 
    * Initialise the servlet by loading the database connection settings.
    */
   public void init() {
      db = (DatabaseService)getServletContext().getAttribute("nzilbb.dtt.DatabaseService");
   }
   
   /**
    * GET handler
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         try {
            // return a list of users
            Connection connection = db.newConnection();
            PreparedStatement sql = connection.prepareStatement(
               "SELECT user, email, reset_password FROM user ORDER BY user");
            ResultSet rs = sql.executeQuery();
            JsonGenerator json = Json.createGenerator(response.getWriter());
            json.writeStartArray();
            try {
               while (rs.next()) {
                  json.writeStartObject();
                  json.write("user", rs.getString("user"));
                  if (rs.getString("email") != null) {
                     json.write("email", rs.getString("email"));
                  }
                  json.write("reset_password", rs.getInt("reset_password") != 0);
                  json.writeEnd();
               } // next user
            } finally {
               json.writeEnd();
               json.close();
               
               rs.close();
               sql.close();
               connection.close();
            }
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log("AdminUsers GET: ERROR: " + exception);
         }
      } 
   }

   /**
    * POST handler - add a new user.
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         try {
            // read the incoming object
            JsonReader reader = Json.createReader(request.getReader());
            JsonObject jsonUser = reader.readObject();
            String user = jsonUser.getString("user");
            String email = jsonUser.getString("email");
            boolean resetPassword = jsonUser.getBoolean("reset_password");
            String password = jsonUser.getString("password");
            
            // insert the user
            Connection connection = db.newConnection();
            PreparedStatement sql = connection.prepareStatement(
               "INSERT INTO user (user, email, reset_password) VALUES (?,?,?)");
            sql.setString(1, user);
            sql.setString(2, email);
            sql.setInt(3, resetPassword?1:0);
            int rows = sql.executeUpdate();
            if (rows == 0) {
               // TODO return JSON-encoded informative message 
               response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {

               // set password
               if (!db.setUserPassword(user, password)) {
                  log("AdminUsers POST: Could not set the password for: " + user);
               }
               
               // user added, so return it
               JsonGenerator json = Json.createGenerator(response.getWriter());
               json.writeStartObject();
               try {
                  json.write("user", user);
                  if (email != null) {
                     json.write("email", email);
                  }
                  json.write("reset_password", resetPassword);
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
            // TODO return JSON-encoded informative message 
         } catch(SQLException exception) {
            // TODO return JSON-encoded informative message 
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log("AdminUsers POST: ERROR: " + exception);
         }
      } 
   }

   private static final long serialVersionUID = 1;
} // end of class AdminUsers
