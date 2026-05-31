/* project srl - libreria javascript / jquery */

function inviaApprovazione(){
	$("#WORKFLOW_ACTION").val("APPROVE");
    $("#formInserimento").submit();
}
    	

function GetTdByName(row, column_name) {
        var idx = row.nodes().column(column_name + ":name")[0]; 
        var selector = "td:eq(" + idx + ")";
        return row.nodes().to$().find(selector)
}

$(function(){
	
	if ($('#box-required-fields').length) {
		$('#box-required-fields').hide();
	}
	
	$('form').find('input').each(function(){
	    if($(this).prop('required')){
	    	if ($('#box-required-fields').length) {
	    		$('#box-required-fields').show();
	    		 return false;
	    	}
	    }
	});
	
	$( "input:required" ).change(function() {
		if (!$(this).is(":disabled")) {
			if ($(this).val()!=null && $(this).val()!="") {
				$(this).css('background-color', 'white');
			} else {
				$(this).css('background-color', 'beige');
			}
		}
	});
	
	$( "textarea" ).change(function() {
		if (!$(this).is(":disabled")) {
			if ($(this).val()!=null && $(this).val()!="") {
				$(this).css('background-color', 'white');
			} else {
				$(this).css('background-color', 'beige');
			}
		}
	});
	
	$( "select" ).change(function() {
		if (!$(this).is(":disabled")) {
			if ($(this).val()!=null && $(this).val()!="") {
				$(this).siblings(".select2-container").find('.select2-selection').css('background-color', 'white');
			} else {
				var isRequired=$(this).attr('required');
				if (isRequired!=null && isRequired=="required") {				
					$(this).siblings(".select2-container").find('.select2-selection').css('background-color', 'beige');
				}
			}
		}
	});
	
	/* Metodo per popolare con Ajax le option di un controllo <select> caricando i dati di un dataset
	*
	* Sintassi: $(<<id select>>).trigger("loadDataSet",[ <<nome del dataset da config>> ,<<campo contenente valore della option>>,<<campo contenente testo della option>>,<<lista valori iniziali separati da ; >>]); 
	*
	* esempio: $("#LISTA_AZIENDE").trigger("loadDataSet",["DataSetAziende","CODICE","RAGSOC","0000001;CRODA0001;PROJECT001"]);
	*/
	$('select').bind('loadDataSet',function(event,dataset,valueField,textField,selectedList,whereCondition,selectFirstIfUnique) {
	
		var objectID=this.id;
		var $selectObject=$("#"+objectID).select2({
			theme: "bootstrap",
			containerCssClass: ':all:',
		    width: '100%'				
		} );			
		

		$.ajax({
			url: "astro?FUNCTIONID=JSONDataSet&dataset="+dataset+"&columns="+valueField+","+textField+"&WHERECONDITION="+whereCondition,
			dataType: 'json',
			type: 'GET',
			success: function (data) {
				
				if (selectFirstIfUnique == null ){
					selectFirstIfUnique=true;
				}  

				var firstValue="";
				
				if (selectFirstIfUnique){
					if(data.rows.length==1) {
						selectFirstIfUnique=true;
					} else {
						selectFirstIfUnique=false;
					}
				}
				
				var isMultiple=$selectObject.attr('multiple');
				if (isMultiple!=null && isMultiple=="multiple") {
					selectFirstIfUnique=false;
				}
				
				$.each(data.rows,function(i,data) {
					if (i==0) {
						firstValue=eval("data."+valueField);
						if (isMultiple!=null && isMultiple=="multiple") {
						} else {
							if (firstValue!="") {
								var blankOption="<option value=\"\">Selezionare...</option>";
								$(blankOption).appendTo($selectObject); 
							}
						} 
					}
					var option="<option value='"+eval("data."+valueField)+"'>"+eval("data."+textField)+"</option>";
					$(option).appendTo($selectObject); 
				}); 
				
				if (selectFirstIfUnique){
					$selectObject.val(firstValue).change();
				    return;
				}				
				
				
				if (selectedList == null || selectedList==""){
					$selectObject.val("").change();
				    return;
				}
				
				var selectedArray = selectedList.split(";");
				$selectObject.val(selectedArray).change();
				
				
				if (!$(this).is(":disabled")) {
					var isRequired=$(this).attr('required');
					if (isRequired!=null && isRequired=="required") {
						//$(this).siblings(".select2-container").css('border', '1px solid red');
						//$(this).siblings(".select2-container").css('border-radius', '4px');
						$(this).siblings(".select2-container").find('.select2-selection').css('background-color', 'beige');
					} else {
						$(this).siblings(".select2-container").find('.select2-selection').css('background-color', 'white');
					}
				}
				
				
			}
		});
	});
	
	$('.input-group.date').datepicker({
		format: "dd/mm/yyyy",
		todayBtn: "linked",
		keyboardNavigation: false,
		forceParse: false,
		calendarWeeks: true,
		autoclose: true
	});  	
	
	
	
	$('textarea').bind('loadDataSetText',function(event,dataset,valueField,textField,whereCondition) {
	
		var objectID=this.id;
		var $textareaObject=$("#"+objectID);

		$.ajax({
			url: "astro?FUNCTIONID=JSONDataSet&dataset="+dataset+"&columns="+valueField+","+textField+"&WHERECONDITION="+whereCondition,
			dataType: 'json',
			type: 'GET',
			success: function (data) {
				$.each(data.rows,function(i,data) {
					$textareaObject.val(eval("data."+textField));
				}); 
				
			}
		});
	});
	
	
	$('input[type="text"]').bind('loadDataSetSimpleText',function(event,dataset,valueField,textField,whereCondition) {
		var objectID=this.id;
		var $textObject=$("#"+objectID);
		

		$.ajax({
			url: "astro?FUNCTIONID=JSONDataSet&dataset="+dataset+"&columns="+valueField+","+textField+"&WHERECONDITION="+whereCondition,
			dataType: 'json',
			type: 'GET',
			success: function (data) {
				$.each(data.rows,function(i,data) {
					$textObject.val(eval("data."+textField));
				}); 
			}
		});
	});
	
	
	
	$('input[type="number"]').bind('loadDataSetSimpleNumber',function(event,dataset,valueField,textField,whereCondition) {
		var objectID=this.id;
		var $textObject=$("#"+objectID);
		

		$.ajax({
			url: "astro?FUNCTIONID=JSONDataSet&dataset="+dataset+"&columns="+valueField+","+textField+"&WHERECONDITION="+whereCondition,
			dataType: 'json',
			type: 'GET',
			success: function (data) {
				$.each(data.rows,function(i,data) {
					$textObject.val(eval("data."+textField));
				}); 
			}
		});
	});
	
	
	$('input[type="hidden"]').bind('loadDataSetSimpleTextHidden',function(event,dataset,valueField,textField,whereCondition) {
		var objectID=this.id;
		var $textObject=$("#"+objectID);
		
		$.ajax({
			url: "astro?FUNCTIONID=JSONDataSet&dataset="+dataset+"&columns="+valueField+","+textField+"&WHERECONDITION="+whereCondition,
			dataType: 'json',
			type: 'GET',
			success: function (data) {
				$.each(data.rows,function(i,data) {
					$textObject.val(eval("data."+textField));
				}); 
			}
		});
	});
	
});

