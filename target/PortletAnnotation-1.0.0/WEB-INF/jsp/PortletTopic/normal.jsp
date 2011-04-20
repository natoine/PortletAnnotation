<%@ page language="java" 
	import="javax.portlet.*"
	import="java.util.List"
	import="fr.natoine.model_annotation.Topic"
	import="fr.natoine.PortletTopic.PortletTopicsList"
%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<link href="<%=request.getContextPath()%>/css/PortletTopicsList/all.css" rel=stylesheet />
<portlet:defineObjects/>
<%
	if(request.getAttribute("admin_message")!=null)
	{
		%>
		<div class=error_message>
			<%=request.getAttribute("admin_message")%>
		</div>
		<%
	}
	else
	{
		List _top_topics = (List)request.getAttribute("topics_list");
	%>
		<div class=topics_list >
	<%
		if(_top_topics == null ||_top_topics.size() == 0)
		{
		%>
			Pas de topics pour l'instant.
		<%
		}
		else
		{
			String _html = PortletTopicsList.processTopicListView(_top_topics , request);
		%>
			<%=_html %>
		<%
		}
	%>
		</div>
	<%
	}
%>

<form id=updateRefForm method=post action="<portlet:actionURL/>">
	<input name="op" type="hidden" value="to_load_url" />
	<input id="updateRefForm_url" name="url" type="hidden" value="" />
</form>

<script>
function updateRef(url)
{
	document.getElementById("updateRefForm_url").value = url;
	document.forms['updateRefForm'].submit();
}
</script>