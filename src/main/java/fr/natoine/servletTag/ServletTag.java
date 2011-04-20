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
package fr.natoine.servletTag;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.natoine.dao.annotation.DAOTag;
import fr.natoine.model_annotation.Tag;

public class ServletTag extends HttpServlet
{
	private static DAOTag daoTag = null ;
	private static EntityManagerFactory emf_annotation = null ;
	
	/**
     * @see HttpServlet#HttpServlet()
     */
    public ServletTag() 
    {
        super();
        emf_annotation = Persistence.createEntityManagerFactory("annotation");
        daoTag = new DAOTag(emf_annotation);
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
    	toDo(request , response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
    	toDo(request, response);
    }
    
    private void toDo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
    	if(request.getParameter("type")!=null)
    	{
    		String type = request.getParameter("type") ;
    		List<Tag> tags = null;
    		if(type.equalsIgnoreCase("tag"))
    			tags = daoTag.retrieveAllTag();
    		else if(type.equalsIgnoreCase("judgment"))
    			tags = daoTag.retrieveAllJudgment();
    		else if(type.equalsIgnoreCase("mood"))
    			tags = daoTag.retrieveAllMood();
    		else if(type.equalsIgnoreCase("domain"))
    			tags = daoTag.retrieveAllDomain();
    		if(tags != null && tags.size() > 0)
    		{
    			JSONArray jsonarray = new JSONArray();
    			for(Tag tag : tags)
    			{
    				JSONObject jsonobj = new JSONObject();
    				try 
    				{
						jsonobj.put("id", tag.getId());
						jsonobj.put("label", tag.getLabel());
	    				jsonarray.put(jsonobj);
    				}
    				catch (JSONException e) 
    				{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    			//String liste = jsonarray.toString();
    			JSONObject list_obj = new JSONObject();
    			try 
    			{
					list_obj.put("liste", jsonarray);
				} catch (JSONException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//response.setContentType("text");
				response.setContentType("application/json");
				String liste = list_obj.toString();
				//liste = liste.replaceAll("'", "\'");
				//liste = liste.replaceAll("\"", "\\\\\"");
				//liste = liste.replaceAll("\"", "");
				PrintWriter out = response.getWriter() ;
		    	out.print(liste);
    			//request.setAttribute("liste", liste);
    		}
    	}
    	//response.setContentType("text/html");
    	//RequestDispatcher _srd = this.getServletContext().getRequestDispatcher(response.encodeURL("/WEB-INF/jsp/servletTag/tagsList.jsp"));
		//_srd.include(request, response);
    }
}