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
package fr.natoine.servletConsultation;

import java.io.IOException;
import java.util.Date;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.natoine.dao.annotation.DAOTag;
import fr.natoine.dao.consultation.DAOConsultation;
import fr.natoine.dao.resource.DAOResource;
import fr.natoine.dao.user.DAOUser;
import fr.natoine.model_resource.URI;
import fr.natoine.model_user.Agent;
import fr.natoine.model_user.UserAccount;

public class ServletConsultation extends HttpServlet 
{
	private static DAOConsultation daoconsultation = null ;
	private static EntityManagerFactory emf_consultation = null ;
	
	private static DAOUser daouser = null ;
	private static EntityManagerFactory emf_user = null ;
	
	private static DAOResource daoURI = null ;
	private static EntityManagerFactory emf_uri = null ;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public ServletConsultation() 
    {
        super();
        emf_consultation = Persistence.createEntityManagerFactory("consultation");
        daoconsultation = new DAOConsultation(emf_consultation);
        
        emf_user = Persistence.createEntityManagerFactory("user");
        daouser = new DAOUser(emf_user);
        
        emf_uri = Persistence.createEntityManagerFactory("resource");
        daoURI = new DAOResource(emf_uri);
    }
	
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
		createConsultation(request);
    }
	
	private void createConsultation(HttpServletRequest request)
	{
		if(request.getParameter("uid") != null && request.getParameter("uri") != null)
		{
			long uid = Long.parseLong(request.getParameter("uid"));
			Agent agent = daouser.retrieveAgent(uid);
			String uri = request.getParameter("uri");
			URI true_uri = daoURI.createAndGetURI(uri);
			String context = "";
			context = request.getParameter("context");
			Date new_date = new Date();
			if(agent instanceof UserAccount && true_uri.getEffectiveURI() != null)
			{
				daoconsultation.createsConsultation((UserAccount)agent, new_date, new_date, true_uri, context);
			}
		}
	}
}