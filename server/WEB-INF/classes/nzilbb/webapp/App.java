//
// Copyright 2023-2025 New Zealand Institute of Language, Brain and Behaviour, 
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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that simply serves the content of /index.html,
 * but also adds a Content-Security-Policy HTTP header as well.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
  urlPatterns = "",
  loadOnStartup = 0)
public class App extends HttpServlet {
  /**
   * GET handler
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    String contentSecurityPolicy = getServletContext().getInitParameter(
      "Content-Security-Policy");
    if (contentSecurityPolicy != null) {
      // add Content-Security-Policy header to meet common security requirements
      response.addHeader("Content-Security-Policy", contentSecurityPolicy);
    }
    String strictTransportSecurity = getServletContext().getInitParameter(
      "Strict-Transport-Security");
    if (strictTransportSecurity == null) strictTransportSecurity = "max-age=63072000";
    if (strictTransportSecurity.length() > 0) {
      response.setHeader("Strict-Transport-Security", strictTransportSecurity);
    }

    response.setHeader("Cache-Control", "no-store");
    response.setHeader("X-Content-Type-Options", "nosniff");
    response.setHeader("X-Frame-Options", "DENY");
    // serve the content of "index.html"
    response.setContentType("text/html");
    getServletContext().getRequestDispatcher("/index.html").include(request, response);
  }

  private static final long serialVersionUID = 1;
}
