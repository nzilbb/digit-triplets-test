<%@ taglib prefix="hex" tagdir="/WEB-INF/tags" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%>
<h2>${pg.title}</h2>

<p><a href="instances?content-type=text/csv">CSV</a></p>

<table class="instances">
  <thead><tr>
      <th>ID</th>
      <th>Language</th>
      <th>Mode</th>
      <c:forEach var="field" items="${fields}">
	<th title="${field.description}">${field.name}</th></c:forEach>
      <th>Start</th>
      <th>End</th>
      <th>Set</th>
      <th>Result</th>
      <th>Mean SNR</th>
  </tr></thead>
  <tbody>
    <c:forEach var="instance" items="${instances}"><tr>
	<td title="${instance.ip} - ${instance.user_agent}">${instance.instance_id}</td>
	<td>${instance.language}</td>
	<td>${instance.mode}</td>
	<c:forEach var="field" items="${fields}">
	  <td title="${field.name}">${instance.fields[field.field]}</td></c:forEach>
	<td>${instance.start_time_formatted}</td>
	<td>${instance.end_time_formatted}</td>
	<td>${instance.trial_set_number}</td>
	<td>${instance.test_result}</td>
	<td>${instance.mean_snr}</td>
	<td><a href="trials?instance_id=${instance.instance_id}">trials</a></td>
    </tr></c:forEach>
  </tbody>
</table>

