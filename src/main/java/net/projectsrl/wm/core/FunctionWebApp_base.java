
package net.projectsrl.wm.core;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;

import org.apache.fop.apps.FOPException;

import it.project.webapp.core.Costanti_itf;
import it.project.webapp.core.MenuItem;
import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.Function_base;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.gui.PageFactory;
import net.project.servlet.gui.Page_itf;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.core.PDFCreator;
import net.projectsrl.db.PjDAO_base;
import net.projectsrl.mail.MyAuthenticator;
import net.projectsrl.wm.mail.SendSMTPMail;
import net.projectsrl.wm.mail.DeferredMailSender;
import net.projectsrl.wm.utils.Utils;

public abstract class FunctionWebApp_base extends Function_base implements Costanti_itf {

	public static final String WEB_INF = "WEB-INF";
	public static final String DB_PROPERTIES_FILENAME = "db_connection.properties";
	private static final String DATASET_FORMAZIONEPROFESSIONALE = "DataSetEsperienzeProfessionali";
	private static final String DATASET_CURRICUL_TAGGANCIO = "DataSetCurriculTaggancio";
	private static final String DATASET_CURRICUL_TAGGANCIO_AUE = "DataSetCurriculTaggancioAUE";

	@SuppressWarnings("unused")
	private String _pageTitle = "";

	public FunctionWebApp_base() {

		super();
	}

	public FunctionWebApp_base(ApplicationServices_itf applServices, String functionID, String functionName) {

		super(applServices, functionID, functionName);
	}

	protected boolean checkField(SsbServletRequest req) {

		if (req.getSession(false) != null) {
			Utils.logOperation(getSessionUser(req),
					getSessionRole(req) + " - " + req.getSession(false).getAttribute("USER_COGNOME") + " "
							+ req.getSession(false).getAttribute("USER_NOME"),
					getFunctionID() + " " + getName(), Utils.getRequestParameters(req));
		}

		return true;
	}

	protected String getPageTitle() {
		return readPageTitle();
	}

