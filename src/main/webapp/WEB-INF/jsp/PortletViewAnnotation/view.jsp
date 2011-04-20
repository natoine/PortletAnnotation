<%@ page language="java" 
	import="javax.portlet.*"
	import="java.util.List"
	import="java.util.ArrayList"
	import="java.util.Date"
	import="java.util.Collection"
	import="fr.natoine.model_annotation.Annotation"
	import="fr.natoine.model_annotation.AnnotationStatus"
	import="fr.natoine.model_annotation.Definition"
	import="fr.natoine.model_annotation.Post"
	import="fr.natoine.model_annotation.TagAgent"
	import="fr.natoine.model_resource.Resource"
	import="fr.natoine.model_htmlDocs.SelectionHTML"
	import="fr.natoine.model_user.UserAccount"
%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<portlet:defineObjects/>
<link href="<%=request.getContextPath()%>/css/ServletViewResources/view.css" rel=stylesheet />
<link href="<%=request.getContextPath()%>/css/PortletCreateAnnotation/listeAnnotation.css" rel=stylesheet />
<%
List<Annotation> _annotations = (List<Annotation>)request.getAttribute("annotations");
ArrayList<String> annotations_content = (ArrayList<String>)request.getAttribute("annotations_content");
ArrayList<Boolean> agreements_expressed = (ArrayList<Boolean>)request.getAttribute("agreements_expressed");
ArrayList<Boolean> flames_expressed = (ArrayList<Boolean>)request.getAttribute("flames_expressed");
ArrayList<Boolean> trolls_expressed = (ArrayList<Boolean>)request.getAttribute("trolls_expressed");
ArrayList<Boolean> spams_expressed = (ArrayList<Boolean>)request.getAttribute("spams_expressed");
List<Annotation> _coloreds = (List<Annotation>)request.getAttribute("colored_annotation");
List<AnnotationStatus> list_annotations_status = (List<AnnotationStatus>)request.getAttribute("list_annotation_status");
String _current_url = (String)request.getAttribute("current_url");
UserAccount user = (UserAccount)request.getAttribute("user");
String uid_for_form = "portletViewAnnotation";
String url_consultation = (String)request.getAttribute("url_consultation") ;
String url_servlet = (String)request.getAttribute("url_servlet") ;
%>
<div class=current_url>current url : <%= _current_url %></div>
<%
		if(request.getAttribute("annotation_status") != null && url_servlet != null)
		{
			AnnotationStatus status = (AnnotationStatus)request.getAttribute("annotation_status");
		%>
		<div class=annotation_form>
		<div class=annotation_form_header title="Pour associer une annotation à cette ressource" onclick="switchMenu('annotation_form_view');">Annotation Rapide</div>
		<div  id=annotation_form_view style="display:none;">
		<form id="doAnnotation" method="post" action="<portlet:actionURL/>">
			<input name="op" type="hidden" value="create_annotation" />
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
				<%=status.getHTMLForm(uid_for_form) %>
				<div class=validate_form_annotation>
				<input type="submit" value="Annoter" onclick="validateForm('<%=uid_for_form %>');"/>
				</div>
		</form>
		</div>
		</div>
		<%
		}
		%>
