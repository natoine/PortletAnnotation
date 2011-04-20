<%@ 
	page language="java" 
	import="javax.servlet.*"
	import="java.lang.reflect.Field"
	import="fr.natoine.model_resource.*"
	import="fr.natoine.model_annotation.*"
	import="fr.natoine.model_document.*"
	import="fr.natoine.model_htmlDocs.*"
	import="fr.natoine.model_user.*"
	import="java.util.List"
	import="java.util.Collection"
	contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"
%>
    <servlet:defineObjects/>
<%
	Resource _resource = (Resource)request.getAttribute("resource");
	//EntityManagerFactory emf_annotation = Persistence.createEntityManagerFactory("annotation");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title><%=_resource.getLabel()%></title>
<link href="<%=request.getContextPath()%>/css/ServletViewResources/view.css" rel=stylesheet />
<%
if(_resource instanceof Annotation)
{
	%>
	<style>
	<%= ((Annotation)_resource).getStatus().toCSS() %>
	</style>
	<%
}
%>
</head>
<body>
<div class=resource id=resource<%=_resource.getId()%>>
		<%
		if(_resource instanceof Annotation)
		{
			//ViewAnnotation _view = new ViewAnnotation();
			//ViewAnnotation _view = new ViewAnnotation(emf_annotation);
			String html = (String)request.getAttribute("annotation_content_html");
			%>
			<div class="annotation annotation_<%=((Annotation)_resource).getStatus().getLabel().replaceAll(" ", "_")%>">
			<%=html%>
			</div>
			<%
		}
		else
		{
			%>
			<%=_resource.toHTML()%>
			<%
		}
		%>
</div>
</body>
</html>