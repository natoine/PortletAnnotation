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
package fr.natoine.PortletTopic;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import fr.natoine.dao.annotation.DAOTopic;
import fr.natoine.dao.resource.DAOResource;
import fr.natoine.model_annotation.Topic;
import fr.natoine.model_resource.URI;
import fr.natoine.properties.PropertiesUtils;
import fr.natoine.stringOp.StringOp;

public class PortletTopicsList extends GenericPortlet 
{
	private Properties applicationProps ;
	private final String saved_properties = "/properties/appProperties";
	private final String default_properties = "/properties/defaultProperties";
	
    private static final String NORMAL_VIEW = "/WEB-INF/jsp/PortletTopic/normal.jsp";
    private static final String MAXIMIZED_VIEW = "/WEB-INF/jsp/PortletTopic/normal.jsp";
    private static final String HELP_VIEW = "/WEB-INF/jsp/PortletTopic/help.jsp";
    private static final String EDIT_VIEW = "/WEB-INF/jsp/PortletTopic/edit.jsp";
    
    private static String URL_SERVLET_ANNOTATIONS = null ;
    private static URI URI_TOPICS_REPRESENTS = null ;

    private PortletRequestDispatcher normalView;
    private PortletRequestDispatcher maximizedView;
    private PortletRequestDispatcher helpView;
    private PortletRequestDispatcher editView;
    
    //private static RetrieveTopic RETRIEVER_TOPIC = null ;
	//private static CreateTopic CREATOR_TOPIC = null ;
    private static DAOTopic daoTopic = null ;
    private static EntityManagerFactory emf_annotation = null ; // Persistence.createEntityManagerFactory("annotation");
    

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

    protected void doHelp( RenderRequest request, RenderResponse response )
        throws PortletException, IOException {

        helpView.include( request, response );

    }
    
    protected void doEdit( RenderRequest request, RenderResponse response )
	throws PortletException, IOException 
	{
		setRenderAttributes(request);
		editView.include( request, response );
	}

    public void init( PortletConfig config ) throws PortletException {
        super.init( config );
        normalView = config.getPortletContext().getRequestDispatcher( NORMAL_VIEW );
        maximizedView = config.getPortletContext().getRequestDispatcher( MAXIMIZED_VIEW );
        helpView = config.getPortletContext().getRequestDispatcher( HELP_VIEW );
        editView = config.getPortletContext().getRequestDispatcher( EDIT_VIEW );
        
       // CREATOR_TOPIC = new CreateTopic();
       // RETRIEVER_TOPIC = new RetrieveTopic();
        
        emf_annotation = Persistence.createEntityManagerFactory("annotation");
        daoTopic = new DAOTopic(emf_annotation) ;
        
     // create application properties with default
		Properties defaultProps = PropertiesUtils.loadDefault(getPortletContext().getRealPath(default_properties));
		applicationProps = new Properties(defaultProps);
		
		// now load properties from last invocation
		applicationProps = PropertiesUtils.loadLastState(applicationProps, getPortletContext().getRealPath(saved_properties));
		
		//sets values 
		URL_SERVLET_ANNOTATIONS = applicationProps.getProperty("url_servlet_annotations");
		if(URL_SERVLET_ANNOTATIONS != null)
		{
			//URI_TOPICS_REPRESENTS = new CreateUri().createAndGetURI(URL_SERVLET_ANNOTATIONS);
			DAOResource daoResource = new DAOResource(Persistence.createEntityManagerFactory("resource"));
			URI_TOPICS_REPRESENTS = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
		}
    }

    public void destroy() {
        normalView = null;
        maximizedView = null;
        helpView = null;
        editView = null;
        super.destroy();
    }

    private void recursiveGetTopicsChild(Topic _top_topic, RenderRequest request)
    {
		//List _childs_topics = RETRIEVER_TOPIC.retrieveChildTopics(_top_topic);
    	List _childs_topics = daoTopic.retrieveChildTopics(_top_topic);
		if(_childs_topics.size() >0 ) 
		{
			request.setAttribute("child_topics_list_" + _top_topic.getId(), _childs_topics);
			for(Object _child_topic : _childs_topics)
			recursiveGetTopicsChild((Topic)_child_topic, request);
		}
    }
	
    private void setRenderAttributes(RenderRequest request) 
	{
    	if(URL_SERVLET_ANNOTATIONS == null)
    	{
    		request.setAttribute("admin_message", "L'administrateur doit d'abord définir une URL pour la serlvet de création des topics (voir les fichiers properties)");
    	}
    	else
    	{
	    	//récupére tous les topics de premier niveau
	    	//List _top_topics = RETRIEVER_TOPIC.retrieveAllTopTopics();
    		List _top_topics = daoTopic.retrieveAllTopTopics();
	    	if(_top_topics.size()>0)
	    	{
	    		request.setAttribute("topics_list" , _top_topics);
	    		for(Object _top_topic : _top_topics)
	    		{
	    			//récupére les enfants récursivement
	    			recursiveGetTopicsChild((Topic)_top_topic, request);
	    		}
	    	}
    	}
	}
    
