
package net.projectsrl.csv;

import java.io.OutputStream;
import java.util.Map;

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
import net.projectsrl.webapp.core.WebAppUtils;
import project.misc.Utils;

public class FunctionCsv extends FunctionProjectWebApp_base {

    public FunctionCsv(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String csvFileName = req.getField("csvfilename");
        ErrDetector.GetInstance().param(!Utils.IsEmpty(csvFileName), "parametro csvfilename non valorizzato");

        String dataSetName = req.getField("dataset");
        ErrDetector.GetInstance().param(!Utils.IsEmpty(dataSetName), "parametro dataset non valorizzato");

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        WebAppUtils.composeWhereCondition(req, templateData);

        DataSet_itf dataSet = null;

        try {

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet("", dataSetName);
            dataSet.setParam(templateData);

            dataSet.open();

            StringBuilder csv = new StringBuilder();

            String[] columnNames = dataSet.getColumnNames();

            for (int i = 0; i < columnNames.length; i++) {
                if (i > 0) {
                    csv.append(";");
                }

                csv.append(columnNames[i]);
            }

            csv.append("\n");

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                for (int i = 0; i < columnNames.length; i++) {
                    if (i > 0) {
                        csv.append(";");
                    }

                    Object filedValueObject = dbRow.getField(columnNames[i]);

                    String filedValue = "";

                    if (filedValueObject != null) {

                        if (filedValueObject instanceof String) {
                            filedValue = (String) filedValueObject;
                        } else
                        if (filedValueObject instanceof Number) {
                            filedValue = filedValueObject.toString();
                            filedValue=filedValue.replace(".",",");
                        } else
                        filedValue = filedValueObject.toString();

                    }
                    csv.append(filedValue);
                }

                if (dataSet.hasMoreElements()) {
                    csv.append("\n");
                }
            }

            res.setContentType("application/donwload");
            res.setHeader("Content-Disposition", "attachment; filename=\"" + csvFileName + ".csv\"");
            OutputStream outputStream = res.getOutputStream();
            outputStream.write(csv.toString().getBytes());
            outputStream.flush();
            outputStream.close();

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

}
