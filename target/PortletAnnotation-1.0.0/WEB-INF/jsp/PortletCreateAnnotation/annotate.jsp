<%@ page language="java" 
	import="javax.portlet.*"
	import ="java.util.ArrayList"
	import ="java.util.List"
	import = "fr.natoine.model_htmlDocs.HighlightSelectionHTML"
	import = "fr.natoine.model_htmlDocs.WebPage"
	import = "fr.natoine.model_user.UserAccount"
	import = "fr.natoine.model_resource.Resource"
	import = "fr.natoine.model_annotation.AnnotationStatus"
%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<link href="<%=request.getContextPath()%>/css/PortletCreateAnnotation/listeAnnotation.css" rel=stylesheet></link>

<portlet:defineObjects/>
<%
if(request.getAttribute("url_servlet") == null || ((String)request.getAttribute("url_servlet")).equalsIgnoreCase(""))
{
	%>
	<div class=message> L'admin doit d'abord configurer la servlet de création des annotations en mode EDIT !!! </div>
	<%
}
else
{
	UserAccount _user = (UserAccount)request.getAttribute("user");
	ArrayList _selections = (ArrayList)request.getAttribute("selections");
	String _current_url = (String)request.getAttribute("url");
	ArrayList<HighlightSelectionHTML> _coloreds = (ArrayList<HighlightSelectionHTML>)request.getAttribute("colored_selections");
	String uniqueId_for_form = "portletCreateAnnotation";
%>
	<%
	if(_user == null)
	{
		%>
		<div class=user_pseudo> User : guest </div>
		<%
	}
	else
	{
		%>
		<div class=user_pseudo> User : <%= _user.getPseudonyme() %> </div>
		<%
	}
	%>
	<div class=current_url>URL en cours : <%=(String)request.getAttribute("url") %></div>
	<%
	if(_selections.size() == 0) 
	{
		%>
		<div class=message>Vous n'avez fait aucune sélection pour l'instant.</div>
		<%
	}
	else
	{
		%>
	<div class=panier>
	<table class="liste_selections">
	<caption>Liste de mes sélections</caption>
		<%
		int _cpt_selection = 0 ;
		for(Object _selection : _selections)
		{
			%>
			<tr>
				<td>
				<%
				if(_selection instanceof HighlightSelectionHTML)
				{
					boolean _to_color = false ;
					boolean _to_uncolor = false ;
					String _origin_url = ((HighlightSelectionHTML)_selection).getSelection().getSelectionOrigin().getRepresentsResource().getEffectiveURI() ;
					if(_current_url != null && _origin_url.compareTo(_current_url) == 0)
					{
						_to_color = true ;
						if(_coloreds != null)
						{
							for(HighlightSelectionHTML _colored : _coloreds)
							{
								if(_colored.getId().compareTo(((HighlightSelectionHTML)_selection).getId())==0)
								{
									_to_uncolor = true ;
									break ; //sortir du parcours des déjà colorés
								}
							}
						}
					}
					%>
					<div class=selection>
					<div class=selection_header>
						<div class=extrait_selection>
						<input type=checkbox id=selection_<%=_cpt_selection%> name=checkbox_to_annotate_<%=uniqueId_for_form %>></input>
						<span>Extrait de : <a href="<portlet:actionURL><portlet:param name='op' value='load_page'/><portlet:param name='url_origin' value='<%=_origin_url%>'/></portlet:actionURL>"><%=_origin_url%></a></span>
						</div>
						<div class=suppr>
						<a href="<portlet:actionURL><portlet:param name="op" value="delete_selection"/><portlet:param name="cpt_selection" value="<%="" + _cpt_selection %>"/></portlet:actionURL>" >Supprimer</a>
						</div>
					</div>
					<div class=selection_content><%=((HighlightSelectionHTML)_selection).getSelection().getHTMLContent() %></div>
					<%
					if(_to_color)
					{
						if(_to_uncolor)
						{
							%>
							<div class=uncolor>
							<form id="doUnColorSelection" method="post" action="<portlet:actionURL/>">
								<input name="op" type="hidden" value="uncolor_selection" />
								<input name="to_uncolor" type="hidden" value="<%="" + ((HighlightSelectionHTML)_selection).getId() %>" />
								<input type="submit" value="Décolorer" />
							</form> 
							</div>
							<%
						}
						else
						{
						%>
						<div class=color>
						<form id="doColorSelection" method="post" action="<portlet:actionURL/>">
							<input name="op" type="hidden" value="color_selection" />
							<input name="to_color" type="hidden" value="<%="" + ((HighlightSelectionHTML)_selection).getId() %>" />
							<input type="submit" value="Colorer" />
						</form> 
						</div>
						<%
						}
					}
					%>
					</div>
					<%
				}
				else if(_selection instanceof WebPage)
				{
					%>
					<div class=webpage>
					<div class=webpage_header>
						<div class=extrait_webpage>
							<input type=checkbox id=webpage_<%=_cpt_selection%> name=checkbox_to_annotate_<%=uniqueId_for_form %>></input>
							<span>Page : <a href="<portlet:actionURL><portlet:param name='op' value='load_page'/><portlet:param name='url_origin' value='<%=((WebPage)_selection).getRepresentsResource().getEffectiveURI()%>'/></portlet:actionURL>"><%=((WebPage)_selection).getLabel() %> <%=((WebPage)_selection).getRepresentsResource().getEffectiveURI() %></a></span>
						</div>
						<div class=suppr>
							<a href="<portlet:actionURL><portlet:param name="op" value="delete_selection"/><portlet:param name="cpt_selection" value="<%="" + _cpt_selection %>"/></portlet:actionURL>" >Supprimer</a>
						</div>
					</div>
					<div class=webpage_content>
					[ Page entière ] </br>
					</div>
					</div>
					<%
				}
				else if(_selection instanceof Resource)
				{
					%>
					<div class=resource>
					<div class=resource_header>
						<div class=extrait_resource>
							<input type=checkbox id=resource_<%=_cpt_selection%> name=checkbox_to_annotate_<%=uniqueId_for_form %>></input>
							<span>Ressource : <%=((Resource)_selection).getLabel() %></span>
						</div>
						<div class=suppr>
							<a href="<portlet:actionURL><portlet:param name="op" value="delete_selection"/><portlet:param name="cpt_selection" value="<%="" + _cpt_selection %>"/></portlet:actionURL>" >Supprimer</a>
						</div>
					</div>
					<div class=resource_content>
					[ Une ressource entière ] </br>
					<%=((Resource)_selection).getRepresentsResource().getEffectiveURI() %>
					</div>
					</div>
					<%
				}
				else
				{
					%>
					<div class=unknown>
					<div class=unknown_header>
						<div class=extrait_unknown>
							<input type=checkbox id=resource_<%=_cpt_selection%> name=checkbox_to_annotate_<%=uniqueId_for_form %>></input>
							<span>Unknown type</span>
						</div>
						<div class=suppr>
							<a href="<portlet:actionURL><portlet:param name="op" value="delete_selection"/><portlet:param name="cpt_selection" value="<%="" + _cpt_selection %>"/></portlet:actionURL>" >Supprimer</a>
						</div>
					</div>
					<div class=unknown_content>
					You should delete this</br>
					no url ...
					</div>
					</div>
					<%
				}
				%>
			</td>
			</tr>
			<%
			_cpt_selection ++ ;
		}
		%>
		</table>
		<%
		if(request.getAttribute("annotation_status") != null)
		{
			AnnotationStatus status = (AnnotationStatus)request.getAttribute("annotation_status");
		%>
		<div class=annotation_form>
			<div class=annotation_form_header title="<%=status.getComment()%>" >
			<%=status.getLabel().toUpperCase() %>
			</div>
		<form id="doAnnotation" method="post" action="<portlet:actionURL/>">
			 <input name="op" type="hidden" value="create_annotation" />
			 <input type="hidden" id="list_selection_to_annotate_<%=uniqueId_for_form %>" name="list_selection_to_annotate" value=""/>
				<%
				if(! status.getLabel().equalsIgnoreCase("tag"))
				{
					%>
					<div class=formelt>
					<span class=eltheader>Titre : </span><input id="annotation_title" name="annotation_title" type="text" value=""/> <br/>
					</div>
					<%
				}
				%>
				<%=status.getHTMLForm(uniqueId_for_form) %>
				<div class=validate_form_annotation>
				<input type="submit" value="Annoter" onclick="validateForm('<%=uniqueId_for_form %>');"/>
				</div>
		</form>
		</div>
		<%
		}
		else
		{
		%>
			<div class=message>Choisissez un formulaire d'annotation au préalable !!!</div>
		<%
		}
		%>
	</div>
	<div class=clear_annotation>
		<form id="doClearSelection" method="post" action="<portlet:actionURL/>">
			<input name="op" type="hidden" value="clear_selections" />
			<input type="submit" value="Vider la liste de sélections" />
		</form> 
	</div>
		<%
	}
}
%>
<script  type="text/javascript" src="<%=request.getContextPath()%>/javascript/validateFormCreateAnnotation.js">
</script>