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
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Event;
import javax.portlet.EventPortlet;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.htmlparser.util.ParserException;
import org.json.JSONException;


import fr.natoine.dao.annotation.DAOAnnotation;
import fr.natoine.dao.annotation.DAOPost;
import fr.natoine.dao.annotation.DAOTag;
import fr.natoine.dao.htmlDocs.DAOHtmlDocs;
import fr.natoine.dao.resource.DAOResource;
import fr.natoine.model_annotation.Annotation;
import fr.natoine.model_annotation.AnnotationStatus;
import fr.natoine.model_annotation.Judgment;
import fr.natoine.model_annotation.Post;
import fr.natoine.model_annotation.PostStatus;
import fr.natoine.model_annotation.TagAgent;
import fr.natoine.model_htmlDocs.HighlightSelectionHTML;
import fr.natoine.model_htmlDocs.SelectionHTML;
import fr.natoine.model_htmlDocs.WebPage;
import fr.natoine.model_resource.Resource;
import fr.natoine.model_resource.URI;
import fr.natoine.model_user.Agent;
import fr.natoine.model_user.UserAccount;
import fr.natoine.properties.PropertiesUtils;
import fr.natoine.stringOp.StringOp;
import fr.natoine.viewAnnotations.ViewAnnotation;
import fr.natoine.html.HTMLPage;

public class PortletViewAnnotation extends GenericPortlet implements EventPortlet
{
	private Properties applicationProps ;
	private final String saved_properties = "/properties/appProperties";
	private final String default_properties = "/properties/defaultProperties";
	private final String color_properties = "/properties/defaultColorProperties";
	private static String defaultURL = "http://www.google.com";
	
	private static String APPLICATION_NAME = "PortletViewAnnotation" ;

	private static String URL_SERVLET_ANNOTATIONS = null ;
	private static String URL_SERVLET_CONSULTATION = null ;

	private static DAOAnnotation daoAnnotation = null ;
	private static DAOResource daoResource = null ;
	private static DAOPost daoPost = null ;
	private static DAOTag daoTag = null ;
	private static DAOHtmlDocs daoHtml = null ;
	
	private static EntityManagerFactory emf_annotation = null ; // Persistence.createEntityManagerFactory("annotation");
	private static EntityManagerFactory emf_resource = null ; // Persistence.createEntityManagerFactory("resource");
	private static EntityManagerFactory emf_html = null ; // Persistence.createEntityManagerFactory("htmlDocs");
	
	private static ViewAnnotation VIEW_ANNOTATION = null ;
	
	private static AnnotationStatus comment_status = null ;
	private static AnnotationStatus ok_status = null ;
	private static AnnotationStatus not_ok_status = null ;
	private static AnnotationStatus flame_status = null ;
	private static AnnotationStatus troll_status = null ;
	private static AnnotationStatus understand_status = null ;
	private static AnnotationStatus spam_status = null ;
	
	private static final String NORMAL_VIEW = "/WEB-INF/jsp/PortletViewAnnotation/view.jsp";
	private static final String MAXIMIZED_VIEW = "/WEB-INF/jsp/PortletViewAnnotation/view.jsp";
	private static final String HELP_VIEW = "/WEB-INF/jsp/PortletViewAnnotation/help.jsp";

	private PortletRequestDispatcher normalView;
	private PortletRequestDispatcher maximizedView;
	private PortletRequestDispatcher helpView;
	private PortletRequestDispatcher editView;

	public void doView( RenderRequest request, RenderResponse response )
	throws PortletException, IOException {
		setRenderAttributes(request);
		if( WindowState.MINIMIZED.equals( request.getWindowState() ) ) {
			return;
		}
		if ( WindowState.NORMAL.equals( request.getWindowState() ) ) {
			normalView.include( request, response );
		} else {
			maximizedView.include( request, response );
		}
	}

	protected void doEdit( RenderRequest request, RenderResponse response )
	throws PortletException, IOException 
	{
		setRenderAttributes(request);
		editView.include( request, response );
	}

	protected void doHelp( RenderRequest request, RenderResponse response )
	throws PortletException, IOException {
		setRenderAttributes(request);
		helpView.include( request, response );
	}

	public void init( PortletConfig config ) throws PortletException {
		super.init( config );
		normalView = config.getPortletContext().getRequestDispatcher( NORMAL_VIEW );
		maximizedView = config.getPortletContext().getRequestDispatcher( MAXIMIZED_VIEW );
		helpView = config.getPortletContext().getRequestDispatcher( HELP_VIEW );
		
		emf_annotation = Persistence.createEntityManagerFactory("annotation");
		emf_resource = Persistence.createEntityManagerFactory("resource");
		emf_html = Persistence.createEntityManagerFactory("htmlDocs");
		
		daoAnnotation = new DAOAnnotation(emf_annotation) ;
		daoResource = new DAOResource(emf_resource) ;
		daoPost =  new DAOPost(emf_annotation) ;
		daoTag = new DAOTag(emf_annotation) ;
		daoHtml = new DAOHtmlDocs(emf_html) ;
		VIEW_ANNOTATION = new ViewAnnotation(emf_annotation);
		
		//Load color properties
    	Properties colorProps = PropertiesUtils.loadDefault(getPortletContext().getRealPath(color_properties));
		
		daoAnnotation.createSimpleCommentStatus(colorProps.getProperty("color_comment"));
		daoAnnotation.createAgreeStatus(colorProps.getProperty("color_accord"));
		daoAnnotation.createDisAgreeStatus(colorProps.getProperty("color_desaccord"));
		daoAnnotation.createClarifyStatus(colorProps.getProperty("color_clarify")) ;
		daoAnnotation.createSpamStatus(colorProps.getProperty("color_spam")) ;
        
        comment_status = daoAnnotation.retrieveAnnotationStatus("commentaire");
    	ok_status = daoAnnotation.retrieveAnnotationStatus("Accord");
    	not_ok_status = daoAnnotation.retrieveAnnotationStatus("Désaccord");
        flame_status = daoAnnotation.retrieveAnnotationStatus("Flame");
        troll_status = daoAnnotation.retrieveAnnotationStatus("Troll");
        understand_status = daoAnnotation.retrieveAnnotationStatus("A clarifier");
        spam_status = daoAnnotation.retrieveAnnotationStatus("Spam");
        
		// create application properties with default
		Properties defaultProps = PropertiesUtils.loadDefault(getPortletContext().getRealPath(default_properties));
		applicationProps = new Properties(defaultProps);
		
		// now load properties from last invocation
		applicationProps = PropertiesUtils.loadLastState(applicationProps, getPortletContext().getRealPath(saved_properties));
		
		//sets values 
		if(applicationProps.getProperty("defaultURL")!=null) defaultURL = applicationProps.getProperty("defaultURL");
		URL_SERVLET_ANNOTATIONS = applicationProps.getProperty("url_servlet_annotations");
		URL_SERVLET_CONSULTATION = applicationProps.getProperty("url_servlet_consultation");
	}

