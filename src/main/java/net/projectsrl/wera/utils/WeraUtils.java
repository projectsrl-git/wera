
package net.projectsrl.wera.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.projectsrl.bow.parameters.ParametriDAO;
import net.projectsrl.wm.utils.RandomPasswordGenerator;
import net.projectsrl.wm.utils.Utils;

public class WeraUtils {

	//private static Random _randomSource = new Random();

	private static final String DATASET_CREDITI = "DataSetAziendeSessione";
	private static final String DATASET_CREDITI_UTILIZZATI = "DataSetConteggioRilevatori";
	
	private static final String DATASET_SALVATAGGIO_RIPARTIZIONE= "DataSetSalvataggioRipartizione";

	// PARTE NUOVA per wera 2018

	public static String creaPassword() throws AppCrash {

		int noOfCAPSAlpha = 1;
		int noOfDigits = 1;
		int noOfSplChars = 0;
		int minLen = 6;
		int maxLen = 7;

		String new_pwd = "";
		char[] pswd = RandomPasswordGenerator.generatePswd(minLen, maxLen, noOfCAPSAlpha, noOfDigits, noOfSplChars);
		new_pwd = new String(pswd);
		if (new_pwd.length() < 7) {
			new_pwd = new_pwd + Utils.getUnique().substring(14, 18);
		} else {
			new_pwd = new_pwd + Utils.getUnique().substring(14, 17);
		}
		return new_pwd;
	}