    public void processAction(ActionRequest request, ActionResponse response)
	throws PortletException, PortletSecurityException, IOException {
	String op = request.getParameter("op");
	StringBuffer message = new StringBuffer(1024);
	if ((op != null) && (op.trim().length() > 0)) 
	{
		//Création d'un topic top
		if (op.equalsIgnoreCase("addTopTopic")) 
		{
			//vider la liste de sélections
			doAddTopTopic(request, response);
			return;
		} 
		//Création d'un topic child
		else if(op.equalsIgnoreCase("addChildTopic"))
		{
			doAddChildTopic(request , response);
			return;
		}
		//Notification du changement de topic consulté 
		//TODO corriger, mettre un event spécifique aux topics
		else if(op.equalsIgnoreCase("to_load_url"))
		{
			doLoadURL(request , response);
			return;
		}
		else
		{
			System.out.println("[PortletTopicsList.processAction 2]" + op);
			message.append("Operation not found");
		}
	} 
	else 
	{
		System.out.println("[PortletTopicsList.processAction 3]" + op);
		message.append("Operation is null");
	}
	System.out.println("[PortletTopicsList.processAction 4]" + op);
	response.setRenderParameter("message", message.toString());
	response.setPortletMode(PortletMode.VIEW);
	}
    
    private void sendEvent(String _event_type , Serializable _event_object , ActionResponse response)
	{
		System.out.println("[PortletTopicsList.sendEvent] type : " + _event_type + " value : " + _event_object);
		response.setEvent(_event_type, _event_object);
	}
    
    private void doLoadURL(ActionRequest request, ActionResponse response) 
    {
    	if(request.getParameter("url")!=null)
		{
			String url = request.getParameter("url");
			this.sendEvent("toLoadUrl", url , response);
			this.sendEvent("loadedurl", url, response);
		}
	}

	private void doAddChildTopic(ActionRequest request, ActionResponse response) 
    {
    	String topic_title = request.getParameter("topic_label") ;
		topic_title = StringOp.deleteBlanks(topic_title);
		if(!StringOp.isNull(topic_title) && request.getParameter("father_id")!=null)
		{
			long _father_id = Long.parseLong(request.getParameter("father_id"));
			String topic_description = request.getParameter("topic_description");
			//CREATOR_TOPIC.createTopicChild(topic_title, "[PortletTopicsList] mode Edit", URI_TOPICS_REPRESENTS, topic_description, _father_id);
			daoTopic.createTopicChild(topic_title, "[PortletTopicsList] mode Edit", URI_TOPICS_REPRESENTS, topic_description, _father_id);
		}
	}

	private void doAddTopTopic(ActionRequest request, ActionResponse response) 
	{
		String topic_title = request.getParameter("topic_label") ;
		topic_title = StringOp.deleteBlanks(topic_title);
		if(!StringOp.isNull(topic_title))
		{
			String topic_description = request.getParameter("topic_description") ;
			//CREATOR_TOPIC.createTopic(topic_title, "[PortletTopicsList] mode Edit", URI_TOPICS_REPRESENTS, topic_description);
			daoTopic.createTopic(topic_title, "[PortletTopicsList] mode Edit", URI_TOPICS_REPRESENTS, topic_description);
		}
	}

	public static String processTopicListView(List _topics , HttpServletRequest request)
    {
    	String _html = "";
    	for(Object _topic : _topics)
		{
			long _topic_id = ((Topic)_topic).getId() ;
			String _lien_topic = ((Topic)_topic).getRepresentsResource().getEffectiveURI() + "?id=" + ((Topic)_topic).getId();
			String _topic_label = ((Topic)_topic).getLabel();
			String _topic_description = ((Topic)_topic).getDescription();
			_html = _html.concat("<div class=topic>");
			_html = _html.concat("<span class=topic_label title=\"" + _topic_description + "\"")
			.concat("onclick=\"updateRef('" + _lien_topic + "');\" >").concat(_topic_label).concat("</span>");
			if(request.getAttribute("child_topics_list_" + _topic_id)!= null)
			{
				List _topic_childs = (List)request.getAttribute("child_topics_list_" + _topic_id);
				if(_topic_childs.size() > 0)
					_html = _html.concat(processTopicListView(_topic_childs , request));
			}
			_html = _html.concat("</div>");
		}
    	return _html ;
    }
    public static String processTopicListEdit(List _topics , HttpServletRequest request)
    {
    	String _html = "";
    	for(Object _topic : _topics)
		{
			long _topic_id = ((Topic)_topic).getId() ;
			String _lien_topic = ((Topic)_topic).getRepresentsResource() + "?id=" + ((Topic)_topic).getId();
			String _topic_label = ((Topic)_topic).getLabel();
			String _topic_description = ((Topic)_topic).getDescription();
			_html = _html.concat("<div class=topic>");
			_html = _html.concat("<span class=topic_label title=\"" + _topic_description + "\"")
			.concat("onclick=\"updateRef('" + _lien_topic + "');\" >").concat(_topic_label).concat("</span>");
			//ajout d'un formulaire de création de topic child
			_html = _html.concat("<div class=add_child_topic>")
				.concat("<form id=addChildTopic_" + _topic_id + " method=post action=\"javascript:updateFormAddTopicChild('" + _topic_id + "');\">")
				//.concat("<input name=op type=hidden value=addChildTopic />")
				.concat("<input id=father_id_"+ _topic_id +" name=father_id type=hidden value="+ _topic_id +" />")
				.concat("Label : <input id=topic_label_"+ _topic_id +" type=text name=topic_label></input>")
				.concat("Description : <textarea id=topic_description_"+ _topic_id +" rows=3 cols=20 name=topic_description></textarea>")
				.concat("<input type=submit value=\"Créer ce Topic\" /></form></div>");			
			if(request.getAttribute("child_topics_list_" + _topic_id)!= null)
			{
				List _topic_childs = (List)request.getAttribute("child_topics_list_" + _topic_id);
				if(_topic_childs.size() > 0)
					_html = _html.concat(processTopicListEdit(_topic_childs , request));
			}
			_html = _html.concat("</div>");
		}
    	return _html ;
    }
}