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

import fr.natoine.dao.annotation.DAOAnnotation;
import fr.natoine.model_annotation.AnnotationStatus;
import fr.natoine.properties.PropertiesUtils;

public class PortletAnnotationStatus extends GenericPortlet implements EventPortlet
{
	private Properties applicationProps ;
	private final String default_properties = "/properties/defaultProperties";
	private final String saved_properties = "/properties/appProperties";
	
	private static final String NORMAL_VIEW = "/WEB-INF/jsp/PortletAnnotationStatus/status_choice.jsp";
	private static final String EDIT_VIEW = "/WEB-INF/jsp/PortletAnnotationStatus/status_creation.jsp";
	
	private static DAOAnnotation DAOANNOTATION = null;
	private static EntityManagerFactory emf_annotation = null ; // Persistence.createEntityManagerFactory("annotation");
	
	private PortletRequestDispatcher normalView;
	private PortletRequestDispatcher editView;
	
	private static String URL_SERVLET_JSON_TAGS = null ;

	public void doView( RenderRequest request, RenderResponse response )
	throws PortletException, IOException 
	{
		setRenderAttributes(request);
		normalView.include( request, response );
	}

	public void doEdit( RenderRequest request, RenderResponse response)
	throws PortletException, IOException
	{
		setRenderAttributes(request);
		editView.include(request, response);
	}
	
	public void init( PortletConfig config ) throws PortletException 
	{
		super.init( config );
		normalView = config.getPortletContext().getRequestDispatcher( NORMAL_VIEW );
		editView = config.getPortletContext().getRequestDispatcher( EDIT_VIEW );
		emf_annotation = Persistence.createEntityManagerFactory("annotation");
		DAOANNOTATION = new DAOAnnotation(emf_annotation);
		
		// create application properties with default
		Properties defaultProps = PropertiesUtils.loadDefault(getPortletContext().getRealPath(default_properties));
		applicationProps = new Properties(defaultProps);
		
		// now load properties from last invocation
		applicationProps = PropertiesUtils.loadLastState(applicationProps, getPortletContext().getRealPath(saved_properties));
		
		URL_SERVLET_JSON_TAGS = applicationProps.getProperty("url_json_tags");
	}

	public void destroy() 
	{
		normalView = null ;
		editView = null ;
		super.destroy() ;
	}

	private void sendEvent(String _event_type , Serializable _event_object , ActionResponse response)
	{
		System.out.println("[PortletAnnotationStatus.sendEvent] type : " + _event_type + " value : " + _event_object);
		response.setEvent(_event_type, _event_object);
	}

	private void setRenderAttributes(RenderRequest request) 
	{
		//récupération de tous les status existants
		List as = DAOANNOTATION.retrieveAnnotationStatus();
		request.setAttribute("status" , as);
		request.setAttribute("servlet_json_tags", URL_SERVLET_JSON_TAGS);
	}

	public void processAction(ActionRequest request, ActionResponse response)
	throws PortletException, PortletSecurityException, IOException {
		String op = request.getParameter("op");
		StringBuffer message = new StringBuffer(1024);
		if ((op != null) && (op.trim().length() > 0)) 
		{
			//Mise à jour de l'url dans la barre de navigation de la portlet en View
			if (op.equalsIgnoreCase("change_status")) 
			{
				//vider la liste de sélections
				doChangeStatus(request, response);
				return;
			}
			else if(op.equalsIgnoreCase("create_annotation_status"))
			{
				doCreateAnnotationStatus(request, response);
				return;
			}
			else
			{
				//System.out.println("[BrowserPortlet.processAction 2]" + op);
				message.append("Operation not found");
			}
		} 
		else 
		{
			//System.out.println("[PortletCreateAnnotation.processAction 3]" + op);
			message.append("Operation is null");
		}
		//System.out.println("[PortletCreateAnnotation.processAction 4]" + op);
		response.setRenderParameter("message", message.toString());
		response.setPortletMode(PortletMode.VIEW);
	}

	public void processEvent(EventRequest request, EventResponse response)
	{
		Event event = request.getEvent();
		//System.out.println("[PortletViewAnnotation.processEvent] event : " + event.getName());
		String _event_name = event.getName() ;
		if(_event_name.equalsIgnoreCase("url_servlet_json_tags"))
		{
			//System.out.println("[PortletViewAnnotation] process url_servlet_annotations");
			if(event.getValue() instanceof String)
			{
				String url = (String) event.getValue();
				if (url.startsWith("http://")) 
				{
					URL_SERVLET_JSON_TAGS = url ;
				}
			}
		}
	}
	
	private void doChangeStatus(ActionRequest request, ActionResponse response)
	{
		if(request.getParameter("id_status")!=null)
		{
			AnnotationStatus status = DAOANNOTATION.retrieveAnnotationStatus(Long.parseLong(request.getParameter("id_status")));
			//System.out.println("[PortletAnnotationStatus.doChangeStatus] Status : " + status.getLabel());
			this.sendEvent("change_status", status, response);
		}
	}
	
	private void doCreateAnnotationStatus(ActionRequest request, ActionResponse response)
	{
		System.out.println("[PortletAnnotationStatus.doCreateAnnotationStatus]");
		if(request.getParameter("status_title") != null && request.getParameter("status_descripteur") != null)
		{
			String label = request.getParameter("status_title") ; //TODO vérification de la disponibilité
			String comment = "";
			if(request.getParameter("status_comment") != null) comment = request.getParameter("status_comment") ;
			String color = "yellow" ;
			if(request.getParameter("status_color") != null) color = request.getParameter("status_color") ;
			String descripteur = request.getParameter("status_descripteur");
			if(request.getParameter("father_id") != null)
			{
				System.out.println("[PortletAnnotationStatus.doCreateAnnotationStatus] gonna creates a status child");
				long father_id = Long.parseLong(request.getParameter("father_id")) ;
				DAOANNOTATION.createAnnotationStatusChild(label, comment, color, father_id, descripteur);
			}
			else
			{
				System.out.println("[PortletAnnotationStatus.doCreateAnnotationStatus] gonna creates a status");
				DAOANNOTATION.createAnnotationStatus(label, comment, color, descripteur);
			}
		}
	}
}