package net.projectsrl.wera.importazioni.core;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.wera.anagrafiche.db.CondominiDAO;
import net.projectsrl.wera.anagrafiche.db.UtenzeDAO;
import net.projectsrl.wera.anagrafiche.db.UtenzeDettagliDAO;
import net.projectsrl.wm.utils.Utils;

public abstract class UtilsInserimentiImportAnagraficaUtenzeXLS extends FunctionProjectWebApp_base {

	public UtilsInserimentiImportAnagraficaUtenzeXLS() {
		super();
	}

	public UtilsInserimentiImportAnagraficaUtenzeXLS(ApplicationServices_itf applServices, String functionID,
			String functionName) {
		super(applServices, functionID, functionName);
	}
	
	
	protected static void inserimentoUtenze(Integer azienda, HSSFWorkbook workbook, String dataOggi, Integer idCondominio)
			throws AppCrash {
		for (int i = 1; i <= workbook.getNumberOfSheets() - 1; i++) {

			HSSFSheet worksheet = workbook.getSheet("Foglio " + i);

			HSSFRow row2d = worksheet.getRow(6);
			HSSFCell cellG6 = row2d.getCell(6);
			cellG6.setCellType(CellType.STRING);
			String telefono = cellG6.getStringCellValue();

			HSSFRow row3 = worksheet.getRow(8);
			HSSFCell cellA3 = row3.getCell(1);
			String locatario = cellA3.getStringCellValue();
			locatario = locatario.replace("’", "''");
			HSSFCell cellB3 = row3.getCell(6);
			cellB3.setCellType(CellType.STRING);
			String scala = cellB3.getStringCellValue();
			HSSFCell cellC3 = row3.getCell(8);
			cellC3.setCellType(CellType.STRING);
			String piano = cellC3.getStringCellValue();
			HSSFCell cellD3 = row3.getCell(10);
			cellD3.setCellType(CellType.STRING);
			String interno = cellD3.getStringCellValue();
			HSSFCell cellE3 = row3.getCell(12);
			cellE3.setCellType(CellType.STRING);
			String millesimi = cellE3.getStringCellValue();

			if (millesimi.equals("")) {
				millesimi = "0";
			}

			Integer idModulo = 0;
			if (!locatario.trim().equals("")) {

				UtenzeDAO utenze = new UtenzeDAO();
				utenze.setAttribute(UtenzeDAO.ID_AZIENDA, azienda);
				utenze.setAttribute(UtenzeDAO.ID_CONDOMINIO, idCondominio);
				utenze.setAttribute(UtenzeDAO.DENOMINAZIONE, locatario.replace("'", "''"));
				utenze.setAttribute(UtenzeDAO.LOCATARIO, locatario.replace("'", "''"));
				utenze.setAttribute(UtenzeDAO.SCALA, scala);
				utenze.setAttribute(UtenzeDAO.PIANO, piano);
				utenze.setAttribute(UtenzeDAO.INTERNO, interno);
				utenze.setAttribute(UtenzeDAO.N_TELEFONO, telefono);
				utenze.setAttribute(UtenzeDAO.MILLESIMI, millesimi);
				utenze.setAttribute(UtenzeDAO.MILLESIMI_ACS, "0");
				utenze.setAttribute(UtenzeDAO.MILLESIMI_CLIMA, millesimi);
				utenze.setAttribute(UtenzeDAO.DT_MODULO, dataOggi);
				utenze.setAttribute(UtenzeDAO.ID_UTENTE_INS, 0);
				utenze.insert();

				idModulo = (Integer) utenze.getAttribute(UtenzeDAO.ID_MODULO);
			}

			for (int j = 11; j < 30; j++) {
				HSSFRow rowGrid = worksheet.getRow(j);

				HSSFCell cellA = rowGrid.getCell(0);
				cellA.setCellType(CellType.STRING);
				String locale = cellA.getStringCellValue();

				HSSFCell cellB = rowGrid.getCell(1);
				cellB.setCellType(CellType.STRING);
				String tipoCS = cellB.getStringCellValue();

				HSSFCell cellC = rowGrid.getCell(2);
				cellC.setCellType(CellType.STRING);
				String marcaCS = cellC.getStringCellValue();

				HSSFCell cellD = rowGrid.getCell(3);
				cellD.setCellType(CellType.STRING);
				String largh = cellD.getStringCellValue();

				HSSFCell cellE = rowGrid.getCell(4);
				cellE.setCellType(CellType.STRING);
				String altezza = cellE.getStringCellValue();

				HSSFCell cellF = rowGrid.getCell(5);
				cellF.setCellType(CellType.STRING);
				String prof = cellF.getStringCellValue();

				HSSFCell cellG = rowGrid.getCell(6);
				cellG.setCellType(CellType.STRING);
				String elemColon = cellG.getStringCellValue();

				HSSFCell cellH = rowGrid.getCell(7);
				cellH.setCellType(CellType.STRING);
				String coeff = cellH.getStringCellValue();

				HSSFCell cellI = rowGrid.getCell(8);
				cellI.setCellType(CellType.STRING);
				String potenza = cellI.getStringCellValue();

				if (potenza.contains(".")) {
					potenza = potenza.substring(0, potenza.indexOf("."));
					if (potenza.substring(potenza.indexOf(".") + 1).startsWith("5")
							|| potenza.substring(potenza.indexOf(".") + 1).startsWith("6")
							|| potenza.substring(potenza.indexOf(".") + 1).startsWith("7")
							|| potenza.substring(potenza.indexOf(".") + 1).startsWith("8")
							|| potenza.substring(potenza.indexOf(".") + 1).startsWith("9")) {
						int potenza2 = Integer.parseInt(potenza) + 1;
						potenza = Integer.toString(potenza2);
					}
				}

				HSSFCell cellJ = rowGrid.getCell(9);
				cellJ.setCellType(CellType.STRING);
				String esp1 = cellJ.getStringCellValue();

				HSSFCell cellK = rowGrid.getCell(10);
				cellK.setCellType(CellType.STRING);
				String esp2 = cellK.getStringCellValue();

				HSSFCell cellL = rowGrid.getCell(11);
				cellL.setCellType(CellType.STRING);
				String etichetta = cellL.getStringCellValue();

				HSSFCell cellM = rowGrid.getCell(12);
				cellM.setCellType(CellType.STRING);
				String progr = cellM.getStringCellValue();

				HSSFCell cellN = rowGrid.getCell(13);
				cellN.setCellType(CellType.STRING);
				String tipo = cellN.getStringCellValue();

				HSSFCell cellO = rowGrid.getCell(14);
				cellO.setCellType(CellType.STRING);
				String posizione = cellO.getStringCellValue();

				HSSFCell cellP = rowGrid.getCell(15);
				cellP.setCellType(CellType.STRING);
				String diametroTubo = cellP.getStringCellValue();

				HSSFCell cellQ = rowGrid.getCell(16);
				cellQ.setCellType(CellType.STRING);
				String materialeTubo = cellQ.getStringCellValue();

				HSSFCell cellR = rowGrid.getCell(17);
				if (cellR.getCellTypeEnum() == CellType.FORMULA) {
					FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
					evaluator.evaluateFormulaCell(cellR);
				}
				cellR.setCellType(CellType.STRING);
				String preRegolazioneValvola = cellR.getStringCellValue();

				if (locale.toUpperCase().equals("K")) {
					locale = "CUCINA";
				}
				if (locale.toUpperCase().equals("I")) {
					locale = "INGRESSO";
				}
				if (locale.toUpperCase().equals("W")) {
					locale = "SOGGIORNO";
				}
				if (locale.toUpperCase().equals("C")) {
					locale = "CAMERA";
				}
				if (locale.toUpperCase().equals("KZ")) {
					locale = "CAMERETTA";
				}
				if (locale.toUpperCase().equals("B")) {
					locale = "BAGNO";
				}
				if (locale.toUpperCase().equals("S")) {
					locale = "STUDIO";
				}
				if (locale.toUpperCase().equals("H")) {
					locale = "HOBBY";
				}
				if (locale.toUpperCase().equals("T")) {
					locale = "TAVERNA";
				}
				if (locale.toUpperCase().equals("U")) {
					locale = "UFFICIO";
				}
				if (locale.toUpperCase().equals("E")) {
					locale = "SEMINTERRATO";
				}
				if (locale.toUpperCase().equals("A")) {
					locale = "AMMEZZATO";
				}
				if (locale.toUpperCase().equals("L")) {
					locale = "LAVANDERIA";
				}

				if (!etichetta.equals("")) {

					UtenzeDettagliDAO dao = new UtenzeDettagliDAO();
					dao.setAttribute(UtenzeDettagliDAO.ID_MODULO, idModulo);
					dao.setAttribute(UtenzeDettagliDAO.STANZA, locale);
					dao.setAttribute(UtenzeDettagliDAO.TIPO, tipoCS.replace("'", "''"));
					dao.setAttribute(UtenzeDettagliDAO.MARCA, marcaCS.replace("'", "''"));
					dao.setAttribute(UtenzeDettagliDAO.LARGHEZZA, largh);
					dao.setAttribute(UtenzeDettagliDAO.ALTEZZA, altezza);
					dao.setAttribute(UtenzeDettagliDAO.PROFONDITA, prof);
					dao.setAttribute(UtenzeDettagliDAO.ELEMENTI, elemColon);
					dao.setAttribute(UtenzeDettagliDAO.COEFF, coeff);
					dao.setAttribute(UtenzeDettagliDAO.POTENZA, potenza);
					dao.setAttribute(UtenzeDettagliDAO.ESP, esp1);
					dao.setAttribute(UtenzeDettagliDAO.ESP_2, esp2);
					dao.setAttribute(UtenzeDettagliDAO.RILEVATORE, etichetta);
					dao.setAttribute(UtenzeDettagliDAO.N_PROG, progr);
					dao.setAttribute(UtenzeDettagliDAO.POS, posizione);
					dao.setAttribute(UtenzeDettagliDAO.DIAMETRO, diametroTubo);
					dao.setAttribute(UtenzeDettagliDAO.PREREG, preRegolazioneValvola);
					dao.setAttribute(UtenzeDettagliDAO.MAT_TUBO, materialeTubo);
					dao.setAttribute(UtenzeDettagliDAO.TIPO_VALVOLA, tipo);
					dao.setAttribute(UtenzeDettagliDAO.ID_UTENTE_INS, 0);
					dao.insert();

				}

			}

			// if (!locatario.trim().equals("")) {
			// creaUtente(req, locatario, stabile, scala, piano,interno,
			// telefono, azienda, taggancio);
			// }
		}
	}

