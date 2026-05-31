package net.projectsrl.wera.importazioni.core;

import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.wera.anagrafiche.db.AntenneDAO;
import net.projectsrl.wera.anagrafiche.db.CondominiDAO;
import net.projectsrl.wera.anagrafiche.db.ContatoriDAO;
import net.projectsrl.wera.anagrafiche.db.UtenzeDAO;
import net.projectsrl.wera.anagrafiche.db.UtenzeDettagliDAO;
import net.projectsrl.wera.importazioni.db.UtentiImportDAO;
import net.projectsrl.wm.utils.Utils;

public abstract class UtilsInserimentiImportAnagraficaUtenze extends FunctionProjectWebApp_base {

	public UtilsInserimentiImportAnagraficaUtenze() {
		super();
	}

	public UtilsInserimentiImportAnagraficaUtenze(ApplicationServices_itf applServices, String functionID,
			String functionName) {
		super(applServices, functionID, functionName);
	}
	
	
	protected static void inserisciNetwork(boolean gateway, String antenna, Integer azienda, Integer idCondominio,
			String dataOggi) throws AppCrash {
		if (!antenna.equals("")) {

			AntenneDAO network = new AntenneDAO();
			network.setAttribute(AntenneDAO.ID_AZIENDA, azienda);
			network.setAttribute(AntenneDAO.ID_CONDOMINIO, idCondominio);
			network.setAttribute(AntenneDAO.ANTENNA, antenna);
			network.setAttribute(AntenneDAO.GATEWAY, gateway);
			network.setAttribute(AntenneDAO.TIPOLOGIA, "");
			network.setAttribute(AntenneDAO.DT_MODULO, dataOggi);
			network.setAttribute(AntenneDAO.ID_UTENTE_INS, 0);
			network.insert();

		}
	}

	protected static void inserisciContatori(String tipologia, String contatore, Integer azienda, Integer idCondominio,
			String dataOggi) throws AppCrash {
		if (!contatore.equals("")) {

			ContatoriDAO contatori = new ContatoriDAO();
			contatori.setAttribute(ContatoriDAO.ID_AZIENDA, azienda);
			contatori.setAttribute(ContatoriDAO.ID_CONDOMINIO, idCondominio);
			contatori.setAttribute(ContatoriDAO.CONTATORE, contatore);
			contatori.setAttribute(ContatoriDAO.ID_TIPOLOGIA, tipologia);
			contatori.setAttribute(ContatoriDAO.DT_MODULO, dataOggi);
			contatori.setAttribute(ContatoriDAO.ID_UTENTE_INS, 0);
			contatori.insert();

		}
	}
	
	
	protected static Integer impostaIdAmministratore(SsbServletRequest req, String nominativoA, String localitaA, String capA,
			String provinciaA, String indirizzoA, String codfiscA, String telefonoA, String mailA, Integer idCondominio,
			Integer azienda, String stabile) throws AppCrash {

		Integer idAmministratore = 0;
		String nome = "";
		String cognome = "";

		if (nominativoA.contains(" ")) {
			nome = nominativoA.substring(0, nominativoA.indexOf(" "));
			cognome = nominativoA.substring(nominativoA.indexOf(" "));
		} else {
			nome = nominativoA;
			cognome = nominativoA;
		}
		if(!nominativoA.trim().isEmpty()){
			DataSet_itf dataSet = null;
			
			try {
				DataSetFactory dsFactory = DataSetFactory.getInstance();
				dsFactory = DataSetFactory.getInstance();
				dataSet = dsFactory.makeDataSet("", "DataSetAmministratori");
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("COMPOSED_WHERE_COND", " ((CODFISC='" + codfiscA.trim() + "') or EMAIL='" + mailA.trim() + "')");
				dataSet.setParam(params);
				dataSet.open();
				if (dataSet.hasMoreElements()) {
					Row_itf dbRow = (Row_itf) dataSet.nextElement();
					idAmministratore = (Integer) dbRow.getField("ID_UTENTE");
				}else {
					idAmministratore = creaNuovoAmministratore(req, nominativoA, localitaA, capA, provinciaA, indirizzoA,
							codfiscA, telefonoA, mailA, idCondominio, azienda, stabile);
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
						//ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
					}
				}
			}
		}
		
