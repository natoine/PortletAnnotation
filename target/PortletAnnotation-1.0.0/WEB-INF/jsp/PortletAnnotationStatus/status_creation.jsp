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
	int cpt_form = 0 ;
	String servlet_json_tags = (String)request.getAttribute("servlet_json_tags");
	%>
	<div class=status_content id=status_content>
	<span class=header>Créer le contenu du formulaire d'annotation : </span>
		<br/>
		<button onclick="addField('<%=servlet_json_tags %>');">Ajouter un champ</button>
	</div>
	
	<div class=creation_statut>
	<span class=header>Créer un nouveau statut : </span>
	</div>
	<form id="doCreateSimpleAnnotationStatus" method="post" action="<portlet:actionURL/>">
		<input name="op" type="hidden" value="create_annotation_status" />
		<input type="hidden" id="json_descripteur_<%=cpt_form %>" name="status_descripteur" value=""/>
		
		<table>
			<tr>
				<td>Titre du statut : </td>
				<td><input type="text" id="status_title" name="status_title" value="" /></td>
			</tr>
			<tr>
				<td>Commentaire : </td>
				<td><input type="text" id="status_comment" name="status_comment" value="" /></td>
			</tr>
			<tr>
				<td>Couleur : </td>
				<td><input type="text" id="status_color" name="status_color" value="" /></td>
			</tr>
		</table>
		
		<input type="submit" value="Créer le statut" onclick="validateCreateAnnotationStatusForm('<%=cpt_form %>');"/>
	</form>
	<%
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
		<span class=header>Créer un statut fils : </span>
		<br/>
		<%
		for(int i = 0 ; i<status_size ; i++)
		{
			cpt_form ++ ;
			AnnotationStatus _status = (AnnotationStatus)status.get(i);
			%>
			<a title="<%=_status.getComment()%>" onclick="showForm('createStatus_<%=_status.getId()%>');"><%=_status.getLabel()%></a>
			<div id="createStatus_<%=_status.getId() %>" style="display : none;">
				<div class=creation_statut>
				<span class=header>Créer un nouveau statut Fils: </span>
				</div>
				<form id="doCreateSimpleAnnotationStatus_<%=_status.getId() %>" method="post" action="<portlet:actionURL/>">
					<input name="op" type="hidden" value="create_annotation_status" />
					<input type="hidden" id="json_descripteur_<%=cpt_form %>" name="status_descripteur" value=""/>
					<input name="father_id" type="hidden" value="<%=_status.getId()%>"/>
				<table>
				<tr>
					<td>Titre du statut : </td>
					<td><input type="text" id="status_title" name="status_title" value="" /></td>
				</tr>
				<tr>
					<td>Commentaire : </td>
					<td><input type="text" id="status_comment" name="status_comment" value="" /></td>
				</tr>
				<tr>
					<td>Couleur : </td>
					<td><input type="text" id="status_color" name="status_color" value="" /></td>
				</tr>
				</table>
		
					<input type="submit" value="Créer le statut" onclick="validateCreateAnnotationStatusForm('<%=cpt_form%>');"/>
				</form>	
			</div>
			<br/>
			<%
		}
		%>
		</div>
		<%
	}
%>
<script src="<%=request.getContextPath()%>/javascript/jquery.js"></script>
<script  type="text/javascript" src="<%=request.getContextPath()%>/javascript/dynamicForm.js">
</script>