	public void destroy() {
		normalView = null;
		maximizedView = null;
		helpView = null;
		super.destroy();
	}

	private void sendEvent(String _event_type , Serializable _event_object , ActionResponse response)
	{
		System.out.println("[PortletViewAnnotation.sendEvent] type : " + _event_type + " value : " + _event_object);
		response.setEvent(_event_type, _event_object);
	}
	
	private void setRenderAttributes(RenderRequest request) 
	{
		String _current_url = (String)request.getPortletSession().getAttribute("current_url");
		if(_current_url == null) _current_url = defaultURL ;
		request.setAttribute("current_url", _current_url);
		//récupérer toutes les annotations concernant cette url
		int _indice_pagination = 0 ;
		if(request.getPortletSession().getAttribute("pagination")!=null) _indice_pagination = Integer.parseInt((String)request.getPortletSession().getAttribute("pagination")) ;
		request.getPortletSession().setAttribute("pagination", ""+_indice_pagination);
		request.setAttribute("pagination", ""+_indice_pagination) ;
		//int _nb_annotations = (int) RETRIEVER_ANNOTATIONS.computeNbAnnotations(_current_url);
		int _nb_annotations = (int) daoAnnotation.computeNbAnnotations(_current_url);
		int _max_pagination = _nb_annotations / 10 ;
		if(_max_pagination!=0 && _max_pagination * 10 == _nb_annotations) _max_pagination -- ;
		request.setAttribute("max_pagination", ""+_max_pagination);
		int _first_indice = _indice_pagination * 10 ;
		int _max_result = 10 ;
		List<Annotation> _annotations = null;
		if(request.getPortletSession().getAttribute("annotation_order")!=null)
		{
			String _order = (String)request.getPortletSession().getAttribute("annotation_order") ;
			if(_order.equalsIgnoreCase("status"))
			{
				 if(request.getPortletSession().getAttribute("annotation_chrono")!=null)
				 {
					 String _chrono = (String)request.getPortletSession().getAttribute("annotation_chrono") ;
					 if(_chrono.equalsIgnoreCase("asc")) _annotations = daoAnnotation.retrieveAnnotationsGroupByStatus(_current_url , false , _first_indice , _max_result);
					 else if(_chrono.equalsIgnoreCase("dsc"))  _annotations = daoAnnotation.retrieveAnnotationsGroupByStatus(_current_url , true , _first_indice , _max_result);
				 }
				 else _annotations = daoAnnotation.retrieveAnnotationsGroupByStatus(_current_url , false , _first_indice , _max_result);
			}
			else if(_order.equalsIgnoreCase("auteur"))
			{
				 if(request.getPortletSession().getAttribute("annotation_chrono")!=null)
				 {
					 String _chrono = (String)request.getPortletSession().getAttribute("annotation_chrono") ;
					 if(_chrono.equalsIgnoreCase("asc")) _annotations = daoAnnotation.retrieveAnnotationsGroupByFirstAuthor(_current_url , false , _first_indice , _max_result);
					 else if(_chrono.equalsIgnoreCase("dsc"))  _annotations = daoAnnotation.retrieveAnnotationsGroupByFirstAuthor(_current_url , true , _first_indice , _max_result);
				 }
				 else _annotations = daoAnnotation.retrieveAnnotationsGroupByFirstAuthor(_current_url , false , _first_indice , _max_result);
			}
		}
		else if(request.getPortletSession().getAttribute("annotation_chrono")!=null)
		{
			String _chrono = (String)request.getPortletSession().getAttribute("annotation_chrono") ;
			if(_chrono.equalsIgnoreCase("asc")) _annotations = daoAnnotation.retrieveAnnotations(_current_url , false , _first_indice , _max_result);
			else if(_chrono.equalsIgnoreCase("dsc"))  _annotations = daoAnnotation.retrieveAnnotations(_current_url , true , _first_indice , _max_result);
		}
		else _annotations = daoAnnotation.retrieveAnnotations(_current_url , false , _first_indice , _max_result);
		if(_annotations!= null && _annotations.size() > 0)
		{
			//lister tous les status d'annotation possibles pour l'affichage du style
			List list_annotation_status = daoAnnotation.retrieveAnnotationStatus() ;
			if(list_annotation_status != null && list_annotation_status.size() >0 )	request.setAttribute("list_annotation_status", list_annotation_status);
			
			//Traiter les annotations
			request.getPortletSession().setAttribute("annotations", _annotations);
			request.setAttribute("annotations",_annotations);
			//récupérer le contenu des annotations directement en HTML
			//ainsi que interroger pour savoir si l'accord ou le désaccord est déjà exprimé, le spam, le flame, le troll ...
			ArrayList<String> annotations_content = new ArrayList<String>();
			ArrayList<Boolean> agreements_expressed = new ArrayList<Boolean>();
			ArrayList<Boolean> flames_expressed = new ArrayList<Boolean>();
			ArrayList<Boolean> trolls_expressed = new ArrayList<Boolean>();
			ArrayList<Boolean> spams_expressed = new ArrayList<Boolean>();
			for(Annotation annotation : _annotations)
			{
				annotations_content.add(VIEW_ANNOTATION.annotationToHTMLShort(annotation)); 
				if(request.getPortletSession().getAttribute("user")!=null) 
				{
					agreements_expressed.add(daoAnnotation.agreementExpressed((Agent)request.getPortletSession().getAttribute("user"), annotation.getAccess().getEffectiveURI() + "?id=" + annotation.getId() ));
					flames_expressed.add(daoAnnotation.flameExpressed((Agent)request.getPortletSession().getAttribute("user"), annotation.getAccess().getEffectiveURI() + "?id=" + annotation.getId() ));
					trolls_expressed.add(daoAnnotation.trollExpressed((Agent)request.getPortletSession().getAttribute("user"), annotation.getAccess().getEffectiveURI() + "?id=" + annotation.getId() ));
					spams_expressed.add(daoAnnotation.spamExpressed((Agent)request.getPortletSession().getAttribute("user"), annotation.getAccess().getEffectiveURI() + "?id=" + annotation.getId() ));
				}
				else
				{
					agreements_expressed.add(true);
					flames_expressed.add(true);
					trolls_expressed.add(true);
					spams_expressed.add(true);
				}
			}
			request.setAttribute("annotations_content" , annotations_content);
			request.setAttribute("agreements_expressed", agreements_expressed);
			request.setAttribute("flames_expressed" , flames_expressed);
			request.setAttribute("trolls_expressed", trolls_expressed);
			request.setAttribute("spams_expressed", spams_expressed);
		}
		else
		{
			request.getPortletSession().removeAttribute("annotations");
		}
		
		ArrayList<HighlightSelectionHTML> _colored = (ArrayList<HighlightSelectionHTML>)request.getPortletSession().getAttribute("colored");
		if(_colored == null) _colored = new ArrayList<HighlightSelectionHTML>();
		request.getPortletSession().setAttribute("colored", _colored);
		request.setAttribute("colored", _colored);
		ArrayList<Annotation> _coloreds_annotation = (ArrayList<Annotation>)request.getPortletSession().getAttribute("colored_annotation");
		if(_coloreds_annotation == null) _coloreds_annotation = new ArrayList<Annotation>();
		request.getPortletSession().setAttribute("colored_annotation", _coloreds_annotation);
		request.setAttribute("colored_annotation", _coloreds_annotation);
		if(request.getPortletSession().getAttribute("user")!=null) 
		{
			request.getPortletSession().setAttribute("user", request.getPortletSession().getAttribute("user"));
			request.setAttribute("user", request.getPortletSession().getAttribute("user"));
		}
			
		if(request.getPortletSession().getAttribute("annotation_status")!=null) request.setAttribute("annotation_status", request.getPortletSession().getAttribute("annotation_status"));
		if(URL_SERVLET_ANNOTATIONS != null) request.setAttribute("url_servlet", URL_SERVLET_ANNOTATIONS);
		if(URL_SERVLET_CONSULTATION != null) request.setAttribute("url_consultation" , URL_SERVLET_CONSULTATION);
	}

