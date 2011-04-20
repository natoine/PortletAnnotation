<%@ page language="java" extends="org.jboss.portal.core.servlet.jsp.PortalJsp" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects/>
<%
if(request.getAttribute("message") != null)
{
	%>
	<div class=error_message>
	<%=(String)request.getAttribute("message") %>
	</div>
	<%
}
 %>
<form method="post" action="<portlet:actionURL/>">
	<table>
		 <tr class="portlet-section-body">
			<td>URL de la servlet d'affichage des annotations : </td>
			<td><input type="text" name="servlet-url" value="<%= request.getAttribute("url_servlet") %>" size="50"/></td>
		</tr>
		 <tr class="portlet-section-body">
			<td>URL de la servlet de création des consultations : </td>
			<td><input type="text" name="servletconsultation-url" value="<%= request.getAttribute("url_servletConsultation") %>" size="50"/></td>
		</tr>
		 <tr class="portlet-section-body">
			<td>URL de la servlet de Tags JSON : </td>
			<td><input type="text" name="servletjsontags-url" value="<%= request.getAttribute("url_servletJSONTags") %>" size="50"/></td>
		</tr>
		 <tr class="portlet-section-body">
			<td>URL par défaut au chargement de la page : </td>
			<td><input type="text" name="default-url" value="<%= request.getAttribute("default_url") %>" size="50"/></td>
		</tr>
		 <tr class="portlet-section-body">
			<td align="right"><input type="submit" name="op" value="Update"/></td>
            <td align="left"><input type="submit" name="op" value="Cancel"/></td>
        </tr>
	</table>
</form>