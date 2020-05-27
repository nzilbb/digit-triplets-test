<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%>
<div class="htmlContent">${htmlContent}</div>

<c:if test="${!empty mailError}">
<!--
    ERROR SENDING EMAIL:
    ${mailError}
-->
</c:if>
