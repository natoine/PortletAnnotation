/*
 * Copyright 2010 Antoine Seilles (Natoine)
 *   This file is part of PortletAnnotation.

    PortletUserAccount is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PortletUserAccount is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with PortletAnnotation.  If not, see <http://www.gnu.org/licenses/>.

 */
package fr.natoine.PortletAnnotation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Event;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletConfig;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ValidatorException;
import javax.portlet.WindowState;

import org.json.JSONException;

import fr.natoine.dao.annotation.DAOAnnotation;
import fr.natoine.dao.annotation.DAOPost;
import fr.natoine.dao.annotation.DAOTag;
import fr.natoine.dao.htmlDocs.DAOHtmlDocs;
import fr.natoine.dao.resource.DAOResource;
import fr.natoine.dao.user.DAOUser;
import fr.natoine.model_annotation.AnnotationStatus;
import fr.natoine.model_annotation.TagAgent;
import fr.natoine.model_htmlDocs.HighlightSelectionHTML;
import fr.natoine.model_htmlDocs.SelectionHTML;
import fr.natoine.model_htmlDocs.WebPage;
import fr.natoine.model_resource.Resource;
import fr.natoine.model_resource.URI;
import fr.natoine.model_user.UserAccount;
import fr.natoine.properties.PropertiesUtils;
import fr.natoine.stringOp.StringOp;

public class PortletCreateAnnotation extends GenericPortlet implements EventPortlet
{
	private Properties applicationProps ;
	private Properties colorProps ;
	private final String saved_properties = "/properties/appProperties";
	private final String default_properties = "/properties/defaultProperties";
	private final String color_properties = "/properties/defaultColorProperties";
	
	private static String defaultURL = "http://www.google.com";
	
	private static DAOResource daoResource = null ;
	private static DAOAnnotation daoAnnotation = null ;
	private static DAOPost daoPost = null ;
	private static DAOTag daoTag = null ;
	private static DAOUser daoUser = null ;
	private static DAOHtmlDocs daoHtml = null ;
	
	private static EntityManagerFactory emf_annotation = null ; // Persistence.createEntityManagerFactory("annotation");
	private static EntityManagerFactory emf_resource = null ; // Persistence.createEntityManagerFactory("resource");
	private static EntityManagerFactory emf_user = null ; // Persistence.createEntityManagerFactory("user");
	private static EntityManagerFactory emf_html = null ; // Persistence.createEntityManagerFactory("htmlDocs");
	
	private static String APPLICATION_NAME = "PortletCreateAnnotation" ;
	private static String URL_SERVLET_ANNOTATIONS = null ;
	private static String URL_SERVLET_CONSULTATION = null ;
	private static String URL_SERVLET_JSON_TAGS = null ;
	
    private static final String NORMAL_VIEW = "/WEB-INF/jsp/PortletCreateAnnotation/annotate.jsp";
    private static final String MAXIMIZED_VIEW = "/WEB-INF/jsp/PortletCreateAnnotation/annotate.jsp";
    private static final String HELP_VIEW = "/WEB-INF/jsp/PortletCreateAnnotation/help.jsp";
    private static final String EDIT_VIEW = "/WEB-INF/jsp/PortletCreateAnnotation/edit.jsp";

    private PortletRequestDispatcher normalView;
    private PortletRequestDispatcher maximizedView;
    private PortletRequestDispatcher helpView;
    private PortletRequestDispatcher editView;

    public void doView( RenderRequest request, RenderResponse response )
    throws PortletException, IOException
    {
    	setRenderAttributes(request);
    	if( WindowState.MINIMIZED.equals( request.getWindowState() ) ) return;
    	if ( WindowState.NORMAL.equals( request.getWindowState() ) ) normalView.include( request, response );
    	else maximizedView.include( request, response );
    }

    protected void doEdit( RenderRequest request, RenderResponse response )
	throws PortletException, IOException 
	{
		setRenderAttributes(request);
		editView.include( request, response );
	}
    
    protected void doHelp( RenderRequest request, RenderResponse response )
        throws PortletException, IOException
    {
    	setRenderAttributes(request);
        helpView.include( request, response );
    }

