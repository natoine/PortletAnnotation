function validateForm(uid)
{
	list_checkboxes = document.getElementsByName('checkbox_to_annotate_' + uid);
	to_annotate = "";
	for(i=0 ; i < list_checkboxes.length ; i++)
	{
		if(list_checkboxes[i].checked) to_annotate = to_annotate + i + " ";
	}
	//si null, c'est qu'on est dans le cas du formulaire d'annotation rapide pour le PortletViewAnnotation
	if(document.getElementById('list_selection_to_annotate_' + uid) != null) document.getElementById('list_selection_to_annotate_' + uid).value = to_annotate ;
	
	fields_tags = document.getElementsByName('formfield_Tag_' + uid);
	for(i=0 ; i < fields_tags.length ; i++ )
	{
		id_formField = fields_tags[i].id ;
		//alert(id_formField);
		tosub = 'formfield_'.length;
		status_id = id_formField.substring(tosub);
		//alert(status_id);
		input_for_form = document.getElementById('checkboxes_annotation_added_tag_' + status_id);
		if(input_for_form != null)
		{
			//parcourir les checkboxes
			checkboxes_tag = document.getElementsByName('checkbox_tag_' + status_id);
			good_value = "{\"liste_tags\":[";
			cpt_values = 0 ;
			for(j=0 ; j < checkboxes_tag.length ; j++)
			{
				if(checkboxes_tag[j].checked == true) 
				{
					if(cpt_values > 0) good_value = good_value + ",\"" + checkboxes_tag[j].value + "\"";
					else good_value = good_value + "\"" + checkboxes_tag[j].value + "\"";
					cpt_values ++ ;	
				}
			}
			good_value = good_value + "]}";
			//setter la value de input_for_form
			//alert(good_value);
			input_for_form.value = good_value ;
			alert("new_value : " + input_for_form.value);
		}
	}
	fields_jgts = document.getElementsByName('formfield_Judgment_' + uid);
	for(i=0 ; i < fields_jgts.length ; i++ )
	{
		id_formField = fields_jgts[i].id ;
		//alert(id_formField);
		tosub = 'formfield_'.length;
		status_id = id_formField.substring(tosub);
		//alert(status_id);
		input_for_form = document.getElementById('checkboxes_annotation_added_judgment_' + status_id);
		if(input_for_form != null)
		{
			//parcourir les checkboxes
			checkboxes_jgt = document.getElementsByName('checkbox_judgment_' + status_id);
			good_value = "{\"liste_jgts\":[";
			cpt_values = 0 ;
			for(j=0 ; j < checkboxes_jgt.length ; j++)
			{
				if(checkboxes_jgt[j].checked == true) 
				{
					if(cpt_values > 0) good_value = good_value + ",\"" + checkboxes_jgt[j].value + "\"";
					else good_value = good_value + "\"" + checkboxes_jgt[j].value + "\"";
					cpt_values ++ ;	
				}
			}
			good_value = good_value + "]}";
			//setter la value de input_for_form
			//alert(good_value);
			input_for_form.value = good_value ;
			alert("new_value : " + input_for_form.value);
		}
	}
	fields_moods = document.getElementsByName('formfield_Mood_' + uid);
	for(i=0 ; i < fields_moods.length ; i++ )
	{
		id_formField = fields_moods[i].id ;
		//alert(id_formField);
		tosub = 'formfield_'.length;
		status_id = id_formField.substring(tosub);
		//alert(status_id);
		input_for_form = document.getElementById('checkboxes_annotation_added_mood_' + status_id);
		if(input_for_form != null)
		{
			//parcourir les checkboxes
			checkboxes_mood = document.getElementsByName('checkbox_mood_' + status_id);
			good_value = "{\"liste_moods\":[";
			cpt_values = 0 ;
			for(j=0 ; j < checkboxes_mood.length ; j++)
			{
				if(checkboxes_mood[j].checked == true) 
				{
					if(cpt_values > 0) good_value = good_value + ",\"" + checkboxes_mood[j].value + "\"";
					else good_value = good_value + "\"" + checkboxes_mood[j].value + "\"";
					cpt_values ++ ;	
				}
			}
			good_value = good_value + "]}";
			//setter la value de input_for_form
			//alert(good_value);
			input_for_form.value = good_value ;
			alert("new_value : " + input_for_form.value);
		}
	}
	fields_domains = document.getElementsByName('formfield_Domain_' + uid);
	for(i=0 ; i < fields_domains.length ; i++ )
	{
		id_formField = fields_domains[i].id ;
		//alert(id_formField);
		tosub = 'formfield_'.length;
		status_id = id_formField.substring(tosub);
		//alert(status_id);
		input_for_form = document.getElementById('checkboxes_annotation_added_domain_' + status_id);
		if(input_for_form != null)
		{
			//parcourir les checkboxes
			checkboxes_domain = document.getElementsByName('checkbox_domain_' + status_id);
			good_value = "{\"liste_domains\":[";
			cpt_values = 0 ;
			for(j=0 ; j < checkboxes_domain.length ; j++)
			{
				if(checkboxes_domain[j].checked == true) 
				{
					if(cpt_values > 0) good_value = good_value + ",\"" + checkboxes_domain[j].value + "\"";
					else good_value = good_value + "\"" + checkboxes_domain[j].value + "\"";
					cpt_values ++ ;	
				}
			}
			good_value = good_value + "]}";
			//setter la value de input_for_form
			//alert(good_value);
			input_for_form.value = good_value ;
			alert("new_value : " + input_for_form.value);
		}
	}
}