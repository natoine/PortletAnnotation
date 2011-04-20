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
		<div class=add_top_topic>
			<form id="addTopTopic" method="post" action="<portlet:actionURL/>">
				<input name="op" type="hidden" value="addTopTopic" />
				Label : <input type=text name=topic_label></input>
				Description : <textarea rows="3" cols="20" name=topic_description></textarea>
				<input type="submit" value="Créer ce Topic" />
			</form>
		</div>
	<%
		if(_top_topics == null ||_top_topics.size() == 0)
		{
			%>
			Pas de topics pour l'instant.
			<%
		}
		else
		{
			String _html = PortletTopicsList.processTopicListEdit(_top_topics , request);
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

<form id=addChildTopicHiddenForm method=post action="<portlet:actionURL/>">
	<input name=op type=hidden value=addChildTopic />
	<input id="addChildTopicHidden_father_id" name=father_id type=hidden value="" />
	<input id="addChildTopicHidden_topic_label" name=topic_label type=hidden value="" />
	<input id="addChildTopicHidden_topic_description" name=topic_description type=hidden value="" />
</form>

<script>
function updateRef(url)
{
	document.getElementById("updateRefForm_url").value = url;
	document.forms['updateRefForm'].submit();
}
function updateFormAddTopicChild(topic_id)
{
	document.getElementById("addChildTopicHidden_father_id").value = document.getElementById("father_id_" + topic_id).value;
	document.getElementById("addChildTopicHidden_topic_label").value = document.getElementById("topic_label_" + topic_id).value;
	document.getElementById("addChildTopicHidden_topic_description").value = document.getElementById("topic_description_" + topic_id).value;
	document.forms['addChildTopicHiddenForm'].submit();
}
</script>