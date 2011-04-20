<%@ 
	page language="java" 
	import="javax.servlet.*"
%>
<%
String liste = (String)request.getAttribute("liste");
if(liste != null && liste.length() > 0 )
{
	%>
	<%=liste %>
	<%
}
%>