	public void processEvent(EventRequest request, EventResponse response)
	{
		Event event = request.getEvent();
		//System.out.println("[PortletViewAnnotation.processEvent] event : " + event.getName());
		String _event_name = event.getName() ;
		//if(_event_name.equalsIgnoreCase("loadedurl"))
		if(_event_name.equalsIgnoreCase("loadedurl") || _event_name.equalsIgnoreCase("toLoadUrl"))
		{
			if(event.getValue() instanceof String)
			{
				String url = (String) event.getValue();
				if(StringOp.isValidURI(url))
				{
					request.getPortletSession().setAttribute("current_url", url);
					//on change d'url, on vide les colorations
					request.getPortletSession().removeAttribute("colored");
					request.getPortletSession().removeAttribute("colored_annotation");
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
		if(_event_name.equalsIgnoreCase("change_status"))
		{
			AnnotationStatus status = (AnnotationStatus)event.getValue();
			//System.out.println("[PortletCreateAnnotation.processevent] Status : " + status.getLabel());
			request.getPortletSession().setAttribute("annotation_status", status);
		}
		if(_event_name.equalsIgnoreCase("UserLog"))
		{
			if(event.getValue() instanceof UserAccount)
			{
				UserAccount _current_user = (UserAccount)event.getValue() ;
				if(_current_user.getId() != null) request.getPortletSession().setAttribute("user", _current_user);
			}
		}
		if(_event_name.equalsIgnoreCase("UserUnLog"))
		{
			request.getPortletSession().removeAttribute("user");
		}
		if(_event_name.equalsIgnoreCase("loadedurl"))
		{
			if(event.getValue() instanceof String)
			{
				String url = (String) event.getValue();
				if (url.startsWith("http://")) 
				{
					//response.setRenderParameter("url", url.toLowerCase());
					request.getPortletSession().setAttribute("current_url",  url.toLowerCase());	
					//vider la liste des sélections colorées
					request.getPortletSession().removeAttribute("colored_selections");
				}
			}
		}
		if(_event_name.equalsIgnoreCase("url_servlet_annotations"))
		{
			//System.out.println("[PortletViewAnnotation] process url_servlet_annotations");
			if(event.getValue() instanceof String)
			{
				String url = (String) event.getValue();
				if (url.startsWith("http://")) 
				{
					URL_SERVLET_ANNOTATIONS = url ;
				}
			}
		}
		if(_event_name.equalsIgnoreCase("url_servlet_consultation"))
		{
			//System.out.println("[PortletViewAnnotation] process url_servlet_consultation");
			if(event.getValue() instanceof String)
			{
				String url = (String) event.getValue();
				if (url.startsWith("http://")) 
				{
					URL_SERVLET_CONSULTATION = url ;
				}
			}
		}
	}

	public void processAction(ActionRequest request, ActionResponse response)
	throws PortletException, PortletSecurityException, IOException 
	{
		String op = request.getParameter("op");
		//System.out.println("[PortletViewAnnotation.processAction] op : " + op);
		if ((op != null) && (op.trim().length() > 0)) 
		{
			//Colorer une annotation
			if (op.equalsIgnoreCase("color_annotation")) 
			{
				doColorAnnotation(request, response);
				return;
			}
			//décolorer une annotation
			if(op.equalsIgnoreCase("uncolor_annotation"))
			{
				doUnColorAnnotation(request , response);
				return;
			}
			if(op.equalsIgnoreCase("reply_annotation"))
			{
				doReplyAnnotation(request , response);
				return;
			}
			if(op.equalsIgnoreCase("create_annotation"))
			{
				try {
					doCreateAnnotation(request , response);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					System.out.println("[PortletViewAnnotation.processAction] doCreateAction encounters a JSON Parsing exception.");
					e.printStackTrace();
				} catch (ParserException e) {
					// TODO Auto-generated catch block
					System.out.println("[PortletViewAnnotation.processAction] doCreateAction encounters a HTML Parsing exception to extract title of the current page.");
					e.printStackTrace();
				}
				return;
			}
			if(op.equalsIgnoreCase("order"))
			{
				doOrderAnnotation(request , response);
				return;
			}
			if(op.equalsIgnoreCase("change_pagination"))
			{
				doChangePagination(request , response);
				return;
			}
			if(op.equalsIgnoreCase("quick_reply"))
			{
				//System.out.println("[PortletViewAnnotation.prosessAction] do quick reply");
				doCreateQuickReply(request, response);
				return;
			}
			if(op.equalsIgnoreCase("reply_ok"))
			{
				doCreateAgreementReply(request , response , true);
				return ;
			}
			if(op.equalsIgnoreCase("reply_not_ok"))
			{
				doCreateAgreementReply(request , response , false);
				return ;
			}
			if(op.equalsIgnoreCase("reply_understand"))
			{
				doCreateUnderstandReply(request , response);
				return ;
			}
			if(op.equalsIgnoreCase("reply_flame"))
			{
				doCreateFlameReply(request , response);
				return ;
			}
			if(op.equalsIgnoreCase("reply_troll"))
			{
				doCreateTrollReply(request , response);
				return ;
			}
			if(op.equalsIgnoreCase("reply_spam"))
			{
				doCreateSpamReply(request , response);
				return ;
			}
		} 
		else 
		{
			System.out.println("[PortletViewAnnotation.processAction 3]" + op);
		}
		System.out.println("[PortletViewAnnotation.processAction 4]" + op);
		response.setPortletMode(PortletMode.VIEW);
	}

	private void doCreateUnderstandReply(ActionRequest request, ActionResponse response)
	{
		if(URL_SERVLET_ANNOTATIONS == null)
    	{
    		System.out.println("[PortletCreationAnnotation.doCreateAnnotation] URL_SERVLET_ANNOTATIONS is null");
    	}
    	else
    	{
    		UserAccount _author = (UserAccount)request.getPortletSession().getAttribute("user");
    		//créer l'uri d'accés et de représentation de l'annotation.
			//URI access = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
    		URI access = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
			//traitement du jugement seule ressource ajoutée
			Collection<Resource> added = new ArrayList<Resource>();
			Judgment _tag;
			//List _tags = RETRIEVER_TAG.retrieveJudgment("je ne comprends pas");
			List _tags = daoTag.retrieveJudgment("je ne comprends pas");
			if(_tags.size() > 0)
			{
				_tag = (Judgment)_tags.get(0);
			}
			else
			{
				/*URI representsResource = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				_tag = CREATOR_TAG.createAndGetJudgment("je ne comprends pas", APPLICATION_NAME, representsResource);*/
				URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				_tag = daoTag.createAndGetJudgment("je ne comprends pas", APPLICATION_NAME, representsResource);
			}
			added.add(_tag);
			//une seule ressource annotée, l'annotation sur laquelle porte l'accord ou le désaccord
			Collection<Resource> annotateds = new ArrayList<Resource>();
			Collection<URI> annotatedURIs = new ArrayList<URI>();
			long id = Long.parseLong((String)request.getParameter("id_annotate"));
			//Resource annotated = RETRIEVER_ANNOTATIONS.retrieveResource(id);
			Resource annotated = daoAnnotation.retrieveResource(id);
			annotateds.add(annotated);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] URI annotated : " + annotated.getRepresentsResource());
			//TODO décommenter quand le problème de la génération d'url de représentation des annotations sera réglé 
			//annotatedURIs.add(annotated.getRepresentsResource());
			String true_url = URL_SERVLET_ANNOTATIONS + "?id=" + id ;
			//URI true_annotated_representation = CREATOR_URI.createAndGetURI(true_url);
			URI true_annotated_representation = daoResource.createAndGetURI(true_url);
			annotatedURIs.add(true_annotated_representation);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] creates quick reply");
			//CREATOR_ANNOTATION.createAnnotation("Troll", APPLICATION_NAME, access, access, understand_status, added, annotateds , annotatedURIs, _author);
			daoAnnotation.createAnnotation("Troll", APPLICATION_NAME, access, access, understand_status, added, annotateds , annotatedURIs, _author);
    	}
	}
	
	private void doCreateSpamReply(ActionRequest request, ActionResponse response)
	{
		if(URL_SERVLET_ANNOTATIONS == null)
    	{
    		System.out.println("[PortletCreationAnnotation.doCreateAnnotation] URL_SERVLET_ANNOTATIONS is null");
    	}
    	else
    	{
    		UserAccount _author = (UserAccount)request.getPortletSession().getAttribute("user");
    		//créer l'uri d'accés et de représentation de l'annotation.
			//URI access = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
    		URI access = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
			//traitement du jugement seule ressource ajoutée
			Collection<Resource> added = new ArrayList<Resource>();
			Judgment _tag;
			//List _tags = RETRIEVER_TAG.retrieveJudgment("C'est déjà dit");
			List _tags = daoTag.retrieveJudgment("C'est déjà dit");
			if(_tags.size() > 0)
			{
				_tag = (Judgment)_tags.get(0);
			}
			else
			{
				//URI representsResource = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				//_tag = CREATOR_TAG.createAndGetJudgment("C'est déjà dit", APPLICATION_NAME, representsResource);
				URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				_tag = daoTag.createAndGetJudgment("C'est déjà dit", APPLICATION_NAME, representsResource);
			}
			added.add(_tag);
			//une seule ressource annotée, l'annotation sur laquelle porte l'accord ou le désaccord
			Collection<Resource> annotateds = new ArrayList<Resource>();
			Collection<URI> annotatedURIs = new ArrayList<URI>();
			long id = Long.parseLong((String)request.getParameter("id_annotate"));
			//Resource annotated = RETRIEVER_ANNOTATIONS.retrieveResource(id);
			Resource annotated = daoAnnotation.retrieveResource(id);
			annotateds.add(annotated);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] URI annotated : " + annotated.getRepresentsResource());
			//TODO décommenter quand le problème de la génération d'url de représentation des annotations sera réglé 
			//annotatedURIs.add(annotated.getRepresentsResource());
			String true_url = URL_SERVLET_ANNOTATIONS + "?id=" + id ;
			//URI true_annotated_representation = CREATOR_URI.createAndGetURI(true_url);
			URI true_annotated_representation = daoResource.createAndGetURI(true_url);
			annotatedURIs.add(true_annotated_representation);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] creates quick reply");
			//CREATOR_ANNOTATION.createAnnotation("Spam", APPLICATION_NAME, access, access, spam_status, added, annotateds , annotatedURIs, _author);
			daoAnnotation.createAnnotation("Spam", APPLICATION_NAME, access, access, spam_status, added, annotateds , annotatedURIs, _author);
    	}
	}
	
	private void doCreateTrollReply(ActionRequest request, ActionResponse response)
	{
		if(URL_SERVLET_ANNOTATIONS == null)
    	{
    		System.out.println("[PortletCreationAnnotation.doCreateAnnotation] URL_SERVLET_ANNOTATIONS is null");
    	}
    	else
    	{
    		UserAccount _author = (UserAccount)request.getPortletSession().getAttribute("user");
    		//créer l'uri d'accés et de représentation de l'annotation.
			//URI access = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
    		URI access = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
			//traitement du jugement seule ressource ajoutée
			Collection<Resource> added = new ArrayList<Resource>();
			Judgment _tag;
			//List _tags = RETRIEVER_TAG.retrieveJudgment("Troll");
			List _tags = daoTag.retrieveJudgment("Troll");
			if(_tags.size() > 0)
			{
				_tag = (Judgment)_tags.get(0);
			}
			else
			{
				//URI representsResource = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				//_tag = CREATOR_TAG.createAndGetJudgment("Troll", APPLICATION_NAME, representsResource);
				URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				_tag = daoTag.createAndGetJudgment("Troll", APPLICATION_NAME, representsResource);
			}
			added.add(_tag);
			//une seule ressource annotée, l'annotation sur laquelle porte l'accord ou le désaccord
			Collection<Resource> annotateds = new ArrayList<Resource>();
			Collection<URI> annotatedURIs = new ArrayList<URI>();
			long id = Long.parseLong((String)request.getParameter("id_annotate"));
			//Resource annotated = RETRIEVER_ANNOTATIONS.retrieveResource(id);
			Resource annotated = daoAnnotation.retrieveResource(id);
			annotateds.add(annotated);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] URI annotated : " + annotated.getRepresentsResource());
			//TODO décommenter quand le problème de la génération d'url de représentation des annotations sera réglé 
			//annotatedURIs.add(annotated.getRepresentsResource());
			String true_url = URL_SERVLET_ANNOTATIONS + "?id=" + id ;
			//URI true_annotated_representation = CREATOR_URI.createAndGetURI(true_url);
			URI true_annotated_representation = daoResource.createAndGetURI(true_url);
			annotatedURIs.add(true_annotated_representation);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] creates quick reply");
			//CREATOR_ANNOTATION.createAnnotation("Troll", APPLICATION_NAME, access, access, troll_status, added, annotateds , annotatedURIs, _author);
			daoAnnotation.createAnnotation("Troll", APPLICATION_NAME, access, access, troll_status, added, annotateds , annotatedURIs, _author);
    	}
	}

	private void doCreateFlameReply(ActionRequest request, ActionResponse response) 
	{
		if(URL_SERVLET_ANNOTATIONS == null)
    	{
    		System.out.println("[PortletCreationAnnotation.doCreateAnnotation] URL_SERVLET_ANNOTATIONS is null");
    	}
    	else
    	{
    		UserAccount _author = (UserAccount)request.getPortletSession().getAttribute("user");
    		//créer l'uri d'accés et de représentation de l'annotation.
			//URI access = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
    		URI access = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
			//traitement du jugement seule ressource ajoutée
			Collection<Resource> added = new ArrayList<Resource>();
			Judgment _tag;
			//List _tags = RETRIEVER_TAG.retrieveJudgment("Flame");
			List _tags = daoTag.retrieveJudgment("Flame");
			if(_tags.size() > 0)
			{
				_tag = (Judgment)_tags.get(0);
			}
			else
			{
				//URI representsResource = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				//_tag = CREATOR_TAG.createAndGetJudgment("Flame", APPLICATION_NAME, representsResource);
				URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				_tag = daoTag.createAndGetJudgment("Flame", APPLICATION_NAME, representsResource);
			}
			added.add(_tag);
			//une seule ressource annotée, l'annotation sur laquelle porte l'accord ou le désaccord
			Collection<Resource> annotateds = new ArrayList<Resource>();
			Collection<URI> annotatedURIs = new ArrayList<URI>();
			long id = Long.parseLong((String)request.getParameter("id_annotate"));
			//Resource annotated = RETRIEVER_ANNOTATIONS.retrieveResource(id);
			Resource annotated = daoAnnotation.retrieveResource(id);
			annotateds.add(annotated);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] URI annotated : " + annotated.getRepresentsResource());
			//TODO décommenter quand le problème de la génération d'url de représentation des annotations sera réglé 
			//annotatedURIs.add(annotated.getRepresentsResource());
			String true_url = URL_SERVLET_ANNOTATIONS + "?id=" + id ;
			//URI true_annotated_representation = CREATOR_URI.createAndGetURI(true_url);
			URI true_annotated_representation = daoResource.createAndGetURI(true_url);
			annotatedURIs.add(true_annotated_representation);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] creates quick reply");
			//CREATOR_ANNOTATION.createAnnotation("Flame", APPLICATION_NAME, access, access, flame_status, added, annotateds , annotatedURIs, _author);
			daoAnnotation.createAnnotation("Flame", APPLICATION_NAME, access, access, flame_status, added, annotateds , annotatedURIs, _author);
    	}
	}

	private void doChangePagination(ActionRequest request, ActionResponse response) 
	{
		if(request.getParameter("value_pagination")!=null) request.getPortletSession().setAttribute("pagination", request.getParameter("value_pagination"));
		//System.out.println("[PortletViewAnnotation.doChangePagination] new pagination : " + request.getAttribute("pagination"));
	}

	private void doOrderAnnotation(ActionRequest request, ActionResponse response) 
	{
		if(request.getParameter("annotation_order")!= null ) request.getPortletSession().setAttribute("annotation_order" , request.getParameter("annotation_order"));
		else request.getPortletSession().removeAttribute("annotation_order");
		if(request.getParameter("annotation_chrono")!= null) request.getPortletSession().setAttribute("annotation_chrono", request.getParameter("annotation_chrono"));
	}

	private void doCreateAgreementReply(ActionRequest request , ActionResponse response , boolean agree)
	{
		if(URL_SERVLET_ANNOTATIONS == null)
    	{
    		System.out.println("[PortletCreationAnnotation.doCreateAnnotation] URL_SERVLET_ANNOTATIONS is null");
    	}
    	else
    	{
    		UserAccount _author = (UserAccount)request.getPortletSession().getAttribute("user");
    		//créer l'uri d'accés et de représentation de l'annotation.
			//URI access = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
    		URI access = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
			//traitement du jugement seule ressource ajoutée
			Collection<Resource> added = new ArrayList<Resource>();
			Judgment _tag;
			if(agree)
			{
				//List _tags = RETRIEVER_TAG.retrieveJudgment("je suis d'accord");
				List _tags = daoTag.retrieveJudgment("je suis d'accord");
				if(_tags.size() > 0)
				{
					_tag = (Judgment)_tags.get(0);
				}
				else
				{
					//URI representsResource = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
					//_tag = CREATOR_TAG.createAndGetJudgment("je suis d'accord", APPLICATION_NAME, representsResource);
					URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
					_tag = daoTag.createAndGetJudgment("je suis d'accord", APPLICATION_NAME, representsResource);
				}
			}
			else
			{
				//List _tags = RETRIEVER_TAG.retrieveJudgment("je ne suis pas d'accord");
				List _tags = daoTag.retrieveJudgment("je ne suis pas d'accord");
				if(_tags.size() > 0)
				{
					_tag = (Judgment)_tags.get(0);
				}
				else
				{
					//URI representsResource = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
					//_tag = CREATOR_TAG.createAndGetJudgment("je ne suis pas d'accord", APPLICATION_NAME, representsResource);
					URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
					_tag = daoTag.createAndGetJudgment("je ne suis pas d'accord", APPLICATION_NAME, representsResource);
				}
			}
			added.add(_tag);
			//une seule ressource annotée, l'annotation sur laquelle porte l'accord ou le désaccord
			Collection<Resource> annotateds = new ArrayList<Resource>();
			Collection<URI> annotatedURIs = new ArrayList<URI>();
			long id = Long.parseLong((String)request.getParameter("id_annotate"));
			//Resource annotated = RETRIEVER_ANNOTATIONS.retrieveResource(id);
			Resource annotated = daoAnnotation.retrieveResource(id);
			annotateds.add(annotated);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] URI annotated : " + annotated.getRepresentsResource());
			//TODO décommenter quand le problème de la génération d'url de représentation des annotations sera réglé 
			//annotatedURIs.add(annotated.getRepresentsResource());
			String true_url = URL_SERVLET_ANNOTATIONS + "?id=" + id ;
			//URI true_annotated_representation = CREATOR_URI.createAndGetURI(true_url);
			URI true_annotated_representation = daoResource.createAndGetURI(true_url);
			annotatedURIs.add(true_annotated_representation);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] creates quick reply");
			//if(agree) CREATOR_ANNOTATION.createAnnotation("Accord", APPLICATION_NAME, access, access, ok_status, added, annotateds , annotatedURIs, _author);
			//else CREATOR_ANNOTATION.createAnnotation("Désaccord", APPLICATION_NAME, access, access, not_ok_status, added, annotateds , annotatedURIs, _author);
			if(agree) daoAnnotation.createAnnotation("Accord", APPLICATION_NAME, access, access, ok_status, added, annotateds , annotatedURIs, _author);
			else daoAnnotation.createAnnotation("Désaccord", APPLICATION_NAME, access, access, not_ok_status, added, annotateds , annotatedURIs, _author);
    	}
	}
	
	private void doCreateQuickReply(ActionRequest request, ActionResponse response)
	{
		if(URL_SERVLET_ANNOTATIONS == null)
    	{
    		System.out.println("[PortletCreationAnnotation.doCreateAnnotation] URL_SERVLET_ANNOTATIONS is null");
    	}
    	else
    	{
    		UserAccount _author = (UserAccount)request.getPortletSession().getAttribute("user");
    		String annotation_title = "Réponse rapide" ;
    		//créer l'uri d'accés et de représentation de l'annotation.
			//URI access = CREATOR_URI.createAndGetURI(URL_SERVLET_ANNOTATIONS);
    		URI access = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
			//URI access = RETRIEVER_URI.retrieveURI(_effective_uri);
			//traitement du commentaire seule ressource ajoutée
			Collection<Resource> added = new ArrayList<Resource>();
			String _comment = request.getParameter("quick_reply_added_simpletext_commentaire");
			//PostStatus status_st = RETRIEVER_SIMPLETEXT_STATUS.retrievePostStatus("commentaire");
			//Post st = CREATOR_SIMPLETEXT.createAndGetPost(APPLICATION_NAME, "commentaire de la réponse rapide : ", access, _comment, status_st, _author);
			PostStatus status_st = daoPost.retrievePostStatus("commentaire");
			Post st = daoPost.createAndGetPost(APPLICATION_NAME, "commentaire de la réponse rapide : ", access, _comment, status_st, _author);
			added.add(st);
			//une seule ressource annotée, l'annotation sur laquelle porte la réponse rapide
			Collection<Resource> annotateds = new ArrayList<Resource>();
			Collection<URI> annotatedURIs = new ArrayList<URI>();
			long id = Long.parseLong((String)request.getParameter("id_annotate"));
			//Resource annotated = RETRIEVER_ANNOTATIONS.retrieveResource(id);
			Resource annotated = daoAnnotation.retrieveResource(id);
			annotateds.add(annotated);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] URI annotated : " + annotated.getRepresentsResource());
			//TODO décommenter quand le problème de la génération d'url de représentation des annotations sera réglé 
			//annotatedURIs.add(annotated.getRepresentsResource());
			String true_url = URL_SERVLET_ANNOTATIONS + "?id=" + id ;
			//URI true_annotated_representation = CREATOR_URI.createAndGetURI(true_url);
			URI true_annotated_representation = daoResource.createAndGetURI(true_url);
			annotatedURIs.add(true_annotated_representation);
			//System.out.println("[PortletViewAnnotation.doCreateQuickReply] creates quick reply");
			//CREATOR_ANNOTATION.createAnnotation(annotation_title, APPLICATION_NAME, access, access, comment_status, added, annotateds , annotatedURIs, _author);
			daoAnnotation.createAnnotation(annotation_title, APPLICATION_NAME, access, access, comment_status, added, annotateds , annotatedURIs, _author);
    	}
	}
	
	private void doCreateAnnotation(ActionRequest request, ActionResponse response) throws JSONException, ParserException 
	{
		String url = (String)request.getPortletSession().getAttribute("current_url");
		if(url == null) url = defaultURL ;
		if(URL_SERVLET_ANNOTATIONS == null) System.out.println("[PortletViewAnnotation.doCreateAnnotation] URL_SERVLET_ANNOTATIONS is null");
    	else
    	{
    		boolean _test_status_tag = false ;
    		UserAccount _author = (UserAccount)request.getPortletSession().getAttribute("user");
    		AnnotationStatus status = (AnnotationStatus)request.getPortletSession().getAttribute("annotation_status");
    		String annotation_title = request.getParameter("annotation_title") ;
    		annotation_title = UtilsForAnnotation.prepareAnnotationTitle(annotation_title, _author, status);
    		if(status.getLabel().equalsIgnoreCase("tag")) _test_status_tag = true ;
    		
    		//créer l'uri d'accés et de représentation de l'annotation.
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
			
			//Une seule ressource annotée, la ressource en cours
			Collection<Resource> annotated = new ArrayList<Resource>();
			Collection<URI> annotatedURIs = new ArrayList<URI>();
			URI _access = daoResource.createAndGetURI(url);
			HTMLPage annotated_page = new HTMLPage();
			annotated_page.setURL(url);
			String title = annotated_page.extractTitle();
			//TODO Modifier pour gérer quand on est face à un topic et non une pageWeb classique
			//TODO pour l'instant on se fout du contenu de la page
			WebPage _page = daoHtml.createAndGetWebPage(title, APPLICATION_NAME, null, _access, _access, _access);
			annotatedURIs.add(_access);
			annotated.add(_page);
			if(_author != null) UtilsForAnnotation.processAuthor(_author , added, daoTag, daoResource, URL_SERVLET_ANNOTATIONS, APPLICATION_NAME);
			System.out.println("[PortletViewAnnotation.doCreateAnnotation] creates annotation ...");
			daoAnnotation.createAnnotation(annotation_title, APPLICATION_NAME, access, access, status, added, annotated , annotatedURIs, _author);
    	}
	}

	private void doColorAnnotation(ActionRequest request, ActionResponse response)
	{
		long _annotation_id = Long.parseLong(request.getParameter("to_color"));
		List<Annotation> _annotations = (List<Annotation>)request.getPortletSession().getAttribute("annotations");
		ArrayList<HighlightSelectionHTML> _colored = (ArrayList<HighlightSelectionHTML>)request.getPortletSession().getAttribute("colored");
		ArrayList<Annotation> _coloreds_annotation = (ArrayList<Annotation>) request.getPortletSession().getAttribute("colored_annotation");
		if(_colored == null) _colored = new ArrayList<HighlightSelectionHTML>();
		if(_coloreds_annotation == null) _coloreds_annotation = new ArrayList<Annotation>();
		if(_annotations != null)
		{
			Annotation _to_color = null ;
			for(Annotation _annotation : _annotations)
			{
				if(_annotation.getId() == _annotation_id)
				{
					_to_color = _annotation ;
					_coloreds_annotation.add(_annotation);
					break ;
				}
			}
			if(_to_color != null) 
			{
				String info = VIEW_ANNOTATION.toShortResume(_to_color) ;
				for(Resource _annotated : _to_color.getAnnotated())
				{
					if(_annotated instanceof SelectionHTML)
					{
						HighlightSelectionHTML _to_highlight = new HighlightSelectionHTML();
						_to_highlight.setSelection((SelectionHTML)_annotated);
						//TODO prévoir d'afficher un commentaire s'il y en a un
						//String _info = "sélection de " + _to_color.getLabel();
						//_to_highlight.setInfo(_info);
						_to_highlight.setInfo(info);
						_to_highlight.setId(generateHighlightId(request));
						//TODO prévoir de récupérer un paramétre indiquant le style
						setHighlightStyle(_to_color , _to_highlight);
						//_to_highlight.setStyle("background-color:red;");
						_colored.add(_to_highlight);
						sendEvent("tohighlight" , _to_highlight, response);
					}
				}
			}
		}
		request.getPortletSession().setAttribute("colored" , _colored);
		request.getPortletSession().setAttribute("colored_annotation" , _coloreds_annotation);
	}
	
	private void setHighlightStyle(Annotation annotation , HighlightSelectionHTML highlight)
	{
		AnnotationStatus status = annotation.getStatus() ;
		if(status.getColor() != null) highlight.setStyle("background-color: " + status.getColor() + " ;");
		else highlight.setStyle("background-color:orange;");
	}
	
	private void doUnColorAnnotation(ActionRequest request, ActionResponse response)
	{
		long _annotation_id = Long.parseLong(request.getParameter("to_uncolor"));
		List<Annotation> _annotations = (List<Annotation>)request.getPortletSession().getAttribute("annotations");
		ArrayList<HighlightSelectionHTML> _coloreds = (ArrayList<HighlightSelectionHTML>)request.getPortletSession().getAttribute("colored");
		ArrayList<Annotation> _coloreds_annotation = (ArrayList<Annotation>) request.getPortletSession().getAttribute("colored_annotation");
		if(_coloreds != null && _annotations != null && _coloreds_annotation != null)
		{
			//récupérer l'annotation à décolorer
			Annotation _to_uncolor = null ;
			for(Annotation _annotation : _annotations)
			{
				if(_annotation.getId() == _annotation_id)
				{
					_to_uncolor = _annotation ;
					for(Annotation _already_colored : _coloreds_annotation)
					{
						if(_to_uncolor.getId().compareTo(_already_colored.getId()) == 0 )
						{
							_coloreds_annotation.remove(_already_colored);
							break ;
						}
					}
					//System.out.println("[PortletViewAnnotation.doUnColorAnnotation] remove uncolor " + _coloreds_annotation.size() );
					break ;
				}
			}
			if(_to_uncolor != null) 
			{
				//System.out.println("[PortletViewAnnotation.doUnColorAnnotation] _to_uncolor not null");
				//pour chaque élément annoté, si c'est une sélection, signaler qu'il faut le décolorer
				for(Resource _annotated : _to_uncolor.getAnnotated())
				{
					if(_annotated instanceof SelectionHTML)
					{
						//retrouver le HighLight correspondant
						for(HighlightSelectionHTML _colored : _coloreds)
						{
							if(_colored.getSelection().getId().compareTo(_annotated.getId()) == 0 )
							{
								//envoyer le highlight à décolorer
								sendEvent("todelete" , _colored , response);
								_coloreds.remove(_colored);
								break ;
							}
						}
					}
				}
			}
		}
		request.getPortletSession().setAttribute("colored" , _coloreds);
		request.getPortletSession().setAttribute("colored_annotation" , _coloreds_annotation);
	}
	
	private String generateHighlightId(ActionRequest request)
	{
		int nb_highlight = 0 ;
		if(request.getPortletSession().getAttribute("nb_highlight")!=null)
		{
			nb_highlight = (Integer)request.getPortletSession().getAttribute("nb_highlight");
			int _new_value = nb_highlight + 1;
			request.getPortletSession().setAttribute("nb_highlight", _new_value);
		}
		else request.getPortletSession().setAttribute("nb_highlight", 1);
		return request.getPortletSession().getId() + this.getPortletName() + nb_highlight;
	}
	
	private void doReplyAnnotation(ActionRequest request, ActionResponse response)
	{
		String _url_to_reply = (String)request.getParameter("url_reply");
		this.sendEvent("toLoadUrl", _url_to_reply, response);
		request.getPortletSession().setAttribute("current_url", _url_to_reply);
	}
}