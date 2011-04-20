function switchMenu(obj)
{
	var el = document.getElementById(obj);
	if ( el.style.display != 'none' ) 
	{
		el.style.display = 'none';
	}
	else 
	{
		el.style.display = '';
	}
}
function showAnnotationContent(obj , url_servlet_consultation, url_servlet, annotation_id , user_id)
{
	var el = document.getElementById(obj);
	if ( el.style.display != 'none' ) 
	{
		el.style.display = 'none';
	}
	else 
	{
		el.style.display = '';
		if(obj.indexOf("annotation_content_") != -1 )
		{
			//alert("il faut enregistrer la consultation dans les logs");
			if(user_id != null) $.post(url_servlet_consultation, 
					{ uid: "" + user_id, uri: "" + url_servlet + "?id=" + annotation_id, context : "[PortletViewAnnotation] short reading" } );
			else alert("utilisateur non loggé");
		}
	}
}