	protected static Integer inserimentoDatiCondominio(SsbServletRequest req, Integer azienda, HSSFSheet worksheetNetwork,
			String dataOggi) throws AppCrash {
		HSSFRow row1 = worksheetNetwork.getRow(4);
		HSSFCell cellA1 = row1.getCell(1);
		String stabile = cellA1.getStringCellValue();
		HSSFCell cellB1 = row1.getCell(6);
		String localita = cellB1.getStringCellValue();
		HSSFCell cellC1 = row1.getCell(10);
		cellC1.setCellType(CellType.STRING);
		String cap = cellC1.getStringCellValue();
		HSSFCell cellD1 = row1.getCell(12);
		String prov = cellD1.getStringCellValue();

		HSSFRow row2 = worksheetNetwork.getRow(6);
		HSSFCell cellA2 = row2.getCell(1);
		String indirizzo = cellA2.getStringCellValue();

		HSSFCell cellB2 = row2.getCell(10);
		cellB2.setCellType(CellType.STRING);
		String sim = cellB2.getStringCellValue();

		HSSFCell cellC2 = row2.getCell(12);
		cellC2.setCellType(CellType.STRING);
		String scaricoGG = cellC2.getStringCellValue();

		// dati amministratore - inizio
		HSSFRow row11 = worksheetNetwork.getRow(11);
		HSSFCell cellB11 = row11.getCell(1);
		cellB11.setCellType(CellType.STRING);
		String nominativoA = cellB11.getStringCellValue();

		HSSFCell cellG11 = row11.getCell(6);
		cellG11.setCellType(CellType.STRING);
		String localitaA = cellG11.getStringCellValue();

		HSSFCell cellK11 = row11.getCell(10);
		cellK11.setCellType(CellType.STRING);
		String capA = cellK11.getStringCellValue();

		HSSFCell cellN11 = row11.getCell(13);
		cellN11.setCellType(CellType.STRING);
		String provinciaA = cellN11.getStringCellValue();

		HSSFRow row13 = worksheetNetwork.getRow(13);
		HSSFCell cellB13 = row13.getCell(1);
		cellB13.setCellType(CellType.STRING);
		String indirizzoA = cellB13.getStringCellValue();

		HSSFCell cellG13 = row13.getCell(6);
		cellG13.setCellType(CellType.STRING);
		String codfiscA = cellG13.getStringCellValue();

		HSSFCell cellK13 = row13.getCell(10);
		cellK13.setCellType(CellType.STRING);
		String telefonoA = cellK13.getStringCellValue();

		HSSFCell cellN13 = row13.getCell(13);
		cellN13.setCellType(CellType.STRING);
		String mailA = cellN13.getStringCellValue();

		Integer idCondominio = 0;

		// Boolean giaImportato = countCondominioDoppi(stabile.replace("'",
		// "''"),indirizzo.replace("'", "''"),localita.replace("'", "''"),prov);
		// if (!giaImportato){
		if (!stabile.trim().equals("")) {

			CondominiDAO condomini = new CondominiDAO();
			condomini.setAttribute(CondominiDAO.ID_AZIENDA, azienda);
			condomini.setAttribute(CondominiDAO.DENOMINAZIONE, stabile);
			condomini.setAttribute(CondominiDAO.INDIRIZZO, indirizzo);
			condomini.setAttribute(CondominiDAO.LOCALITA, localita);
			condomini.setAttribute(CondominiDAO.PROVINCIA, prov);
			condomini.setAttribute(CondominiDAO.CAP, cap);
			condomini.setAttribute(CondominiDAO.SIM, sim);
			condomini.setAttribute(CondominiDAO.SCARICO, scaricoGG);
			condomini.setAttribute(CondominiDAO.TIPO, Constants_itf.SOLA_LETTURA);
			condomini.setAttribute(CondominiDAO.DT_MODULO, dataOggi);
			condomini.setAttribute(CondominiDAO.ID_UTENTE_INS, 0);
			condomini.setAttribute(CondominiDAO.ID_AMMINISTRATORE, UtilsInserimentiImportAnagraficaUtenze.impostaIdAmministratore(req, nominativoA, localitaA,
					capA, provinciaA, indirizzoA, codfiscA, telefonoA, mailA, idCondominio, azienda, stabile));
			condomini.setAttribute(CondominiDAO.DATA_PRIMO_SCARICO, Utils.getStringDataOggiRibaltata());
			condomini.setAttribute(CondominiDAO.DATA_PRIMA_SEGNALAZIONE, Utils.getStringDataOggiRibaltata());
			condomini.insert();

			idCondominio = (Integer) condomini.getAttribute(CondominiDAO.ID_MODULO);
			req.getSession(false).setAttribute("ID_CONDOMINIO", idCondominio);
		}
		/*
		 * }else{ idCondominio=trovaIdCondominio(stabile.replace("'",
		 * "''"),indirizzo.replace("'", "''"),localita.replace("'",
		 * "''"),prov,userInfo);
		 * 
		 * CondominiDAO condomini = new CondominiDAO();
		 * condomini.setAttribute(CondominiDAO.ID_MODULO, idCondominio);
		 * condomini.setAttribute(CondominiDAO.SIM,sim);
		 * condomini.setAttribute(CondominiDAO.SCARICO,scaricoGG);
		 * condomini.setAttribute(CondominiDAO.TIPO,Constants_itf.SOLA_LETTURA);
		 * condomini.setAttribute(CondominiDAO.DT_MODULO,dataOggi);
		 * condomini.setAttribute(CondominiDAO.ID_UTENTE_INS,0);
		 * condomini.setAttribute(CondominiDAO.ID_AMMINISTRATORE,
		 * impostaIdAmministratore(req, nominativoA, localitaA, capA,
		 * provinciaA, indirizzoA, codfiscA, telefonoA, mailA, idCondominio,
		 * azienda, stabile)); condomini.update(); }
		 */
		return idCondominio;
	}
	
	
	protected static void inserimentoContatoriCisterna(Integer azienda, HSSFSheet worksheetNetwork, Integer idCondominio,
			String dataOggi) throws AppCrash {
		HSSFRow row55 = worksheetNetwork.getRow(55);
		HSSFCell cellB1ci1 = row55.getCell(1);
		cellB1ci1.setCellType(CellType.STRING);
		String contatorec = cellB1ci1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatorec, azienda, idCondominio, dataOggi);

