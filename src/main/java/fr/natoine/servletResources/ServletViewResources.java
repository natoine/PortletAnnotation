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
package fr.natoine.servletResources;

import java.io.IOException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.natoine.dao.annotation.DAOAnnotation;
import fr.natoine.model_annotation.Annotation;
import fr.natoine.model_resource.Resource;
import fr.natoine.viewAnnotations.ViewAnnotation;


/**
 * Servlet implementation class ServletViewResources
 */
public class ServletViewResources extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//Problem. if you do another controler api to extend tags or annotation, you will have to change RetrieveTag into your new Retriever...
	//private static RetrieveAnnotation RETRIEVER_RESOURCES = null ;
	//private static RetrieveTag RETRIEVER_TAGS = null ;
	private static DAOAnnotation daoAnnotation = null ;   
	private static EntityManagerFactory emf_annotation = null ;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ServletViewResources() {
        super();
        emf_annotation = Persistence.createEntityManagerFactory("annotation");
        daoAnnotation = new DAOAnnotation(emf_annotation);
        //RETRIEVER_RESOURCES = new RetrieveAnnotation();
        //RETRIEVER_TAGS = new RetrieveTag();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(request.getParameter("id")!=null) //retrouver la bonne ressource
		{
			String _id_string = request.getParameter("id") ;
			if(_id_string.contains("//"))
			{
				//System.out.println("[ServletViewResource.doGet] probablement appellé par un portlet");
				_id_string = _id_string.substring(0, _id_string.indexOf("//"));
			}
			if(_id_string.contains("/"))
			{
				//System.out.println("[ServletViewResource.doGet] probablement appellé par un portlet");
				_id_string = _id_string.substring(0, _id_string.indexOf("/"));
			}
			//System.out.println("[ServletViewResource.doGet] id : " + _id_string);
			long id = Long.parseLong(_id_string);
			Resource _to_view = daoAnnotation.retrieveResource(id);
			if(_to_view.getId() != null)
			{
				//afficher la ressource
				request.setAttribute("resource", _to_view);
				if(_to_view instanceof Annotation)
				{
					ViewAnnotation _view = new ViewAnnotation(emf_annotation);
					request.setAttribute("annotation_content_html",_view.annotationToHTML((Annotation)_to_view));
				}
				response.setContentType("text/html");
				RequestDispatcher _srd = this.getServletContext().getRequestDispatcher(response.encodeURL("/WEB-INF/jsp/servletViewResources/view_resource.jsp"));
				_srd.include(request, response);
			}
			else
			{
				//pas un id valide
				response.setContentType("text/html");
				RequestDispatcher _srd = this.getServletContext().getRequestDispatcher(response.encodeURL("/WEB-INF/jsp/servletViewResources/not_valid_id.jsp"));
				_srd.include(request, response);
			}
		}
		else //pas de ressource demandée
		{
			response.setContentType("text/html");
			RequestDispatcher _srd = this.getServletContext().getRequestDispatcher(response.encodeURL("/WEB-INF/jsp/servletViewResources/no_resource.jsp"));
			_srd.include(request, response);
		}
	}
}
