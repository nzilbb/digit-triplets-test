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
import java.sql.ResultSetMetaData;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Types;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class that handles generic database table management.
 * <p> Subclasses specify the table name, key fields, and fields.
 * @author Robert Fromont robert@fromont.net.nz
 */
public class TableServletBase extends ServletBase {

   /** The name of the data table */
   protected String table;

   /** An ordered list of key field names */
   protected List<String> keys;

   /** An ordered list of non-key fields */
   protected List<String> columns;

   /** WHERE condition */
   protected String whereClause;

   /** ORDER clause */
   protected String orderClause;

   /** Whether Create operations are allowed via POST */
   protected boolean create = false;

   /** Whether Read operations are allowed via GET */
   protected boolean read = false;

   /** Whether Update operations are allowed via PUT */
   protected boolean update = false;

   /** Whether Delete operations are allowed via DELETE */
   protected boolean delete = false;

   /** Key that is automatically generated when records are created */
   protected String autoKey = null;

   /** 
    * Constructor from attributes.
    * @param table The name of the data table.
    * @param keys An ordered list of key field names.
    * @param columns An ordered list of non-key fields.
    * @param whereClause WHERE condition.
    * @param orderClause ORDER clause.
    * @param create Whether Create operations are allowed via POST.
    * @param read Whether Read operations are allowed via GET.
    * @param update Whether Update operations are allowed via PUT.
    * @param delete Whether Delete operations are allowed via DELETE.
    */
   protected TableServletBase(
      String table, List<String> keys, List<String> columns,
      String whereClause, String orderClause,
      boolean create, boolean read, boolean update, boolean delete) {      
      this.table = table;
      this.keys = keys;
      this.columns = columns;
      this.whereClause = whereClause;
      this.orderClause = orderClause;
      this.create = create;
      this.read = read;
      this.update = update;
      this.delete = delete;
   }
   