		HSSFCell cellE4ci1 = row55.getCell(4);
		cellE4ci1.setCellType(CellType.STRING);
		String contatoreci1 = cellE4ci1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatoreci1, azienda, idCondominio, dataOggi);

		HSSFCell cellH7ci1 = row55.getCell(7);
		cellH7ci1.setCellType(CellType.STRING);
		String contatoreci2 = cellH7ci1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatoreci2, azienda, idCondominio, dataOggi);

		HSSFCell cellK10ci1 = row55.getCell(10);
		cellK10ci1.setCellType(CellType.STRING);
		String contatorec3 = cellK10ci1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatorec3, azienda, idCondominio, dataOggi);

		HSSFCell cellN13ci1 = row55.getCell(13);
		cellN13ci1.setCellType(CellType.STRING);
		String contatorec4 = cellN13ci1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatorec4, azienda, idCondominio, dataOggi);

		HSSFRow row57 = worksheetNetwork.getRow(57);

		HSSFCell cellH20c2 = row57.getCell(1);
		cellH20c2.setCellType(CellType.STRING);
		String contatorec9 = cellH20c2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatorec9, azienda, idCondominio, dataOggi);

		HSSFCell cellB1ci2 = row57.getCell(4);
		cellB1ci2.setCellType(CellType.STRING);
		String contatorec5 = cellB1ci2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatorec5, azienda, idCondominio, dataOggi);

		HSSFCell cellE4ci2 = row57.getCell(7);
		cellE4ci2.setCellType(CellType.STRING);
		String contatorec6 = cellE4ci2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatorec6, azienda, idCondominio, dataOggi);

		HSSFCell cellK10ci2 = row57.getCell(10);
		cellK10ci2.setCellType(CellType.STRING);
		String contatorec7 = cellK10ci2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatorec7, azienda, idCondominio, dataOggi);

		HSSFCell cellN20ci2 = row57.getCell(13);
		cellN20ci2.setCellType(CellType.STRING);
		String contatorec8 = cellN20ci2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("C", contatorec8, azienda, idCondominio, dataOggi);
	}

	protected static void inserimentoContatoriElettrici(Integer azienda, HSSFSheet worksheetNetwork, Integer idCondominio,
			String dataOggi) throws AppCrash {
		HSSFRow row48 = worksheetNetwork.getRow(48);
		HSSFCell cellB1e1 = row48.getCell(1);
		cellB1e1.setCellType(CellType.STRING);
		String contatoree = cellB1e1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree, azienda, idCondominio, dataOggi);

		HSSFCell cellE4e1 = row48.getCell(4);
		cellE4e1.setCellType(CellType.STRING);
		String contatoree1 = cellE4e1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree1, azienda, idCondominio, dataOggi);

		HSSFCell cellH7e1 = row48.getCell(7);
		cellH7e1.setCellType(CellType.STRING);
		String contatoree2 = cellH7e1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree2, azienda, idCondominio, dataOggi);

		HSSFCell cellK10e1 = row48.getCell(10);
		cellK10e1.setCellType(CellType.STRING);
		String contatoree3 = cellK10e1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree3, azienda, idCondominio, dataOggi);

		HSSFCell cellN13e1 = row48.getCell(13);
		cellN13e1.setCellType(CellType.STRING);
		String contatoree4 = cellN13e1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree4, azienda, idCondominio, dataOggi);

		HSSFRow row50 = worksheetNetwork.getRow(50);

		HSSFCell cellH20e2 = row50.getCell(1);
		cellH20e2.setCellType(CellType.STRING);
		String contatoree9 = cellH20e2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree9, azienda, idCondominio, dataOggi);

		HSSFCell cellB1e2 = row50.getCell(4);
		cellB1e2.setCellType(CellType.STRING);
		String contatoree5 = cellB1e2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree5, azienda, idCondominio, dataOggi);

		HSSFCell cellE4e2 = row50.getCell(7);
		cellE4e2.setCellType(CellType.STRING);
		String contatoree6 = cellE4e2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree6, azienda, idCondominio, dataOggi);

		HSSFCell cellK10e2 = row50.getCell(10);
		cellK10e2.setCellType(CellType.STRING);
		String contatoree7 = cellK10e2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree7, azienda, idCondominio, dataOggi);

		HSSFCell cellN20e2 = row50.getCell(13);
		cellN20e2.setCellType(CellType.STRING);
		String contatoree8 = cellN20e2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("E", contatoree8, azienda, idCondominio, dataOggi);
	}

	protected static void inserimentoContatoriMetano(Integer azienda, HSSFSheet worksheetNetwork, Integer idCondominio,
			String dataOggi) throws AppCrash {
		HSSFRow row41 = worksheetNetwork.getRow(41);
		HSSFCell cellB1m1 = row41.getCell(1);
		cellB1m1.setCellType(CellType.STRING);
		String contatorem = cellB1m1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem, azienda, idCondominio, dataOggi);

		HSSFCell cellE4m1 = row41.getCell(4);
		cellE4m1.setCellType(CellType.STRING);
		String contatorem1 = cellE4m1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem1, azienda, idCondominio, dataOggi);

		HSSFCell cellH7m1 = row41.getCell(7);
		cellH7m1.setCellType(CellType.STRING);
		String contatorem2 = cellH7m1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem2, azienda, idCondominio, dataOggi);

		HSSFCell cellK10m1 = row41.getCell(10);
		cellK10m1.setCellType(CellType.STRING);
		String contatorem3 = cellK10m1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem3, azienda, idCondominio, dataOggi);

		HSSFCell cellN13m1 = row41.getCell(13);
		cellN13m1.setCellType(CellType.STRING);
		String contatorem4 = cellN13m1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem4, azienda, idCondominio, dataOggi);

		HSSFRow row43 = worksheetNetwork.getRow(43);

		HSSFCell cellH1c2m = row43.getCell(1);
		cellH1c2m.setCellType(CellType.STRING);
		String contatore9m = cellH1c2m.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatore9m, azienda, idCondominio, dataOggi);

		HSSFCell cellB1m2 = row43.getCell(4);
		cellB1m2.setCellType(CellType.STRING);
		String contatorem5 = cellB1m2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem5, azienda, idCondominio, dataOggi);

		HSSFCell cellE4m2 = row43.getCell(7);
		cellE4m2.setCellType(CellType.STRING);
		String contatorem6 = cellE4m2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem6, azienda, idCondominio, dataOggi);

		HSSFCell cellK10m2 = row43.getCell(10);
		cellK10m2.setCellType(CellType.STRING);
		String contatorem7 = cellK10m2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem7, azienda, idCondominio, dataOggi);

		HSSFCell cellN20m2 = row43.getCell(13);
		cellN20m2.setCellType(CellType.STRING);
		String contatorem8 = cellN20m2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("M", contatorem8, azienda, idCondominio, dataOggi);
	}

	protected static void inserimentoContatoriRiscaldamento(Integer azienda, HSSFSheet worksheetNetwork, Integer idCondominio,
			String dataOggi) throws AppCrash {
		HSSFRow row34 = worksheetNetwork.getRow(34);
		HSSFCell cellB1r1 = row34.getCell(1);
		cellB1r1.setCellType(CellType.STRING);
		String contatoreR = cellB1r1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatoreR, azienda, idCondominio, dataOggi);

		HSSFCell cellE4r1 = row34.getCell(4);
		cellE4r1.setCellType(CellType.STRING);
		String contatore1R = cellE4r1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore1R, azienda, idCondominio, dataOggi);

		HSSFCell cellH7r1 = row34.getCell(7);
		cellH7r1.setCellType(CellType.STRING);
		String contatore2R = cellH7r1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore2R, azienda, idCondominio, dataOggi);

		HSSFCell cellK10r1 = row34.getCell(10);
		cellK10r1.setCellType(CellType.STRING);
		String contatore3R = cellK10r1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore3R, azienda, idCondominio, dataOggi);

		HSSFCell cellN13r1 = row34.getCell(13);
		cellN13r1.setCellType(CellType.STRING);
		String contatore4R = cellN13r1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore4R, azienda, idCondominio, dataOggi);

		HSSFRow row36 = worksheetNetwork.getRow(36);

		HSSFCell cellH1c2r = row36.getCell(1);
		cellH1c2r.setCellType(CellType.STRING);
		String contatore9R = cellH1c2r.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore9R, azienda, idCondominio, dataOggi);

		HSSFCell cellB1r2 = row36.getCell(4);
		cellB1r2.setCellType(CellType.STRING);
		String contatore5R = cellB1r2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore5R, azienda, idCondominio, dataOggi);

		HSSFCell cellE4r2 = row36.getCell(7);
		cellE4r2.setCellType(CellType.STRING);
		String contatore6R = cellE4r2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore6R, azienda, idCondominio, dataOggi);

		HSSFCell cellK10r2 = row36.getCell(10);
		cellK10r2.setCellType(CellType.STRING);
		String contatore7R = cellK10r2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore7R, azienda, idCondominio, dataOggi);

		HSSFCell cellN20r2 = row36.getCell(13);
		cellN20r2.setCellType(CellType.STRING);
		String contatore8R = cellN20r2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("R", contatore8R, azienda, idCondominio, dataOggi);
	}

	protected static void inserimentoContatoriAcquaCalda(Integer azienda, HSSFSheet worksheetNetwork, Integer idCondominio,
			String dataOggi) throws AppCrash {
		HSSFRow row27 = worksheetNetwork.getRow(27);
		HSSFCell cellB1c1 = row27.getCell(1);
		cellB1c1.setCellType(CellType.STRING);
		String contatore = cellB1c1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore, azienda, idCondominio, dataOggi);

		HSSFCell cellE4c1 = row27.getCell(4);
		cellE4c1.setCellType(CellType.STRING);
		String contatore1 = cellE4c1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore1, azienda, idCondominio, dataOggi);

		HSSFCell cellH7c1 = row27.getCell(7);
		cellH7c1.setCellType(CellType.STRING);
		String contatore2 = cellH7c1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore2, azienda, idCondominio, dataOggi);

		HSSFCell cellK10c1 = row27.getCell(10);
		cellK10c1.setCellType(CellType.STRING);
		String contatore3 = cellK10c1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore3, azienda, idCondominio, dataOggi);

		HSSFCell cellN13c1 = row27.getCell(13);
		cellN13c1.setCellType(CellType.STRING);
		String contatore4 = cellN13c1.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore4, azienda, idCondominio, dataOggi);

		HSSFRow row29 = worksheetNetwork.getRow(29);

		HSSFCell cellH1c2 = row29.getCell(1);
		cellH1c2.setCellType(CellType.STRING);
		String contatore9 = cellH1c2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore9, azienda, idCondominio, dataOggi);

		HSSFCell cellB1c2 = row29.getCell(4);
		cellB1c2.setCellType(CellType.STRING);
		String contatore5 = cellB1c2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore5, azienda, idCondominio, dataOggi);

		HSSFCell cellE4c2 = row29.getCell(7);
		cellE4c2.setCellType(CellType.STRING);
		String contatore6 = cellE4c2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore6, azienda, idCondominio, dataOggi);

		HSSFCell cellK10c2 = row29.getCell(10);
		cellK10c2.setCellType(CellType.STRING);
		String contatore7 = cellK10c2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore7, azienda, idCondominio, dataOggi);

		HSSFCell cellN20c2 = row29.getCell(13);
		cellN20c2.setCellType(CellType.STRING);
		String contatore8 = cellN20c2.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciContatori("A", contatore8, azienda, idCondominio, dataOggi);
	}

	protected static void inserimentoAntenne(Integer azienda, HSSFSheet worksheetNetwork, Integer idCondominio, String dataOggi)
			throws AppCrash {
		// antenne - inizio
		HSSFRow row19 = worksheetNetwork.getRow(18);

		HSSFCell cellB19 = row19.getCell(1);
		cellB19.setCellType(CellType.STRING);
		String gateway = cellB19.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(true, gateway, azienda, idCondominio, dataOggi);

		HSSFCell cellE19 = row19.getCell(4);
		cellE19.setCellType(CellType.STRING);
		String antenna1 = cellE19.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna1, azienda, idCondominio, dataOggi);

		HSSFCell cellH19 = row19.getCell(7);
		cellH19.setCellType(CellType.STRING);
		String antenna2 = cellH19.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna2, azienda, idCondominio, dataOggi);

		HSSFCell cellK19 = row19.getCell(10);
		cellK19.setCellType(CellType.STRING);
		String antenna3 = cellK19.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna3, azienda, idCondominio, dataOggi);

		HSSFCell cellN19 = row19.getCell(13);
		cellN19.setCellType(CellType.STRING);
		String antenna4 = cellN19.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna4, azienda, idCondominio, dataOggi);

		HSSFRow row20 = worksheetNetwork.getRow(20);

		HSSFCell cellE20 = row20.getCell(4);
		cellE20.setCellType(CellType.STRING);
		String antenna5 = cellE20.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna5, azienda, idCondominio, dataOggi);

		HSSFCell cellH20 = row20.getCell(7);
		cellH20.setCellType(CellType.STRING);
		String antenna6 = cellH20.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna6, azienda, idCondominio, dataOggi);

		HSSFCell cellK20 = row20.getCell(10);
		cellK20.setCellType(CellType.STRING);
		String antenna7 = cellK20.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna7, azienda, idCondominio, dataOggi);

		HSSFCell cellN20 = row20.getCell(13);
		cellN20.setCellType(CellType.STRING);
		String antenna8 = cellN20.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna8, azienda, idCondominio, dataOggi);

		HSSFRow row23 = worksheetNetwork.getRow(22);

		HSSFCell cellE23 = row23.getCell(4);
		cellE23.setCellType(CellType.STRING);
		String antenna9 = cellE23.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna9, azienda, idCondominio, dataOggi);

		HSSFCell cellH23 = row23.getCell(7);
		cellH23.setCellType(CellType.STRING);
		String antenna10 = cellH23.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna10, azienda, idCondominio, dataOggi);

		HSSFCell cellK23 = row23.getCell(10);
		cellK23.setCellType(CellType.STRING);
		String antenna11 = cellK23.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna11, azienda, idCondominio, dataOggi);

		HSSFCell cellN23 = row23.getCell(13);
		cellN23.setCellType(CellType.STRING);
		String antenna12 = cellN23.getStringCellValue();
		UtilsInserimentiImportAnagraficaUtenze.inserisciNetwork(false, antenna12, azienda, idCondominio, dataOggi);
		// antenne - fine
	}

}