	@Override
	protected void authenticationFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

	}

	@Override
	protected void checkFieldAutFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

	}

	@Override
	protected void checkFieldFailed(SsbServletRequest req, SsbServletResponse res) throws AppCrash {

	}

	public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

		_applicationSrv.displayPage("ERRORE", setCommonTags(req, userInfo), res);

	}

	/**
	 * Verifica la necessità di permessi per accedere alla funzione. Di default
	 * restituisce false percio', se non ridefinito, fa si che l'accesso alla
	 * funzione non sia libero
	 * 
	 * @return true se non sono necessari permessi, false altrimenti.
	 */
	public boolean isAccessFree() {

		return true;
	}

	/**
	 * Destroy della sessione
	 * 
	 * @param req
	 *            SsbServletRequest Request.
	 */
	protected void destroySession(SsbServletRequest req) {

		if (req.getSession(false) != null) {

			Utils.logOperation(getSessionUser(req),
					getSessionRole(req) + " - " + req.getSession(false).getAttribute("USER_COGNOME") + " "
							+ req.getSession(false).getAttribute("USER_NOME"),
					"Chiusura sessione di lavoro", Utils.getRequestParameters(req));
		}
		req.getSession().invalidate();
	}

	protected boolean checkSession(SsbServletRequest req) {

		HttpSession session = req.getSession(false);
		if (session == null) {
			return false;
		} else {
			UserSecurityInfo userInfo = (UserSecurityInfo) session.getAttribute("LOGIN");
			if (userInfo == null) {
				return false;
			} else if (userInfo.getUserId().equals("")) {
				return false;
			}
		}

		Config.GetInstance().setProperty("DB.ConnectionURL", getSessionDbUrl(req));

		return true;
	}

	/**
	 * Verifica la necessità di effettuare l'autenticazione. Trattandosi della
	 * funzione di login, tramite la quale ci si autentica, ovviamente non
	 * richiede autenticazione.
	 * 
	 * @return false, perche' questa funzione non richiede autenticazione.
	 */
	public boolean isAuthenticationRequired() {

		return false;
	}

	/**
	 * Setta i tag di freemarker comuni alle Function figlie di questa _base
	 * 
	 * @param SsbServletRequest
	 *            req
	 * @param UserSecurityInfo
	 *            userInfo
	 * 
	 * @return java.util.Hashtable
	 */
	protected HashMap<String, Object> setCommonTags(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {

		try {
			HashMap<String, Object> templateData = new HashMap<String, Object>();

			String treeGridSerialCode = Config.GetInstance().getProperty("TreeGrid.SerialCode", "");

			if (treeGridSerialCode.equals("")) {
				templateData.put("PAGE_NAME", _functionName.toLowerCase());
			} else {
				templateData.put("PAGE_NAME", _functionName.toLowerCase() + "' Code='" + treeGridSerialCode);
			}

			String tagIncludedFrame = Config.GetInstance().getProperty("Page.DefaultMenuName");
			templateData.put("INCLUDED_FRAME", tagIncludedFrame);

			String tagIncludedFrameRicerche = Config.GetInstance().getProperty("Page.DefaultMenuNameRicerche");
			templateData.put("INCLUDED_FRAME_RICERCHE", tagIncludedFrameRicerche);

			String tagIncludedFrameGantt = Config.GetInstance().getProperty("Page.DefaultMenuNameGantt");
			templateData.put("INCLUDED_FRAME_GANTT", tagIncludedFrameGantt);

			String tagIncludedFrameTablet = Config.GetInstance().getProperty("Page.DefaultMenuTabletName");
			templateData.put("INCLUDED_FRAME_TABLET", tagIncludedFrameTablet);

			String tagIncludedFrameDialog = Config.GetInstance().getProperty("Page.DefaultMenuNameDialog");
			templateData.put("INCLUDED_FRAME_DIALOG", tagIncludedFrameDialog);

			String tagIncludedFrameDialogRicerche = Config.GetInstance()
					.getProperty("Page.DefaultMenuNameDialogRicerche");
			templateData.put("INCLUDED_FRAME_DIALOG_RICERCHE", tagIncludedFrameDialogRicerche);

			String tagIncludedFrameNoMenu = Config.GetInstance().getProperty("Page.DefaultMenuNameNoMenu");
			templateData.put("INCLUDED_FRAME_NOMENU", tagIncludedFrameNoMenu);

			String tagIncludedFrameMetro = Config.GetInstance().getProperty("Page.DefaultMenuNameMetro");
			templateData.put("INCLUDED_FRAME_METRO", tagIncludedFrameMetro);

			String tagIncludedFrameMetroMenu = Config.GetInstance().getProperty("Page.DefaultMenuNameMetroMenu");
			templateData.put("INCLUDED_FRAME_METRO_MENU", tagIncludedFrameMetroMenu);

			String tagIncludedFrameMetroRicerche = Config.GetInstance()
					.getProperty("Page.DefaultMenuNameMetroRicerche");
			templateData.put("INCLUDED_FRAME_METRO_RICERCHE", tagIncludedFrameMetroRicerche);

			String tagIncludedFrameMetroTabs = Config.GetInstance().getProperty("Page.DefaultMenuNameMetroTabs");
			templateData.put("INCLUDED_FRAME_METRO_TABS", tagIncludedFrameMetroTabs);

			String tagIncludedMetroFullCalendar = Config.GetInstance()
					.getProperty("Page.DefaultMenuNameMetroFullCalendar");
			templateData.put("INCLUDED_FRAME_METRO_FULLCALENDAR", tagIncludedMetroFullCalendar);

			String tagIncludedFrameMetroPwd = Config.GetInstance().getProperty("Page.DefaultMenuNameMetroPwd");
			templateData.put("INCLUDED_FRAME_METRO_PWD", tagIncludedFrameMetroPwd);

			HttpSession session = req.getSession(false);

			String logo = "";
			String logo_in_elenchi = "";
			String societa = "";
			String pageTitle = "";

			if (session != null) {
				logo = (String) session.getAttribute("LOGO");
				if (logo!=null && logo.equals("")) {
					logo = "logo_standard";
				}
				logo_in_elenchi = (String) session.getAttribute("LOGO_IN_ELENCHI");

				societa = (String) session.getAttribute("NOME_SOCIETA");
				if (societa == null) {
					societa = "";
				}

				pageTitle = (String) session.getAttribute("PAGE_TITLE");
				if (pageTitle == null) {
					pageTitle = "";
				}
			}
			templateData.put("LOGO", logo);
			templateData.put("LOGO_IN_ELENCHI", logo_in_elenchi);
			templateData.put("NOME_SOCIETA", societa);
			templateData.put("PAGE_TITLE", pageTitle);

			templateData.put("BLOCCO", "NO");

			// controllo pressione dei tasti senza aver salvato
			//
			templateData.put("CONTROLLO_SALVATAGGIO", "SI");

			// gestione blocco
			//
			String sessionId = req.getRequestedSessionId();
			String sessioneModificato = Config.GetInstance().getProperty("SESSIONE_IN_MODIFICA", "");

			if (sessioneModificato.equals(sessionId)) {
				Config.GetInstance().setProperty("SESSIONE_IN_MODIFICA", "");
			}

			getMessages(req, templateData);

			loadDataFromSession(session, templateData);

			return templateData;

		} catch (Throwable t) {
			AppCrash ap = new AppCrash(t);
			throw ap;
		}
	}

	protected void loadDataFromSession(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

		loadMenu(session, templateData);
	}

	@SuppressWarnings("unchecked")
	private void loadMenu(HttpSession session, HashMap<String, Object> templateData) throws AppCrash {

		String user = getSessionUser(session);
		String ruolo = getSessionRole(session);

		String subMenu = getFunctionID();
		String subMenuTerzoLivello = subMenu;
		if (subMenuTerzoLivello.length() == 3) {
			subMenuTerzoLivello = subMenuTerzoLivello.substring(0, 2);
		}

		templateData.put("USER", user);
		templateData.put("SUBMENU", subMenu);
		templateData.put("SUBMENU_TERZOLIVELLO", subMenuTerzoLivello);
		templateData.put("SUBMENU_BREVE", subMenu);
		templateData.put("RUOLO_SESSIONE", ruolo);
		templateData.put("USER_COGNOME", ((String) session.getAttribute("USER_COGNOME")));
		templateData.put("USER_NOME", ((String) session.getAttribute("USER_NOME")));
		templateData.put("ID_CODICE", ((String) session.getAttribute("ID_CODICE")));

		if (user.equals("")) {
			user = null;
			return;
		}

		ArrayList<MenuItem> vociMenu = (ArrayList<MenuItem>) session.getAttribute("voci_menu");
		if (vociMenu != null) {
			templateData.put("voci_menu", vociMenu);
		}

		ArrayList<MenuItem> dettVociMenu = (ArrayList<MenuItem>) session.getAttribute("dett_voci_menu");
		if (dettVociMenu != null) {
			templateData.put("dett_voci_menu", dettVociMenu);
		}

		ArrayList<MenuItem> dettSubVociMenu = (ArrayList<MenuItem>) session.getAttribute("dett_sub_voci_menu");
		if (dettSubVociMenu != null) {
			templateData.put("dett_sub_voci_menu", dettSubVociMenu);
		}

	}

	private void getMessages(SsbServletRequest req, HashMap<String, Object> templateData) throws AppCrash {

		String messageCode = req.getField("MESSAGECODE").trim();
		String messageString = req.getField("MESSAGE").trim();

		if (!messageCode.equals("")) {
			messageString = Config.GetInstance().getProperty("Messages." + _functionName + "." + messageCode, "");
		}

		if (messageString.equals("")) {
			return;
		}

		templateData.put("MESSAGE", messageString);
	}

	@SuppressWarnings("unchecked")
	protected HashMap<String, Object> setTemplateDataFromRequest(HashMap<String, Object> templateData,
			SsbServletRequest req) {

		// legge tutti i parametri della request
		Enumeration param = req.getParameterNames();

		while (param.hasMoreElements()) {
			String name = (String) param.nextElement();
			String value = req.getField(name);
			if (value.contains("\"")) {
				value = value.replaceAll("\"", "&quot;");
			}
			templateData.put(name, value);
		}

		return templateData;
	}

	@SuppressWarnings("unchecked")
	protected void putRequestDataInSession(SsbServletRequest req) {

		Enumeration param = req.getParameterNames(); // Collezione di
		// parametri

		Hashtable<String, String> requestData = new Hashtable<String, String>();
		while (param.hasMoreElements()) {
			String name = (String) param.nextElement();
			String value = req.getField(name);
			requestData.put(name, value);
		}

		HttpSession sessione = req.getSession(false);
		if (sessione != null) {
			sessione.setAttribute(PREVIOUS_REQUEST_HASH, requestData);
		}

	}

	@SuppressWarnings("unchecked")
	protected String concatPreviousRequestDataInString(SsbServletRequest req) {

		Hashtable requestData = new Hashtable();
		String stringRequestData = "";

		HttpSession sessione = req.getSession(false);
		if (sessione != null) {
			requestData = (Hashtable) sessione.getAttribute(PREVIOUS_REQUEST_HASH);
		}

		if (requestData != null) {
			stringRequestData = concatHashDataInRequestString(requestData);
		}

		return stringRequestData;
	}

	@SuppressWarnings("unchecked")
	protected String concatHashDataInRequestString(Hashtable requestData) {

		String stringRequestData = "";

		Enumeration param = requestData.keys();

		while (param.hasMoreElements()) {
			String name = (String) param.nextElement();
			String value = (String) requestData.get(name);

			if (name != null && !name.equals("") && value != null) {
				stringRequestData += "&" + name.trim() + "=" + value.trim();
			}

		}

		return stringRequestData;
	}

	private String[] getFormazioneProfessionale(String idDipendente) throws AppCrash {
		String[] dati = { "", "", "" };
		String obbligoLegge = "";
		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", DATASET_FORMAZIONEPROFESSIONALE);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("DAGGANCIO", idDipendente);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				obbligoLegge = dbRow.getField("OBBLIGO_LEGGE").toString().trim();
				if (obbligoLegge.equals("S")) {
					dati[0] = dati[0] + dbRow.getField("TITOLO_ATTIVITA").toString().trim();
					dati[1] = dbRow.getField("FORMAZIONE_VALIDA").toString().trim();
					try {
						String strDate1 = dati[1].substring(6) + "/" + dati[1].substring(3, 5) + "/"
								+ dati[1].substring(0, 2) + " 00:00:00";
						String strDate2 = Utils.getStringDataOggiRibaltata() + " 00:00:00";
						SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						fmt.setLenient(false);
						Date d1 = fmt.parse(strDate1);
						Date d2 = fmt.parse(strDate2);
						long millisDiff = d2.getTime() - d1.getTime();
						int days = (int) (millisDiff / 86400000);
						if (days < 0 && Math.abs(days) < 30) {
							dati[2] = dati[2] + "; Attestazione formazione "
									+ dbRow.getField("TITOLO_ATTIVITA").toString().trim() + " in scadenza fra "
									+ Math.abs(days) + " giorni ("
									+ dbRow.getField("FORMAZIONE_VALIDA").toString().trim() + ")";
						}
						if (days >= 0) {
							dati[2] = dati[2] + "; Attestazione formazione "
									+ dbRow.getField("TITOLO_ATTIVITA").toString().trim() + " scaduta da "
									+ Math.abs(days) + " giorni ("
									+ dbRow.getField("FORMAZIONE_VALIDA").toString().trim() + ")";
						}
					} catch (Exception e) {
						System.err.println(e);
					}

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
					ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
				}
			}
		}
		return dati;
	}

	private String[] getDatiExtracomunitari(String idDipendente) throws AppCrash {
		String[] dati = { "", "", "" };
		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", DATASET_CURRICUL_TAGGANCIO_AUE);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("TAGGANCIO", idDipendente);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				dati[0] = dbRow.getField("TIPO_AUTORIZZ").toString().trim();
				dati[1] = dbRow.getField("DATA_SCADENZA").toString().trim();
				try {
					String strDate1 = dati[1].substring(6) + "/" + dati[1].substring(3, 5) + "/"
							+ dati[1].substring(0, 2) + " 00:00:00";
					String strDate2 = Utils.getStringDataOggiRibaltata() + " 00:00:00";
					SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					fmt.setLenient(false);
					Date d1 = fmt.parse(strDate1);
					Date d2 = fmt.parse(strDate2);
					long millisDiff = d2.getTime() - d1.getTime();
					int days = (int) (millisDiff / 86400000);
					if (days < 0 && Math.abs(days) < 30) {
						dati[2] = dati[2] + "Extracomunitari: " + dbRow.getField("DESCRI").toString().trim()
								+ " in scadenza fra " + Math.abs(days) + " giorni ("
								+ dbRow.getField("DATA_SCADENZA").toString().trim() + ")";
					}
					if (days >= 0) {
						dati[2] = dati[2] + "Extracomunitari: " + dbRow.getField("TIPO_AUTORIZZ").toString().trim()
								+ " scaduta da " + Math.abs(days) + " giorni ("
								+ dbRow.getField("DATA_SCADENZA").toString().trim() + ")";
					}
				} catch (Exception e) {
					System.err.println(e);
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
					ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
				}
			}
		}
		return dati;
	}

	private String[] getDatiPatenti(String idDipendente) throws AppCrash {
		String[] dati = { "", "", "", "", "", "", "", "", "", "" };
		String strDate1 = "";
		String strDate2 = "";
		String strDate3 = "";
		String strDate4 = "";
		String strDate5 = "";
		String strDate6 = "";
		String strDate7 = "";
		String strDate8 = "";
		String strDate9 = "";
		DataSet_itf dataSet = null;
		try {
			DataSetFactory dsFactory = DataSetFactory.getInstance();
			dsFactory = DataSetFactory.getInstance();
			dataSet = dsFactory.makeDataSet("", DATASET_CURRICUL_TAGGANCIO);
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("TAGGANCIO", idDipendente);
			dataSet.setParam(params);
			dataSet.open();
			while (dataSet.hasMoreElements()) {
				Row_itf dbRow = (Row_itf) dataSet.nextElement();
				dati[0] = dati[0] + dbRow.getField("PATENTE_A1_S").toString().trim();
				dati[1] = dbRow.getField("PATENTE_A_S").toString().trim();
				dati[2] = dbRow.getField("PATENTE_B_S").toString().trim();
				dati[3] = dbRow.getField("PATENTE_C_S").toString().trim();
				dati[4] = dbRow.getField("PATENTE_D_S").toString().trim();
				dati[5] = dbRow.getField("PATENTE_BE_S").toString().trim();
				dati[6] = dbRow.getField("PATENTE_CE_S").toString().trim();
				dati[7] = dbRow.getField("PATENTE_DE_S").toString().trim();
				dati[8] = dbRow.getField("PATENTE_MULETTO_S").toString().trim();
				String strDateOggi = Utils.getStringDataOggiRibaltata() + " 00:00:00";
				SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				fmt.setLenient(false);
				Date dOggi = fmt.parse(strDateOggi);
				try {
					if (!dati[0].equals("")) {
						strDate1 = dati[0].substring(6) + "/" + dati[0].substring(3, 5) + "/" + dati[0].substring(0, 2)
								+ " 00:00:00";
						Date d1 = fmt.parse(strDate1);
						long millisDiff1 = dOggi.getTime() - d1.getTime();
						int days1 = (int) (millisDiff1 / 86400000);
						if (days1 < 0 && Math.abs(days1) < 30) {
							dati[9] = dati[9] + "; Patente A1 in scadenza fra " + Math.abs(days1) + " giorni ("
									+ dbRow.getField("PATENTE_A1_S").toString().trim() + ")";
						}
						if (days1 >= 0) {
							dati[9] = dati[9] + "; Patente A1 scaduta da " + Math.abs(days1) + " giorni ("
									+ dbRow.getField("PATENTE_A1_S").toString().trim() + ")";
						}
					}
					if (!dati[1].equals("")) {
						strDate2 = dati[1].substring(6) + "/" + dati[1].substring(3, 5) + "/" + dati[1].substring(0, 2)
								+ " 00:00:00";
						Date d2 = fmt.parse(strDate2);
						long millisDiff2 = dOggi.getTime() - d2.getTime();
						int days2 = (int) (millisDiff2 / 86400000);
						if (days2 < 0 && Math.abs(days2) < 30) {
							dati[9] = dati[9] + "; Patente A in scadenza fra " + Math.abs(days2) + " giorni ("
									+ dbRow.getField("PATENTE_A_S").toString().trim() + ")";
						}
						if (days2 >= 0) {
							dati[9] = dati[9] + "; Patente A scaduta da " + Math.abs(days2) + " giorni ("
									+ dbRow.getField("PATENTE_A_S").toString().trim() + ")";
						}
					}
					if (!dati[2].equals("")) {
						strDate3 = dati[2].substring(6) + "/" + dati[2].substring(3, 5) + "/" + dati[2].substring(0, 2)
								+ " 00:00:00";
						Date d3 = fmt.parse(strDate3);
						long millisDiff3 = dOggi.getTime() - d3.getTime();
						int days3 = (int) (millisDiff3 / 86400000);
						if (days3 < 0 && Math.abs(days3) < 30) {
							dati[9] = dati[9] + "; Patente B in scadenza fra " + Math.abs(days3) + " giorni ("
									+ dbRow.getField("PATENTE_B_S").toString().trim() + ")";
						}
						if (days3 >= 0) {
							dati[9] = dati[9] + "; Patente B scaduta da " + Math.abs(days3) + " giorni ("
									+ dbRow.getField("PATENTE_B_S").toString().trim() + ")";
						}
					}
					if (!dati[3].equals("")) {
						strDate4 = dati[3].substring(6) + "/" + dati[3].substring(3, 5) + "/" + dati[3].substring(0, 2)
								+ " 00:00:00";
						Date d4 = fmt.parse(strDate4);
						long millisDiff4 = dOggi.getTime() - d4.getTime();
						int days4 = (int) (millisDiff4 / 86400000);
						if (days4 < 0 && Math.abs(days4) < 30) {
							dati[9] = dati[9] + "; Patente C in scadenza fra " + Math.abs(days4) + " giorni ("
									+ dbRow.getField("PATENTE_C_S").toString().trim() + ")";
						}
						if (days4 >= 0) {
							dati[9] = dati[9] + "; Patente C scaduta da " + Math.abs(days4) + " giorni ("
									+ dbRow.getField("PATENTE_C_S").toString().trim() + ")";
						}
					}
					if (!dati[4].equals("")) {
						strDate5 = dati[4].substring(6) + "/" + dati[4].substring(3, 5) + "/" + dati[4].substring(0, 2)
								+ " 00:00:00";
						Date d5 = fmt.parse(strDate5);
						long millisDiff5 = dOggi.getTime() - d5.getTime();
						int days5 = (int) (millisDiff5 / 86400000);
						if (days5 < 0 && Math.abs(days5) < 30) {
							dati[9] = dati[9] + "; Patente D in scadenza fra " + Math.abs(days5) + " giorni ("
									+ dbRow.getField("PATENTE_D_S").toString().trim() + ")";
						}
						if (days5 >= 0) {
							dati[9] = dati[9] + "; Patente D scaduta da " + Math.abs(days5) + " giorni ("
									+ dbRow.getField("PATENTE_D_S").toString().trim() + ")";
						}
					}
					if (!dati[5].equals("")) {
						strDate6 = dati[5].substring(6) + "/" + dati[5].substring(3, 5) + "/" + dati[5].substring(0, 2)
								+ " 00:00:00";
						Date d6 = fmt.parse(strDate6);
						long millisDiff6 = dOggi.getTime() - d6.getTime();
						int days6 = (int) (millisDiff6 / 86400000);
						if (days6 < 0 && Math.abs(days6) < 30) {
							dati[9] = dati[9] + "; Patente BE in scadenza fra " + Math.abs(days6) + " giorni ("
									+ dbRow.getField("PATENTE_BE_S").toString().trim() + ")";
						}
						if (days6 >= 0) {
							dati[9] = dati[9] + "; Patente BE scaduta da " + Math.abs(days6) + " giorni ("
									+ dbRow.getField("PATENTE_BE_S").toString().trim() + ")";
						}
					}
					if (!dati[6].equals("")) {
						strDate7 = dati[6].substring(6) + "/" + dati[6].substring(3, 5) + "/" + dati[6].substring(0, 2)
								+ " 00:00:00";
						Date d7 = fmt.parse(strDate7);
						long millisDiff7 = dOggi.getTime() - d7.getTime();
						int days7 = (int) (millisDiff7 / 86400000);
						if (days7 < 0 && Math.abs(days7) < 30) {
							dati[9] = dati[9] + "; Patente CE in scadenza fra " + Math.abs(days7) + " giorni ("
									+ dbRow.getField("PATENTE_CE_S").toString().trim() + ")";
						}
						if (days7 >= 0) {
							dati[9] = dati[9] + "; Patente CE scaduta da " + Math.abs(days7) + " giorni ("
									+ dbRow.getField("PATENTE_CE_S").toString().trim() + ")";
						}
					}
					if (!dati[7].equals("")) {
						strDate8 = dati[7].substring(6) + "/" + dati[7].substring(3, 5) + "/" + dati[7].substring(0, 2)
								+ " 00:00:00";
						Date d8 = fmt.parse(strDate8);
						long millisDiff8 = dOggi.getTime() - d8.getTime();
						int days8 = (int) (millisDiff8 / 86400000);
						if (days8 < 0 && Math.abs(days8) < 30) {
							dati[9] = dati[9] + "; Patente DE in scadenza fra " + Math.abs(days8) + " giorni ("
									+ dbRow.getField("PATENTE_DE_S").toString().trim() + ")";
						}
						if (days8 >= 0) {
							dati[9] = dati[9] + "; Patente DE scaduta da " + Math.abs(days8) + " giorni ("
									+ dbRow.getField("PATENTE_DE_S").toString().trim() + ")";
						}
					}
					if (!dati[8].equals("")) {
						strDate9 = dati[8].substring(6) + "/" + dati[8].substring(3, 5) + "/" + dati[8].substring(0, 2)
								+ " 00:00:00";
						Date d9 = fmt.parse(strDate9);
						long millisDiff9 = dOggi.getTime() - d9.getTime();
						int days9 = (int) (millisDiff9 / 86400000);
						if (days9 < 0 && Math.abs(days9) < 30) {
							dati[9] = dati[9] + "; Patentino muletto in scadenza fra " + Math.abs(days9) + " giorni ("
									+ dbRow.getField("PATENTE_MULETTO_S").toString().trim() + ")";
						}
						if (days9 >= 0) {
							dati[9] = dati[9] + "; Patentino muletto scaduto da " + Math.abs(days9) + " giorni ("
									+ dbRow.getField("PATENTE_MULETTO_S").toString().trim() + ")";
						}
					}

				} catch (Exception e) {
					System.err.println(e);
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
					ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
				}
			}
		}
		return dati;
	}

	@SuppressWarnings("unchecked")
	protected HashMap[] setPageDatasetParam(String page, SsbServletRequest req, HashMap templateData) {

		HashMap<String, String> queryParameter = new HashMap<String, String>();
		queryParameter.put("AZIENDA_SESSIONE", (String) req.getSession(false).getAttribute("AZIENDA_SESSIONE"));

		// alert scandenze nel cv
		String from = Config.GetInstance().getProperty("mail.from", "noreply@projectsrl.net");
		String oggetto = "";
		String destinatari_mail = "";
		String nominativo = "";
		String aziendaMail = "";
		try {
			templateData.put("CHECK_LINE0", "");
			if (!getFormazioneProfessionale(req.getField("TAGGANCIO"))[2].equals("")) {
				try {
					templateData.put("CHECK_LINE0",
							getFormazioneProfessionale(req.getField("TAGGANCIO"))[2].replaceFirst("; ", ""));
					if (!templateData.get("CHECK_LINE0").equals("")) {
						templateData = inviaMail(req, templateData, from, oggetto, destinatari_mail, nominativo,
								aziendaMail);
					}
				} catch (Exception e) {
					System.err.println(e);
				}
			}
		} catch (AppCrash e1) {
			e1.printStackTrace();
		}

		try {
			templateData.put("CHECK_LINE1", "");
			if (!getDatiPatenti(req.getField("TAGGANCIO"))[9].equals("")) {
				try {
					templateData.put("CHECK_LINE1",
							getDatiPatenti(req.getField("TAGGANCIO"))[9].replaceFirst("; ", ""));
				} catch (Exception e) {
					System.err.println(e);
				}
			}
		} catch (AppCrash e1) {
			e1.printStackTrace();
		}

		try {
			templateData.put("CHECK_LINE2", "");
			if (!getDatiExtracomunitari(req.getField("TAGGANCIO"))[2].equals("")) {
				try {
					templateData.put("CHECK_LINE2",
							getDatiExtracomunitari(req.getField("TAGGANCIO"))[2].replaceFirst("; ", ""));
				} catch (Exception e) {
					System.err.println(e);
				}
			}
		} catch (AppCrash e1) {
			e1.printStackTrace();
		}

		String tipologia = req.getField("TIPOLOGIA");
		String azienda = req.getField("AZIENDA");
		if (tipologia.equals("D")) {
			azienda = req.getField("AZIENDA_D");
			if (azienda.equals("") || azienda.equals(null)) {
				azienda = (String) templateData.get("AZIENDA_D");
			}
		}
		if (tipologia.equals("V")) {
			azienda = req.getField("AZIENDA_V");
			if (azienda.equals("") || azienda.equals(null)) {
				azienda = (String) templateData.get("AZIENDA_V");
			}
		}

		if (azienda.equals("")) {
			if (templateData.get("AZIENDA_TENDINA") == null) {
				if (req.getField("AZIENDA_R") == null || req.getField("AZIENDA_R").equals("")) {
					queryParameter.put("AZIENDA_TENDINA", "");
				} else {
					queryParameter.put("AZIENDA_TENDINA", req.getField("AZIENDA_R"));
				}
			} else {
				queryParameter.put("AZIENDA_TENDINA", (String) templateData.get("AZIENDA_TENDINA"));
			}
		} else {
			queryParameter.put("AZIENDA_TENDINA", azienda);
		}

		HttpSession session = req.getSession(true);
		if (azienda.equals("") || azienda.equals(null)) {
			if (templateData.get("AZIENDA") == null) {
				templateData.put("AZIENDA", (String) session.getAttribute("AZIENDA_SESSIONE"));
			}
			if (templateData.get("AZIENDA").equals("")) {
				templateData.put("AZIENDA", (String) session.getAttribute("AZIENDA_SESSIONE"));
			}
		} else {
			templateData.put("AZIENDA", azienda);
		}

		String risorsaSessione = (String) session.getAttribute("ID_DIPENDENTE_SESSIONE");
		String risorsaTendina = req.getField("ID_DIPENDENTE_SESSIONE");
		String risorsaTendina2 = req.getField("DIPENDENTE"); // per pagina
																// inserimento
																// trasferte
		String risorsaEsatta = "";

		String anno = req.getField("ANNO");
		if (anno == null || anno.equals("")) {
			anno = Utils.getAnnoOggi();
		}
		String mese = req.getField("MESE");
		if (mese == null || mese.equals("")) {
			mese = Utils.getMeseOggi();
		}
		String giorno = req.getField("GIORNO");
		if (giorno == null || giorno.equals("")) {
			giorno = "";
		}
		queryParameter.put("ANNO_SELEZIONATO", anno);
		queryParameter.put("MESE_SELEZIONATO", mese);
		queryParameter.put("GIORNO_SELEZIONATO", giorno);

		if (azienda.equals("")) {
			queryParameter.put("AZIENDA", (String) session.getAttribute("AZIENDA_SESSIONE"));
		}
		if (!risorsaTendina.equals("")) {
			queryParameter.put("DIPENDENTE_SELEZIONATO", risorsaTendina);
			risorsaEsatta = risorsaTendina;
		} else {
			if (!risorsaTendina2.equals("")) {
				queryParameter.put("DIPENDENTE_SELEZIONATO", risorsaTendina2);
				risorsaEsatta = risorsaTendina2;
			} else {
				queryParameter.put("DIPENDENTE_SELEZIONATO", risorsaSessione);
				risorsaEsatta = risorsaSessione;
			}
		}

		if (!risorsaTendina.equals("")) {
			queryParameter.put("DIPENDENTE_SELEZIONATO_APPROVAZIONI", risorsaTendina);
		} else {
			queryParameter.put("DIPENDENTE_SELEZIONATO_APPROVAZIONI", "");
		}

		if (!risorsaTendina2.equals("")) {
			queryParameter.put("DIPENDENTE_SELEZIONATO_RICHIESTE", risorsaTendina2);
		} else {
			queryParameter.put("DIPENDENTE_SELEZIONATO_RICHIESTE", "");
		}

		if (!azienda.equals("")) {
			queryParameter.put("AZIENDA_SELEZIONATA_APPROVATORI", azienda);
		} else {
			queryParameter.put("AZIENDA_SELEZIONATA_APPROVATORI", (String) session.getAttribute("AZIENDA_SESSIONE"));
		}

		String ateco = req.getField("SETTORE_TITOLO");
		if (!ateco.equals("")) {
			queryParameter.put("ATECO_TITOLO", ateco);
		} else {
			queryParameter.put("ATECO_TITOLO", "");
		}

		String codiceAnagraficaAzienda = req.getField("CODICE");
		if (!codiceAnagraficaAzienda.equals("")) {
			queryParameter.put("COD_AZIENDA", codiceAnagraficaAzienda);
		} else {
			queryParameter.put("COD_AZIENDA", "");
		}

		queryParameter.put("DIPENDENTE_ASSOCIAZIONI", req.getField("ID_DIPENDENTE"));
		if (req.getField("ID_DIPENDENTE").equals("")) {
			queryParameter.put("DIPENDENTE_ASSOCIAZIONI", (String) templateData.get("ID_DIPENDENTE"));
		}

		queryParameter.put("COMMESSA_ASSOCIAZIONI", req.getField("ID_COMMESSA"));
		if (req.getField("ID_COMMESSA").equals("")) {
			queryParameter.put("COMMESSA_ASSOCIAZIONI", (String) templateData.get("ID_COMMESSA"));
		}

		if (templateData.get("ID_FERIEPERMESSO") == null) {
			queryParameter.put("ID_FERIEPERMESSO", "");
		} else {
			queryParameter.put("ID_FERIEPERMESSO", (String) templateData.get("ID_FERIEPERMESSO"));
		}

		queryParameter.put("ID_TRASFERTA", (String) templateData.get("ID_TRASFERTA"));
		queryParameter.put("ID_MALATTIAINFORTUNIO", (String) templateData.get("ID_MALATTIAINFORTUNIO"));
		queryParameter.put("ID_RENDICONTAZIONE", (String) templateData.get("ID_RENDICONTAZIONE"));
		queryParameter.put("RUOLO_MENURAPIDO", (String) session.getAttribute("RUOLO"));
		queryParameter.put("COMMESSA_RICERCA_ATTIVITA", (String) templateData.get("ID_COMMESSA"));
		queryParameter.put("RISORSA_RICERCA_ATTIVITA", risorsaEsatta);
		queryParameter.put("TIPO_PERMESSO_INSERIMENTO", "GST" + (String) templateData.get("TIPO_PERMESSO"));
		queryParameter.put("ID_DOCAZIENDA", (String) templateData.get("ID_DOCAZIENDA"));
		queryParameter.put("ID_GRUPPO", (String) templateData.get("ID_GRUPPO"));

		queryParameter.put("AZIENDA_CV", req.getField("AZIENDA_CV"));

		queryParameter.put("TAGGANCIO", (String) templateData.get("TAGGANCIO"));
		queryParameter.put("DAGGANCIO", (String) templateData.get("TAGGANCIO"));

		if (req.getField("TENDINA").equals("NO")) {
			templateData.put("TIPO_PERMESSO", req.getField("TIPO_PERMESSO"));
		}

		// recupera il numero di dataset presenti nella pagina
		int dsNum = Integer.parseInt(Config.GetInstance().getProperty("Page." + page + ".DSNum"));

		// crea un array di hashtable
		HashMap[] dsArray = new HashMap[dsNum];

		// i parametri sono gli stessi per tutti i dataset
		for (int i = 0; i < dsNum; i++) {

			dsArray[i] = prepareWhereCondition(req, prepareWhereCondition(templateData, queryParameter));
		}

		return dsArray;

	}

	protected HashMap<String, String> prepareWhereCondition(SsbServletRequest req,
			HashMap<String, String> queryParameter) {

		return queryParameter;
	}

	protected HashMap<String, String> prepareWhereCondition(HashMap<String, String> templateData,
			HashMap<String, String> queryParameter) {

		return queryParameter;
	}

	@SuppressWarnings("unchecked")
	protected boolean refresh(String page, SsbServletRequest req, HashMap templateData, SsbServletResponse res)
			throws AppCrash {

		String option = req.getField(Costanti_itf.OPZIONE_INSERIMENTO_MODIFICA);

		if (option != null && (option.equals(Costanti_itf.OPZIONE_REFRESH_INSERIMENTO)
				|| option.equals(Costanti_itf.OPZIONE_REFRESH_MODIFICA))) {

			// SE OPZIONE_INSERIMENTO_MODIFICA è un opzione di REFRESH
			// (REFRESH_INSERIMENTO o REFRESH_MODIFICA)
			// allora faccio REFRESH
			//
			templateData = setTemplateDataFromRequest(templateData, req);
			if (option.equals(Costanti_itf.OPZIONE_REFRESH_INSERIMENTO)) {
				templateData.put(Costanti_itf.OPZIONE_INSERIMENTO_MODIFICA, Costanti_itf.OPZIONE_INSERIMENTO);
			} else if (option.equals(Costanti_itf.OPZIONE_REFRESH_MODIFICA)) {
				templateData.put(Costanti_itf.OPZIONE_INSERIMENTO_MODIFICA, Costanti_itf.OPZIONE_MODIFICA);
			}

			templateData = modificaTemplateDataPerRefresh(templateData, req);

			_applicationSrv.displayPage(page, templateData, setPageDatasetParam(page, req, templateData), res);

			// ritorno TRUE per far sapere alla procedura che mi ha chiamato che
			// HO FATTO REFRESH
			//
			return true;
		} else {

			// altrimenti esco senza far nulla
			// ritorno FALSE per far sapere alla procedura che mi ha chiamato
			// che NON HO FATTO REFRESH
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	protected HashMap modificaTemplateDataPerRefresh(HashMap templateData, SsbServletRequest req) throws AppCrash {

		// Da modificare con i campi che servono
		//
		return templateData;
	}

	/**
	 * Popola il PjDAO_base dao in input con tuti i campi/valori provenienti
	 * dalla SsbServletRequest req (in input) aventi gli stessi nomi dei campi
	 * del dao
	 * 
	 * @param SsbServletRequest
	 *            req
	 * @param PjDAO_base
	 *            dao
	 * @return PjDAO_base dao
	 * @throws AppCrash
	 */
	@SuppressWarnings("unchecked")
	protected PjDAO_base setDAOFieldsFromRequest(SsbServletRequest req, PjDAO_base dao) throws AppCrash {

		// condizioni iniziali sui parametri
		//
		ErrDetector.GetInstance().preCond(req != null, "setDAOFieldsFromRequest - req!=null");
		ErrDetector.GetInstance().preCond(dao != null, "setDAOFieldsFromRequest - dao!=null");

		Iterator daoFields = dao.iterator();

		while (daoFields != null && daoFields.hasNext()) {
			String element = (String) daoFields.next();
			String reqElement = element;
			if (element.startsWith("?")) {
				reqElement = element.substring(2);
			}
			dao.setField(element, req.getField(reqElement));
		}

		return dao;
	}

	/**
	 * Recupera l'azienda dalla sessione
	 * 
	 * @param SsbServletRequest
	 *            req
	 * 
	 * @return String user Azienda
	 */
	protected String getSessionAzienda(SsbServletRequest req) {

		HttpSession session = req.getSession(false);
		String azienda = "";
		if (session != null) {
			azienda = (String) session.getAttribute("NOME_SOCIETA");
		}
		return azienda;
	}

	/**
	 * Recupera dalla sessione
	 * 
	 * @param SsbServletRequest
	 *            req
	 * 
	 * @return String user Azienda
	 */
	protected String getSessionApprovaFPS(SsbServletRequest req) {

		HttpSession session = req.getSession(false);
		String approvaFPS = "";
		if (session != null) {
			approvaFPS = (String) session.getAttribute("APPROVA_FPS_SESSIONE");
		}
		return approvaFPS;
	}

	/**
	 * Recupera dalla sessione
	 * 
	 * @param SsbServletRequest
	 *            req
	 * 
	 * @return String user Azienda
	 */
	protected String getSessionApprovaRendicontazioni(SsbServletRequest req) {

		HttpSession session = req.getSession(false);
		String approvaR = "";
		if (session != null) {
			approvaR = (String) session.getAttribute("APPROVA_RENDICONTAZIONI_SESSIONE");
		}
		return approvaR;
	}

	/**
	 * Recupera il ruolo utente dalla sessione
	 * 
	 * @param SsbServletRequest
	 *            req
	 * 
	 * @return String user ruolo utente
	 */
	protected String getSessionRole(SsbServletRequest req) {

		HttpSession session = req.getSession(false);
		return getSessionRole(session);

	}

	/**
	 * Recupera il ruolo utente dalla sessione
	 * 
	 * @param HttpSession
	 *            session
	 * 
	 * @return String user ruolo utente
	 */
	protected String getSessionRole(HttpSession session) {

		String ruolo = "";

		if (session == null) {
			return ruolo;
		}

		UserSecurityInfo userInfo = (UserSecurityInfo) session.getAttribute("LOGIN");

		if (userInfo != null && userInfo.getRoleId() != null && !(userInfo.getRoleId().equals(""))) {
			ruolo = userInfo.getRoleId();
			return ruolo;
		}

		// compatibilità con vecchia gestione ruolo AstroWeb
		ruolo = (String) session.getAttribute("RUOLO_SESSIONE");
		return ruolo;

	}

	protected String getSessionQuery(SsbServletRequest req) {

		HttpSession session = req.getSession(false);
		String query_ruolo = "";
		if (session != null) {
			query_ruolo = (String) session.getAttribute("QUERY_RUOLO");
		}
		return query_ruolo;
	}

	// FINE LUCA

	/**
	 * Recupera il codice utente dalla sessione
	 * 
	 * @param SsbServletRequest
	 *            req
	 * 
	 * @return String user codice utente
	 */
	protected String getSessionUser(SsbServletRequest req) {

		HttpSession session = req.getSession(false);

		return getSessionUser(session);
	}

	/**
	 * Recupera il codice utente dalla sessione
	 * 
	 * @param HttpSession
	 *            session
	 * 
	 * @return String user codice utente
	 */
	protected String getSessionUser(HttpSession session) {

		String user = "";
		if (session != null) {
			user = (String) session.getAttribute("USER");
		}
		return user;
	}

	/**
	 * Recupera la stringa di connessione dalla sessione
	 * 
	 * @param SsbServletRequest
	 *            req
	 * 
	 * @return String DbUrl stringa di connessione
	 */
	protected String getSessionDbUrl(SsbServletRequest req) {

		HttpSession session = req.getSession(false);
		String dbUrl = "";
		if (session != null) {
			dbUrl = (String) session.getAttribute("DBURL");
		}
		return dbUrl;
	}

	protected String readDBHostName() {

		String applicationPath = _applicationSrv.getRoot() + WEB_INF;
		File file = new File(applicationPath + File.separator + DB_PROPERTIES_FILENAME);

		Properties properties = new Properties();
		String dbHostName = "";
		try {
			properties.load(new FileInputStream(file));
			dbHostName = properties.getProperty("host_name");
		} catch (IOException e) {
			dbHostName = "---proprieta' non trovata---";
		}

		return dbHostName;
	}

	protected String readPageTitle() {

		String applicationPath = _applicationSrv.getRoot() + WEB_INF;
		File file = new File(applicationPath + File.separator + DB_PROPERTIES_FILENAME);

		Properties properties = new Properties();
		String pageTitle = "";
		try {
			properties.load(new FileInputStream(file));
			// pageTitle = properties.getProperty("page_title");
			pageTitle = "Dafne";
		} catch (IOException e) {
			pageTitle = "Dafne";
		}

		return pageTitle;
	}

	protected String readSecondaryDBHostName() {

		String applicationPath = _applicationSrv.getRoot() + WEB_INF;
		File file = new File(applicationPath + File.separator + DB_PROPERTIES_FILENAME);

		Properties properties = new Properties();
		String dbHostName = "";
		try {
			properties.load(new FileInputStream(file));
			dbHostName = properties.getProperty("host_name2");
		} catch (IOException e) {
			dbHostName = "";
		}

		return dbHostName;
	}

	/**
	 * Redirige l'applicazione alla stessa pagina corrente, facendo comparire
	 * però un messaggio <br>
	 * <br>
	 * La sintassi per la codifica del messaggio su file di configurazione è:
	 * <br>
	 * <br>
	 * Messages.<Function name come da functionList.txt>.<codice del
	 * messaggio>=<testo del messaggio in formato HTML> <br>
	 * <br>
	 * esempio: Messages.LoginInterno.1=Login failure: unknown user or password
	 * incorrect <br>
	 * <br>
	 * Attenzione! per far comparire nella pagina richiamata il testo del
	 * messaggio deve essere presente<br>
	 * nel metodo "mostra" un richiamo alla setCommonTags per popolare la
	 * struttura della Map che definisce<br>
	 * i dati della Page_itf costruita con freemarker <br>
	 * <br>
	 * esempio: nella mostra ci sarà un richiamo della displayPage di questo
	 * tipo<br>
	 * <br>
	 * _applicationSrv.displayPage(PAGE, setCommonTags(req, userInfo), res);
	 * <br>
	 * 
	 * @param SsbServletResponse
	 *            res
	 * @param String
	 *            messageCode Codice del messaggio che si vuol far comparire
	 * @throws AppCrash
	 */
	protected void redirectToThisPageWithMessage(SsbServletResponse res, String messageCode) throws AppCrash {

		try {
			res.sendRedirect("astro?FUNCTIONID=" + this._functionName + "&MESSAGECODE=" + messageCode);
		} catch (IOException e) {
			throw new AppCrash(e);
		}

	}

	protected void redirectToLogoutPage(SsbServletResponse res, String messageCode) throws AppCrash {

		try {
			res.sendRedirect("astro?FUNCTIONID=Logout");
		} catch (IOException e) {
			throw new AppCrash(e);
		}

	}

	protected void redirectToLogoutPageExpiredUser(SsbServletResponse res, String messageCode) throws AppCrash {

		try {
			res.sendRedirect("astro?FUNCTIONID=Logout");
		} catch (IOException e) {
			throw new AppCrash(e);
		}

	}

	protected void redirectToThisPageExpiredUser(SsbServletResponse res, String messageCode) throws AppCrash {

		try {
			res.sendRedirect("astro?FUNCTIONID=LoginInterno&MESSAGECODE=NonAttivo");
		} catch (IOException e) {
			throw new AppCrash(e);
		}

	}

	@SuppressWarnings("unchecked")
	protected String salvaFormatoPDF(String fileName, String page, HashMap templateData, Map[] dataSourceParam,
			String xmlName, String xslName) throws AppCrash {

		if (fileName.contains("Modulitrasferte")) {
			String allegato = templateData.get("AZIENDA_TENDINA") + "/" + templateData.get("DIPENDENTE") + "/"
					+ templateData.get("ID_MODTRASFERTA") + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
			String queryUpdate = "UPDATE MOD_TRASFERTE SET ALLEGATO='" + allegato + "', FILENAME='"
					+ Utils.normalizeASCIIFilename(fileName) + "' WHERE ID_MODTRASFERTA='"
					+ templateData.get("ID_MODTRASFERTA") + "'";
			net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
			fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.moduli.trasferte")
					+ allegato;
		}

		if (fileName.contains("Rendicontazione")) {
			String allegato = templateData.get("AZIENDA_TENDINA") + "/" + templateData.get("DIPENDENTE") + "/"
					+ templateData.get("ID_RENDICONTAZIONE") + "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
			String queryUpdate = "UPDATE RENDICONTAZIONI SET ALLEGATO='" + allegato + "', FILENAME='"
					+ Utils.normalizeASCIIFilename(fileName) + "' WHERE ID_RENDICONTAZIONE='"
					+ templateData.get("ID_RENDICONTAZIONE") + "'";
			net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
			fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("cartella.moduli.rendicontazioni")
					+ allegato;

		}

		if (fileName.contains("InfortunioINAIL")) {
			fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
					+ "/" + templateData.get("AZIENDA_TENDINA") + "/Infortuni_INAIL/"
					+ Utils.normalizeASCIIFilename(fileName) + ".pdf";

			String queryUpdate = "UPDATE INFORTUNIO_INAIL SET ALLEGATO='" + "areadocumenti/"
					+ templateData.get("AZIENDA_TENDINA") + "/Infortuni_INAIL/InfortunioINAIL_"
					+ templateData.get("TAGGANCIO") + ".pdf" + "', FILENAME='InfortunioINAIL_"
					+ templateData.get("TAGGANCIO") + ".pdf' WHERE TAGGANCIO='" + templateData.get("TAGGANCIO") + "'";
			net.projectsrl.wm.utils.WMUtils.executeQuery(queryUpdate);
		}

		if (!fileName.contains("Modulitrasferte") && !fileName.contains("Rendicontazione")
				&& !fileName.contains("InfortunioINAIL")) {
			fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
					+ "/" + Utils.normalizeASCIIFilename(fileName) + ".pdf";
		}

		File baseDir = new File(_applicationSrv.getRoot());
		File xsltfile = new File(baseDir, "xsl/" + xslName);
		File xmltfile = new File(baseDir, "WEB-INF/template/" + xmlName);

		PageFactory pf = PageFactory.getInstance();

		Page_itf template = pf.makePage(page);

		template.setPageRootData(templateData);
		for (int i = 0; i < dataSourceParam.length; i++) {
			if (dataSourceParam[i] != null) {
				template.setDataSourceParam(dataSourceParam[i], i);
			}
		}

		writeByteArrayOutputStreamToPDFFile(fileName, template, xmltfile, xsltfile);
		return fileName;
	}

	private void writeByteArrayOutputStreamToPDFFile(String fileName, Page_itf template, File xmlFile, File xslFileName)
			throws AppCrash {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		String xmlFileName = xmlFile.getName() + ".tmp";
		// Creazione file XML
		try {
			PrintWriter pw = new PrintWriter(baos);
			template.display(pw);
			pw.flush();

			FileOutputStream fos = new FileOutputStream(xmlFileName, false);
			OutputStreamWriter wrtout = new OutputStreamWriter(fos);

			String s = baos.toString("UTF-8");

			wrtout.write(s);

			wrtout.flush();
			wrtout.close();

		} catch (Throwable e) {
			throw new AppCrash(e);
		}

		File pdfFile = new File(fileName);

		// Creazione file PDF
		try {
			PDFCreator.convertXML2PDF(xmlFileName, xslFileName, pdfFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FOPException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}

	protected void readPDFFile(String fileName, String setHeaderElenco, String functionName, SsbServletResponse res)
			throws AppCrash {

		res.setContentType("application/donwload");
		if (setHeaderElenco.contains("Modulitrasferte")) {
			res.setHeader("Content-Disposition", "attachment; filename=\"" + setHeaderElenco + ".pdf\"");
		} else {
			res.setHeader("Content-Disposition",
					"attachment; filename=\"" + setHeaderElenco + "_" + Utils.getUnique() + ".pdf\"");
		}

		InputStream ist = null;

		String text = "";
		try {
			ist = new FileInputStream(_applicationSrv.getRoot() + fileName);

			byte[] buffer = new byte[1024];
			int length;

			ServletOutputStream op = res.getOutputStream();
			while ((ist != null) && ((length = ist.read(buffer)) != -1)) {
				op.write(buffer, 0, length);
			}
			ist.close();

		} catch (Throwable e) {

			new AppCrash(e).logContext(functionName, "CRASH " + text);

		} finally {

			if (ist != null) {
				try {
					ist.close();
				} catch (Throwable th) {
					new AppCrash(_functionName + " errore nella chiusura di InputStream ist");
				}
			}

		}
	}

	protected void readPDFFileTimesheet(String fileName, String setHeaderElenco, String functionName,
			SsbServletResponse res, String anno, String mese, String risorsa) throws AppCrash {

		res.setContentType("application/donwload");
		res.setHeader("Content-Disposition",
				"attachment; filename=\"" + setHeaderElenco + "_" + risorsa + "_" + mese + "_" + anno + ".pdf\"");

		InputStream ist = null;

		String text = "";
		try {
			ist = new FileInputStream(_applicationSrv.getRoot() + fileName);

			byte[] buffer = new byte[1024];
			int length;

			ServletOutputStream op = res.getOutputStream();
			while ((ist != null) && ((length = ist.read(buffer)) != -1)) {
				op.write(buffer, 0, length);
			}
			ist.close();

		} catch (Throwable e) {

			new AppCrash(e).logContext(functionName, "CRASH " + text);

		} finally {

			if (ist != null) {
				try {
					ist.close();
				} catch (Throwable th) {
					new AppCrash(_functionName + " errore nella chiusura di InputStream ist");
				}
			}

		}
	}

	protected String salvaFormatoWord(String fileName, String page, HashMap templateData, Map[] dataSourceParam)
			throws AppCrash {

		fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
				+ "/" + Utils.normalizeASCIIFilename(fileName) + ".doc";

		PageFactory pf = PageFactory.getInstance();

		Page_itf template = pf.makePage(page);

		template.setPageRootData(templateData);
		for (int i = 0; i < dataSourceParam.length; i++) {
			if (dataSourceParam[i] != null) {
				template.setDataSourceParam(dataSourceParam[i], i);
			}
		}

		writeByteArrayOutputStreamToWordFile(fileName, template);
		return fileName;
	}

	protected String salvaFormatoWord(String fileName, String page, HashMap templateData) throws AppCrash {
		fileName = _applicationSrv.getRoot() + Config.GetInstance().getProperty("Page." + page + ".storePath", "./")
				+ "/" + Utils.normalizeASCIIFilename(fileName) + ".doc";

		PageFactory pf = PageFactory.getInstance();

		Page_itf template = pf.makePage(page);

		template.setPageRootData(templateData);

		writeByteArrayOutputStreamToWordFile(fileName, template);

		return fileName;
	}

	private void writeByteArrayOutputStreamToWordFile(String fileName, Page_itf template) throws AppCrash {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			PrintWriter pw = new PrintWriter(baos);
			template.display(pw);
			pw.flush();

			FileOutputStream fos = new FileOutputStream(fileName, false);
			OutputStreamWriter wrtout = new OutputStreamWriter(fos);

			String s = baos.toString("UTF-8");

			wrtout.write(s);

			wrtout.flush();
			wrtout.close();

		} catch (Throwable e) {
			throw new AppCrash(e);
		}

	}

	@SuppressWarnings("unchecked")
	private HashMap inviaMail(SsbServletRequest req, HashMap templateData, String from, String oggetto,
			String destinatari_mail, String nominativo, String aziendaMail) throws AppCrash {
		String portale = Config.GetInstance().getProperty("indirizzo.portale");
		String elencoDestinatari = destinatari_mail;

		String corpo = "Gentile " + nominativo + ",\n/nQuesta è una mail inviata automaticamente da DAFNE.\n/n";
		corpo += "E' stato creato il suo profilo utente per l'accesso a <a href='" + portale + "'>" + portale
				+ "</a>, qui di seguito trova le credenziali per l'accesso al portale:\n" + "/n";
		corpo += "<i>Userid</i>: " + destinatari_mail + "\n/n";
		corpo += "\n/nAcceda al sistema utilizzando la nuova password. Potrà modificarla con una nuova password utilizzando l'apposita pagina di Cambio Password.\n\n/n/nCordiali Saluti\n/n<i>Il Team DAFNE</i>";

		System.out.println(elencoDestinatari);
		SendSMTPMail sendSMTPMail = new SendSMTPMail();
		sendSMTPMail.setFrom(from);
		sendSMTPMail.setSubject(oggetto);
		sendSMTPMail.setBody(corpo);
		sendSMTPMail.setTo(elencoDestinatari);
		sendSMTPMail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"));

		try {
			MyAuthenticator auth = null;
			if (!Config.GetInstance().getProperty("mail.SMTPHost.user", "").equals("")) {
				auth = new MyAuthenticator();
			}
			sendSMTPMail.prepareMail(auth, false, "", "", "S");

			DeferredMailSender.getInstance().offer(sendSMTPMail);
			templateData.put("EMAIL_INVIATA", "OK");
			templateData.put("EMAIL_INVIATA_MESSAGE",
					Config.GetInstance().getProperty("Message.email_inviata_ok", NO_MESSAGE));

		} catch (Throwable e) {
			templateData.put("EMAIL_INVIATA", "KO");
			templateData.put("EMAIL_INVIATA_MESSAGE",
					Config.GetInstance().getProperty("Message.email_inviata_ko", NO_MESSAGE));
			new AppCrash(e);
		}

		return templateData;
	}

	protected String leggiHtml() throws IOException {

		String everything = "";
		String template = _applicationSrv.getRoot() + Config.GetInstance().getProperty("mail.template");
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

}
