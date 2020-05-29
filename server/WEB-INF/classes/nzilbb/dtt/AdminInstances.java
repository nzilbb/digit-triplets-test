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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import nzilbb.webapp.TableServletBase;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * Servlet that allows listing of instances.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/admin/instances/*",
   loadOnStartup = 20)
public class AdminInstances extends TableServletBase {   

   public AdminInstances() {
      super("instance", // table
            new Vector<String>() {{ add("instance_id"); }}, // keys
            new Vector<String>() {{ // columns
               add("other_instance_id");
               add("user_agent");
               add("ip");
               add("start_time");
               add("end_time");
               add("trial_set_id");
               add("test_result");
               add("mean_snr");
               add("mode");
            }},
            null, // where
            "start_time DESC", // order
            false, // create
            true, // read
            false, // update
            false); // delete
   }

   /**
    * GET handler lists all rows. 
    * <p> The return is JSON encoded, unless the "Accept" request header, or the "Accept"
    * request parameter, is "text/csv", in which case CSV is returned.
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

      // This catches CSV export, which needs to include field values as well

      if (db == null || db.getVersion() == null) { // not installed yet
         response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      } else if (!read) {
         response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      } else {
         // if it's not a CSV request, just use the default implementation
         if (request.getHeader("Accept").indexOf("text/csv") < 0
             && !"text/csv".equals(request.getParameter("Accept"))) {
            super.doGet(request, response);
         } else { // CSV - we need to include field values too
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + table + ".csv");
            response.setCharacterEncoding("UTF-8");
            try {
               Connection connection = db.newConnection();

               Vector<String> allColumns = new Vector<String>(keys);

               // load the list of fields
               PreparedStatement sql = connection.prepareStatement(
                  "SELECT field FROM form_field ORDER BY display_order");
               ResultSet rs = sql.executeQuery();
               Vector<String> fields = new Vector<String>();
               while (rs.next()) {
                  // add to colunm list
                  fields.add(rs.getString("field"));
                  // also add to select list
                  allColumns.add("value_" + rs.getString("field"));
               }
               rs.close();
               sql.close();

               // fill in the list of columns
               allColumns.addAll(columns);

               // build a query to include all field values
               StringBuilder query = new StringBuilder();
               query.append("SELECT *");
               for (String field : fields) {
                  query.append(", `field_");
                  query.append(field);
                  query.append("`.value AS `value_");
                  query.append(field);
                  query.append("`");
               } // next field
               query.append(" FROM instance");
               for (String field : fields) {
                  query.append(" LEFT OUTER JOIN instance_field `field_");
                  query.append(field);
                  query.append("` ON `field_");
                  query.append(field);
                  query.append("`.instance_id = instance.instance_id AND `field_");
                  query.append(field);
                  query.append("`.field = ?");
               } // next field
               query.append(" ORDER BY start_time DESC");
               sql = connection.prepareStatement(query.toString());
               int f = 1;
               for (String field : fields) sql.setString(f++, field);
               rs = sql.executeQuery();
               
               CSVPrinter csvOut
                  = new CSVPrinter(response.getWriter(), CSVFormat.EXCEL.withDelimiter(','));

               boolean headersWritten = false;
               while (rs.next()) {
                  headersWritten = outputRow(
                     rs, allColumns, rs.getMetaData(), csvOut, headersWritten);
               } // next row
               if (csvOut != null) {
                  csvOut.close();
               }
               
               rs.close();
               sql.close();
               connection.close();
            } catch(SQLException exception) {
               response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
               returnMessage("ERROR: " + exception.getMessage(), response);
               log("AdminInstances GET: ERROR: " + exception);
            }
         } // csv request
      } // installed
   } // doGet

   private static final long serialVersionUID = 1;
} // end of class AdminInstances
