<%@ page import = "java.util.*" 
%><%@ page import = "nz.net.fromont.hexagon.*" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%><%@ taglib  prefix="db" uri="/WEB-INF/dbtags.tld" 
%><%
   Page pg = (Page) request.getAttribute("page");
%><c:set var="db_table" scope="request"><db:table 
   type="editdelete" 
   insert="true"
   view="form"
   columns="3"
   tableName="${module}_form_field"
   title="Form Fields"
   connection="${page.connection}"
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
     access="readwriteonce"
     />
  <db:field
     name="type"
     label="${resources['Type']}"
     description="${resources['What kind of data will be entered']}"
     type="string"     
     required="true"
     access="readwrite"
     optionValues="${resources['string:Short Text|text:Long Text|number:Number|email:Email address|select:Select from options']}"
     />
  <db:field
     name="size"
     label="${resources['Size']}"
     description="${resources['The size of the box e.g. 25 or 80x5']}"
     type="string"     
     size="2"
     required="true"
     access="readwrite"
     defaultValue="15"
     />
  <db:field
     name="name"
     label="${resources['Name']}"
     description="${resources['Label to display on the form']}"
     type="string"
     required="true"
     access="readwrite"
     languages="${languages}"
     language="${language}"
     columns="2"
     size="20"
     />
  <db:field
     name="required"
     label="${resources['Required']}"
     type="integer"     
     required="true"
     access="readwrite"
     optionValues="${resources['1:Required|0:Optional']}"
     />
  <db:field
     name="description"
     label="${resources['Description']}"
     description="${resources['Longer description of the field']}"
     type="string"
     required="true"
     access="readwrite"
     languages="${languages}"
     language="${language}"
     size="30"
     columns="2"
     />
  <db:field
     name="display_order"
     label="${resources['Order']}"
     type="integer"     
     access="readwrite"
     size="1"
     order="0"
     newValueQuery="SELECT COALESCE(MAX(`display_order`), 0) + 1  FROM ${module}_form_field"
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
  <db:link
     text="${resources['Options']}"
     url="field_options"
     icon="${template_path}icon/document-properties.png"
     />
</db:table></c:set><%
   pg.addBreadCrumb("Fields");
%>
