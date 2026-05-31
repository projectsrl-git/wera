/* project srl - libreria javascript / jquery */

var unsaved=false;
var mousedownCounter=0;

function resetUnsaved(){
	unsaved=false;
	mousedownCounter=0;
}

function unloadPage() {
    if (unsaved) {
        return "Sono state effettuate modifiche non salvate";
    }
}
window.onbeforeunload = unloadPage;    	

$(function(){

	$('body').on('mousedown', 'a, input, select, .form-control', function(){
		mousedownCounter+=1;
		if (mousedownCounter==1) unsaved=false;
	});
	
	$('body').on('mousedown', '.signature-pad', function(){
		mousedownCounter+=1;
		unsaved=true;
	});

	$('body').on('change', 'input, select, select2, textarea', function(){
		unsaved=true;
	});

});


