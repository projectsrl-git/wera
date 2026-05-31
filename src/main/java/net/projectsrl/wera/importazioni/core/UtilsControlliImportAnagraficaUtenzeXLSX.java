package net.projectsrl.wera.importazioni.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.wera.utils.WeraUtils;

public abstract class UtilsControlliImportAnagraficaUtenzeXLSX extends FunctionProjectWebApp_base {

	public UtilsControlliImportAnagraficaUtenzeXLSX() {
		super();
	}

	public UtilsControlliImportAnagraficaUtenzeXLSX(ApplicationServices_itf applServices, String functionID,
			String functionName) {
		super(applServices, functionID, functionName);
	}
	
	
	
	protected static String verificaNomeSchede(XSSFWorkbook workbook) {
		String nomeSchedeErrato = "";
		String nomeScheda = "";
		for (int i = 1; i <= workbook.getNumberOfSheets() - 1; i++) {
			nomeScheda = workbook.getSheetName(i);
			if (!nomeScheda.equals("Generale") && (!nomeScheda.equals("Foglio " + i))) {
				nomeSchedeErrato = ", " + nomeSchedeErrato;
				// templateData.put("MESSAGGIO_OUT", "Foglio importato in
				// formato non corretto, verificare il nome delle schede");
			}
		}
		return nomeSchedeErrato.replaceFirst(", ", "");
	}
	
	
	protected static String verificaUtenzeConStessoContatore(XSSFWorkbook workbook) throws AppCrash {
		String duplicato="";
		
		List<String> myArray = new ArrayList<String>();
		for (int i = 1; i <= workbook.getNumberOfSheets() - 1; i++) {

			XSSFSheet worksheet = workbook.getSheet("Foglio " + i);

			XSSFRow row3 = worksheet.getRow(8);
			XSSFCell cellA3 = row3.getCell(1);
			String locatario = cellA3.getStringCellValue();
			locatario = locatario.replace("’", "''");

			for (int j = 11; j < 30; j++) {
				XSSFRow rowGrid = worksheet.getRow(j);

				XSSFCell cellA = rowGrid.getCell(0);
				cellA.setCellType(CellType.STRING);
				String locale = cellA.getStringCellValue();

				XSSFCell cellL = rowGrid.getCell(11);
				cellL.setCellType(CellType.STRING);
				String etichetta = cellL.getStringCellValue();

				if (!etichetta.isEmpty()){
					
					if( myArray.contains(etichetta)){
						duplicato=duplicato+", "+etichetta;
					}else{
						myArray.add(etichetta);
					}
				}
				

			}
		}

		duplicato=duplicato.replaceFirst(", ","");
		return duplicato;

	}

