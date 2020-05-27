<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%><%@ taglib  prefix="db" uri="/WEB-INF/dbtags.tld" 
%><%@ page import = "java.io.*" 
%><%@ page import = "java.sql.*" 
%><%@ page import = "java.util.zip.*" 
%><%@ page import = "java.util.*" 
%><%@ page import = "nz.net.fromont.hexagon.*" 
%><%
{
   File f = (File)request.getAttribute("prompt");
   InputStream i = new FileInputStream(f);
   OutputStream o = response.getOutputStream();
   byte[] buffer = new byte[1024];
   int bytesRead = i.read(buffer);
   while(bytesRead >= 0)
   {
      o.write(buffer, 0, bytesRead);
      bytesRead = i.read(buffer);
   } // next chunk of data
   i.close();
}%>