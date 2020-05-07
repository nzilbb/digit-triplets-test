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

   private static final long serialVersionUID = 1;
} // end of class ServletBase
