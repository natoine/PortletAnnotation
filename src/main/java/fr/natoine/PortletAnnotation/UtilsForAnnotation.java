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

import java.util.Collection;
import java.util.List;

import javax.portlet.ActionRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.natoine.dao.annotation.DAOPost;
import fr.natoine.dao.annotation.DAOTag;
import fr.natoine.dao.resource.DAOResource;
import fr.natoine.model_annotation.AnnotationStatus;
import fr.natoine.model_annotation.Domain;
import fr.natoine.model_annotation.Judgment;
import fr.natoine.model_annotation.Mood;
import fr.natoine.model_annotation.Post;
import fr.natoine.model_annotation.PostStatus;
import fr.natoine.model_annotation.Tag;
import fr.natoine.model_annotation.TagAgent;
import fr.natoine.model_resource.Resource;
import fr.natoine.model_resource.URI;
import fr.natoine.model_user.UserAccount;

public class UtilsForAnnotation 
{
	protected static String prepareAnnotationTitle(String _annotation_title , UserAccount _author, AnnotationStatus _status)
	{
		if(_annotation_title == null || _annotation_title.isEmpty())
		{
			String _author_name = "guest" ;
			if(_author != null) _author_name = _author.getPseudonyme() ; 
			if(! _status.getLabel().equalsIgnoreCase("tag")) _annotation_title = _status.getLabel() + " de : " + _author_name ;
			else _annotation_title = "tag : ";
		}
		return _annotation_title ;
	}

	protected static void processSimpleTextParameter(ActionRequest _request , String _parameter_name, String _annotation_title, URI _access, UserAccount _author, Collection<Resource> _added, DAOPost daoPost , String APPLICATION_NAME)
	{
		String content = _request.getParameter(_parameter_name);
		if(! content.equalsIgnoreCase("Tapez ici votre texte"))
		{
			String status_name = _parameter_name.substring(_parameter_name.lastIndexOf("_") + 1);
			PostStatus status_st = daoPost.retrievePostStatus(status_name);
			if(status_st.getId() == null) status_st = daoPost.retrievePostStatus("commentaire");
			Post st = daoPost.createAndGetPost(APPLICATION_NAME, status_name + " de l'annotation : " + _annotation_title, _access, content, status_st, _author);
			_added.add(st);
		}
	}
	
	protected static void processMoodParameter(ActionRequest _request, String _parameter_name, URI _access, Collection<Resource> _added, DAOTag daoTag, DAOResource daoResource, String URL_SERVLET_ANNOTATIONS, String APPLICATION_NAME) throws JSONException
    {
    	if(_parameter_name.contains("checkboxes_"))
    	{
    		String parameter = _request.getParameter(_parameter_name);
    		JSONObject json = null ;
    		if(parameter != null)
    		{
    			json = new JSONObject(parameter);
    			JSONArray liste_tags = json.getJSONArray("liste_moods");
    			for(int cpt_tag = 0 ; cpt_tag < liste_tags.length() ; cpt_tag ++)
    			{
    				Mood _tag;
    				String _tag_label = liste_tags.getString(cpt_tag);
    				List _tags = daoTag.retrieveMood(_tag_label);
    				if(_tags.size() > 0) _tag = (Mood)_tags.get(0);
    				else
    				{
    					URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
    					_tag = daoTag.createAndGetMood(_tag_label, APPLICATION_NAME, representsResource);
    				}
    				_added.add(_tag);
    			}
    		}
    	}
    	else
    	{
    		Mood _tag;
    		String _mood_label = _request.getParameter(_parameter_name);
    		List _tags = daoTag.retrieveMood(_mood_label);
    		if(_tags.size() > 0)
    		{
    			_tag = (Mood)_tags.get(0);
    		}
    		else
    		{
    			URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
    			_tag = daoTag.createAndGetMood(_mood_label, APPLICATION_NAME, representsResource);
    		}
    		_added.add(_tag);
    	}
    }
    
    protected static void processJgtParameter(ActionRequest _request, String _parameter_name, URI _access, Collection<Resource> _added, DAOTag daoTag, DAOResource daoResource, String URL_SERVLET_ANNOTATIONS, String APPLICATION_NAME) throws JSONException
	{
		if(_parameter_name.contains("checkboxes_"))
		{
			String parameter = _request.getParameter(_parameter_name);
			JSONObject json = null ;
			if(parameter != null)
			{
				json = new JSONObject(parameter);
				JSONArray liste_tags = json.getJSONArray("liste_jgts");
				for(int cpt_tag = 0 ; cpt_tag < liste_tags.length() ; cpt_tag ++)
				{
					Judgment _tag;
					String _tag_label = liste_tags.getString(cpt_tag);
					List _tags = daoTag.retrieveJudgment(_tag_label);
					if(_tags.size() > 0) _tag = (Judgment)_tags.get(0);
					else
					{
						URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
						_tag = daoTag.createAndGetJudgment(_tag_label, APPLICATION_NAME, representsResource);
					}
					_added.add(_tag);
				}
			}
		}
		else
		{
			Judgment _tag;
			String _judgment_label = _request.getParameter(_parameter_name);
			List _tags = daoTag.retrieveJudgment(_judgment_label);
			if(_tags.size() > 0) _tag = (Judgment)_tags.get(0);
			else
			{
				URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				_tag = daoTag.createAndGetJudgment(_judgment_label, APPLICATION_NAME, representsResource);
			}
			_added.add(_tag);
		}
	}
    
