<%@ taglib prefix="hex" tagdir="/WEB-INF/tags"  
%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<h1>${page.title}</h1>

<form method="post">
  <input type="hidden" name="file" value="${file}"
  <table class="edit_page">
    <tr>
      <td>
	<hex:richtextarea
	   id="htmlContent"
	   height="400"
	   folder="/${module}/files"
	   ui="Full"
	   languages="${languages}"
	   >${htmlContent}</hex:richtextarea>
      </td>
    </tr>  
    <tr>
      <td style="text-align: right;">
	<hex:submitbutton text="${resources['Save']}" name="editForm" title="${resources['Save']}" />
      </td>
    </tr>
  </table>
</form>
