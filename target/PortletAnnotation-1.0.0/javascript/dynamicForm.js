var cpt = 0 ;

function showForm(obj)
{
	var el = document.getElementById(obj);
	if ( el.style.display != 'none' ) 
	{
		el.style.display = 'none';
	}
	else 
	{
		el.style.display = '';
	}
}

function addField(url_json)
{
	div = document.getElementById('status_content');

	newSelectFieldType = document.createElement('select');
	newSelectFieldType.setAttribute('name' , 'fieldType');
	newSelectFieldType.setAttribute('onchange' , "fieldTypeChange(" + cpt + " , this.value, '" + url_json + "');");
	newOpt1 = document.createElement('option');
	newOpt1.setAttribute('value' , '0');
	newOpt1.innerHTML = "Post";
	newSelectFieldType.appendChild(newOpt1);
	newOpt2 = document.createElement('option');
	newOpt2.setAttribute('value' , '1');
	newOpt2.innerHTML = "Tag";
	newSelectFieldType.appendChild(newOpt2);
	newOpt3 = document.createElement('option');
	newOpt3.setAttribute('value' , '2');
	newOpt3.innerHTML = "Jugement";
	newSelectFieldType.appendChild(newOpt3);
	newOpt4 = document.createElement('option');
	newOpt4.setAttribute('value' , '3');
	newOpt4.innerHTML = "Mood";
	newSelectFieldType.appendChild(newOpt4);
	newOpt5 = document.createElement('option');
	newOpt5.setAttribute('value' , '4');
	newOpt5.innerHTML = "Domaine";
	newSelectFieldType.appendChild(newOpt5);

	newDiv = document.createElement('div');
	newDiv.setAttribute('id' , 'fieldAttributes_'+ cpt);
	
	//div.appendChild(newBR);
	div.appendChild(newSelectFieldType);
	div.appendChild(newDiv);
	cpt ++ ;
}

function fieldTypeChange(intValue , choiceValue, url_json)
{
	div = document.getElementById('fieldAttributes_' + intValue);

	div.innerHTML = "";
	
	newSpanStatus = document.createElement('span') ;
	newSpanStatus.innerHTML = "Statut :" ;
	newTextStatus = document.createElement('input');
	newTextStatus.setAttribute('id' , 'fieldStatus_' + intValue);
	newTextStatus.setAttribute('type' , 'text');
	newTextStatus.setAttribute('value' , '');

	div.appendChild(newSpanStatus);
	div.appendChild(newTextStatus);

	if(choiceValue != 0)
	{
		newSelectChoiceType = document.createElement('select');
		newSelectChoiceType.setAttribute('id' , 'choice_type_' + intValue);
		newSelectChoiceType.setAttribute('onchange' , "choiceTypeChange(" + intValue + " , this.value, " + choiceValue + " , '" + url_json + "')");
		newOpt1 = document.createElement('option');
		newOpt1.setAttribute('value' , '0');
		newOpt1.innerHTML = "Radio Bouton";
		newSelectChoiceType.appendChild(newOpt1);
		newOpt2 = document.createElement('option');
		newOpt2.setAttribute('value' , '1');
		newOpt2.innerHTML = "Saisie libre";
		newSelectChoiceType.appendChild(newOpt2);
		div.appendChild(newSelectChoiceType);

		newDiv = document.createElement('div');
		newDiv.setAttribute('id' , 'possible_choices_' + intValue);
		div.appendChild(newDiv);
		
		newSpanCardinalite = document.createElement('span') ;
		newSpanCardinalite.innerHTML = "Cardinalité :" ;
		newTextCardinalite = document.createElement('input');
		newTextCardinalite.setAttribute('id' , 'cardinalite_' + intValue);
		newTextCardinalite.setAttribute('type' , 'text');
		newTextCardinalite.setAttribute('value' , '');
		div.appendChild(newSpanCardinalite);
		div.appendChild(newTextCardinalite);
	}
	
	newBr = document.createElement('br');
	div.appendChild(newBr);
}