<%
if(_annotations != null)
{
	if(list_annotations_status != null)
	{
		%>
		<style>
			<%
			for(AnnotationStatus status : list_annotations_status)
			{
				String css = status.toCSS() + "\n";
				 %>
				 <%= css%>
				 <%
			}
			%>
		</style>
		<%
	}
	%>
	<table class=annotation_list>
	<tr>
		<td>Group by : <span><a href="<portlet:actionURL><portlet:param name='op' value='order'/><portlet:param name='annotation_order' value='status'/></portlet:actionURL>">type</a></span><span> , </span><span><a href="<portlet:actionURL><portlet:param name='op' value='order'/><portlet:param name='annotation_order' value='auteur'/></portlet:actionURL>">Auteur</a></span></td>
		<td>
		<a class=order_asc title="classer de la plus récente à la plus ancienne" href="<portlet:actionURL><portlet:param name='op' value='order'/><portlet:param name='annotation_chrono' value='asc'/></portlet:actionURL>"><img src="<%=request.getContextPath()%>/images/down_arrow.png" /></a>
		<a class=order_dsc title="classer de la plus ancienne à la plus récente" href="<portlet:actionURL><portlet:param name='op' value='order'/><portlet:param name='annotation_chrono' value='dsc'/></portlet:actionURL>"><img src="<%=request.getContextPath()%>/images/up_arrow.png" /></a>
		</td>
	</tr>
	<%
	for(int i = 0 ; i < _annotations.size() ; i ++)
	{
		Annotation _current_annotation = _annotations.get(i);
		Date _creation = _current_annotation.getCreation();
		String _title = _current_annotation.getLabel();
		String _status = _current_annotation.getStatus().getLabel();
		long annotation_id = _current_annotation.getId() ;
		String _lien_annotation = _current_annotation.getAccess().getEffectiveURI() + "?id=" + annotation_id; 
		Collection<Resource> _addeds = _current_annotation.getAdded();
		Collection<Resource> _annotateds = _current_annotation.getAnnotated();
		%>
		<tr class=annotation_row>
		<td>
		<%
		if(user != null)
		{
		%>
		<div class="annotation annotation_<%=_status.replaceAll(" " , "_")%>" id=anotation_view_<%=annotation_id%> onclick="showAnnotationContent('annotation_content_<%=annotation_id%>' , '<%=url_consultation%>' , '<%=url_servlet%>' , '<%=annotation_id%>' , '<%=user.getId()%>');"">
		<%
		}
		else
		{
		%>
		<div class="annotation annotation_<%=_status.replaceAll(" " , "_")%>" id=anotation_view_<%=annotation_id%> onclick="showAnnotationContent('annotation_content_<%=annotation_id%>' , '<%=url_consultation%>' , '<%=url_servlet%>' , '<%=annotation_id%>');"">
		<%	
		}
		%>
			<%=annotations_content.get(i)%>
		</div>
		</td>
		<td class=reply>
			<table>
			<tr>
				<td>
				<a href="<portlet:actionURL><portlet:param name='op' value='reply_annotation'/><portlet:param name='url_reply' value='<%=_lien_annotation%>'/></portlet:actionURL>">Répondre à cette annotation</a>
				</td>
			</tr>
			<%
			if(user != null)
			{
				if(!agreements_expressed.get(i))
				{
				%>
				<tr>
					<td>
						<a href="<portlet:actionURL><portlet:param name='op' value='reply_ok'/><portlet:param name='id_annotate' value='<%=""+annotation_id%>'/></portlet:actionURL>">
							<img title="je suis d'accord" src="<%=request.getContextPath()%>/images/ok.jpg"></img>
						</a>
						<a href="<portlet:actionURL><portlet:param name='op' value='reply_not_ok'/><portlet:param name='id_annotate' value='<%=""+annotation_id%>'/></portlet:actionURL>">
							<img title="je ne suis pas d'accord" src="<%=request.getContextPath()%>/images/not_ok.jpg"></img>
						</a>
						<a href="<portlet:actionURL><portlet:param name='op' value='reply_understand'/><portlet:param name='id_annotate' value='<%=""+annotation_id%>'/></portlet:actionURL>">
							<img title="je ne comprends pas" src="<%=request.getContextPath()%>/images/question.jpg"></img>
						</a>
					</td>
				</tr>
				<%
				}
				boolean flame = flames_expressed.get(i);
				boolean troll = trolls_expressed.get(i);
				boolean spam = spams_expressed.get(i);
				%>
				<tr>
					<td>
					<%
					if(!flame)
					{
						%>
						<a href="<portlet:actionURL><portlet:param name='op' value='reply_flame'/><portlet:param name='id_annotate' value='<%=""+annotation_id%>'/></portlet:actionURL>">
						<img title="le contenu est malveillant" src="<%=request.getContextPath()%>/images/flame.jpg"></img>
						</a>
						<%
					}
					else 
					{
						%>
						<span class=flame>contenu malveillant</span>
						<%
					}
					if(!troll)
					{
						%>
						<a href="<portlet:actionURL><portlet:param name='op' value='reply_troll'/><portlet:param name='id_annotate' value='<%=""+annotation_id%>'/></portlet:actionURL>">
						<img title="le contenu est provocateur" src="<%=request.getContextPath()%>/images/troll.jpg"></img>
						</a>
						<%
					}
					else 
					{
						%>
						<span class=troll>contenu provocateur</span>
						<%
					}
					if(!spam)
					{
						%>
						<a href="<portlet:actionURL><portlet:param name='op' value='reply_spam'/><portlet:param name='id_annotate' value='<%=""+annotation_id%>'/></portlet:actionURL>">
						<img title="c'est déjà dit" src="<%=request.getContextPath()%>/images/spam.jpg"></img>
						</a>
						<%
					}
					else 
					{
						%>
						<span class=troll>c'est déjà dit</span>
						<%
					}
					%>
					</td>
				</tr>
				<%
			}
			%>
			<tr>
				<td>
				<a class=quick_reply title="réponse rapide" id="quick_reply_<%=annotation_id%>" onclick="switchMenu('comment_rapide_<%=annotation_id%>');">Laisser un commentaire</a>
				<div class="comment_rapide" id="comment_rapide_<%=annotation_id%>" style="display : none ;">
					<form id="do_quick_reply_<%=annotation_id%>" action="<portlet:actionURL/>" method="post">
						<input name="op" type="hidden" value="quick_reply" />
						<input name="id_annotate" type="hidden" value="<%=annotation_id%>" >
						<table>
							<tr>
								<td>
								<span class=elt_header>Commentaire : </span>
								</td>
							</tr>
							<tr>
								<td>
								<textarea onfocus="this.value=''; this.onfocus=null;" rows=3 name="quick_reply_added_simpletext_commentaire">Votre réponse rapide ...</textarea>
								</td>
							</tr>
							<tr>
								<td>
								<input type=submit value=Annoter></input>
								</td>
							</tr>
						</table>
					</form>
				</div>
				</td>
			</tr>
			</table>
		</td>
		<td class=color>
		<%
			boolean _to_color = false ;
			boolean _to_uncolor = false ;
			for(Resource _annotated : _annotateds)
			{
				if(_annotated instanceof SelectionHTML)
				{
					String _origin_url = ((SelectionHTML)_annotated).getSelectionOrigin().getRepresentsResource().getEffectiveURI();
					if(_origin_url.compareTo(_current_url) == 0)
					{
						_to_color = true ;
						if(_coloreds != null)
						{
							for(Annotation _colored : _coloreds)
							{
								if(_colored.getId().compareTo(_current_annotation.getId())==0)
								{
									_to_uncolor = true ;
									break ; // sortir du parcours des déjà colorés
								}
							}
						}
						break ; //sortir du parcours des ressources annotées
					}
				}
			}
			if(_to_color)
			{
				if(_to_uncolor)
				{
				%>
						<form id="doUnColorAnnotation" method="post" action="<portlet:actionURL/>">
							<input name="op" type="hidden" value="uncolor_annotation" />
							<input name="to_uncolor" type="hidden" value="<%="" + _current_annotation.getId() %>" />
							<input type="submit" value="Décolorer" />
						</form> 
				<%
				}
				else
				{
				%>
						<form id="doColorAnnotation" method="post" action="<portlet:actionURL/>">
							<input name="op" type="hidden" value="color_annotation" />
							<input name="to_color" type="hidden" value="<%="" + _current_annotation.getId() %>" />
							<input type="submit" value="Colorer" />
						</form> 
				<%
				}
			}
			%>
			</td>
		</tr>
		<%
	}
	%>
	</table>
	<%
	int _max_pagination = Integer.parseInt((String)request.getAttribute("max_pagination"));
	int _current_pagination = Integer.parseInt((String)request.getAttribute("pagination"));
	if(_max_pagination > 0)
	{
		%>
		<div class=pagination>
		<%
		for(int i = 0 ; i<=_max_pagination ; i++)
		{
			if(i!= _current_pagination)
			{
				%>
				<a href="<portlet:actionURL><portlet:param name='op' value='change_pagination'/><portlet:param name='value_pagination' value='<%=""+i%>'/></portlet:actionURL>"><%=""+i%></a>
				<%
			}
			else
			{
				%>
				<span title="page courante"><%=""+i%></span>
				<%
			}
		}
		%>
		</div>
		<%
	}
}
else 
{
	%>
	Pas d'annotation sur cette page.
	<%
}
%>
<script src="<%=request.getContextPath()%>/javascript/jquery.js"></script>
<script  type="text/javascript" src="<%=request.getContextPath()%>/javascript/validateFormCreateAnnotation.js">
</script>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/portletViewAnnotation.js">
</script>