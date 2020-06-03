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
 * Servlet that provides information about the current user.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = { "/user", "/admin/user" },
   loadOnStartup = 20)
public class User extends ServletBase {   
   
   /**
    * GET handler lists all users. 
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else {
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         System.out.println("User " + request.getRemoteUser());
         try {
            JsonGenerator json = Json.createGenerator(response.getWriter());
            try {
               json.writeStartObject();
               if (request.getRemoteUser() != null) {
                  // return user info
                  Connection connection = db.newConnection();
                  PreparedStatement sql = connection.prepareStatement(
                     "SELECT user, reset_password FROM user WHERE user = ?");
                  sql.setString(1, request.getRemoteUser());
                  ResultSet rs = sql.executeQuery();
                  try {
                     if (rs.next()) {
                        json.write("user", rs.getString("user"));
                        json.write("reset_password", rs.getInt("reset_password") != 0);
                     } else {
                        json.write("user", rs.getString("user"));
                     }
                  } finally {               
                     rs.close();
                     sql.close();
                     connection.close();
                  }
               }
            } finally {
               json.writeEnd();
               json.close();
            }
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("User GET: ERROR: " + exception);
         }
      } 
   }

   /**
    * PUT handler - update user password.
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
         String password = jsonUser.containsKey("password")?jsonUser.getString("password"):null;
         if (password != null && password.length() == 0) password = null;
         if (password != null) {
            try {
               if (!db.setUserPassword(request.getRemoteUser(), password)) {
                  response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                  returnMessage("Could not set password: " + request.getRemoteUser(), response);
                  log("User PUT: Could not set the password for: " + request.getRemoteUser());
               }
            } catch(SQLException exception) {
               response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
               returnMessage("ERROR: " + exception.getMessage(), response);
               log("User POST: ERROR: " + exception);
            }
         } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            returnMessage("Now new password specified", response);
         }
      } 
   }

   private static final long serialVersionUID = 1;
} // end of class User
