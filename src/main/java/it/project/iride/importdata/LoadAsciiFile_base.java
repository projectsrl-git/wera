
package it.project.iride.importdata;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;

public abstract class LoadAsciiFile_base {

    private ArrayList<String> _fieldNames  = new ArrayList<String>();
    private ArrayList<String> _fieldValues = new ArrayList<String>();

    public abstract void setDefaultData(HashMap<String, String> defaultData) throws AppCrash;

    public void loadData(String user, String absolutePathFileName) throws AppCrash {

        String text = "";

        DBTransaction dbTransaction = new DBTransaction();

        try {

            InputStream ist = new FileInputStream(absolutePathFileName);
            BufferedReader istream = new BufferedReader(new InputStreamReader(ist));

            int rowCounter = 0;

            while (true) {
                text = istream.readLine();

                if (text == null) {
                    break;
                }

                if (text.equals("")) {
                    continue;
                }

                if (!text.contains(getSeparator())) {
                    continue;
                }

                if (text.contains(";;")) {
                    text = text.replace(";;", "; ;");
                }

                rowCounter++;

                if (rowCounter == 1) {
                    setFieldNames(text);
                    continue;
                }

                setFieldValues(text);
                store(dbTransaction, user);
                dbTransaction.commit();

            }

        } catch (Throwable e) {
            if (dbTransaction != null) {
                dbTransaction.rollBack();
            }
            new AppCrash(e).logContext("LoadAsciiFile_base::", "CARICAMENTO INTERROTTO - riga " + text);
        } finally {
            if (dbTransaction != null) {
                dbTransaction.end();
            }
        }

    }

    protected void setFieldNames(String text) {

        int index = 0;
        StringTokenizer campi = new StringTokenizer(text, getSeparator());
        while (campi.hasMoreTokens()) {
            String singoloCampo = campi.nextToken();
            getFieldNames().add(singoloCampo);
            index++;
        }
    }

    protected void setFieldValues(String text) {

        int index = 0;
        _fieldValues = new ArrayList<String>();
        StringTokenizer campi = new StringTokenizer(text, getSeparator());
        while (campi.hasMoreTokens()) {
            String singoloCampo = campi.nextToken();
            getFieldValues().add(index, singoloCampo);
            index++;
        }

    }

    protected ArrayList<String> getFieldNames() {

        return _fieldNames;
    }

    protected void setFieldNames(ArrayList<String> names) {

        _fieldNames = names;
    }

    protected ArrayList<String> getFieldValues() {

        return _fieldValues;
    }

    protected void setFieldValues(ArrayList<String> values) {

        _fieldValues = values;
    }

    protected abstract void store(DBTransaction dbtransaction, String user) throws AppCrash;

    protected void store(DBTransaction dbtransaction) throws AppCrash {

        store(dbtransaction, "");
    }

    protected abstract String getSeparator();

}
