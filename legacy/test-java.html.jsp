<%@ taglib prefix="hex" tagdir="/WEB-INF/tags" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%>
<h2>${pg.title}</h2>

<div class="htmlContent">${htmlContent}</div>

<form id="frmDTT" action="results" method="post">
<div class="hiddeninput">
 <input type="hidden" name="m" id="Mode" value="${Mode}" />
 <input type="hidden" name="InstanceId" id="InstanceId" value="${InstanceId}" />
 <input type="hidden" name="TrialSetNumber" id="TrialSetNumber" value="" />
 <input type="hidden" name="TrialsCsv" id="TrialsCsv" value="" />
 <input type="hidden" name="TestResult" id="TestResult" value="" />
 <input type="hidden" name="MeanSNR" id="MeanSNR" value="" />
</div>
<applet
   width="600" height="400"  
   code="nz.ac.canterbury.nzilbb.digittripletstest.DigitTripletsTest"
   archive="jar/digittripletstest${Mode}-${language}.jar?${jarVersion}" mayscript="mayscript">
</applet>
</form>