function processJSONTags(data , div, intValue)
{
	if(data != null)
	{
		nb_tags = data.liste.length ;
		if(nb_tags > 0)
		{
			for(i=0 ; i<nb_tags; i ++)
			{
				newInputCheck = document.createElement('input');
				newInputCheck.setAttribute('type', 'checkbox');
				newInputCheck.setAttribute('name', 'tags_choice_' + intValue);
				newInputCheck.setAttribute('id', 'tagId_' + data.liste[i].id);
				newInputCheck.setAttribute('value', data.liste[i].label);
				newSpanLabel = document.createElement('span');
				newSpanLabel.innerHTML = data.liste[i].label + " : " ;
				div.appendChild(newSpanLabel);
				div.appendChild(newInputCheck);
				newBr = document.createElement('br');
				div.appendChild(newBr);
			}
		}
		else
		{
			emptyChoice = document.createElement('span');
			emptyChoice.innerHTML = "Pas de choix possible..." ;
			div.appendChild(emptyChoice);
		}
	}
	else
	{
		emptyChoice = document.createElement('span');
		emptyChoice.innerHTML = "Pas de choix possible..." ;
		div.appendChild(emptyChoice);
	}
}

function choiceTypeChange(intValue , choiceValue, kindOfElt, url_json)
{
	div = document.getElementById('possible_choices_' + intValue);
	div.innerHTML = "";
	if(choiceValue == 0)
	{
		newSpan = document.createElement('span');
		newSpan.innerHTML = "Choix possibles : ";
		div.appendChild(newSpan);
		
		newBR = document.createElement('br');
		div.appendChild(newBR);

		if(kindOfElt == 1)//Tag
		{
			tags = $.getJSON(url_json + "?type=tag" , function(data){processJSONTags(data , div, intValue);});
		}
		else if(kindOfElt == 2)//Jugement
		{
			tags = $.getJSON(url_json + "?type=judgment" , function(data){processJSONTags(data , div, intValue);});
		}
		else if(kindOfElt == 3)//Mood
		{
			tags = $.getJSON(url_json + "?type=mood" , function(data){processJSONTags(data , div, intValue);});
		}
		else if(kindOfElt == 4)//Domain
		{
			tags = $.getJSON(url_json + "?type=domain" , function(data){processJSONTags(data , div, intValue);});
		}
	}	
}
function validateCreateAnnotationStatusForm(cpt_form)
{
	json_description = "[{\"status\":\"sélection\",\"cardinalite\":\"infinite\",\"choices\":[],\"className\":\"Resource\",\"type\":\"ANNOTATED\"}" ;
	
	list_fields = document.getElementsByName('fieldType');
	for(i = 0 ; i < list_fields.length ; i ++)
	{
		field_type_value = list_fields[i].value ;
		classname = getClassname(field_type_value);
		field_status = document.getElementById('fieldStatus_' + i).value;
		list_checkboxes_tags = document.getElementsByName('tags_choice_' + i);
		choices = "";
		cpt_choices = 0 ;
		for(j = 0 ; j < list_checkboxes_tags.length ; j++)
		{
			if(list_checkboxes_tags[j].checked) 
			{
				if(cpt_choices == 0)
				{
					choices = choices + "\""+ list_checkboxes_tags[j].value +"\"";
					cpt_choices ++ ;
				}
				else choices = choices + ",\""+ list_checkboxes_tags[j].value +"\"";
			}
		}
		cardinalite = '';
		if(field_type_value != 0)
		{
			cardValue = document.getElementById('cardinalite_' + i).value;
			cardinalite = getCardinalite(cardValue);
		}
		json_description = json_description + ",{\"status\":\"" + field_status + "\",\"cardinalite\":\"|" + cardinalite + "|\",\"choices\":[" 
			+ choices + "],\"className\":\"" + classname + "\",\"type\":\"ADDED\"}";
	}
	json_description = json_description + "]";
	alert(json_description);
	document.getElementById('json_descripteur_' + cpt_form).value = json_description ;
}
function getCardinalite(cardValue)
{
	if(cardValue == null) return '';
	else 
	{
		card = parseInt(cardValue);
		if(isNaN(card)) return 'n';
		else return card;
	}
}
function getClassname(intValue)
{
	if(intValue == 0) return "Post" ;
	else if(intValue == 1) return "Tag" ;
	else if(intValue == 2) return "Judgment" ;
	else if(intValue == 3) return "Mood" ;
	else if(intValue == 4) return "Domain" ;
	else return "Resource" ;
}