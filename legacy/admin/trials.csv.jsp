<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%>"Number","Signal","Triplet","Entered","Result"<c:forEach items="${trials}" var="trial">
${trial.trial_number},${trial.decibels_signal},"${trial.correct_answer}","${trial.participant_answer}",${trial.correct_answer eq trial.participant_answer}</c:forEach>