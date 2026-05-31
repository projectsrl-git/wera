
package net.projectsrl.webapp.core;

import java.io.IOException;
import java.io.PrintWriter;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;

public class FunctionSaveGrid extends FunctionProjectWebApp_base {

    public enum OperationType {

        INSERT, UPDATE, DELETE
    }

    private static final String DAO_CLASS      = "daoClass";

    private static final String INSERT_ARRAY   = "insertArray[]";
    private static final String UPDATE_ARRAY   = "updateArray[]";
    private static final String DELETE_ARRAY   = "deleteArray[]";

    private static final String COLUMNS_ARRAYS = "columns[]";

    public FunctionSaveGrid(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public boolean isAuthenticationRequired() {

        return false;
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String[] insertedRows = req.getParameterValues(INSERT_ARRAY);
        String[] updatedRows = req.getParameterValues(UPDATE_ARRAY);
        String[] deletedRows = req.getParameterValues(DELETE_ARRAY);
        String[] columns = req.getParameterValues(COLUMNS_ARRAYS);
        String daoClassName = req.getParameter(DAO_CLASS);

        int changesDone = 0;
        boolean result = false;

        try {

            ErrDetector.GetInstance().param(columns != null, "columns null");
            ErrDetector.GetInstance().param(daoClassName != null, "daoClassName null");

            if (insertedRows != null && insertedRows.length != 0) {
                changesDone += commitChanges(insertedRows, columns, daoClassName, OperationType.INSERT);
                result = true;
            }

            if (updatedRows != null && updatedRows.length != 0) {
                changesDone += changesDone + commitChanges(updatedRows, columns, daoClassName, OperationType.UPDATE);
                result = true;
            }

            if (deletedRows != null && deletedRows.length != 0) {
                changesDone += changesDone + commitChanges(deletedRows, columns, daoClassName, OperationType.DELETE);
                result = true;
            }

        } catch (Throwable th) {

            result = false;

            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "errore salvataggio dati grid - insertedRows:" + insertedRows
                    + " - updatedRows:" + updatedRows + " - deletedRows:" + deletedRows + " - columns:" + columns
                    + " - daoClassName:" + daoClassName);
            throw ac;

        } finally {

            sendResponseJSON(res, result, changesDone);
        }

    }

    /**
     * Questo metodo
     * 
     * @param res
     */
    private void sendResponseJSON(SsbServletResponse res, boolean result, int changesCount) {

        try {
            res.setContentType("application/json");
            PrintWriter out = res.getWriter();
            out.println("{\"result\":" + result + ",\"changesCount\":" + changesCount + "}");
            out.close();

        } catch (IOException e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }

    /*
     * salva le modifiche su db
     */
    private int commitChanges(String[] changes, String[] columns, String daoClassName, OperationType operationType)
            throws AppCrash {

        int changesCount = 0;

        try {

            Class<?> daoClass = Class.forName(daoClassName);
            PjNDAO_base PjDAO_base = null;

            int countColumn = 0;

            for (int i = 0; i < changes.length; i++) {

                countColumn++;

                // conto le colonne e alla prima di ogni serie intanzio in nuovo DAO
                if (countColumn == 1) {
                    PjDAO_base = (PjNDAO_base) daoClass.newInstance();
                }

                String fieldName = columns[i - (int) Math.floor(i / columns.length) * columns.length];
                String fieldValue = changes[i];

                // valorizzo solo i campi corrispondenti a colonne valide
                if (PjDAO_base.isValidField(fieldName)) {
                    PjDAO_base.setAttribute(fieldName, fieldValue);
                    Logger.GetInstance().log3(fieldName + "-" + fieldValue + "\n");
                }

                // conto le colonne e all'ultima di ogni serie faccio update del DAO
                if (countColumn == columns.length) {

                    if (operationType == OperationType.INSERT) {
                        PjDAO_base.insert();

                    } else if (operationType == OperationType.UPDATE) {
                        PjDAO_base.update();

                    } else if (operationType == OperationType.DELETE) {
                        PjDAO_base.delete();

                    }

                    changesCount++;
                    countColumn = 0;
                }

            }

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "errore nell'aggiornamento di datagrid - changes:" + changes
                    + " - gridRows:" + columns + " - daoClassName:" + daoClassName);
            throw ac;
        }

        return changesCount;
    }
}
