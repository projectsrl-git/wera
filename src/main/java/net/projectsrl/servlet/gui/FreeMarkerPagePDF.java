package net.projectsrl.servlet.gui;

import net.project.errors.AppCrash;
import net.project.servlet.gui.FreeMarkerPage;

public class FreeMarkerPagePDF extends FreeMarkerPage {
	
	
    public FreeMarkerPagePDF() {
		super();
	}

	public FreeMarkerPagePDF(String name, String cfName) throws AppCrash {
		super(name, cfName);
	}

	public String convertValue(String value) {
		
		
		return converteCaratteriSpeciali(value);
	}
	
    /**
     * Converte le stringhe con caratteri speciali
     * 
     * @param String stringaConCaratteriSpeciali stringa da convertite
     * @return String stringa convertita
     */
    private String converteCaratteriSpeciali(String stringaConCaratteriSpeciali) {

        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("à", "a'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("é", "e'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("è", "e'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("ò", "o'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("ù", "u'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("ì", "i'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("&", "&amp;");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("°", "'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("À", "A'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("Á", "A'");
        stringaConCaratteriSpeciali = stringaConCaratteriSpeciali.replace("€", "&euro;");

        return stringaConCaratteriSpeciali;
    }	

}
