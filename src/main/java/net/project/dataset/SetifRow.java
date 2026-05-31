/*
  SetifRow.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Anna L.

  Note:

  Modifiche:

 */

package net.project.dataset;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.ParamCrash;
import net.project.mess.fixlen.FixRead;

/**
 * Classe che rappresenta una riga ossia una disposizione di un oggetto SetifDataSet.
 */
public class SetifRow implements Row_itf {

    // Map contenente la disposizione
    private Map _disposizione = null;

    /**
     * Costruttore.
     * 
     * @param disposizione java.util.Hashtable la disposizione
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     */
    public SetifRow(Map disposizione) throws ParamCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().param(disposizione);

        _disposizione = disposizione;
    }

    /**
     * Restituisce il valore di un campo del record o l'insieme dei record del tipo richiesto.
     * 
     * @param java.lang String fieldName il nome del campo nel formato [tipoRecord-nomeCampo] oppure nel formato
     *            [tipoRecord] in caso di record multipli
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(String fieldName) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().param(fieldName);
        Object obj = null;

        try {
            if (fieldName.indexOf("-") == -1) {
                // è stato richiesto un insieme di record multipli di tipo "fieldName" -> caso [tipoRecord]
                obj = _disposizione.get(fieldName);
                if (obj == null) return obj;
                // l'oggetto deve essere un dataset
                if (!(obj instanceof net.project.dataset.CollectionDataSet)) {
                    throw new AppCrash("SetifRow - Il record " + fieldName
                            + " è singolo: Specificare il nome del campo!");
                }
                return obj;
            }

            // è stato richiesto il campo di un record singolo -> caso [tipoRecord-nomeCampo]
            obj = _disposizione.get(fieldName.substring(0, 2));
            if (obj == null) return obj;
            // l'oggetto deve essere un FixRead
            if (!(obj instanceof net.project.mess.fixlen.FixRead)) {
                throw new AppCrash("SetifRow - Il record " + fieldName + " è multiplo");
            }
            return ((FixRead) obj).getField(fieldName.substring(3)).trim();

        } catch (AppCrash ap) {
            ap.logContext("SetifRow", "fieldName = " + fieldName);
            throw ap;
        }
    }

    /**
     * Non implementato.
     * 
     * @param int fieldNo il numero del campo
     * @return net.project.dataset.CollectionDataSet l'insieme dei record del tipo richiesto
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(int fieldNo) throws AppCrash {

        throw new NumberFormatException("SetifRow - metodo getField(int) non implementato!");
    }

    /**
     * Conta il numero di colonne del DataSet
     * 
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.DBCrash
     */
    @Override
    public int getColumnNo() throws DBCrash {

        return 1;
    }
}
