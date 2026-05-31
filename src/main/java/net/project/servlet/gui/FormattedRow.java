/*
  FormattedRow.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Data creazione:

  Autore: Simone Z.

  Note:

  Modifiche:

 */

package net.project.servlet.gui;

import java.text.ChoiceFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.DBCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.errors.ParamCrash;
import net.project.mess.atdat.ATDATURLRead;
import net.project.misc.Config;
import net.project.misc.Importo;

/**
 * Questa classe viene utilizzata direttamente dalla classe RowToTemplateAdapter per formattare i campi dei DataSet_itf.
 * Il suo utilizzo viene scatenato dalla presenza della proprieta' <code>Page.nomepagina.DSX.CampiDaFormattare</code>
 * nel file di configurazione.
 * <P>
 * Tramite questa proprieta' per ogni dataset presente in una pagina possono essere specificati i campi che devono
 * essere formattati ed il modo di formattarli.
 * <P>
 * La sintassi della stringa contenuta nella proprieta' deve essere la seguente:
 * <P>
 * <p>
 * (nomecampo=?-formato&nomecampo=?-formato&....&)
 * <P>
 * <p>
 * dove:
 * <P>
 * <b>nomecampo</b> e' il nome del campo da formattare come definito nel dataset
 * <P>
 * ? rappresenta il tipo del campo: D significa data; I significa numerico; V significa divisa;A significa che la
 * colonna e' un importo e che la divisa dell'importo e' presente nella colonna il cui nome e' al posto della stringa
 * 'formato'.(La divisa deve essere indicata con i numeri 380 o 978); la formattazione avviene appunto come un importo.
 * <p>
 * <b>'formato'</b> e' la stringa che rappresenta il formato da usare
 * <P>
 * <p>
 * La formattazione avviene utilizzando un SimpleDateFormat in caso D-; un DecimalFormat se I- con V- vengono tradotti i
 * valori 380 e 978 nelle corrispondenti stringhe lette dalle proprieta' di configurazione
 * <code>Importo.Valuta.Euro Importo.Valuta.Lire</code>
 * <p>
 * Le stringhe formato devono essere adeguate a quanto accettato dalle classi SimpleDateFormat e DecimalFormat.
 * <p>
 * <p>
 * Esempio:
 * <p>
 * Page.autord.DS0.CampiDaFormattare=(DATA=D-yyyy-MM-dd&V_OP=A-C_DV&C_DV=V-&V_TOT_AUTZT=A-C_DV&)
 * <p>
 *
 * @author zorzetti
 *
 */
public class FormattedRow implements Row_itf {

    private Row_itf      _row;
    private Map          _fieldsFormat = new HashMap();
    private ATDATURLRead _formatInstruction;

    /**
     * Constructor della classe. La stringa contenente le istruzioni di formattazione viene letta tramite un
     * MsgReader_itf di tipo ATDATURLRead.
     *
     * @param format stringa con le istruzioni di formattazione per i campi
     * @param row riga da formattare
     * @throws AppCrash in caso di errore nei parametri
     */
    public FormattedRow(String format, Row_itf row) throws AppCrash {

        ErrDetector.GetInstance().param(format);
        ErrDetector.GetInstance().param(row);
        _formatInstruction = new ATDATURLRead(format.getBytes());
        _row = row;
    }

    private String formatField(Object objFormat, Object value) throws AppCrash {

        if (objFormat instanceof String) {
            return formatField((String) objFormat, value);
        }
        if (objFormat instanceof Format) {
            return formatField((Format) objFormat, value);
        }
        if (Logger.GetInstance().getLogLevel() >= 2) {
            Logger.GetInstance().log2(
                    "Class: FormattedRow; Method: formatField(Object, Object) - enrambi i cast sono falliti");
        }
        throw new ParamCrash("objFormat = " + objFormat + "; value = " + value);

    }

