<%@ taglib prefix="hex" tagdir="/WEB-INF/tags" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%>"ID",<c:forEach var="field" items="${fields}">"${field.field}",</c:forEach>"Start","End","Set","Result","Mean SNR","Trials","IP","Browser"
<c:forEach var="instance" items="${instances}">${instance.instance_id},<c:forEach var="field" items="${fields}">"${instance.fields[field.field]}",</c:forEach>"${instance.start_time}","${instance.end_time}",${instance.trial_set_number},${instance.test_result},${instance.mean_snr},"${site.root}/${module}/admin/trials?instance_id=${instance.instance_id}","${instance.ip}","${instance.user_agent}"
</c:forEach>