	public static Integer contaCrediti(String whereConditionAziende) throws AppCrash {

		Integer crediti = 0;

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DSAziende");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("WHERECONDITION", "WHERE 1=1");
			params.put("WHERECONDITION_AZIENDE", whereConditionAziende);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				crediti = (Integer) dbRow.getField("CREDITI");
			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {
					// ac.logContext(this.getClass().getName(), "Errore nella
					// close del dataset");
				}
			}
		}
		return crediti;
	}

	public static Integer contaCreditiUtilizzati(String whereConditionAziende) throws AppCrash {

		Integer contaCrediti = 0;

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", DATASET_CREDITI_UTILIZZATI);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("WHERECONDITION_AZIENDE", whereConditionAziende);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				contaCrediti = (Integer) dbRow.getField("CREDITI_UTILIZZATI");
			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {
					// ac.logContext(this.getClass().getName(), "Errore nella
					// close del dataset");
				}
			}
		}
		return contaCrediti;
	}

	public static Boolean validitaCrediti(String whereConditionAziende) throws AppCrash {

		Boolean validi = false;

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", DATASET_CREDITI);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("WHERECONDITION_AZIENDE", whereConditionAziende);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				validi = (Boolean) dbRow.getField("CREDITI_VALIDI");
			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {
					// ac.logContext(this.getClass().getName(), "Errore nella
					// close del dataset");
				}
			}
		}
		return validi;
	}

	public static String dataScadenzaCrediti(String whereConditionAziende) throws AppCrash {

		String dataScadenzaCrediti = "";

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", DATASET_CREDITI);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("WHERECONDITION_AZIENDE", whereConditionAziende);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				dataScadenzaCrediti = (String) dbRow.getField("DATA_SCADENZA_CREDITI");
			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {
					// ac.logContext(this.getClass().getName(), "Errore nella
					// close del dataset");
				}
			}
		}
		return dataScadenzaCrediti;
	}

	public static String leggiHtml(String template) throws IOException {

		String everything = "";
		BufferedReader br = new BufferedReader(new FileReader(template));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
				System.out.println(line);
			}
			everything = sb.toString();
			System.out.println(everything);
		} finally {
			br.close();
		}
		return everything;
	}

	public static String contaDescriviDoppi(String tabella, String campo, String valore, String join) throws AppCrash {
		String descrizione = "";
		String codice = "";
		String denominazione = "";
		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DSContaDoppi");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("NOMETABELLA", tabella);
			params.put("NOMECAMPO", campo);
			params.put("VALORE", valore);
			params.put("JOIN", join);
			dataSet.setParam(params);
			dataSet.open();
			if (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				Integer conteggio = (Integer) dbRow.getField("DOPPI");
				if (conteggio > 0) {
					codice = (String) dbRow.getField("CODICE");
					denominazione = (String) dbRow.getField("DENOMINAZIONE");
					descrizione = codice + " gi&#224; presente in " + denominazione;
				}

			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {
				}
			}
		}
		return descrizione;
	}

	public static Integer trovaIdAziendaUtente(String idUtente) throws AppCrash {

		Integer idAzienda = 0;

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DSAziendaUtente");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("ID_UTENTE", idUtente);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				idAzienda = (Integer) dbRow.getField("ID_AZIENDA");
			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {
					// ac.logContext(this.getClass().getName(), "Errore nella
					// close del dataset");
				}
			}
		}
		return idAzienda;
	}

	public static String getOrarioCompleto() {

		SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss", java.util.Locale.ITALY);
		return sdf.format(new Date());
	}

	public static Integer trovaIdCondominio(String identnr) throws AppCrash {

		Integer idCondominio = 0;

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DataSetRecuperaIdScaricoRilevatoriNetworkContatori");

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("IDENTNR", identnr);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				idCondominio = (Integer) dbRow.getField("ID_CONDOMINIO");
			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {

				}
			}
		}
		return idCondominio;
	}

	public static String trovaDatiCondominio(String identnr) throws AppCrash {

		String datiCondominio = "";

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();

			dataSet = dsFactory.makeDataSet("", "DataSetRecuperaIdScaricoRilevatoriNetworkContatori");

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("IDENTNR", identnr);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				datiCondominio = (String) dbRow.getField("DENOMINAZIONE") + " - " + (String) dbRow.getField("INDIRIZZO")
						+ " - " + (String) dbRow.getField("LOCALITA") + " (" + (String) dbRow.getField("PROVINCIA")
						+ ")";
			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {

				}
			}
		}
		return datiCondominio;
	}

	public static String doubleToString(double valore, int precisione) throws AppCrash {

		DecimalFormat df0 = new DecimalFormat("#");
		DecimalFormat df2 = new DecimalFormat("#.##");
		DecimalFormat df3 = new DecimalFormat("#.###");
		DecimalFormat df4 = new DecimalFormat("#.####");
		String valoreStringa = "";

		switch (precisione) {
		case 0:
			valoreStringa = df0.format(valore).replace(",", ".");
			break;
		case 2:
			valoreStringa = df2.format(valore).replace(",", ".");
			break;
		case 3:
			valoreStringa = df3.format(valore).replace(",", ".");
			break;
		case 4:
			valoreStringa = df4.format(valore).replace(",", ".");
			break;
		default:
			valoreStringa = df2.format(valore).replace(",", ".");
			break;
		}

		return valoreStringa;
	}

	public static Float doDifferenza(Float numero1, Float numero2) {

		Float risultato;
		if (numero1 < numero2) {
			risultato = numero1;
		} else {
			risultato = numero1 - numero2;
		}
		return risultato;
	}

	public static boolean verificaFileDoppio(String nomeFile) throws AppCrash {

		boolean esisteFile = false;

		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DataSetFileCaricati");
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("NOME_FILE", nomeFile);
			dataSet.setParam(params);
			dataSet.open();
			if (dataSet.hasMoreElements()) {
				esisteFile = true;
			}
			dataSet.close();
		} catch (Throwable t) {
			AppCrash ac = new AppCrash(t);
			throw ac;
		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {
					// ac.logContext(this.getClass().getName(), "Errore nella
					// close del dataset");
				}
			}
		}
		return esisteFile;
	}

	/**
	 * Ribalta la data in input da formato gg/mm/AAAA in formato AAAA/mm/gg.
	 * 
	 * @param String
	 *            data
	 * 
	 * @return String dataRibaltata
	 */
	public static String normalizzaData(String data) {

		if (data == null || data.equals("") || data.length() != 10) {
			return "";
		}

		String dataNormalizzata = "";
		String anno = "";
		String mese = "";
		String giorno = "";

		if (data.indexOf("/") == 4) {
			anno = data.substring(0, 4);
			mese = data.substring(5, 7);
			giorno = data.substring(8, 10);
		} else {
			return data;
		}

		dataNormalizzata = giorno + "/" + mese + "/" + anno;

		return dataNormalizzata;
	}

	public static String getStringDataOggiScaricoACS() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
		return sdf.format(new Date());
	}

	public static String getStringDataDomaniScaricoACS() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ITALY);
		Date data = new Date();
		data.setHours(data.getHours() + 24);
		return sdf.format(data);

	}

	public static String getOrarioMinutiAggiunti() {

		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", java.util.Locale.ITALY);
		Date data = new Date();
		data.setHours(data.getHours() - 1);
		data.setMinutes(data.getMinutes() + 2);
		return sdf.format(data);
	}

	public static String notNumericToNumeric(String str) {
		// match a number with optional '-' and decimal.
		if (!str.matches("-?\\d+(\\.\\d+)?")) {
			str = "0";
		}
		return str;
	}

	public static String emptyToZero(String str) {
		// match a number with optional '-' and decimal.
		if (str.isEmpty()) {
			str = "0";
		}
		return str;
	}

	public static String formattaData(String data) {

		if (data == null || data.equals("")) {
			return "";
		}

		String dataNormalizzata = "";
		String anno = "";
		String mese = "";
		String giorno = "";

		if (data.length() > 10) {
			data = data.substring(0, 10);
		}

		if (data.length() == 10) {
			data = data.replace(".", "/");
		}

		if (data.indexOf("/") == 4) {
			data = project.misc.Utils.ribaltaDataItalia(data);
		}

		return data;
	}

	public static void eliminaDatiRilevatoriDuplicati() {
		String sql="DELETE FROM dati_rilevatori a USING dati_rilevatori b WHERE a.id_modulo < b.id_modulo AND a.rilevatore||coalesce(a.lettura,0)||coalesce(a.lettura_acs,0)::char(20)||a.mese||a.anno = b.rilevatore||coalesce(b.lettura,0)||coalesce(b.lettura_acs,0)::char(20)||b.mese||b.anno";
    	try {
			net.projectsrl.wm.utils.WMUtils.executeQuery(sql);
		} catch (AppCrash e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  	
    
	}
	
	
	public static String setRipartizioneSalvataggio(String tabella, boolean stato, Integer idModulo) {
		
		String sql="UPDATE "+tabella+" SET SALVATAGGIO_IN_CORSO="+stato+" WHERE ID_MODULO="+idModulo;
    	try {
			net.projectsrl.wm.utils.WMUtils.executeQuery(sql);
		} catch (AppCrash e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  	
    
    	String messaggio = "";
    	if (stato){
    		messaggio = "Salvataggio dati in corso";
    	}
    	
		return messaggio;
	}

	
	 // Funzione per creare un codice base di 3 caratteri dalla descrizione
    public static String creaCodiceBase(String descrizione) {
        String codice = descrizione.trim().toUpperCase().replaceAll("[^A-Z]", "");
        if (codice.length() >= 3) {
            return codice.substring(0, 3);
        } else {
            // se meno di 3 caratteri, riempi con 'X'
            return String.format("%-3s", codice).replace(' ', 'X');
        }
    }

    // Funzione per trovare un codice disponibile partendo dal codice base
    public static String trovaCodiceDisponibile(String codiceBase, String dominio) throws AppCrash {
        // Normalizza il codice base
        codiceBase = codiceBase.toUpperCase().replaceAll("[^A-Z]", "");
        if (codiceBase.length() < 3) {
            codiceBase = String.format("%-3s", codiceBase).replace(' ', 'X');  // es. "CA"  "CAX"
        } else {
            codiceBase = codiceBase.substring(0, 3);
        }

        // 1. Verifica se il codice base è già disponibile
        ParametriDAO checkBase = new ParametriDAO();
        checkBase.setAttribute(ParametriDAO.DOMINIO, dominio);
        checkBase.setAttribute(ParametriDAO.CODICE, codiceBase);
        if (!checkBase.retrieve()) {
            return codiceBase;
        }

        // 2. Prova suffissi numerici (CA0  CA9)
        for (int i = 0; i < 10; i++) {
            String variante = codiceBase.substring(0, 2) + i;

            ParametriDAO checker = new ParametriDAO();  // NUOVA ISTANZA
            checker.setAttribute(ParametriDAO.DOMINIO, dominio);
            checker.setAttribute(ParametriDAO.CODICE, variante);

            System.out.println("Provo codice: " + variante);  // log utile per debug

            if (!checker.retrieve()) {
                return variante;
            }
        }
        // 3. Prova suffissi alfabetici (CAA  CAZ)
        String lettere = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < lettere.length(); i++) {
            String variante = codiceBase.substring(0, 2) + lettere.charAt(i);

            ParametriDAO checker = new ParametriDAO();  // NUOVA ISTANZA
            checker.setAttribute(ParametriDAO.DOMINIO, dominio);
            checker.setAttribute(ParametriDAO.CODICE, variante);

            System.out.println("Provo codice: " + variante);  // log utile per debug

            if (!checker.retrieve()) {
                return variante;
            }
        }

        throw new RuntimeException("Impossibile trovare un codice unico di 3 caratteri");
    }


    
    
    
    public static String pulisciFattoreEnergia(String fattoreEnergia) {
        if (fattoreEnergia == null || fattoreEnergia.isEmpty()) {
            return "";
        }

        // 1. Prendiamo solo la parte prima del simbolo |
        // Usiamo limit 2 per fermarci alla prima occorrenza
        String parteIniziale = fattoreEnergia.split("\\|")[0].trim();

        if (parteIniziale.isEmpty()) {
            return "";
        }

        // 2. Controlliamo se è un numero intero (anche con zeri iniziali)
        // Usiamo il regex ^\d+$ che significa "solo cifre da inizio a fine"
        if (parteIniziale.matches("^\\d+$")) {
            // Se è numerico, lo trasformiamo in Integer per togliere gli zeri e poi di nuovo in String
            return String.valueOf(Integer.parseInt(parteIniziale));
        }

        // 3. Se non è puramente numerico (es. "0D"), la restituiamo così com'è
        return parteIniziale;
    }
}