	protected static String verificaUtenze(Integer azienda, XSSFWorkbook workbook) throws AppCrash {
		
		String msgErroreValidazione="";
		UtilsErroreValidazione errore=null;
		
		for (int i = 1; i <= workbook.getNumberOfSheets() - 1; i++) {

			XSSFSheet worksheet = workbook.getSheet("Foglio " + i);
			String nomeFoglio = worksheet.getSheetName();

			XSSFRow row2d = worksheet.getRow(6);
			XSSFCell cellG6 = row2d.getCell(6);
			cellG6.setCellType(CellType.STRING);
			String telefono = cellG6.getStringCellValue();

			XSSFRow row3 = worksheet.getRow(8);
			XSSFCell cellA3 = row3.getCell(1);
			String locatario = cellA3.getStringCellValue();
			locatario = locatario.replace("’", "''");
			XSSFCell cellB3 = row3.getCell(6);
			cellB3.setCellType(CellType.STRING);
			String scala = cellB3.getStringCellValue();
			XSSFCell cellC3 = row3.getCell(8);
			cellC3.setCellType(CellType.STRING);
			String piano = cellC3.getStringCellValue();
			XSSFCell cellD3 = row3.getCell(10);
			cellD3.setCellType(CellType.STRING);
			String interno = cellD3.getStringCellValue();
			XSSFCell cellE3 = row3.getCell(12);
			cellE3.setCellType(CellType.STRING);
			String millesimi = cellE3.getStringCellValue();
			
			
			
			//UtilsValidatoreExcel.validaCampo(telefono, "n_telefono", 6, 6);
			errore = UtilsValidatoreExcel.validaCampo(telefono, "n_telefono", 6 ,6, nomeFoglio);
			if (errore != null) {
				msgErroreValidazione=msgErroreValidazione+ errore; 
			}
			
			errore = UtilsValidatoreExcel.validaCampo(locatario, "locatario", 8, 1, nomeFoglio);
			if (errore != null) {
				msgErroreValidazione=msgErroreValidazione+ errore; 
			}
			
			errore = UtilsValidatoreExcel.validaCampo(scala, "scala", 8, 6, nomeFoglio);
			if (errore != null) {
				msgErroreValidazione=msgErroreValidazione+ errore; 
			}
			
			errore = UtilsValidatoreExcel.validaCampo(piano, "piano", 8, 8, nomeFoglio);
			if (errore != null) {
				msgErroreValidazione=msgErroreValidazione+ errore; 
			}
			
			errore = UtilsValidatoreExcel.validaCampo(interno, "interno", 8, 10, nomeFoglio);
			if (errore != null) {
				msgErroreValidazione=msgErroreValidazione+ errore; 
			}
			
			errore = UtilsValidatoreExcel.validaCampo(millesimi, "millesimi", 8, 12, nomeFoglio);
			if (errore != null) {
				msgErroreValidazione=msgErroreValidazione+ errore; 
			}
			
			
			

			for (int j = 11; j < 30; j++) {
				XSSFRow rowGrid = worksheet.getRow(j);
				

				XSSFCell cellA = rowGrid.getCell(0);
				cellA.setCellType(CellType.STRING);
				String locale = cellA.getStringCellValue();

				XSSFCell cellB = rowGrid.getCell(1);
				cellB.setCellType(CellType.STRING);
				String tipoCS = cellB.getStringCellValue();

				XSSFCell cellC = rowGrid.getCell(2);
				cellC.setCellType(CellType.STRING);
				String marcaCS = cellC.getStringCellValue();

				XSSFCell cellD = rowGrid.getCell(3);
				cellD.setCellType(CellType.STRING);
				String largh = cellD.getStringCellValue();

				XSSFCell cellE = rowGrid.getCell(4);
				cellE.setCellType(CellType.STRING);
				String altezza = cellE.getStringCellValue();

				XSSFCell cellF = rowGrid.getCell(5);
				cellF.setCellType(CellType.STRING);
				String prof = cellF.getStringCellValue();

				XSSFCell cellG = rowGrid.getCell(6);
				cellG.setCellType(CellType.STRING);
				String elemColon = cellG.getStringCellValue();

				XSSFCell cellH = rowGrid.getCell(7);
				cellH.setCellType(CellType.STRING);
				String coeff = cellH.getStringCellValue();

				XSSFCell cellI = rowGrid.getCell(8);
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

				
				if (nomeFoglio.equals("Foglio 4")){
					String bbb="";
				}
				
				
				XSSFCell cellJ = rowGrid.getCell(9);
				DataFormatter formatter = new DataFormatter(Locale.US);
				String esp1 = formatter.formatCellValue(cellJ);
				//cellJ.setCellType(CellType.STRING);
				//String esp1 = cellJ.getStringCellValue();

				XSSFCell cellK = rowGrid.getCell(10);
				String esp2 = formatter.formatCellValue(cellK);
				//cellK.setCellType(CellType.STRING);
				//String esp2 = cellK.getStringCellValue();

				XSSFCell cellL = rowGrid.getCell(11);
				cellL.setCellType(CellType.STRING);
				String etichetta = cellL.getStringCellValue();

				XSSFCell cellM = rowGrid.getCell(12);
				cellM.setCellType(CellType.STRING);
				String progr = cellM.getStringCellValue();

				XSSFCell cellN = rowGrid.getCell(13);
				cellN.setCellType(CellType.STRING);
				String tipo = cellN.getStringCellValue();

				XSSFCell cellO = rowGrid.getCell(14);
				cellO.setCellType(CellType.STRING);
				String posizione = cellO.getStringCellValue();

				XSSFCell cellP = rowGrid.getCell(15);
				cellP.setCellType(CellType.STRING);
				String diametroTubo = cellP.getStringCellValue();

				XSSFCell cellQ = rowGrid.getCell(16);
				cellQ.setCellType(CellType.STRING);
				String materialeTubo = cellQ.getStringCellValue();

				XSSFCell cellR = rowGrid.getCell(17);
				//if (cellR.getCellTypeEnum() == CellType.FORMULA) {
					//XSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
					//evaluator.evaluate(cellR);
					//XSSFFormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				//	evaluator.evaluateFormulaCell(cellR);
				//	}
				cellR.setCellType(CellType.STRING);
				String preRegolazioneValvola = cellR.getStringCellValue();
				
				
				
				errore = UtilsValidatoreExcel.validaCampo(locale, "stanza"      , j, 0, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(tipoCS, "tipo"        , j, 1, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				
				errore = UtilsValidatoreExcel.validaCampo(marcaCS, "marca"       , j, 2, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(largh, "larghezza"   , j, 3, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(altezza, "altezza"     , j, 4, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(prof, "profondita"  , j, 5, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(elemColon, "elementi"    , j, 6, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(coeff, "coeff"       , j, 7, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(potenza, "potenza"     , j, 8, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(esp1, "esp1"        , j, 9, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(esp2, "esp2"        , j, 10, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(etichetta, "rilevatore"  , j, 11, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(progr, "n_prog"      , j, 12, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(tipo, "tipo_valvola", j, 13, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(posizione, "pos"         , j, 14, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(diametroTubo, "diametro"    , j, 15, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore = UtilsValidatoreExcel.validaCampo(materialeTubo, "mat_tubo"    , j, 16, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}
				errore= UtilsValidatoreExcel.validaCampo(preRegolazioneValvola, "prereg"      , j, 17, nomeFoglio);
				if (errore != null) {
					msgErroreValidazione=msgErroreValidazione+ errore; 
				}

			}

		}
		
		return msgErroreValidazione;
	}
	
	
	
	protected static String verificaContatoriAcquaCalda(XSSFSheet worksheetNetwork) throws AppCrash {

		List<String> myArray = new ArrayList<String>();

		String denominazione = "";

		XSSFRow row27 = worksheetNetwork.getRow(27);
		XSSFCell cellB1c1 = row27.getCell(1);
		cellB1c1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1c1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4c1 = row27.getCell(4);
		cellE4c1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4c1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellH7c1 = row27.getCell(7);
		cellH7c1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH7c1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10c1 = row27.getCell(10);
		cellK10c1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10c1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN13c1 = row27.getCell(13);
		cellN13c1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN13c1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFRow row29 = worksheetNetwork.getRow(29);

		XSSFCell cellH1c2 = row29.getCell(1);
		cellH1c2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH1c2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellB1c2 = row29.getCell(4);
		cellB1c2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1c2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4c2 = row29.getCell(7);
		cellE4c2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4c2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10c2 = row29.getCell(10);
		cellK10c2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10c2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN20c2 = row29.getCell(13);
		cellN20c2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN20c2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		myArray.removeAll(Collections.singleton("''"));
		myArray.removeAll(Collections.singleton(null));
		if (myArray.isEmpty()) {
			return denominazione;
		}
		String tabella = "CONTATORI";
		String campo = "CONTATORE";
		String join = "";
		String valore = myArray.toString().replace("[", "(").replace("]", ")");

		denominazione = WeraUtils.contaDescriviDoppi(tabella, campo, valore, join);
		return denominazione;

	}

	protected static String verificaContatoriCisterna(XSSFSheet worksheetNetwork) throws AppCrash {

		List<String> myArray = new ArrayList<String>();

		String denominazione = "";

		XSSFRow row55 = worksheetNetwork.getRow(55);
		XSSFCell cellB1ci1 = row55.getCell(1);
		cellB1ci1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1ci1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4ci1 = row55.getCell(4);
		cellE4ci1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4ci1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellH7ci1 = row55.getCell(7);
		cellH7ci1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH7ci1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10ci1 = row55.getCell(10);
		cellK10ci1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10ci1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN13ci1 = row55.getCell(13);
		cellN13ci1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN13ci1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFRow row57 = worksheetNetwork.getRow(57);

		XSSFCell cellH20c2 = row57.getCell(1);
		cellH20c2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH20c2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellB1ci2 = row57.getCell(4);
		cellB1ci2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1ci2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4ci2 = row57.getCell(7);
		cellE4ci2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4ci2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10ci2 = row57.getCell(10);
		cellK10ci2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10ci2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN20ci2 = row57.getCell(13);
		cellN20ci2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN20ci2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		myArray.removeAll(Collections.singleton("''"));
		myArray.removeAll(Collections.singleton(null));
		if (myArray.isEmpty()) {
			return denominazione;
		}
		String tabella = "CONTATORI";
		String campo = "CONTATORE";
		String join = "";
		String valore = myArray.toString().replace("[", "(").replace("]", ")");

		denominazione = WeraUtils.contaDescriviDoppi(tabella, campo, valore, join);
		return denominazione;

	}

	protected static String verificaContatoriElettrici(XSSFSheet worksheetNetwork) throws AppCrash {
		List<String> myArray = new ArrayList<String>();

		String denominazione = "";

		XSSFRow row48 = worksheetNetwork.getRow(48);
		XSSFCell cellB1e1 = row48.getCell(1);
		cellB1e1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1e1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4e1 = row48.getCell(4);
		cellE4e1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4e1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellH7e1 = row48.getCell(7);
		cellH7e1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH7e1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10e1 = row48.getCell(10);
		cellK10e1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10e1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN13e1 = row48.getCell(13);
		cellN13e1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN13e1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFRow row50 = worksheetNetwork.getRow(50);

		XSSFCell cellH20e2 = row50.getCell(1);
		cellH20e2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH20e2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellB1e2 = row50.getCell(4);
		cellB1e2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1e2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4e2 = row50.getCell(7);
		cellE4e2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4e2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10e2 = row50.getCell(10);
		cellK10e2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10e2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN20e2 = row50.getCell(13);
		cellN20e2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN20e2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		myArray.removeAll(Collections.singleton("''"));
		myArray.removeAll(Collections.singleton(null));
		if (myArray.isEmpty()) {
			return denominazione;
		}
		String tabella = "CONTATORI";
		String campo = "CONTATORE";
		String join = "";
		String valore = myArray.toString().replace("[", "(").replace("]", ")");

		denominazione = WeraUtils.contaDescriviDoppi(tabella, campo, valore, join);
		return denominazione;
	}

	protected static String verificaContatoriMetano(XSSFSheet worksheetNetwork) throws AppCrash {
		List<String> myArray = new ArrayList<String>();

		String denominazione = "";

		XSSFRow row41 = worksheetNetwork.getRow(41);
		XSSFCell cellB1m1 = row41.getCell(1);
		cellB1m1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1m1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4m1 = row41.getCell(4);
		cellE4m1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4m1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellH7m1 = row41.getCell(7);
		cellH7m1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH7m1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10m1 = row41.getCell(10);
		cellK10m1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10m1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN13m1 = row41.getCell(13);
		cellN13m1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN13m1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFRow row43 = worksheetNetwork.getRow(43);

		XSSFCell cellH1c2m = row43.getCell(1);
		cellH1c2m.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH1c2m.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellB1m2 = row43.getCell(4);
		cellB1m2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1m2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4m2 = row43.getCell(7);
		cellE4m2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4m2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10m2 = row43.getCell(10);
		cellK10m2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10m2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN20m2 = row43.getCell(13);
		cellN20m2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN20m2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		myArray.removeAll(Collections.singleton("''"));
		myArray.removeAll(Collections.singleton(null));
		if (myArray.isEmpty()) {
			return denominazione;
		}
		String tabella = "CONTATORI";
		String campo = "CONTATORE";
		String join = "";
		String valore = myArray.toString().replace("[", "(").replace("]", ")");

		denominazione = WeraUtils.contaDescriviDoppi(tabella, campo, valore, join);
		return denominazione;
	}

	protected static String verificaContatoriRiscaldamento(XSSFSheet worksheetNetwork) throws AppCrash {
		List<String> myArray = new ArrayList<String>();

		String denominazione = "";

		XSSFRow row34 = worksheetNetwork.getRow(34);
		XSSFCell cellB1r1 = row34.getCell(1);
		cellB1r1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1r1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4r1 = row34.getCell(4);
		cellE4r1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4r1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellH7r1 = row34.getCell(7);
		cellH7r1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH7r1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10r1 = row34.getCell(10);
		cellK10r1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10r1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN13r1 = row34.getCell(13);
		cellN13r1.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN13r1.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFRow row36 = worksheetNetwork.getRow(36);

		XSSFCell cellH1c2r = row36.getCell(1);
		cellH1c2r.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH1c2r.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellB1r2 = row36.getCell(4);
		cellB1r2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB1r2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE4r2 = row36.getCell(7);
		cellE4r2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE4r2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK10r2 = row36.getCell(10);
		cellK10r2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK10r2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN20r2 = row36.getCell(13);
		cellN20r2.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN20r2.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		myArray.removeAll(Collections.singleton("''"));
		myArray.removeAll(Collections.singleton(null));

		if (myArray.isEmpty()) {
			return denominazione;
		}
		String tabella = "CONTATORI";
		String campo = "CONTATORE";
		String join = "";
		String valore = myArray.toString().replace("[", "(").replace("]", ")");

		denominazione = WeraUtils.contaDescriviDoppi(tabella, campo, valore, join);
		return denominazione;
	}

	protected static String verificaAntenne(XSSFSheet worksheetNetwork) throws AppCrash {
		List<String> myArray = new ArrayList<String>();

		String denominazione = "";
		XSSFRow row19 = worksheetNetwork.getRow(18);

		XSSFCell cellB19 = row19.getCell(1);
		cellB19.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellB19.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellE19 = row19.getCell(4);
		cellE19.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE19.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellH19 = row19.getCell(7);
		cellH19.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH19.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK19 = row19.getCell(10);
		cellK19.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK19.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN19 = row19.getCell(13);
		cellN19.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN19.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFRow row20 = worksheetNetwork.getRow(20);

		XSSFCell cellE20 = row20.getCell(4);
		cellE20.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE20.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellH20 = row20.getCell(7);
		cellH20.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH20.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK20 = row20.getCell(10);
		cellK20.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK20.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN20 = row20.getCell(13);
		cellN20.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN20.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFRow row23 = worksheetNetwork.getRow(22);

		XSSFCell cellE23 = row23.getCell(4);
		cellE23.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellE23.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellH23 = row23.getCell(7);
		cellH23.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellH23.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellK23 = row23.getCell(10);
		cellK23.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellK23.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		XSSFCell cellN23 = row23.getCell(13);
		cellN23.setCellType(CellType.STRING);
		myArray.add(Constants_itf.APICE_SINGOLO + cellN23.getStringCellValue() + Constants_itf.APICE_SINGOLO);

		myArray.removeAll(Collections.singleton("''"));
		myArray.removeAll(Collections.singleton(null));
		if (myArray.isEmpty()) {
			return denominazione;
		}
		String tabella = "NETWORK";
		String campo = "ANTENNA";
		String join = "";
		String valore = myArray.toString().replace("[", "(").replace("]", ")");

		denominazione = WeraUtils.contaDescriviDoppi(tabella, campo, valore, join);
		return denominazione;
	}

	protected static String verificaRilevatori(XSSFWorkbook workbook) throws AppCrash {
		List<String> myArray = new ArrayList<String>();

		String denominazione = "";

		for (int i = 1; i <= workbook.getNumberOfSheets() - 1; i++) {
			XSSFSheet worksheet = workbook.getSheet("Foglio " + i);
			for (int j = 11; j < 30; j++) {
				XSSFRow rowGrid = worksheet.getRow(j);
				XSSFCell cellL = rowGrid.getCell(11);
				cellL.setCellType(CellType.STRING);
				myArray.add(Constants_itf.APICE_SINGOLO + cellL.getStringCellValue() + Constants_itf.APICE_SINGOLO);
			}
		}

		myArray.removeAll(Collections.singleton("''"));
		myArray.removeAll(Collections.singleton(null));
		if (myArray.isEmpty()) {
			return denominazione;
		}
		String tabella = "UTENZE_DETTAGLIO";
		String campo = "RILEVATORE";
		String join = "left outer join utenze as ut on ut.id_modulo=tab.id_modulo";
		String valore = myArray.toString().replace("[", "(").replace("]", ")");

		denominazione = WeraUtils.contaDescriviDoppi(tabella, campo, valore, join);
		return denominazione;

	}
	
	
	private static Map<String, UtilsCampoDB> schemaDB = new HashMap<>();
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
	    
	    String tipoPrevisto;
	    if ("DECIMAL".equals(campo.tipo)) {
	        tipoPrevisto = String.format("DECIMAL(%d,%d)", campo.precisione, campo.scala);
	    } else if ("STRING".equals(campo.tipo)) {
	        tipoPrevisto = String.format("STRING(%d)", campo.maxLength);
	    } else {
	        tipoPrevisto = campo.tipo;
	    }

	    // Lunghezza (per STRING e INTEGER se maxLength > 0)
	    if ((campo.tipo.equals("STRING") || campo.tipo.equals("INTEGER")) && campo.maxLength > 0) {
	        if (valore.length() > campo.maxLength) {
	            return new UtilsErroreValidazione(nomeFoglio,nomeCampo,
	                    "Lunghezza superiore al massimo consentito",
	                    valore, tipoPrevisto, "lunghezza " + valore.length(), riga, colonna);
	        }
	    }

	    // Tipo e validazioni specifiche
	    switch (campo.tipo) {
	        case "INTEGER":
	            try {
	                Integer.parseInt(valore);
	            } catch (NumberFormatException e) {
	                return new UtilsErroreValidazione(nomeFoglio,nomeCampo,
	                        "Valore non è un intero valido",
	                        valore, tipoPrevisto, "non intero", riga, colonna);
	            }
	            break;

	        case "DECIMAL":
	            try {
	                BigDecimal valoreDecimal = new BigDecimal(valore);

	                int actualPrecision = valoreDecimal.precision();
	                int actualScale = valoreDecimal.scale();

	                if (actualPrecision > campo.precisione) {
	                    return new UtilsErroreValidazione(nomeFoglio,nomeCampo,
	                            "Precisione superiore a quella prevista",
	                            valore, tipoPrevisto, "precisione " + actualPrecision, riga, colonna);
	                }

	                if (actualScale > campo.scala) {
	                    return new UtilsErroreValidazione(nomeFoglio,nomeCampo,
	                            "Scala decimale superiore a quella prevista",
	                            valore, tipoPrevisto, "scala " + actualScale, riga, colonna);
	                }
	            } catch (NumberFormatException e) {
	                return new UtilsErroreValidazione(nomeFoglio,nomeCampo,
	                        "Valore non è un numero decimale valido",
	                        valore, tipoPrevisto, "non decimale", riga, colonna);
	            }
	            break;

	        case "STRING":
	        default:
	            // niente da fare per STRING oltre la lunghezza già controllata
	    }

	    // Nessun errore
	    return null;
	}


}