<%@ taglib prefix="hex" tagdir="/WEB-INF/tags" 
%><%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%>
<h2>${pg.title}</h2>

<div class="htmlContent">${htmlContent}</div>
<div id="test">
  <form id="frmDTT" action="results" method="post">
    <div class="hiddeninput">
      <input type="hidden" name="instance_id" id="instance_id" value="${instance_id}" />
    </div>
    
    <script type="text/javascript">//<![CDATA[

document.onkeydown = checkKey;

function checkKey(e) 
{
    e = e || window.event;
    var btn = e.key; 
    if (e.keyCode == '13') // return
    {
      btn = 'n'; // next
    }
    else if (e.keyCode == '46' // del
             || e.keyCode == '8') // bksp
    {
      btn = 'c'; // clear
    }
    if (!btn)
    { // e.key isn't set
       if (e.keyCode > 58) // keypad
       {
          btn = ''+(e.keyCode - 2*48);
       }
       else // keyboard
       {
          btn = ''+(e.keyCode - 48);
       }
    }

    switch (btn)
    { // only valid keys
      case 'c':
      case 'n':
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
        press(btn);
        break;
    }
}

var bStarted = false;
var enableTimeout;

// button press
function press(btn)
{
  if (btn == "n" && document.getElementById("imgNext").src.indexOf("playing.png") >= 0) return; // disabled
  lastInput = document.getElementById("LastInput");
  document.getElementById("btn" + btn).style.background = "gray";
  setTimeout("document.getElementById('btn" + btn + "').style.background = ''", 200);
  if (btn == "test")
  {
    document.getElementById('player').play();
    return;
  }
  if (btn == "n")
  {
    if (!bStarted)
    {
       start();
    }
    else
    {
       sendAnswer(lastInput.value);
    }
    lastInput.value = "";
    enableNext(false);
  }
  else if (btn == "c")
  {
    lastInput.value = "";
  }
  else 
  {
    if (lastInput.value.length < 3) lastInput.value += btn;
    if (lastInput.value.length >= 3) enableNext(true);
  }
  lastInput.focus();
}

var iProgress = 0;

function start()
{
 try
 {
   document.getElementById("btntest").style.display = "none";
   document.getElementById("dtt_input").style.display = "";
   document.getElementById("LastInput").focus();
   var player = document.getElementById("player");
   document.getElementById("player").src = "trial?i=${instance_id}";
   document.getElementById("player").play();
   var btnNext = document.getElementById("btnNext");
   btnNext.innerHTML = "${resources['Next']}";
   bStarted = true;
 }
 catch (x)
 {
   message(x);
 }
}

function sendAnswer(answer)
{
 try
 {
   var progress = document.getElementById("progress");
   if (iProgress < ${numTrials} - 1)
   {
     var player = document.getElementById("player");
     // TODO send the input to the server, the result is the audio to play
     player.src = "trial?i=${instance_id}&a="+answer+"&t="+iProgress;
     player.play();
     iProgress++;
     if (progress) progress.value++; else document.getElementById("soFar").innerHTML = iProgress;
     if (iProgress >= ${numTrials} - 1)
     {
        var btnNext = document.getElementById("btnNext");
        btnNext.innerHTML = "${resources['Finish']}";
     }
   }
   else
   { // final submit
     iProgress++;
     if (progress) progress.value++; else document.getElementById("soFar").innerHTML = iProgress;
     message("${resources['Please wait']}");
     document.getElementById("LastInput").value = answer;
     document.getElementById("frmDTT").submit();
   }
 }
 catch (x)
 {
   message(x);
 }
}

function enableNext(bEnabled)
{
  if (!bEnabled)
  { // disable
    document.getElementById("imgNext").src = "playing.png";
    enableTimeout = setTimeout("enableNext(true)", ${resources['timeoutseconds']}000);
  }
  else
  {
    if (enableTimeout != null) clearTimeout(enableTimeout);
    document.getElementById("imgNext").src = "play.png";
  }
}

function message(message)
{
     document.getElementById("dtt_message").innerHTML = message;
}

window.onbeforeunload = function()
{
  if (iProgress < ${numTrials}) return "${resources['The test is not finished.  Are you sure you want to cancel?']}";
}

//]]></script>
    
    <audio id="player" src="${volumeCheckFile}" preload="auto"><p>${resources["Audio not supported. Please try with a different browser."]}</p></audio>
    
    <table class="dtt" onselectstart="return false;">
      <thead>
	<tr><td id="dtt_message" colspan="3"></td></tr>
	<tr><td id="btntest" colspan="3" class="dtt_command" onclick="javascript:press('test');" >${resources['Check Volume']}</td></tr>
	<tr><td id="dtt_input" colspan="3" style="display: none;"><input type="tel" autocomplete="off" autofocus="autofocus" name="LastInput" id="LastInput" value="" onkeypress="return false;" /></td></tr>
	<tr><td colspan="3"><progress id="progress" max="${numTrials}" value="0" title="${resources['How far through the test you are']}"><span id="soFar">0</span>/<span id="numTrials">${numTrials}</span></progress></td></tr>
      </thead>
      <tbody>
	<tr>
	  <td id="btn1" onclick="javascript:press('1');" accesskey="1" class="dtt_button">1</td>
	  <td id="btn2" onclick="javascript:press('2');" accesskey="2" class="dtt_button">2</td>
	  <td id="btn3" onclick="javascript:press('3');" accesskey="3" class="dtt_button">3</td>
	</tr>
	<tr>
	  <td id="btn4" onclick="javascript:press('4');" accesskey="4" class="dtt_button">4</td>
	  <td id="btn5" onclick="javascript:press('5');" accesskey="5" class="dtt_button">5</td>
	  <td id="btn6" onclick="javascript:press('6');" accesskey="6" class="dtt_button">6</td>
	</tr>
	<tr>
	  <td id="btn7" onclick="javascript:press('7');" accesskey="7" class="dtt_button">7</td>
	  <td id="btn8" onclick="javascript:press('8');" accesskey="8" class="dtt_button">8</td>
	  <td id="btn9" onclick="javascript:press('9');" accesskey="9" class="dtt_button">9</td>
	</tr>
	<tr>
	  <td id="btnc" onclick="javascript:press('c');" class="dtt_command" title="${resources['Clear all digits entered']}"><img src="clear.png" alt="&times;"/>${resources["Clear"]}</td>
	  <td id="btn0" onclick="javascript:press('0');" accesskey="0" class="dtt_button">0</td>
	  <td id="btnn" onclick="javascript:press('n');" class="dtt_command" title="${resources['Go to the next step']}"><img id="imgNext" src="play.png" alt="&rarr;"/><span id="btnNext">${resources["Start"]}</span></td>
	</tr>
      </tbody>
      <tfoot></tfoot>
    </table>
  </form>
</div>