function tokenizeOptionSelected (selectFieldId, listId) {

	var lista = ""; 
	
	$('#'+selectFieldId+' :selected').each(function(i, selected){ 
		if (lista != "") {
			lista+=";"; 
		}
		lista+= $(selected).val();
	});
	
	$("#"+listId).val(lista);
	
}

function tokenizeInConditionFromOptionSelected (selectFieldId,quote) {

	var lista = ""; 
	
	$('#'+selectFieldId+' :selected').each(function(i, selected){ 
		if (lista != "") {
			lista+=","; 
		}
		lista+= quote+$(selected).val()+quote;
	});
	
	return lista;
	
}


function initDropzone() {
	
	var myDropzone = Dropzone.forElement("#myDropzone");
		
	var listaFile=$("#LISTA_ALLEGATI").val();
	if (listaFile !="") {
		var myStringArray = listaFile.split(";");
		var arrayLength = myStringArray.length;
		for (var i = 0; i < arrayLength; i++) {

			// Create the mock file:
			var mockFile = { name: myStringArray[i] };

			// Call the default addedfile event handler
			myDropzone.emit("addedfile", mockFile);

			// And optionally show the thumbnail of the file:
			if (myStringArray[i].toLowerCase().indexOf(".jpg")!=-1) {
				myDropzone.emit("thumbnail", mockFile, "upload/"+mockFile.name);
			}

			// Make sure that there is no progress bar, etc...
			myDropzone.emit("complete", mockFile);
		}
		
	}

}		

	


/* Traduzione del validator in italiano */

$(document).ready(function () {
    jQuery.extend(jQuery.validator.messages, {
        required: "Campo obbligatorio",
        remote: "Controlla questo campo",
        email: "Inserisci un indirizzo email valido",
        url: "Inserisci un indirizzo web valido",
        date: "Inserisci una data valida",
        dateISO: "Inserisci una data valida (ISO)",
        number: "Inserisci un numero valido",
        digits: "Inserisci solo numeri",
        creditcard: "Inserisci un numero di carta di credito valido",
        equalTo: "Il valore non corrisponde",
        extension: "Inserisci un valore con un&apos;estensione valida",
        maxlength: $.validator.format("Non inserire pi&ugrave; di {0} caratteri"),
        minlength: $.validator.format("Inserisci almeno {0} caratteri"),
        rangelength: $.validator.format("Inserisci un valore compreso tra {0} e {1} caratteri"),
        range: $.validator.format("Inserisci un valore compreso tra {0} e {1}"),
        max: $.validator.format("Inserisci un valore minore o uguale a {0}"),
        min: $.validator.format("Inserisci un valore maggiore o uguale a {0}"),
        nifES: "Inserisci un NIF valido",
        nieES: "Inserisci un NIE valido",
        cifES: "Inserisci un CIF valido",
        currency: "Inserisci una valuta valida"
    });
});

