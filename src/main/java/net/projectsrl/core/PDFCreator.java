package net.projectsrl.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;

//import freemarker.template.TemplateModelRoot;

public class PDFCreator {
	public static void convertXML2PDF(File xml, File xslt, File pdf) throws IOException, FOPException, TransformerException {
		// Construct driver
		Driver driver = new Driver();

		// Setup logger
		Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
		driver.setLogger(logger);
		MessageHandler.setScreenLogger(logger);

		// Setup Renderer (output format)
		driver.setRenderer(Driver.RENDER_PDF);

		// Setup output
		OutputStream out = new java.io.FileOutputStream(pdf);
		try {
			driver.setOutputStream(out);

			// Setup XSLT
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(xslt));

			// Setup input for XSLT transformation
			Source src = new StreamSource(xml);

			// Resulting SAX events (the generated FO) must be piped through to
			// FOP
			Result res = new SAXResult(driver.getContentHandler());

			// Start XSLT transformation and FOP processing
			transformer.transform(src, res);
		} finally {
			out.close();
		}
	}

	public static void convertXML2PDF(String xmlString, File xslt, File pdf) throws IOException, FOPException, TransformerException {
		// Construct driver
		Driver driver = new Driver();

		// Setup logger
		Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
		driver.setLogger(logger);
		MessageHandler.setScreenLogger(logger);

		// Setup Renderer (output format)
		driver.setRenderer(Driver.RENDER_PDF);

		// Setup output
		OutputStream out = new java.io.FileOutputStream(pdf);
		try {
			driver.setOutputStream(out);

			// Setup XSLT
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(xslt));

			// Setup input for XSLT transformation
			// Source src = new StreamSource(xml);
			Source src = new StreamSource(xmlString);

			// Resulting SAX events (the generated FO) must be piped through to
			// FOP
			Result res = new SAXResult(driver.getContentHandler());

			// Start XSLT transformation and FOP processing
			transformer.transform(src, res);
		} finally {
			out.close();
		}
	}

	/*
	 * public static void createXML(String _query[], String myTemplate,File
	 * myXMLfile, Hashtable parametri, int numQuery) throws AppCrash { try { //
	 * INIZIO esempio freemarker TemplateModelRoot radice = new SimpleHash();
	 * Enumeration Paravalori = parametri.elements(); Enumeration Parachiavi =
	 * parametri.keys();
	 * 
	 * while (Parachiavi.hasMoreElements()) { TemplateModel valore = new
	 * SimpleScalar((String) Paravalori .nextElement()); radice.put((String)
	 * Parachiavi.nextElement(), valore); }
	 * 
	 * Connection DBConnection = ToolsLib.ApriDB();
	 * 
	 * Statement SQLStatement = DBConnection.createStatement(); for (int j=0; j <
	 * numQuery; j++) {
	 * 
	 * ResultSet rsQuery = SQLStatement.executeQuery(_query[j]);
	 *  // Inserimento di uno scalare //TemplateModel chiaveRicerca = new
	 * SimpleScalar("VALORE_SCALARE_DI_PROVA");
	 * //radice.put("TAG_SCALARE_DI_PROVA", chiaveRicerca);
	 * 
	 *  // Prima query //---------------------------------------- SimpleList
	 * listLabel = new SimpleList(); ResultSetMetaData rsmd =
	 * rsQuery.getMetaData();
	 * 
	 * int num_col = rsmd.getColumnCount(); // numero colonne
	 * 
	 * for (int i = 0; i < num_col; i++) { TemplateModel label = new
	 * SimpleScalar(rsmd.getColumnLabel(i + 1)); TemplateModel colName = new
	 * SimpleScalar(rsmd.getColumnName(i + 1)); TemplateModel colno = new
	 * SimpleScalar(Integer.toString(i + 1)); SimpleHash hashLabel = new
	 * SimpleHash(); hashLabel.put("ETICHETTA"+j, label);
	 * hashLabel.put("NOME_COLONNA"+j, colName); hashLabel.put("COLNO"+j,
	 * colno); listLabel.add(hashLabel); }
	 *  // lettura righe String[] valoreCampo = new String[num_col + 2];
	 * SimpleList listRighe = new SimpleList(); int numero_righe = 0;
	 * 
	 * while (rsQuery.next()) { numero_righe++;
	 * 
	 * TemplateModel[] campi = new TemplateModel[num_col + 2]; SimpleHash
	 * hashCampi = new SimpleHash();
	 * 
	 * for (int i = 0; i < num_col; i++) { Object campoQuery =
	 * rsQuery.getObject(i + 1);
	 * 
	 * if (campoQuery != null) { valoreCampo[i] =
	 * StringEscapeUtils.escapeXml(campoQuery.toString()); } else {
	 * valoreCampo[i] = ""; }
	 *  // gestione template per freemarker campi[i] = new
	 * SimpleScalar(valoreCampo[i].trim()); hashCampi.put("CAMPO" + i,
	 * campi[i]);
	 * 
	 * //ToolsLib.logApplication("PRIMA QUERY CAMPO" + i + " - " +
	 * valoreCampo[i]); }
	 * 
	 * valoreCampo[num_col] = Integer.toString(numero_righe);
	 * listRighe.add(hashCampi);
	 *  }
	 *  // prima bisogna fare connessione.setAutoCommit(false); //
	 * connessione.rollback(); // carico le list radice.put("RIGHE_"+j,
	 * listRighe); radice.put("LABEL_"+j, listLabel);
	 * 
	 * if (numero_righe == 0) { // gestione query senza righe }
	 *  // FINE esempio freemarker if (rsQuery != null) { rsQuery.close(); }
	 * 
	 * }// fine for
	 * 
	 * if (SQLStatement != null) { SQLStatement.close(); }
	 * 
	 * if (DBConnection != null) { DBConnection.close(); } // Visualizzazione
	 * della pagina String pagina_da_visualizzare =
	 * Config.GetInstance().getProperty("pathHTML")+
	 * Config.GetInstance().getProperty(myTemplate);
	 * 
	 * File filehtml = new File(pagina_da_visualizzare); Template temp = new
	 * Template(filehtml); FileOutputStream pippo = new
	 * FileOutputStream(myXMLfile); PrintWriter outfreemarker = new
	 * PrintWriter(pippo); temp.process(radice, outfreemarker);
	 * 
	 * outfreemarker.flush();
	 * 
	 * if (DBConnection != null) { DBConnection.close(); } } catch (SQLException
	 * e) { throw new AppCrash(e); } catch (IOException e) { throw new
	 * AppCrash(e); } }
	 */
}
