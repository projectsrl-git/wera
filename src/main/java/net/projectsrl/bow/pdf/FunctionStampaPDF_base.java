
package net.projectsrl.bow.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.pdf.CreatePDF;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;

public abstract class FunctionStampaPDF_base extends FunctionProjectWebApp_base {

    public static final String OUTPUT_PATH = WebAppConstants_itf.OUTPUT_PATH;

    public FunctionStampaPDF_base() {

        super();
    }

    public FunctionStampaPDF_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    protected abstract String creaFilePdf(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash;

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String directory = _applicationSrv.getRoot() + OUTPUT_PATH;
        String filename = creaFilePdf(req, userInfo);

        File file = new File(filename);
        String contentType = getContentType(filename);
        System.out.println(contentType);
        res.setContentType(contentType);
        res.setHeader("Content-Disposition", "attachment; filename=" + filename.replace(directory, ""));
        int length = (int) file.length();

        if (length > Integer.MAX_VALUE) {
        }

        byte[] bytes = new byte[length];

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);

            fin.read(bytes);

            ServletOutputStream os = res.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Throwable ac) {
            new AppCrash(ac);
        }

    }

    protected String getContentType(String fileName) {

        String extension[] = { // File Extensions
                "txt", // 0 - plain text
                "htm", // 1 - hypertext
                "jpg", // 2 - JPEG image
                "png", // 2 - JPEG image
                "gif", // 3 - gif image
                "pdf", // 4 - adobe pdf
                "doc", // 5 - Microsoft Word
                "docx", }; // you can add more
        String mimeType[] = { // mime types
                "text/plain", // 0 - plain text
                "text/html", // 1 - hypertext
                "image/jpg", // 2 - image
                "image/jpg", // 2 - image
                "image/gif", // 3 - image
                "application/pdf", // 4 - Adobe pdf
                "application/msword", // 5 - Microsoft Word
                "application/msword", // 5 - Microsoft Word
        }, // you can add more
                contentType = "text/html"; // default type
        // dot + file extension
        int dotPosition = fileName.lastIndexOf('.');
        // get file extension
        String fileExtension = fileName.substring(dotPosition + 1);
        // match mime type to extension
        for (int index = 0; index < mimeType.length; index++) {
            if (fileExtension.equalsIgnoreCase(extension[index])) {
                contentType = mimeType[index];
                break;
            }
        }
        return contentType;
    }

    protected void fillMapFromRow(Map<String, Object> data, Row_itf dbRow, String[] columnNames) throws AppCrash {

        if (dbRow == null) {
            return;
        }

        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i] != null) {
                String fieldName = columnNames[i].toUpperCase();

                Object fieldValue = dbRow.getField(fieldName);

                if (fieldValue == null) {
                    continue;
                }

                putDataSetFieldInMap(data, fieldName, fieldValue);
            }

        }

    }
    
    
    protected void fillMapFromRowRipartizioni(Map<String, Object> data, Row_itf dbRow, String[] columnNames, Integer conta, Integer contaDettaglio, Boolean specific) throws AppCrash {

        if (dbRow == null) {
            return;
        }

        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i] != null) {
                String fieldName = columnNames[i].toUpperCase()+"_"+conta.toString();
                String fieldNameDettaglio = columnNames[i].toUpperCase()+"_"+conta.toString()+"_"+contaDettaglio.toString();
                String fieldNameSpecific = columnNames[i].toUpperCase()+"_"+conta.toString()+"_S";
                String fieldNameTripleCount = columnNames[i].toUpperCase()+"_"+conta.toString()+"_"+conta.toString()+"_"+conta.toString();

                Object fieldValue = dbRow.getField(columnNames[i].toUpperCase());
                
                if (fieldValue == null) {
                    continue;
                }
                
                Object fieldValueClass= fieldValue.getClass();
                if(fieldValueClass == BigDecimal.class){
                	//fieldValue=fieldValue.toString().replace(".", ",");
                	
                	NumberFormat nf = NumberFormat.getNumberInstance(new Locale("it","IT"));
                	nf.setMinimumFractionDigits(2);
                	nf.setMaximumFractionDigits(2);
                	DecimalFormat df = (DecimalFormat)nf;
                	fieldValue=df.format(fieldValue);
                }
                
                
                if (specific){
                	putDataSetFieldInMap(data, fieldNameSpecific, fieldValue);
                }else{
                	putDataSetFieldInMap(data, fieldName, fieldValue);
                	putDataSetFieldInMap(data, fieldNameTripleCount, fieldValue);
                	putDataSetFieldInMap(data, fieldNameDettaglio, fieldValue);
                }
                
                
                
                
            }

        }

    }
    
    
    
    protected void fillMapFromRowRipartizioniSingolo(Map<String, Object> data, Row_itf dbRow, String[] columnNames, Integer conta, Integer contaDettaglio) throws AppCrash {

        if (dbRow == null) {
            return;
        }

        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i] != null) {
                String fieldName = columnNames[i].toUpperCase()+"_"+conta.toString()+"_"+contaDettaglio.toString();

                Object fieldValue = dbRow.getField(columnNames[i].toUpperCase());

                if (fieldValue == null) {
                    continue;
                }

                putDataSetFieldInMap(data, fieldName, fieldValue);
            }

        }

    }


    protected void putDataSetFieldInMap(Map<String, Object> data, String fieldName, Object fieldValue) {

        data.put(fieldName, fieldValue);
    }


    protected void fillDataFromDataSet(String dsName, Map<String, Object> data, Map<String, String> param)
            throws AppCrash {

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            dataSet.setParam(param);
            dataSet.open();

            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow == null) {
                    return;
                }

                String[] columnNames = dataSet.getColumnNames();

                Map<String, Object> rowMap = new HashMap<>();

                fillRowMapFromDataSet(data, dbRow, columnNames, rowMap);

                rows.add(rowMap);
            }

            data.put(CreatePDF.LIST_OF_ROWS, rows);

            data.put(CreatePDF.DATASET_SIZE, rows.size());

        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + dsName);
                }
            }
        }

    }

    protected void fillRowMapFromDataSet(Map<String, Object> data, Row_itf dbRow, String[] columnNames, Map<String, Object> rowMap)
            throws AppCrash {

        for (int i = 0; i < columnNames.length; i++) {
            if (columnNames[i] != null) {
                String fieldName = columnNames[i].toUpperCase();

                Object fieldValue = dbRow.getField(fieldName);

                if (fieldValue == null) {
                    continue;
                }

                rowMap.put(fieldName, fieldValue);
            }

        }
    }

    protected void fillDataFromSingleRowDataSet(String dsName, Map<String, Object> data, Map<String, String> param)
            throws AppCrash {

        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            dataSet.setParam(param);
            dataSet.open();

            if (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                fillMapFromRow(data, dbRow, dataSet.getColumnNames());
            }

        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + dsName);
                }
            }
        }

    }
    
    
    
   
    
    
    
    
    protected void fillDataFromSingleRowDataSetRipartizioniSingolo(String dsName, Map<String, Object> data, Map<String, String> param, Integer conta, Integer contaDettaglio)
            throws AppCrash {

    	
        DataSet_itf dataSet = null;

        DataSetFactory dsFactory = DataSetFactory.getInstance();
        
        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            dataSet.setParam(param);
            dataSet.open();

            if (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                fillMapFromRowRipartizioniSingolo(data, dbRow, dataSet.getColumnNames(),conta,contaDettaglio);
            }

        } catch (AppCrash ac) {
            ac.logContext(this.getClass().getName(),
                    "Errore nella ricerca dell'ultimo progressivo del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset " + dsName);
                }
            }
        }

    }
    

}