<%@ taglib prefix="hex" tagdir="/WEB-INF/tags" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%>
<h2>${pg.title}</h2>

<div class="htmlContent">${htmlContent}</div>

<form id="frm" action="test" method="post" onsubmit="return form_validate(this);">
  
  <script type="text/javascript">//<![CDATA[
// form validation
function form_validate(frm)
{
    <c:forEach items="${fields}" var="field">
     <c:if test="${field.required ne 0}">
       if (frm.${field.field}.value == '')
       {
          alert('${resources['Please enter a value for ']} ${field.name}');
          frm.${field.field}.focus();
          return false;
       }
     </c:if>
     <c:if test="${field.type eq 'email'}">
       if (frm.${field.field}.value.indexOf('@') < 0)
       {
          alert('${resources['Please enter an email address for ']} ${field.name}');
          frm.${field.field}.focus();
          return false;
       }
     </c:if>
     <c:if test="${field.type eq 'number'}">
       if (isNaN(parseInt(frm.${field.field}.value))) 
       {
       <c:if test="${field.required eq 0}">
	 if (frm.${field.field}.value != '')
       </c:if>
         {
          alert('${resources['Please enter a number for ']} ${field.name}');
          frm.${field.field}.focus();
          return false;
         }
       }
     </c:if>
    </c:forEach>
    if (!document.getElementById('speakers').checked
    && !document.getElementById('headphones').checked)
    {
          alert('${resources['Please choose between headphones and speakers']}');
          document.getElementById('speakers').focus();
          return false;
    }
    return true;
}

//]]></script>
  
  <table class="form">
    <c:forEach items="${fields}" var="field">
      <tr class="form">
	<th class="form" title="${field.description}">
	  ${field.name} 
	  <c:if test="${field.required ne 0}"><span class="form_required">*</span></c:if>
	</th>
	<td class="form">
	  <c:if test="${field.type ne 'text' and field.type ne 'select'}">
	    <input type="text" title="${field.description}"
		   name="${field.field}" value="${field.value}"
		   size="${field.size}"
		   <c:if test="${field.type eq 'number'}">xonchange="if (isNaN(parseInt(this.value))) { <c:if test="${field.required eq 0}">if (this.value != '') </c:if>alert('${resources['Please enter a number for ']} ${field.name}'); this.value = ''; this.focus(); return false; } "</c:if>>
	  </c:if>
	  <c:if test="${field.type eq 'text'}">
	    <textarea title="${field.description}"
		      name="${field.field}"
		      rows="${field.rows}" cols="${field.cols}">${field.value}</textarea>
	  </c:if>
	  <c:if test="${field.type eq 'select'}">
	    <select title="${field.description}"
		    name="${field.field}">
	      <c:if test="${field.required ne 1}"><option></option></c:if>
	      <c:forEach items="${field.options}" var="option">
		<option value="${option.value}" <c:if test="${field.value eq option.value}">selected</c:if>>${option.description}</option>
	      </c:forEach>
	    </select>
	  </c:if>
	</td>
      </tr>
    </c:forEach>
    <tr class="form">
      <td class="form_mode" colspan="2">
	<div class="mode_option"><input type="radio" name="m" value="l" id="headphones" /><label for="headphones">${resources['I have plugged in headphones']}</label></div>
	<div class="mode_option"><input type="radio" name="m" value="" id="speakers" /><label for="speakers">${resources['I have switched my speakers on']}</label></div>
      </td>
    </tr>    
    <tr class="form">
      <td class="form_submit" colspan="2">
	<input id="dtt_start_test" type="submit" value="${resources['Start']}" title="${resources['Start Test']}"/>
      </td>
    </tr>    
  </table>
</form>