    public void init( PortletConfig config ) throws PortletException 
    {
        super.init( config );
        normalView = config.getPortletContext().getRequestDispatcher( NORMAL_VIEW );
        maximizedView = config.getPortletContext().getRequestDispatcher( MAXIMIZED_VIEW );
        helpView = config.getPortletContext().getRequestDispatcher( HELP_VIEW );
        editView = config.getPortletContext().getRequestDispatcher( EDIT_VIEW );
        
        emf_annotation = Persistence.createEntityManagerFactory("annotation");
    	emf_resource = Persistence.createEntityManagerFactory("resource");
    	emf_user = Persistence.createEntityManagerFactory("user");
    	emf_html = Persistence.createEntityManagerFactory("htmlDocs");
        
    	daoResource = new DAOResource(emf_resource) ;
    	daoAnnotation = new DAOAnnotation(emf_annotation) ;
    	daoPost = new DAOPost(emf_annotation) ;
    	daoTag = new DAOTag(emf_annotation) ;
    	daoUser = new DAOUser(emf_user) ;
    	daoHtml = new DAOHtmlDocs(emf_html) ;
        
    	//Load color properties
    	colorProps = PropertiesUtils.loadDefault(getPortletContext().getRealPath(color_properties));
    	
        //CreateAnnotationStatus _annotation_status_creator = new CreateAnnotationStatus() ;
    	daoAnnotation.createSimpleCommentStatus(colorProps.getProperty("color_comment"));
    	daoAnnotation.createSimpleTagStatus(colorProps.getProperty("color_tag"));
    	daoAnnotation.createAgreeStatus(colorProps.getProperty("color_accord"));
    	daoAnnotation.createDisAgreeStatus(colorProps.getProperty("color_desaccord"));
    	daoAnnotation.createFlameStatus(colorProps.getProperty("color_flame"));
    	daoAnnotation.createTrollStatus(colorProps.getProperty("color_troll"));
        
        // create application properties with default
		Properties defaultProps = PropertiesUtils.loadDefault(getPortletContext().getRealPath(default_properties));
		applicationProps = new Properties(defaultProps);
		
		// now load properties from last invocation
		applicationProps = PropertiesUtils.loadLastState(applicationProps, getPortletContext().getRealPath(saved_properties));
		
		//sets values 
		if(applicationProps.getProperty("defaultURL")!=null) defaultURL = applicationProps.getProperty("defaultURL");
		URL_SERVLET_ANNOTATIONS = applicationProps.getProperty("url_servlet_annotations");
		if(URL_SERVLET_ANNOTATIONS != null)
			//A ce moment là on peut créer le status complexe d'annotation.
			daoAnnotation.createComplexAnnotationSample(URL_SERVLET_ANNOTATIONS , colorProps.getProperty("color_annotation") );
		URL_SERVLET_CONSULTATION = applicationProps.getProperty("url_servlet_consultation");
		URL_SERVLET_JSON_TAGS = applicationProps.getProperty("url_json_tags");
    }

    public void destroy() 
    {
        normalView = null;
        maximizedView = null;
        helpView = null;
        editView = null ;
        super.destroy();
    }
    
    private void setRenderAttributes(RenderRequest request) 
	{
    	if(request.getPortletSession().getAttribute("application_name",  PortletSession.APPLICATION_SCOPE)!=null)
		{
			request.removeAttribute("config_error_message");
			request.getPortletSession();
			String _new_app_name = (String)request.getPortletSession().getAttribute("application_name", PortletSession.APPLICATION_SCOPE) ;
			APPLICATION_NAME = _new_app_name ;
		}
    	if(URL_SERVLET_ANNOTATIONS != null)	request.setAttribute("url_servlet", URL_SERVLET_ANNOTATIONS);
    	else request.setAttribute("url_servlet" , "");
    	if(URL_SERVLET_CONSULTATION != null) request.setAttribute("url_servletConsultation", URL_SERVLET_CONSULTATION);
    	else request.setAttribute("url_servletConsultation" , "");
    	if(URL_SERVLET_JSON_TAGS != null) request.setAttribute("url_servletJSONTags", URL_SERVLET_JSON_TAGS);
    	else request.setAttribute("url_servletJSONTags" , "");
    	if(request.getParameter("message")!=null) request.setAttribute("message" , request.getParameter("message"));
    	request.setAttribute("default_url", defaultURL);
		String _current_url = defaultURL ;
    	if(request.getPortletSession().getAttribute("current_url")!= null) _current_url = (String)request.getPortletSession().getAttribute("current_url");
    	request.setAttribute("url", _current_url);
    	request.getPortletSession().setAttribute("current_url" , _current_url);
		if(request.getPortletSession().getAttribute("user")!=null) request.setAttribute("user", request.getPortletSession().getAttribute("user"));
		ArrayList _selections ;
		if(request.getPortletSession().getAttribute("selections") != null) _selections = (ArrayList) request.getPortletSession().getAttribute("selections") ;
		else _selections = new ArrayList();
		request.setAttribute("selections", _selections);
		if(request.getPortletSession().getAttribute("annotation_status") != null) request.setAttribute("annotation_status", request.getPortletSession().getAttribute("annotation_status"));
		ArrayList<HighlightSelectionHTML> _colored_selections = (ArrayList<HighlightSelectionHTML>) request.getPortletSession().getAttribute("colored_selections");
		if(_colored_selections == null) _colored_selections = new ArrayList<HighlightSelectionHTML>();
		request.setAttribute("colored_selections", _colored_selections);
	}
    