    private String formatField(Format format, Object value) throws AppCrash {

        try {
            String resp = "";
            if (format instanceof MessageFormat) {
                Object[] objArray = new Object[1];
                objArray[0] = value;
                resp = format.format(objArray);
            } else if (format instanceof ChoiceFormat) {
                resp = format.format(new java.math.BigDecimal(value.toString()));
            } else {
                resp = format.format(value);
            }
            return resp;

        } catch (IllegalArgumentException iae) {
            AppCrash ac = new AppCrash(iae);
            ac.logContext("FormattedRow", "format = " + format + "; value = " + value);
            throw ac;
        }
    }

    private String formatField(String format, Object value) throws AppCrash {

        try {
            String divisa = _row.getField(format).toString();
            Importo imp = new Importo(new Importo(value.toString(), divisa).getCentesimi(), divisa);
            String resp = imp.getImporto();
            return resp;

        } catch (AppCrash ac) {
            ac.logContext("FormattedRow", "format = " + format + "; value = " + value);
            throw ac;
        }
    }

    /**
     * Restituisce il valore di un campo della riga dopo averlo formattato (se necessario).
     *
     * @param java.lang String fieldName il nome del campo
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(String fieldName) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().param(fieldName);

        try {
            Object fieldValue = _row.getField(fieldName);
            if (fieldValue == null) return "";

            Object formatter = getFieldFormat(fieldName);
            return formatField(formatter, fieldValue);

        } catch (AppCrash ac) {
            ac.logContext("FormattedRow", "fieldName = " + fieldName);
            throw ac;
        }

    }

    /**
     * Restituisce il valore di un campo della riga dopo averlo forattato (se necessario).
     *
     * @param int fieldNo il numero del campo
     * @return java.lang.Object il valore del campo
     * @exception net.project.errors.ParamCrash se il parametro in ingresso è null
     * @exception net.project.errors.AppCrash in caso di errore nel recupero del valore del campo
     */
    @Override
    public Object getField(int fieldNo) throws AppCrash {

        // Controllo formale del parametro in ingresso.
        ErrDetector.GetInstance().preCond(fieldNo > 0);

        try {
            Object fieldValue = _row.getField(Integer.toString(fieldNo));
            if (fieldValue == null) return "";

            Object formatter = getFieldFormat(Integer.toString(fieldNo));
            return formatField(formatter, fieldValue);

        } catch (AppCrash ac) {
            ac.logContext("FormattedRow", "fieldNo = " + fieldNo);
            throw ac;
        }

    }

    /**
     * Conta il numero di colonne del DataSet.
     *
     * @return int numero di colonne del DataSet
     * @exception net.project.errors.AppCrash
     */
    @Override
    public int getColumnNo() throws DBCrash {

        return _row.getColumnNo();
    }

    // Metodo privato che ricava il formattatore da usare per ogni campo. Se il campo non e' elencato
    // fra quelli da formattare nelle _formatInstruction viene restituito un MessageFormat ("{0}") che
    // lascia del tutto inalterato il campo
    //
    private Object getFieldFormat(String fieldName) {

        Object formatter = _fieldsFormat.get(fieldName);
        if (formatter != null) {
            return formatter;
        }

        String format = _formatInstruction.getField(fieldName);
        Object formato = null;

        if (format.startsWith("D-")) {
            formato = new SimpleDateFormat(format.substring(2));
        }
        if (format.startsWith("I-")) {
            formato = new DecimalFormat(format.substring(2));
        }
        if (format.startsWith("V-")) {
            String[] valute = { Config.GetInstance().getProperty("Importo.Valuta.Lire"),
                    Config.GetInstance().getProperty("Importo.Valuta.Euro") };
            double[] valori = { 380, 978 };
            formato = new ChoiceFormat(valori, valute);
        }
        if (format.startsWith("A-")) {
            formato = format.substring(2);
        }
        if (formato == null) {
            formato = new MessageFormat("{0}");
        }
        _fieldsFormat.put(fieldName, formato);
        return formato;

    }

    /**
     * Setta la riga da formattare.Questo metodo viene utilizzato dalla classe RowToTemplateAdapter
     *
     * @param row
     */
    public void setRow(Row_itf row) {

        _row = row;
    }

}
