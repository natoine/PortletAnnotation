<%@ 
	page language="java" 
	import="javax.servlet.*"
    pageEncoding="UTF-8"
%>
<%
	String json = (String)request.getAttribute("json");
if(json != null && json.length() > 0 )
{
	%>
	<%=json %>
	<%
}
%>