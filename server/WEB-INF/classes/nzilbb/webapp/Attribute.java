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

import java.util.Vector;
import javax.servlet.annotation.WebServlet;

/**
 * Servlet that allows access to system attributes.
 * @author Robert Fromont robert@fromont.net.nz
 */
@WebServlet(
   urlPatterns = "/attribute/*",
   loadOnStartup = 20)
public class Attribute extends TableServletBase {   

   public Attribute() {
      super("attribute", // table
            new Vector<String>() {{ add("attribute"); }}, // keys
            new Vector<String>() {{ // columns
               add("value");
            }},
            "attribute <> 'version'", // where
            "attribute", // order
            false, // create
            true, // read
            false, // update
            false); // delete
   }

   private static final long serialVersionUID = 1;
} // end of class Attribute