    private void sendEvent(String _event_type , Serializable _event_object , ActionResponse response)
	{
		System.out.println("[PortletCreateAnnotation.sendEvent] type : " + _event_type + " value : " + _event_object);
		response.setEvent(_event_type, _event_object);
	}
    
    public void processEvent(EventRequest request, EventResponse response)
	{
		Event event = request.getEvent();
		String _event_name = event.getName() ;
		if(_event_name.equalsIgnoreCase("UserLog"))
		{
			if(event.getValue() instanceof UserAccount)
			{
				UserAccount _current_user = (UserAccount)event.getValue() ;
				if(_current_user.getId() != null) request.getPortletSession().setAttribute("user", _current_user);
			}
		}
		if(_event_name.equalsIgnoreCase("UserUnLog")) request.getPortletSession().removeAttribute("user");
		if(_event_name.equalsIgnoreCase("loadedurl") || _event_name.equalsIgnoreCase("toLoadUrl"))
		{
			if(event.getValue() instanceof String)
			{
				String url = (String) event.getValue();
				if (url.startsWith("http://")) 
				{
					request.getPortletSession().setAttribute("current_url",  url.toLowerCase());	
					//vider la liste des sélections colorées
					request.getPortletSession().removeAttribute("colored_selections");
				}
			}
		}
		if(_event_name.equalsIgnoreCase("default_url"))
		{
			if(event.getValue() instanceof String)
			{
				String url = (String) event.getValue();
				if (url.startsWith("http://")) 
					defaultURL = url ;
			}
		}
		if(_event_name.equalsIgnoreCase("selection"))
		{
			if(event.getValue() instanceof HighlightSelectionHTML)
			{
				//ajouter la sélection à la liste des sélections en cours
				HighlightSelectionHTML _new_selection = (HighlightSelectionHTML)event.getValue() ;
				ArrayList _selections ;
				if(request.getPortletSession().getAttribute("selections") != null)
					_selections = (ArrayList) request.getPortletSession().getAttribute("selections") ;
				else _selections = new ArrayList();
				_selections.add(_new_selection);
				request.getPortletSession().setAttribute("selections", _selections);
				//ajouter le sélection à la liste des sélections colorées (quand elle est émise elle est colorée)
				ArrayList<HighlightSelectionHTML> _colored_selections = (ArrayList<HighlightSelectionHTML>) request.getPortletSession().getAttribute("colored_selections");
				if(_colored_selections == null) _colored_selections = new ArrayList<HighlightSelectionHTML>();
				_colored_selections.add(_new_selection);
				request.getPortletSession().setAttribute("colored_selections", _colored_selections);
			}
		}
		if(_event_name.equalsIgnoreCase("Page"))
		{
			if(event.getValue() instanceof WebPage)
			{
				ArrayList _selections ;
				if(request.getPortletSession().getAttribute("selections") != null)
					_selections = (ArrayList) request.getPortletSession().getAttribute("selections") ;
				else _selections = new ArrayList();
				_selections.add((WebPage)event.getValue());
				request.getPortletSession().setAttribute("selections", _selections);
			}
		}
		if(_event_name.equalsIgnoreCase("change_status"))
		{
			AnnotationStatus status = (AnnotationStatus)event.getValue();
			request.getPortletSession().setAttribute("annotation_status", status);
		}
	}
    
