package net.projectsrl.wera.centraletermica.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;

/**
 * Esegue un dataset di config.cfg e restituisce le righe come mappe con le sole colonne richieste.
 * I parametri sostituiscono i segnaposto #NOME# come TESTO (il framework non usa bind variables): chi
 * chiama passa solo valori generati dal server o validati (interi, codici con regex), mai testo libero
 * ricevuto dal browser.
 */
final class LetturaDataset {

    private LetturaDataset() {

    }

    static List<Map<String, Object>> leggi(String dataSetName, Map<String, String> param, String... colonne)
            throws AppCrash {

        List<Map<String, Object>> righe = new ArrayList<Map<String, Object>>();
        DataSet_itf dataSet = null;

        try {
            dataSet = DataSetFactory.getInstance().makeDataSet("", dataSetName);
            dataSet.setParam(param);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                Map<String, Object> riga = new LinkedHashMap<String, Object>();
                for (String colonna : colonne) {
                    riga.put(colonna, dbRow.getField(colonna));
                }
                righe.add(riga);
            }
        } catch (AppCrash ac) {
            ac.logContext(LetturaDataset.class.getName(), "Errore nel dataset " + dataSetName);
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext(LetturaDataset.class.getName(), "Errore nella close del dataset " + dataSetName);
                }
            }
        }

        return righe;
    }

    static Map<String, Object> leggiUna(String dataSetName, Map<String, String> param, String... colonne)
            throws AppCrash {

        List<Map<String, Object>> righe = leggi(dataSetName, param, colonne);
        return righe.isEmpty() ? null : righe.get(0);
    }

    /** Intero da una colonna (Integer, Long, BigDecimal...), null se assente. */
    static Integer intero(Map<String, Object> riga, String colonna) {

        if (riga == null) {
            return null;
        }
        Object valore = riga.get(colonna);
        if (valore instanceof Number) {
            return Integer.valueOf(((Number) valore).intValue());
        }
        if (valore instanceof String && ((String) valore).trim().matches("-?[0-9]{1,9}")) {
            return Integer.valueOf(((String) valore).trim());
        }
        return null;
    }

    /** Booleano da una colonna boolean di Postgres (o 't'/'S'). */
    static boolean vero(Map<String, Object> riga, String colonna) {

        if (riga == null) {
            return false;
        }
        Object valore = riga.get(colonna);
        if (valore instanceof Boolean) {
            return ((Boolean) valore).booleanValue();
        }
        if (valore instanceof String) {
            String s = ((String) valore).trim();
            return s.equalsIgnoreCase("t") || s.equalsIgnoreCase("true") || s.equalsIgnoreCase("S");
        }
        return false;
    }

    static String testo(Map<String, Object> riga, String colonna) {

        if (riga == null || riga.get(colonna) == null) {
            return null;
        }
        return riga.get(colonna).toString().trim();
    }
}
