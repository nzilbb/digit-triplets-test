<%@ page import = "java.util.*" 
%><%@ page import = "java.sql.*" 
%><%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%><%@ taglib  prefix="db" uri="/WEB-INF/dbtags.tld" 
%><%
   Page pg = (Page) request.getAttribute("page");
   nz.net.fromont.hexagon.Module module = pg.getModule();
%><c:set var="db_table" scope="request"><db:table 
   type="editdelete" 
   insert="true"
   view="list"
   tableName="${module}_form_field_option"
      title="<%=pg.localizePattern(\"Options for field ''{0}''\", request.getParameter(\"field\"))%>"
   connection="${page.connection}"
   htmlTableProperties="border=0 cellpadding=0 cellspacing=5"
   deleteButton="${template_path}icon/user-trash.png"
   insertButton="${template_path}icon/document-new.png"
   saveButton="${template_path}icon/document-save.png"
   >
  <db:field
     name="field"
     size="10"
     label="${resources['ID']}"
     description="${resources['Unique name for this field']}"
     type="string"
     required="true"
     isId="true"
     linkAs="field"
     access="hidden"
     defaultValue="${param['field']}"
     forceValue="${param['field']}"
     where="1"
     />
  <db:field
     name="value"
     label="${resources['Value']}"
     description="${resources['The value to be sent if this option is selected']}"
     type="string"     
     size="10"
     isId="true"
     required="true"
     access="readwriteonce"
     />
  <db:field
     name="description"
     label="${resources['Description']}"
     description="${resources['The text to display to the user for this option']}"
     type="string"     
     size="10"
     required="true"
     access="readwrite"
     languages="${languages}"
     language="${language}"
     />
  <db:field
     name="display_order"
     label="${resources['Order']}"
     type="integer"     
     access="readwrite"
     size="1"
     order="0"
     newValueQuery="SELECT COALESCE(MAX(`display_order`), 0) + 1  FROM ${module}_form_field_option"
     />
  <db:field
     name="update_date"
     label="Update Date"
     type="timestamp"
     access="hidden"
     calendar="<%=(Calendar)session.getAttribute(\"java.util.Calendar\")%>"
     defaultValue="now()"
     required="false"
     />
  <db:field
     name="update_user_id"
     label="Update User"
     type="string"
     access="hidden"
     forceValue="{user}"
     required="false"
    />
</db:table></c:set><%
   pg.addBreadCrumb(pg.localize("Fields"), "fields");
   pg.addBreadCrumb(request.getParameter("field"));
   pg.addBreadCrumb(pg.localize("Options"));

   PreparedStatement sql = pg.prepareStatement(
      "SELECT * FROM "+module+"_form_field WHERE field = ? AND type = 'select'"); 
   sql.setString(1, request.getParameter("field"));
   ResultSet rs = sql.executeQuery();	
   if (!rs.next())
   {
      pg.addError(pg.localizePattern("The field ''{0}'' is not currently a 'select' field, so the options specified here will not be displayed", request.getParameter("field")));
   }
   rs.close();
   sql.close();
%>