    public void processAction(ActionRequest request, ActionResponse response)
	throws PortletException, PortletSecurityException, IOException 
	{
		String op = request.getParameter("op");
		StringBuffer message = new StringBuffer(1024);
		if ((op != null) && (op.trim().length() > 0)) 
		{
			//Mise à jour de l'url dans la barre de navigation de la portlet en View
			if (op.equalsIgnoreCase("clear_selections")) 
			{
				//vider la liste de sélections
				doClearSelections(request, response);
				return;
			} 
			//il y a eu une action de sélection pour annotation
			else if(op.equalsIgnoreCase("delete_selection"))
			{
				doDeleteSelection(request , response);
				return;
			}
			//mise à jour des données par interface Edit
			else if (op.equalsIgnoreCase("create_annotation")) 
			{
				try {
					doCreateAnnotation(request , response);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				response.setPortletMode(PortletMode.VIEW);
				return;
			}
			//mise à jour des données par interface Edit
			else if (op.equalsIgnoreCase("update")) 
			{
				doUpdate(request , response);
				return;
			}
			else if (op.equalsIgnoreCase("cancel")) 
			{
				doCancel(response);
				return;
			} 
			else if(op.equalsIgnoreCase("color_selection"))
			{
				doColorSelection(request, response);
				return;
			}
			else if(op.equalsIgnoreCase("uncolor_selection"))
			{
				doUnColorSelection(request, response);
				return;
			}
			else if(op.equalsIgnoreCase("load_page"))
			{
				doLoadPage(request , response);
				return;
			}
			else message.append("Operation not found");
		} 
		else message.append("Operation is null");
		response.setRenderParameter("message", message.toString());
		response.setPortletMode(PortletMode.VIEW);
	}
    
    private void doDeleteSelection(ActionRequest request, ActionResponse response)
    {
    	if(request.getParameter("cpt_selection")!=null)
		{
			int _index_selection_to_delete = Integer.parseInt(request.getParameter("cpt_selection"));
			if(request.getPortletSession().getAttribute("selections")!=null)
			{
				ArrayList _selections = (ArrayList)request.getPortletSession().getAttribute("selections");
				Object _selection_to_delete = _selections.get(_index_selection_to_delete);
				if(_selection_to_delete instanceof HighlightSelectionHTML) this.sendEvent("todelete", (HighlightSelectionHTML)_selection_to_delete, response);
				_selections.remove(_index_selection_to_delete);
				request.getPortletSession().setAttribute("selections", _selections);
			}
			else System.out.println("[PortletCreationAnnotation.doDeleteSelection] selections attribute doesn't exist");
		}
		else System.out.println("[PortletCreationAnnotation.doDeleteSelection] no selection to delete");
    }
    
    private void doCreateAnnotation(ActionRequest request, ActionResponse response) throws JSONException
	{
    	String url = (String)request.getPortletSession().getAttribute("current_url");
    	if(URL_SERVLET_ANNOTATIONS == null) System.out.println("[PortletCreationAnnotation.doCreateAnnotation] URL_SERVLET_ANNOTATIONS is null");
    	else if(request.getParameter("list_selection_to_annotate") != null && request.getParameter("list_selection_to_annotate").length() > 0)
    	{
    		boolean refresh = false ;
    		String[] _to_annotate = request.getParameter("list_selection_to_annotate").split(" ");
			boolean _test_status_tag = false ;
			//récupération des sélections
			ArrayList _selections = (ArrayList)request.getPortletSession().getAttribute("selections");
			if(_selections.size() != 0) //on annote que si il y a quelque chose à annoter
			{
				UserAccount _author = (UserAccount)request.getPortletSession().getAttribute("user");
				AnnotationStatus status = (AnnotationStatus)request.getPortletSession().getAttribute("annotation_status");
				String annotation_title = request.getParameter("annotation_title") ;
				annotation_title = UtilsForAnnotation.prepareAnnotationTitle(annotation_title, _author, status);
				if(status.getLabel().equalsIgnoreCase("tag")) _test_status_tag = true ;
				//créer l'uri d'accés et de représentation de l'annotation.
				//celle ci devrait être un appel à la servlet d'affichage d'annotation
				//Par défaut la servlet affiche toutes les annotations, sinon, en passant l'id de l'annotation en paramétre, elle n'affiche que cette annotation
				String _effective_uri = URL_SERVLET_ANNOTATIONS;
				URI access = daoResource.createAndGetURI(_effective_uri);
				
				//récupérer toutes les valeurs à ajouter dans le formulaire :
				Enumeration<String> _parameters_names = request.getParameterNames() ;
				String _parameter_name;
				Collection<Resource> added = new ArrayList<Resource>();
				while(_parameters_names.hasMoreElements())
				{
					_parameter_name = _parameters_names.nextElement();
					if(_parameter_name.contains("added"))
					{
						if(_parameter_name.contains("_simpletext_")) UtilsForAnnotation.processSimpleTextParameter(request, _parameter_name, annotation_title, access, _author, added, daoPost, APPLICATION_NAME);
						else if(_parameter_name.contains("_tag_")) annotation_title = UtilsForAnnotation.processTagParameter(request, _parameter_name, annotation_title, access, added, _test_status_tag, daoTag, daoResource, URL_SERVLET_ANNOTATIONS, APPLICATION_NAME);
						else if(_parameter_name.contains("_domain_")) UtilsForAnnotation.processDomainParameter(request, _parameter_name, access, added, daoTag, daoResource, URL_SERVLET_ANNOTATIONS, APPLICATION_NAME);
						else if(_parameter_name.contains("_judgment_")) UtilsForAnnotation.processJgtParameter(request, _parameter_name, access, added, daoTag, daoResource, URL_SERVLET_ANNOTATIONS, APPLICATION_NAME);
						else if(_parameter_name.contains("_mood_")) UtilsForAnnotation.processMoodParameter(request, _parameter_name, access, added, daoTag, daoResource, URL_SERVLET_ANNOTATIONS, APPLICATION_NAME);
					}
				}
				//il faut récupérer la liste de sélections à annoter
				Collection<Resource> annotated = new ArrayList<Resource>();
				Collection<URI> annotatedURIs = new ArrayList<URI>();
				//TODO créer HashMap <url , WebPage> pour gérer les webpage déjà créées already_created
				HashMap<String , WebPage> already_created = new HashMap<String , WebPage>();
				for(String index : _to_annotate)
				{
					Object _selection = _selections.get(Integer.parseInt(index));
					if(_selection instanceof HighlightSelectionHTML) refresh = processHighlightSelection((HighlightSelectionHTML)_selection , annotatedURIs, annotated, access, url, refresh, already_created);
					else if(_selection instanceof WebPage) refresh = processWebPageSelection((WebPage)_selection , annotatedURIs, annotated , url , refresh, already_created);
					else if(_selection instanceof Resource) refresh = processResourceSelection((Resource) _selection, annotatedURIs, annotated , url , refresh);
				}
				if(_author != null) UtilsForAnnotation.processAuthor(_author , added, daoTag, daoResource, URL_SERVLET_ANNOTATIONS, APPLICATION_NAME);
				daoAnnotation.createAnnotation(annotation_title, APPLICATION_NAME, access, access, status, added, annotated , annotatedURIs, _author);
				if(refresh) sendEvent("refresh", "", response) ; // Pour forcer le rafraîchissement de PortletViewAnnotation
			}
    	}
	}
    
    private boolean processResourceSelection(Resource _selection, Collection<URI> _annotatedURIs, Collection<Resource> _annotated, String _url, boolean _refresh)
    {
		URI _represents_resource = _selection.getRepresentsResource();
		daoResource.createURI(_represents_resource.getEffectiveURI());
		_represents_resource = daoResource.retrieveURI(_represents_resource.getEffectiveURI());
		Resource _resource = daoResource.createAndGetResource(((Resource) _selection).getContextCreation(), ((Resource) _selection).getLabel(), _represents_resource);
		_annotated.add(_resource);
		_annotatedURIs.add(_represents_resource);
		if(!_refresh && _url.equalsIgnoreCase(_represents_resource.getEffectiveURI())) _refresh = true ;
		return _refresh ;
	}
    
    private boolean processWebPageSelection(WebPage _selection , Collection<URI> _annotatedURIs, Collection<Resource> _annotated , String _url , boolean _refresh, HashMap<String , WebPage> _already_created)
	{
    	
		URI _wp_access = _selection.getAccess();
		_wp_access = daoResource.createAndGetURI(_wp_access.getEffectiveURI());
		URI _wp_represents = ((WebPage)_selection).getRepresentsResource();
		_wp_represents = daoResource.createAndGetURI(_wp_represents.getEffectiveURI());
		URI _wp_principal = ((WebPage)_selection).getPrincipalURL();
		_wp_principal = daoResource.createAndGetURI(_wp_principal.getEffectiveURI());
		WebPage _wp ;
		if(_already_created.containsKey(_wp_access.getEffectiveURI()))
		{
			_wp = _already_created.get(_wp_access.getEffectiveURI());
		}
		else
		{
			//daoResource.createURI(_wp_access.getEffectiveURI());
			//_wp_access = daoResource.retrieveURI(_wp_access.getEffectiveURI());
			//URI _wp_represents = ((WebPage)_selection).getRepresentsResource();
			//daoResource.createURI(_wp_represents.getEffectiveURI());
			//_wp_represents = daoResource.retrieveURI(_wp_represents.getEffectiveURI());
			//URI _wp_principal = ((WebPage)_selection).getPrincipalURL();
			//daoResource.createURI(_wp_principal.getEffectiveURI());
			//_wp_principal = daoResource.retrieveURI(_wp_principal.getEffectiveURI());
			//TODO
			//copy content of WebPage costs too much place in the database currently, nothing to manage versioning so not useful 
			String _wp_title = ((WebPage)_selection).getLabel() ;
			if(StringOp.isNull(_wp_title)) _wp_title = _wp_represents.getEffectiveURI() ;//enregistrer l'url en tant que titre de page si pas de title
			_wp = daoHtml.createAndGetWebPage(_wp_title , ((WebPage)_selection).getContextCreation(), null, _wp_access, _wp_represents, _wp_principal);
			//mise à jour de la HashMap
			_already_created.put(_wp_access.getEffectiveURI() , _wp);
		}
		_annotated.add(_wp);
		_annotatedURIs.add(_wp_access);
		_annotatedURIs.add(_wp_represents);
		_annotatedURIs.add(_wp_principal);
		if(!_refresh && ( _url.equalsIgnoreCase(_wp_represents.getEffectiveURI()) || _url.equalsIgnoreCase(_wp_principal.getEffectiveURI()) || _url.equalsIgnoreCase(_wp_access.getEffectiveURI()))) 
			_refresh = true ;
		return _refresh ;
	}
    
    private boolean processHighlightSelection(HighlightSelectionHTML _selection, Collection<URI> _annotatedURIs, Collection<Resource> _annotated, URI _access, String _url, boolean _refresh, HashMap<String , WebPage> _already_created)
    {
			Resource _selection_origin = _selection.getSelection().getSelectionOrigin() ;
			if(_selection_origin instanceof WebPage) 
			{
				URI _wp_access = ((WebPage)_selection_origin).getAccess();
				_wp_access = daoResource.createAndGetURI(_wp_access.getEffectiveURI());
				//daoResource.createURI(_wp_access.getEffectiveURI());
				//_wp_access = daoResource.retrieveURI(_wp_access.getEffectiveURI());
				URI _wp_represents = ((WebPage)_selection_origin).getRepresentsResource();
				_wp_represents = daoResource.createAndGetURI(_wp_represents.getEffectiveURI());
				//daoResource.createURI(_wp_represents.getEffectiveURI());
				//_wp_represents = daoResource.retrieveURI(_wp_represents.getEffectiveURI());
				URI _wp_principal = ((WebPage)_selection_origin).getPrincipalURL();
				_wp_principal = daoResource.createAndGetURI(_wp_principal.getEffectiveURI());
				//daoResource.createURI(_wp_principal.getEffectiveURI());
				//_wp_principal = daoResource.retrieveURI(_wp_principal.getEffectiveURI());
				//WebPage _selection_origin ;
				if(_already_created.containsKey(_wp_access.getEffectiveURI()))
				{
					_selection_origin = _already_created.get(_wp_access.getEffectiveURI());
				}
				else
				{
					//TODO
					//copy content of WebPage costs too much place in the database currently, nothing to manage versioning so not useful 
					String _wp_title = _selection_origin.getLabel() ;
					if(StringOp.isNull(_wp_title)) _wp_title = _wp_represents.getEffectiveURI() ;//enregistrer l'url en tant que titre de page si pas de title
					_selection_origin = daoHtml.createAndGetWebPage(_wp_title , _selection_origin.getContextCreation(), null, _wp_access, _wp_represents, _wp_principal);
					//mise à jour de la hashmap
					_already_created.put(_wp_access.getEffectiveURI() , (WebPage)_selection_origin);
				}
				_annotatedURIs.add(_wp_represents);
				_annotatedURIs.add(_wp_principal);
				_annotatedURIs.add(_wp_access);
				if(!_refresh && ( _url.equalsIgnoreCase(_wp_represents.getEffectiveURI()) || _url.equalsIgnoreCase(_wp_principal.getEffectiveURI()) || _url.equalsIgnoreCase(_wp_access.getEffectiveURI())))
					_refresh = true ;
			}
			else
			{
				URI _resource_represents = _selection_origin.getRepresentsResource();
				daoResource.createURI(_resource_represents.getEffectiveURI());
				_resource_represents = daoResource.retrieveURI(_resource_represents.getEffectiveURI());
				_selection_origin = daoResource.createAndGetResource(_selection_origin.getContextCreation(), _selection_origin.getContextCreation(), _resource_represents);
				_annotatedURIs.add(_resource_represents);
				if(!_refresh && _url.equalsIgnoreCase(_resource_represents.getEffectiveURI())) _refresh = true ;
			}
			//TODO revoir stratégie de gestion de l'url de représentation d'une selectionHTML
			URI _represents_selectionHTML = _access ; //la même URI que pour l'annotation
			SelectionHTML _selectionHTML = daoHtml.createAndGetSelectionHTML(
					((HighlightSelectionHTML)_selection).getSelection().getLabel(),
					((HighlightSelectionHTML)_selection).getSelection().getContextCreation(),
					((HighlightSelectionHTML)_selection).getSelection().getHTMLContent(),
					((HighlightSelectionHTML)_selection).getSelection().getXpointerBegin(),
					((HighlightSelectionHTML)_selection).getSelection().getXpointerEnd(), 
					_represents_selectionHTML , 
					_selection_origin);
			_annotated.add(_selectionHTML);
			return _refresh ;
    }
    
    private void doCancel(ActionResponse response) throws PortletModeException
	{
		response.setPortletMode(PortletMode.VIEW);
	}
    
    private void doUpdate(ActionRequest request, ActionResponse response) throws ReadOnlyException, ValidatorException, IOException, PortletModeException
	{
		String url = request.getParameter("servlet-url");
		String new_default_url = request.getParameter("default-url");
		String url_consultation = request.getParameter("servletconsultation-url");
		String url_JSONTags = request.getParameter("servletjsontags-url");
		System.out.println("[PortletCreateAnnotation] servletconsultation-url : " + url_consultation) ;
		boolean save_servlet_annotations = true ;
		boolean save_servlet_consultation = true ;
		boolean save_servlet_json = true ;
		if(new_default_url != null)
		{
			if (StringOp.isValidURI(new_default_url) && !new_default_url.equalsIgnoreCase(defaultURL)) 
			{
				defaultURL = new_default_url ;
				applicationProps.setProperty("defaultURL", defaultURL);
				PropertiesUtils.store(applicationProps, getPortletContext().getRealPath(saved_properties), "[PortletCreateAnnotation.doUpdate]");
			}
		}
		if(url != null) 
		{
			if (!StringOp.isValidURI(url)) 
			{
				save_servlet_annotations = false;
				response.setRenderParameter("message", "not a valid URL");
			} 
			if (save_servlet_annotations) 
			{
				URL_SERVLET_ANNOTATIONS = url ;
				applicationProps.setProperty("url_servlet_annotations", URL_SERVLET_ANNOTATIONS);
				PropertiesUtils.store(applicationProps, getPortletContext().getRealPath(saved_properties), "[PortletCreateAnnotation.doUpdate]");
				this.sendEvent("url_servlet_annotations", url, response);
				//A ce moment là on peut tenter de créer le status complexe d'annotation (si il n'y a pas de valeur par défaut dans les properties, c'est la première occasion de le créer)
				//de toute façon il ne peut y avoir qu'un seul status pour un nom de status
				daoAnnotation.createComplexAnnotationSample(url , colorProps.getProperty("color_annotation"));
				//response.setPortletMode(PortletMode.VIEW);
				//return;
			}
		}
		if(url_consultation != null)
		{
			//System.out.println("[PortletCreateAnnotation] new Consultation URL !!!");
			if (!StringOp.isValidURI(url_consultation)) 
			{
				save_servlet_consultation = false;
				response.setRenderParameter("message", "not a valid consultation URL");
			} 
			if (save_servlet_consultation) 
			{
				URL_SERVLET_CONSULTATION = url_consultation ;
				applicationProps.setProperty("url_servlet_consultation", URL_SERVLET_CONSULTATION);
				PropertiesUtils.store(applicationProps, getPortletContext().getRealPath(saved_properties), "[PortletCreateAnnotation.doUpdate]");
				this.sendEvent("url_servlet_consultation", URL_SERVLET_CONSULTATION, response);
			}
		}
		if(url_JSONTags != null)
		{
			//System.out.println("[PortletCreateAnnotation] new Consultation URL !!!");
			if (!StringOp.isValidURI(url_JSONTags)) 
			{
				save_servlet_json = false;
				response.setRenderParameter("message", "not a valid json URL");
			} 
			if (save_servlet_json) 
			{
				URL_SERVLET_JSON_TAGS = url_JSONTags ;
				applicationProps.setProperty("url_json_tags", URL_SERVLET_JSON_TAGS);
				PropertiesUtils.store(applicationProps, getPortletContext().getRealPath(saved_properties), "[PortletCreateAnnotation.doUpdate]");
				this.sendEvent("url_servlet_json_tags", URL_SERVLET_JSON_TAGS, response);
			}
		}
		if(save_servlet_consultation && save_servlet_annotations && save_servlet_json) response.setPortletMode(PortletMode.VIEW);
		else response.setPortletMode(PortletMode.EDIT);
	}
    
    private void doClearSelections(ActionRequest request, ActionResponse response)
    {
    	if(request.getPortletSession().getAttribute("selections")!=null)
    	{
			ArrayList _selections = (ArrayList) request.getPortletSession().getAttribute("selections") ;
			for(Object _selection : _selections)
			{
				if(_selection instanceof HighlightSelectionHTML) sendEvent("todelete", (HighlightSelectionHTML)_selection, response);
			}
    		request.getPortletSession().removeAttribute("selections");	
    	}
    }
    
    private void doColorSelection(ActionRequest request, ActionResponse response)
    {
    	ArrayList<HighlightSelectionHTML> _colored_selections = (ArrayList<HighlightSelectionHTML>) request.getPortletSession().getAttribute("colored_selections");
		if(_colored_selections == null) _colored_selections = new ArrayList<HighlightSelectionHTML>();
		String _selection_id = request.getParameter("to_color");
		//récupérer la sélection
		ArrayList _selections = (ArrayList) request.getPortletSession().getAttribute("selections") ;
		HighlightSelectionHTML _selection_to_color = null ;
		for(Object _selection : _selections)
		{
			if(_selection instanceof HighlightSelectionHTML)
			{
				if(((HighlightSelectionHTML)_selection).getId().compareTo(_selection_id) == 0) 
				{
					_selection_to_color = (HighlightSelectionHTML)_selection ;
					break ;
				}
			}
		}
		if(_selection_to_color != null)
		{
			//envoyer la sélection en event au Browser
			sendEvent("tohighlight" , _selection_to_color, response);
			//ajouter la sélection à la liste des colorés
			_colored_selections.add(_selection_to_color);
			request.getPortletSession().setAttribute("colored_selections" , _colored_selections);
		}
    }
    
    private void doUnColorSelection(ActionRequest request, ActionResponse response)
    {
    	ArrayList<HighlightSelectionHTML> _colored_selections = (ArrayList<HighlightSelectionHTML>) request.getPortletSession().getAttribute("colored_selections");
		if(_colored_selections == null) _colored_selections = new ArrayList<HighlightSelectionHTML>();
		String _selection_id = request.getParameter("to_uncolor");
		//récupérer la sélection
		ArrayList _selections = (ArrayList) request.getPortletSession().getAttribute("selections") ;
		HighlightSelectionHTML _selection_to_color = null ;
		for(Object _selection : _selections)
		{
			if(_selection instanceof HighlightSelectionHTML)
			{
				if(((HighlightSelectionHTML)_selection).getId().compareTo(_selection_id) == 0) 
				{
					_selection_to_color = (HighlightSelectionHTML)_selection ;
					break ;
				}
			}
		}
		if(_selection_to_color != null)
		{
			//envoyer la sélection en event au Browser
			sendEvent("todelete" , _selection_to_color , response);
			//enlever la sélection à la liste des colorés
			_colored_selections.remove(_selection_to_color);
			request.getPortletSession().setAttribute("colored_selections" , _colored_selections);
		}
    }
    
    private void doLoadPage(ActionRequest request, ActionResponse response)
    {
    	String _url_to_load = request.getParameter("url_origin");
    	request.getPortletSession().setAttribute("current_url" , _url_to_load);
    	request.getPortletSession().removeAttribute("colored_selections");
    	sendEvent("toLoadUrl" , _url_to_load , response);
    }
}