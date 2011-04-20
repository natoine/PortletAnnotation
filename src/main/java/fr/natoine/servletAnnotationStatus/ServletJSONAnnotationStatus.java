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
package fr.natoine.servletAnnotationStatus;

import java.io.IOException;
import java.io.PrintWriter;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.natoine.dao.annotation.DAOAnnotation;
import fr.natoine.model_annotation.AnnotationStatus;

public class ServletJSONAnnotationStatus extends HttpServlet
{
	private static DAOAnnotation daoAnnotation = null ;   
	private static EntityManagerFactory emf_annotation = null ;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public ServletJSONAnnotationStatus() 
    {
        super();
        emf_annotation = Persistence.createEntityManagerFactory("annotation");
        daoAnnotation = new DAOAnnotation(emf_annotation);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
    	toDo(request , response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
    	toDo(request , response);
    }
    
    private String getJSONAnnotationStatus(long id)
    {
    	AnnotationStatus status = daoAnnotation.retrieveAnnotationStatus(id);
    	if(status.getId() != null) return status.getDescripteur().toString();
    	else return "";
    }
    
    private void toDo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	if(request.getParameter("id")!=null) //retrouver la bonne ressource
		{
			String id_string = request.getParameter("id") ;
			String json = getJSONAnnotationStatus(Long.parseLong(id_string));
			//request.setAttribute("json", json);
			response.setContentType("application/json");
			PrintWriter out = response.getWriter() ;
	    	out.print(json);
		}
    	//response.setContentType("text/html");
    	//RequestDispatcher _srd = this.getServletContext().getRequestDispatcher(response.encodeURL("/WEB-INF/jsp/servletJSONAnnotationStatus/json.jsp"));
		//_srd.include(request, response);
    }
}
