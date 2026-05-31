
package net.projectsrl.json;

import java.io.PrintWriter;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import project.misc.Utils;

public class FunctionJSON extends FunctionProjectWebApp_base {

    private static final String LOWER = "lower";
    private static final String UPPER = "upper";

    public FunctionJSON(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        String dataSetName = req.getField("dataset");
        String valoreCampo = req.getField("valore").trim();
        String nomeCampo = req.getField("campo");
        String tipo = req.getField("tipo");
        String selected = req.getField("selected");
        String whereCond = req.getField("WHERECONDITION");

        whereCond = whereCond.replace("___", "%");

        if (Utils.IsEmpty(dataSetName)) {
            return;
        }

        DataSet_itf dataSet = null;

        WebAppUtils.composeWhereConditionJSON(req, templateData, nomeCampo, valoreCampo, whereCond);

        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", dataSetName);
            dataSet.setParam(templateData);

            dataSet.open();

            StringBuilder jsonTail = new StringBuilder();

            String[] columnNames = dataSet.getColumnNames();

            int count = 0;

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                boolean isSelected = false;

                jsonTail.append("{");
                for (int i = 0; i < columnNames.length; i++) {
                    if (i > 0) {
                        jsonTail.append(",");
                    }

                    Object filedValueObject = dbRow.getField(columnNames[i]);

                    String filedValue = "";

                    if (filedValueObject != null) {

                        if (filedValueObject instanceof String) {
                            filedValue = (String) filedValueObject;
                            if (filedValue.contains("\"")) {
                                filedValue = filedValue.replaceAll("\"", "'");
                            }

                        }

                        filedValue = filedValueObject.toString();

                        if (!isSelected && Util.IsNotEmpty(selected)) {
                            if (selected.contains(",")) {
                                String selezionati = selected + ",";
                                int numeroSelezionati = StringUtils.countMatches(selezionati, ",");
                                String codice = selected;

                                for (int z = 0; z < numeroSelezionati; z++) {
                                    codice = selezionati.substring(0, selezionati.indexOf(","));
                                    isSelected = filedValue.equals(codice);
                                    selezionati = selezionati.substring(selezionati.indexOf(",") + 1);

                                    if (isSelected) {
                                        jsonTail.append("\"selected\":true,");
                                    }

                                }
                            } else {
                                isSelected = filedValue.equals(selected);
                            }

                        }

                    }

                    String escapedFieldValue = escapeJSONQuote(filedValue);
                    if (escapedFieldValue.contains("ò") || escapedFieldValue.contains("à")
                            || escapedFieldValue.contains("è") || escapedFieldValue.contains("ì")
                            || escapedFieldValue.contains("ù")) {
                        escapedFieldValue = StringEscapeUtils.escapeHtml(escapedFieldValue);
                    }

                    if (filedValueObject instanceof Number) {
                        jsonTail.append("\"" + getColumnName(columnNames, i) + "\":" + escapedFieldValue);
                    } else if (filedValueObject instanceof Boolean) {
                        jsonTail.append("\"" + getColumnName(columnNames, i) + "\":" + escapedFieldValue);
                    } else {
                        jsonTail.append("\"" + getColumnName(columnNames, i) + "\":\"" + escapedFieldValue + "\"");
                    }

                }

                if (isSelected) {
                    jsonTail.append(",\"selected\":true");
                }

                addExtraColumn(dbRow, jsonTail);

                jsonTail.append("}");
                count++;

                if (dataSet.hasMoreElements()) {
                    jsonTail.append(",");
                }
            }

            if (tipo.equals("")) {
                jsonTail.append("]}");
            } else {
                jsonTail.append("]");
            }

            StringBuilder json = new StringBuilder();

            if (tipo.equals("")) {
                json.append("{\"total\":\"" + count + "\",\"rows\":[");
            } else {
                json.append("[");
            }
            json.append(jsonTail);

            PrintWriter out = res.getWriter();
            out.println(json.toString());
            out.close();

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Errore nel dataset:" + dataSetName);
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

    }

    protected void addExtraColumn(Row_itf dbRow, StringBuilder jsonTail) throws AppCrash {

        // default nothing to do

    }

    /**
     * converte in maiuscolo o minuscolo sul tracciato JSON, i nomi dei campi delle query<br>
     * Serve per poter legare i nomi dei campi JSON con i nomi dei campi nel th delle table html
     * 
     * @param String[] columnNames campi della query
     * @param int i indice del campo corrente
     * 
     * @return String columnName
     */
    private String getColumnName(String[] columnNames, int i) {

        String columnName = columnNames[i];
        String jsonColumnNameCase = Config.GetInstance().getProperty("JSON.Query.ColumnNameCase", LOWER);

        if (jsonColumnNameCase.equalsIgnoreCase(LOWER)) {
            columnName = columnName.toLowerCase();
        } else if (jsonColumnNameCase.equalsIgnoreCase(UPPER)) {
            columnName = columnName.toUpperCase();
        } else {
            // noting to do
        }

        return columnName;
    }

    private String escapeJSONQuote(String string) {

        if (string == null || string.length() == 0) {
            return string;
        }

        char c = 0;
        int i;
        int len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    // if (b == '<') {
                    sb.append('\\');
                    // }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }

        return sb.toString();
    }

    @Override
    protected void detectLastActivity(SsbServletRequest req, Map<String, Object> map) {
        //noting to do
    }

}