		return idAmministratore;
	}
	
	
	
	private static Integer creaNuovoAmministratore(SsbServletRequest req, String nominativo, String localita, String cap,
			String provincia, String indirizzo, String codFisc, String telefono, String mail, Integer idCondominio,
			Integer azienda, String stabile) throws AppCrash {
		Integer idUtente = 0;
		if (!nominativo.equals("")) {
			String nome = "";
			String cognome = "";
			if (nominativo.contains(" ")) {
				nome = nominativo.substring(0, nominativo.indexOf(" "));
				cognome = nominativo.substring(nominativo.indexOf(" "));
			} else {
				nome = nominativo;
				cognome = nominativo;
			}

			String user = (nominativo.replace(" ", "").replace("/", "") + progressivo()).toLowerCase() + "@wera.it";
			user = user.toLowerCase().replace("'", "");

			// String destinatarioMail=(String)
			// req.getSession(false).getAttribute("USER_MAIL");

			UtentiImportDAO utenti = new UtentiImportDAO();
			utenti.setAttribute(UtentiImportDAO.USERNAME, user);
			utenti.setAttribute(UtentiImportDAO.COGNOME, cognome);
			utenti.setAttribute(UtentiImportDAO.NOME, nome);
			utenti.setAttribute(UtentiImportDAO.EMAIL, mail);
			utenti.setAttribute(UtentiImportDAO.PROVINCIA, provincia);
			utenti.setAttribute(UtentiImportDAO.CAP, cap);
			utenti.setAttribute(UtentiImportDAO.CODFISC, codFisc);
			utenti.setAttribute(UtentiImportDAO.TELEFONO, telefono);
			utenti.setAttribute(UtentiImportDAO.INDIRIZZO, indirizzo);
			utenti.setAttribute(UtentiImportDAO.LOCALITA, localita);
			utenti.insert();

			idUtente = (Integer) utenti.getAttribute(UtentiImportDAO.ID_UTENTE);
			// inviaMail(req, templateData, nuovaPassword, destinatarioMail,
			// nominativo,user,stabile,nominativo,telefono);
		}
		return idUtente;
	}

	/*
	 * private void updateApici() throws AppCrash { String update =
	 * "update utenti set nome = replace(nome,'''''',''''), cognome = replace(cognome,'''''','''')"
	 * ; net.projectsrl.wm.utils.WMUtils.executeQuery(update); update =
	 * "update condomini set denominazione = replace(denominazione,'''''',''''), indirizzo = replace(indirizzo,'''''',''''), localita = replace(localita,'''''','''')"
	 * ; net.projectsrl.wm.utils.WMUtils.executeQuery(update); update =
	 * "update utenze set denominazione = replace(denominazione,'''''',''''), locatario = replace(locatario,'''''','''')"
	 * ; net.projectsrl.wm.utils.WMUtils.executeQuery(update); }
	 */

	/*
	 * @SuppressWarnings({ "unchecked" }) private HashMap
	 * inviaMail(SsbServletRequest req,HashMap templateData, String
	 * nuovaPassword, String destinatarioMail, String locatario, String user,
	 * String stabile,String denominazione,String scala,String piano,String
	 * interno,String telefono) throws AppCrash { String elencoDestinatari =
	 * destinatarioMail; String from =
	 * Config.GetInstance().getProperty("mail.from", "noreply@projectsrl.net");
	 * 
	 * 
	 * String footer1="WebCredit WERA"; String footer2="www.wera.club"; String
	 * oggetto = "Nuova password WebCredit WERA"; String corpo=""; try { corpo =
	 * WeraUtils.leggiHtml(_applicationSrv.getRoot()+Config.GetInstance().
	 * getProperty("mail.template_utenze")); } catch (IOException e1) { // TODO
	 * Auto-generated catch block e1.printStackTrace(); }
	 * corpo=corpo.replace("#NOME#", ""); corpo=corpo.replace("#TESTO_1#","");
	 * corpo=corpo.replace("#TESTO_2#","E' stato creato il profilo utente di "
	 * +locatario.toUpperCase()
	 * +" per l'accesso a Webcredit WERA, qui di seguito trovi il riepilogo e le credenziali per l'accesso:"
	 * );
	 * 
	 * 
	 * corpo=corpo.replace("#STABILE#", stabile.toUpperCase().replace("''",
	 * "'")); corpo=corpo.replace("#DENOMINAZIONE#",
	 * denominazione.toUpperCase().replace("''", "'"));
	 * corpo=corpo.replace("#LOCATARIO#", locatario.toUpperCase().replace("''",
	 * "'")); corpo=corpo.replace("#SCALA#", scala.toUpperCase());
	 * corpo=corpo.replace("#PIANO#", piano.toUpperCase());
	 * corpo=corpo.replace("#INTERNO#", interno.toUpperCase());
	 * corpo=corpo.replace("#TELEFONO#", telefono);
	 * corpo=corpo.replace("#USERID#", user); corpo=corpo.replace("#PASSWORD#",
	 * nuovaPassword);
	 * 
	 * corpo=corpo.replace("#FIRMA#", "WebCredit WERA");
	 * corpo=corpo.replace("#FOOTER1#", footer1);
	 * corpo=corpo.replace("#FOOTER2#", footer2);
	 * 
	 * SendSMTPMail sendSMTPMail = new SendSMTPMail();
	 * sendSMTPMail.setFrom(from); sendSMTPMail.setSubject(oggetto);
	 * sendSMTPMail.setBody(corpo); sendSMTPMail.setTo(elencoDestinatari);
	 * sendSMTPMail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"))
	 * ;
	 * 
	 * 
	 * try { MyAuthenticator auth = null; if
	 * (!Config.GetInstance().getProperty("mail.SMTPHost.user", "").equals(""))
	 * { auth = new MyAuthenticator(); } sendSMTPMail.prepareMail(auth, false,
	 * "", "");
	 * 
	 * DeferredMailSender.getInstance().offer(sendSMTPMail);
	 * templateData.put("EMAIL_INVIATA", "OK");
	 * //templateData.put("EMAIL_INVIATA_MESSAGE",
	 * Config.GetInstance().getProperty("Message.email_inviata_ok",NO_MESSAGE));
	 * 
	 * } catch (Throwable e) { templateData.put("EMAIL_INVIATA", "KO");
	 * //templateData.put("EMAIL_INVIATA_MESSAGE",
	 * Config.GetInstance().getProperty("Message.email_inviata_ko",NO_MESSAGE));
	 * new AppCrash(e); } return templateData; }
	 */
	
	
	private static String progressivo() throws AppCrash {
		String progressivo = "01";
		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", "DataSetUltimoProgressivo");
			dataSet.open();

			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				String codiceShortDB = (String) dbRow.getField("ULTIMO");
				int ultimoProgressivoInt = 0;
				ultimoProgressivoInt = Integer.parseInt(codiceShortDB) + 1;
				progressivo = String.format("%02d", ultimoProgressivoInt);
			}
		} catch (AppCrash ac) {
			//ac.logContext(this.getClass().getName(), "");
			throw ac;

		} finally {
			if (dataSet != null) {
				try {
					dataSet.close();
				} catch (AppCrash ac) {
					//ac.logContext(this.getClass().getName(), "");
					throw ac;
				}
			}
		}
		return progressivo;
	}
	
}