<%@ page language="java" 
	import="javax.portlet.*"
	import ="java.util.List"
	import = "fr.natoine.model_annotation.AnnotationStatus"
%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>
<%
	List status = (List)request.getAttribute("status");
	int status_size = status.size();
	if(status_size == 0)
	{
		%>
		Pas de statut existant !!!
		<%
	}
	else
	{
		%>
		<div class=list_status>
		<span class=header>Choisissez votre type d'annotation : </span>
		<br/>
		<%
		for(int i = 0 ; i<status_size ; i++)
		{
			AnnotationStatus _status = (AnnotationStatus)status.get(i);
			%>
			<a title="<%=_status.getComment()%>" href="<portlet:actionURL><portlet:param name="op" value="change_status"/><portlet:param name="id_status" value="<%="" + _status.getId() %>"/></portlet:actionURL>"><%=_status.getLabel()%></a>
			<br/>
			<%
		}
		%>
		</div>
		<%
	}
%>