    protected static void processDomainParameter(ActionRequest _request, String _parameter_name, URI _access, Collection<Resource> _added, DAOTag daoTag, DAOResource daoResource, String URL_SERVLET_ANNOTATIONS, String APPLICATION_NAME) throws JSONException
	{
		if(_parameter_name.contains("checkboxes_"))
		{
			String parameter = _request.getParameter(_parameter_name);
			JSONObject json = null ;
			if(parameter != null)
			{
				json = new JSONObject(parameter);
				JSONArray liste_tags = json.getJSONArray("liste_domains");
				for(int cpt_tag = 0 ; cpt_tag < liste_tags.length() ; cpt_tag ++)
				{
					Tag _tag;
					String _tag_label = liste_tags.getString(cpt_tag);
					List _tags = daoTag.retrieveDomain(_tag_label);
					if(_tags.size() > 0) _tag = (Tag)_tags.get(0);
					else
					{
						URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
						_tag = daoTag.createAndGetDomain(_tag_label, APPLICATION_NAME, representsResource);
					}
					_added.add(_tag);
				}
			}
		}
		else
		{
			Domain _tag;
			String _domain_label = _request.getParameter(_parameter_name);
			List _tags = daoTag.retrieveDomain(_domain_label);
			if(_tags.size() > 0) _tag = (Domain)_tags.get(0);
			else
			{
				URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				_tag = daoTag.createAndGetDomain(_domain_label, APPLICATION_NAME, representsResource);
			}
			_added.add(_tag);
		}
	}
    
    protected static String processTagParameter(ActionRequest _request , String _parameter_name, String _annotation_title, URI _access, Collection<Resource> _added , boolean _test_status_tag, DAOTag daoTag, DAOResource daoResource, String URL_SERVLET_ANNOTATIONS, String APPLICATION_NAME) throws JSONException
    {
    	if(_parameter_name.contains("checkboxes_"))
		{
			String parameter = _request.getParameter(_parameter_name);
			JSONObject json = null ;
			if(parameter != null)
			{
				json = new JSONObject(parameter);
				JSONArray liste_tags = json.getJSONArray("liste_tags");
				for(int cpt_tag = 0 ; cpt_tag < liste_tags.length() ; cpt_tag ++)
				{
					Tag _tag;
					String _tag_label = liste_tags.getString(cpt_tag);
					List _tags = daoTag.retrieveTag(_tag_label);
					if(_tags.size() > 0) _tag = (Tag)_tags.get(0);
					else
					{
						URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
						_tag = daoTag.createAndGetTag(_tag_label, APPLICATION_NAME, representsResource);
					}
					_added.add(_tag);
				}
			}
		}
		else
		{
			Tag _tag;
			String _tag_label = _request.getParameter(_parameter_name);
			List _tags = daoTag.retrieveTag(_tag_label);
			if(_tags.size() > 0) _tag = (Tag)_tags.get(0);
			else
			{
				URI representsResource = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS);
				_tag = daoTag.createAndGetTag(_tag_label, APPLICATION_NAME, representsResource);
			}
			_added.add(_tag);
			//modif du titre par défaut si besoin
			if(_test_status_tag) _annotation_title = _annotation_title.concat(_tag_label + " ");
		}
    	return _annotation_title ;
    }
    
    protected static void processAuthor(UserAccount _author , Collection<Resource> _added, DAOTag daoTag, DAOResource daoResource, String URL_SERVLET_ANNOTATIONS, String APPLICATION_NAME)
    {
		//TODO ajouter le status à la collection de status de l'utilisateur
		//créer le tag est autheur
		TagAgent author_tag = daoTag.retrieveTagAgentIsAuthor(_author);
		if(author_tag.getId() == null)
		{
			//TODO corriger les URI
			URI _representsResourceTag = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS );
			URI _representsResourceDefinition = daoResource.createAndGetURI(URL_SERVLET_ANNOTATIONS );
			author_tag = daoTag.createAndGetTagAgentIsAuthor(_author, APPLICATION_NAME, _representsResourceTag, _representsResourceDefinition);
		}
		//ajouter le tag est autheur à l'annotation
		if(author_tag.getId() != null) _added.add(author_tag);
	}
}
