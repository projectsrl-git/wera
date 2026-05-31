package net.projectsrl.wera.importazioni.core;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import net.project.errors.AppCrash;

public class UtilsValidatoreExcel {

	private static Map<String, UtilsCampoDB> schemaDB = new HashMap<>();

	static {
		// Inizializzazione statica della struttura attesa del DB
		schemaDB.put("n_telefono", new UtilsCampoDB("n_telefono", "STRING", 15));
		schemaDB.put("locatario", new UtilsCampoDB("locatario", "STRING", 100));
		schemaDB.put("scala", new UtilsCampoDB("scala", "STRING", 10));
		schemaDB.put("piano", new UtilsCampoDB("piano", "STRING", 3));
		schemaDB.put("interno", new UtilsCampoDB("interno", "STRING", 10));
		schemaDB.put("millesimi", new UtilsCampoDB("millesimi", "DECIMAL", 10, 3));
		// ecc...
		schemaDB.put("millesimi", new UtilsCampoDB("millesimi", "STRING", 10));
		schemaDB.put("stanza", new UtilsCampoDB("stanza", "STRING", 50));
		schemaDB.put("tipo", new UtilsCampoDB("tipo", "STRING", 10));
		schemaDB.put("marca", new UtilsCampoDB("marca", "STRING", 10));
		schemaDB.put("larghezza", new UtilsCampoDB("larghezza", "DECIMAL", 12, 2));
		schemaDB.put("altezza", new UtilsCampoDB("altezza", "DECIMAL", 12, 2));
		schemaDB.put("profondita", new UtilsCampoDB("profondita", "DECIMAL", 12, 2));
		schemaDB.put("elementi", new UtilsCampoDB("elementi", "STRING", 5));
		schemaDB.put("coeff", new UtilsCampoDB("coeff", "INTEGER"));
		schemaDB.put("potenza", new UtilsCampoDB("potenza", "DECIMAL", 12, 2));
		schemaDB.put("esp1", new UtilsCampoDB("esp1", "DECIMAL", 12, 2));
		schemaDB.put("esp2", new UtilsCampoDB("esp2", "DECIMAL", 12, 2));
		schemaDB.put("rilevatore", new UtilsCampoDB("rilevatore", "STRING", 10));
		schemaDB.put("n_prog", new UtilsCampoDB("n_prog", "INTEGER"));
		schemaDB.put("tipo_valvola", new UtilsCampoDB("tipo_valvola", "STRING", 3));
		schemaDB.put("pos", new UtilsCampoDB("pos", "STRING", 3));
		schemaDB.put("diametro", new UtilsCampoDB("diametro", "STRING", 5));
		schemaDB.put("mat_tubo", new UtilsCampoDB("mat_tubo", "STRING", 3));
		schemaDB.put("prereg", new UtilsCampoDB("prereg", "STRING", 3));

	}

	public static UtilsCampoDB getCampo(String nome) {
		return schemaDB.get(nome);
	}

	public static UtilsErroreValidazione validaCampo(String valore, String nomeCampo, int riga, int colonna, String nomeFoglio) {
	    UtilsCampoDB campo = schemaDB.get(nomeCampo);
	    if (campo == null) {
	        return new UtilsErroreValidazione(nomeFoglio,nomeCampo,
	            "Campo non definito nello schemaDB",
	            valore, null, null, riga, colonna);
	    }

	    if (valore == null)
	        valore = "";
	    
	    if (valore.trim().isEmpty()) {
	        return null;
	    }

	    // Lunghezza (per STRING o INTEGER)
	    if ((campo.tipo.equals("STRING") || campo.tipo.equals("INTEGER")) && campo.maxLength > 0) {
	        if (valore.length() > campo.maxLength) {
	            return new UtilsErroreValidazione(nomeFoglio, nomeCampo,
	                "Lunghezza superiore al massimo consentito",
	                valore,
	                campo.tipo + "(" + campo.maxLength + ")",
	                "lunghezza " + valore.length(),
	                riga, colonna);
	        }
	    }

	    // Tipo
	    switch (campo.tipo) {
	        case "INTEGER":
	            try {
	                Integer.parseInt(valore);
	            } catch (NumberFormatException e) {
	                return new UtilsErroreValidazione(nomeFoglio, nomeCampo,
	                    "Valore non è un intero valido",
	                    valore,
	                    "INTEGER",
	                    "non intero",
	                    riga, colonna);
	            }
	            break;

	        case "DECIMAL":
	            try {
	                BigDecimal valoreDecimal = new BigDecimal(valore);

	                int actualPrecision = valoreDecimal.precision(); // tot cifre
	                int actualScale = valoreDecimal.scale();         // cifre decimali

	                if (actualPrecision > campo.precisione) {
	                    return new UtilsErroreValidazione(nomeFoglio, nomeCampo,
	                        "Precisione superiore a quella prevista",
	                        valore,
	                        "DECIMAL(" + campo.precisione + "," + campo.scala + ")",
	                        "precisione " + actualPrecision,
	                        riga, colonna);
	                }

	                if (actualScale > campo.scala) {
	                    return new UtilsErroreValidazione(nomeFoglio, nomeCampo,
	                        "Scala decimale superiore a quella prevista",
	                        valore,
	                        "DECIMAL(" + campo.precisione + "," + campo.scala + ")",
	                        "scala " + actualScale,
	                        riga, colonna);
	                }

	            } catch (NumberFormatException e) {
	                return new UtilsErroreValidazione(nomeFoglio, nomeCampo,
	                    "Valore non è un numero decimale valido",
	                    valore,
	                    "DECIMAL",
	                    "non decimale",
	                    riga, colonna);
	            }
	            break;

	        case "STRING":
	        default:
	            // Nessun controllo oltre la lunghezza
	            break;
	    }

	    // Nessun errore
	    return null;
	}

	
	
	
}
