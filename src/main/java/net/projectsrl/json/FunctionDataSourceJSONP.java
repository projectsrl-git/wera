
package net.projectsrl.json;

import java.io.PrintWriter;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import project.misc.Utils;

public class FunctionDataSourceJSONP extends FunctionProjectWebApp_base {

    public FunctionDataSourceJSONP(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }
    
    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {
        elabora(req,res,userInfo);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        String dataSetName = req.getField("dataset");
        String colums = req.getField("columns");
        String callback = req.getField("callback");
        String draw = req.getField("draw");

        ErrDetector.GetInstance().param(Utils.IsNotEmpty(dataSetName), "dataset is empty");
        ErrDetector.GetInstance().param(Utils.IsNotEmpty(colums), "columns is empty");

        String[] outputColumns = colums.split(",");

        DataSet_itf dataSet = null;

        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", dataSetName);
            dataSet.setParam(templateData);

            dataSet.open();

            StringBuilder jsonTail = new StringBuilder();

            int recordsTotal = 0;

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                jsonTail.append("[");

                for (int i = 0; i < outputColumns.length; i++) {

                    if (i > 0) {
                        jsonTail.append(",");
                    }

                    Object filedValueObject = dbRow.getField(outputColumns[i]);

                    String filedValue = "";

                    if (filedValueObject != null) {

                        if (filedValueObject instanceof String) {
                            filedValue = (String) filedValueObject;
                            if (filedValue.contains("\"")) {
                                filedValue = filedValue.replaceAll("\"", "'");
                            }

                        }

                        filedValue = filedValueObject.toString();

                    }

                    String escapedFieldValue = escapeJSONQuote(filedValue);
                    if (escapedFieldValue.contains("ò") || escapedFieldValue.contains("à")
                            || escapedFieldValue.contains("è") || escapedFieldValue.contains("ì")
                            || escapedFieldValue.contains("ù")) {
                        escapedFieldValue = StringEscapeUtils.escapeHtml(escapedFieldValue);
                    }

                    jsonTail.append("\"" + escapedFieldValue + "\"");

                }

                addExtraColumn(dbRow, jsonTail);

                jsonTail.append("]");
                recordsTotal++;

                if (dataSet.hasMoreElements()) {
                    jsonTail.append(",");
                }
            }

            StringBuilder json = new StringBuilder();

            json.append(callback);
            json.append("({");
            json.append("\"draw\"");
            json.append(":");
            json.append(draw);
            json.append(",");
            json.append("\"recordsTotal\"");
            json.append(":");
            json.append(recordsTotal);
            json.append(",");
            json.append("\"recordsFiltered\"");
            json.append(":");
            json.append(recordsTotal);
            json.append(",");
            json.append("\"data\"");
            json.append(":");
            json.append("[");
            json.append(jsonTail);
            json.append("]});");

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

    protected void formatWhereCondition(Map<String, Object> templateData, String whereCondition) {

        if (!whereCondition.equals("")) {
            if (whereCondition.startsWith(" AND ")) {
                whereCondition = " " + whereCondition.substring(4);
            }
            whereCondition = " WHERE " + whereCondition;
        }

        templateData.put(WebAppConstants_itf.WHERECONDITION, whereCondition);
    }

    protected void addExtraColumn(Row_itf dbRow, StringBuilder jsonTail) throws AppCrash {

        // default nothing to do

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

        // noting to do
    }

}