   /**
    * GET handler lists all rows. 
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else if (!read) {
         response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      } else {
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         try {
            String[] keyValues = null;
            if (request.getPathInfo() != null && !request.getPathInfo().equals("/")) {
               keyValues = request.getPathInfo().substring(1).split("/");
            }
            boolean partialKey = keyValues != null && keyValues.length < keys.size();
            
            // return a list of rows
            Connection connection = db.newConnection();
            StringBuilder query = new StringBuilder();
            Vector<String> allColumns = new Vector<String>(keys);
            allColumns.addAll(columns);
            for (String column : allColumns) {
               if (query.length() == 0) {
                  query.append("SELECT ");
               } else {
                  query.append(", ");
               }
               query.append(column);
            } // next columsn
            query.append(" FROM ");
            query.append(table);
            StringBuffer where = new StringBuffer(); 
            if (whereClause != null && whereClause.length() > 0) {
               where.append(whereClause);
            }
            // or only one row, if there's a path
            if (keyValues != null) {
               int k = 0;
               for (String column : keys) {
                  
                  // only add as many parameters as values
                  if (++k > keyValues.length) break;
                  
                  if (where.length() > 0) {
                     where.append(" AND ");
                  }
                  where.append(column);
                  where.append(" = ?");
               } // next key
            } // key values specified in path
            if (where.length() > 0) {
               query.append(" WHERE ");
               query.append(where);
            }
            if (orderClause != null && orderClause.length() > 0) {
               query.append(" ORDER BY ");
               query.append(orderClause);
            }

            if (request.getParameter("p") != null) { // page
               try {
                  int page = Integer.parseInt(request.getParameter("p"));
                  int pageLength = 20;
                  if (request.getParameter("l") != null) {
                     pageLength = Integer.parseInt(request.getParameter("l"));
                  }
                  int offset = page * pageLength;
                  query.append(" LIMIT " + offset + ", " + pageLength);
               } catch (Exception x) {
                  log("ERROR cannot paginate: " + x);
               }
            } // page

            log("GET " + request.getPathInfo() + " : " + query.toString()); // TODO remove
            PreparedStatement sql = connection.prepareStatement(query.toString());
            if (keyValues != null) {
               int c = 1;
               for (String value : keyValues) {
                  sql.setString(c++, value);
               } // next key
            } // key values specified in path
            ResultSet rs = sql.executeQuery();
            JsonGenerator json = Json.createGenerator(response.getWriter());
            if (keyValues == null || partialKey) {
               json.writeStartArray(); // all rows, start an array
            }
            int rowCount = 0;
            try {
               while (rs.next()) {
                  ResultSetMetaData meta = rs.getMetaData();
                  json.writeStartObject();
                  int c = 1;
                  try {
                     for (String column: allColumns) {
                        String value = rs.getString(column);
                        if (value == null) {
                           json.writeNull(column);
                        } else {
                           try {
                              switch(meta.getColumnType(c++)) { // get the type right
                                 case Types.BIGINT:   json.write(column, rs.getLong(column)); break;
                                 case Types.BIT:      json.write(column, rs.getInt(column) != 0); break;
                                 case Types.BOOLEAN:  json.write(column, rs.getBoolean(column)); break;
                                 case Types.DECIMAL:  json.write(column, rs.getDouble(column)); break;
                                 case Types.DOUBLE:   json.write(column, rs.getDouble(column)); break;
                                 case Types.FLOAT:    json.write(column, rs.getDouble(column)); break;
                                 case Types.NUMERIC:  json.write(column, rs.getDouble(column)); break;
                                 case Types.INTEGER:  json.write(column, rs.getInt(column)); break;
                                 case Types.SMALLINT: json.write(column, rs.getInt(column)); break;
                                 case Types.TINYINT:  json.write(column, rs.getInt(column)); break;
                                 case Types.NULL:     json.writeNull(column); break;
                                 default:             json.write(column, value); break;
                              }
                           } catch (SQLDataException x) { // can't determine the type?
                              json.write(column, value);
                           }
                           
                        } // no null
                     } // next column
                  } finally {
                     json.writeEnd();
                  }
                  rowCount++;
               } // next row
               if (rowCount == 0 && keyValues != null && !partialKey) {
                  response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                  // JsonWriter hates to write nothing, so give it an empty object
                  json.writeStartObject().writeEnd();
               }
            } finally {
               if (keyValues == null || partialKey) {
                  json.writeEnd(); // all rows, finish array
               }
               json.close();
               
               rs.close();
               sql.close();
               connection.close();
            }
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("TableServletBase GET: ERROR: " + exception);
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
      } else if (!create) {
         response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      } else {
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");

         StringBuilder query = new StringBuilder();
         StringBuilder parameters = new StringBuilder();
         Vector<String> allColumns = new Vector<String>(keys);
         allColumns.addAll(columns);
         for (String column : allColumns) {
            log("POST col " + column);
            // skip auto-generated key
            if (column.equals(autoKey)) continue;
            
            if (query.length() == 0) {
               query.append("INSERT INTO ");
               query.append(table);
               query.append(" (");
            } else {
               query.append(", ");
               parameters.append(", ");
            }
            query.append(column);
            parameters.append("?");
         } // next column
         query.append(") VALUES (");
         query.append(parameters);
         query.append(")");
         
         // read the incoming object
         JsonReader reader = Json.createReader(request.getReader());
         JsonObject json = reader.readObject();
         StringBuffer key = new StringBuffer();
         try {
            
            // insert the row
            Connection connection = db.newConnection();
            log("POST " + query.toString()); // TODO remove
            PreparedStatement sql = connection.prepareStatement(query.toString());
            int c = 1;
            for (String column : keys) {
               
               // skip auto-generated key
               if (column.equals(autoKey)) continue;
               
               String value = json.get(column).toString();
               if (json.get(column).getValueType() == JsonValue.ValueType.STRING) {
                  value = json.getString(column);
               }
               sql.setString(c++, value); 
               key.append("/");
               key.append(value);
            } // next key
            for (String column : columns) {
               String value = json.get(column).toString();
               if (json.get(column).getValueType() == JsonValue.ValueType.STRING) {
                  value = json.getString(column);
               }
               sql.setString(c++, value);
            } // next column
            try {
               int rows = sql.executeUpdate();
               if (rows == 0) {
                  response.setStatus(HttpServletResponse.SC_CONFLICT);
                  returnMessage("Record not added: " + key, response);
               } else {

                  // if the key was auto-generated
                  if (autoKey != null) {
                     // get it's value
                     PreparedStatement sqlLastId = connection.prepareStatement(
                        "SELECT LAST_INSERT_ID()");
                     ResultSet rsLastId = sqlLastId.executeQuery();
                     try {                        
                        rsLastId.next();
                        // copy object...
                        JsonObjectBuilder newJson = Json.createObjectBuilder();
                        for (Entry<String, JsonValue> entry : json.entrySet()) {
                           newJson.add(entry.getKey(), entry.getValue());
                        } // next entry
                        // then add the key attribute
                        newJson.add(autoKey, rsLastId.getLong(1));
                        json = newJson.build();
                     } finally {
                        rsLastId.close();
                        sqlLastId.close();
                     }
                  }
                  
                  // record added, so return it
                  JsonWriter writer = Json.createWriter(response.getWriter());
                  try {
                     writer.writeObject(json);
                  } finally {
                     writer.close();
                  }
               }
            } finally {
               sql.close();
               connection.close();
            }
         } catch(SQLIntegrityConstraintViolationException exception) {
            // row is already there
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            returnMessage("Row already exists: " + key, response);
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("TableServletBase POST: ERROR: " + exception);
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
      } else if (!update) {
         response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      } else {
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");

         // prepare the UPDATE
         StringBuilder query = new StringBuilder();
         Vector<String> allColumns = new Vector<String>(keys);
         allColumns.addAll(columns);
         for (String column : columns) {
            if (query.length() == 0) {
               query.append("UPDATE ");
               query.append(table);
               query.append(" SET ");
            } else {
               query.append(", ");
            }
            query.append(column);
            query.append(" = ?");
         } // next column
         StringBuilder where = new StringBuilder();
         for (String column : keys) {
            if (where.length() == 0) {
               where.append(" WHERE ");
            } else {
               where.append(" AND ");
            }
            where.append(column);
            where.append(" = ?");
         } // next column
         query.append(where);

         try {
            Connection connection = db.newConnection();
            log("PUT " + query.toString()); // TODO remove
            PreparedStatement sql = connection.prepareStatement(query.toString());

            try {
               // read the incoming object
               JsonReader reader = Json.createReader(request.getReader());
               JsonObject json = reader.readObject();
               int c = 1;
               StringBuffer key = new StringBuffer();
               for (String column : columns) {
                  String value = json.get(column).toString();
                  if (json.get(column).getValueType() == JsonValue.ValueType.STRING) {
                     value = json.getString(column);
                  }
                  sql.setString(c++, value);
               } // next column
               for (String column : keys) {
                  String value = json.get(column).toString();
                  if (json.get(column).getValueType() == JsonValue.ValueType.STRING) {
                     value = json.getString(column);
                  }
                  if (key.length() > 0) key.append("/");
                  key.append(value);
                  sql.setString(c++, value); 
               } // next key
               
               int rows = sql.executeUpdate();
               if (rows == 0) {
                  response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                  returnMessage("Record not found: " + key, response);
               } else {
                  
                  // record update, so return it
                  JsonWriter writer = Json.createWriter(response.getWriter());
                  try {
                     writer.writeObject(json);
                  } finally {
                     writer.close();
                  }
               }
            } finally {
               sql.close();
               connection.close();
            }
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
            log("TableServletBase POST: ERROR: " + exception);
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
      } else if (!delete) {
         response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      } else {
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         try {
            String[] keyValues = null;
            if (request.getPathInfo() != null && !request.getPathInfo().equals("/")) {
               keyValues = request.getPathInfo().substring(1).split("/");
               // only accept a path if all keys have a value
               if (keyValues.length != keys.size()) keyValues = null;
            }
            if (keyValues == null) {
               response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
               returnMessage("Key values not found in path: " + request.getPathInfo(), response);
               return;
            }

            StringBuilder query = new StringBuilder();
            StringBuilder parameters = new StringBuilder();
            query.append("DELETE FROM ");
            query.append(table);
            StringBuffer where = new StringBuffer(); 
            if (whereClause != null && whereClause.length() > 0) {
               where.append(whereClause);
            }
            for (String column : keys) {
               if (where.length() > 0) {
                  where.append(" AND ");
               }
               where.append(column);
               where.append(" = ?");
            } // next key
            query.append(" WHERE ");
            query.append(where);
            
            Connection connection = db.newConnection();
            log("DELETE " + request.getPathInfo() + " : " + query.toString()); // TODO remove
            PreparedStatement sql = connection.prepareStatement(query.toString());
            try {
               int c = 1;
               for (String value : keyValues) {
                  sql.setString(c++, value); 
               } // next key

               int rows = sql.executeUpdate();
               if (rows == 0) {
                  response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                  returnMessage("Record doesn't exist: " + request.getPathInfo(), response);
               } 
               
            } finally {
               sql.close();
            }
         } catch(SQLException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            returnMessage("ERROR: " + exception.getMessage(), response);
         }
      } 
   }

   private static final long serialVersionUID = 1;
} // end of class TableServletBase
