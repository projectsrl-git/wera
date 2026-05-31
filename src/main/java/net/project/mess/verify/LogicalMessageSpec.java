/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore:

  Note:

 */

package net.project.mess.verify;

import java.util.Iterator;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.mess.LogicalMessageSpecReader;
import net.project.mess.MsgReader_itf;
import net.project.mess.verify.datatype.DiagnosiFieldVerifier_itf;
import net.project.mess.verify.datatype.FieldVerifier_itf;
import net.project.misc.Config;

/**
 * Questa classe LogicalMessageSpecReader e rappresenta la specifica logica di un messaggio in grado di verificare dei
 * messaggi. Per specifica si intende l'insieme dei campi che formano il messaggio divisi in obbligatori e facoltativi.
 * I nomi dei campi sono presi da un vocabolario. Tutte le caratteristiche dei campi sono contenute nel vocabolario. Le
 * specifiche logiche dei messaggi sono contenute in file di testo che sono cosi' composti: colonna 1: nome del campo
 * <P>
 * colonna 2: O/F obbligatorio-facoltativo
 * <P>
 *
 */
public class LogicalMessageSpec extends LogicalMessageSpecReader {

    private static final String TRUE                     = "true";
    public static final String  DIAGNOSI_CAMPO_MANCANTE  = "Manca";
    public static final String  DIAGNOSI_CAMPO_SBAGLIATO = "Sbagliato";
    private boolean             _newErrorMsgFormat       = false;

    private boolean             _strictVerify            = false;

    /**
     * Costruisce una specifica a partire dal nome (tipo) del messaggio. Legge la proprieta'
     * <code>verifier.messaggeDir</code> per recuperare la directory dove si trovano i file di specifica. Questa
     * proprieta' deve esistere e non essere vuota. Il nome del file della specifica per il messaggio viene letto dalla
     * proprieta' <code>verifier.(messageType).messagge</code>
     *
     * @param messageType String tipo messaggio
     * @throws AppCrash
     */
    public LogicalMessageSpec(String messageType) throws AppCrash {

        super(messageType);
        if (Config.GetInstance().getProperty("verifier.useNewErrorFormat", "false").equalsIgnoreCase(TRUE)) {
            _newErrorMsgFormat = true;
        }

        if (Config.GetInstance().getProperty("verifier.strictVerify", "false").equalsIgnoreCase(TRUE)) {
            _strictVerify = true;
        }
    }

    /**
     * Verifica se il messaggio passato rispetta le specifiche. Il risultato della verifica del messaggi e' una stringa
     * nel formato seguente:
     * <p>
     * nomcampo||diagnosi$$nomcampo||diagnosi$$...
     * <p>
     * <p>
     * 
     * @param fields obbl
     * @param msgReader MsgReader_itf
     * @param vocabulary Vocabulary
     * @param obbl
     *
     * @return String Messaggio di errore
     *
     * @exception AppCrash
     */
    private String verify(Iterator fields, MsgReader_itf msgReader, Vocabulary vocabulary, boolean obbl)
            throws AppCrash {

        // Controllo esistenze dei campi
        // Fields obbligatori:
        // 1. controllo se esistono nel msgReader
        // 2. controllo la loro validità
        StringBuffer errors = new StringBuffer();
        String nameField = null;
        String value = null;

        try {

            while (fields.hasNext()) {
                nameField = (String) fields.next();
                value = msgReader.getField(nameField);

                FieldVerifier_itf fieldVerify = vocabulary.getFieldVerifier(nameField);
                ErrDetector.GetInstance().param(fieldVerify);

                String valtrim = "";
                if (value != null) {
                    if (_strictVerify == false && value.length() > 0) {
                        valtrim = value.trim();
                        if (valtrim.length() == 0) {
                            Logger.GetInstance().log0("Verifier STRICTWARNING - " + nameField + " " + obbl);
                        }
                    } else {
                        valtrim = value;
                    }
                }

                if ((value == null) || (valtrim.length() == 0)) {

                    if (obbl == true) {
                        // Errore: non esiste in msgReader
                        Logger.GetInstance().log3("Manca - " + nameField);
                        if (isNewErrorMsgFormat()) {
                            errors.append(nameField).append("||").append(DIAGNOSI_CAMPO_MANCANTE).append("$$");
                        } else {
                            errors.append(" - Manca ").append(nameField);
                        }
                    }

                    // passo ad un altro campo
                    continue;
                }

                if (fieldVerify instanceof DiagnosiFieldVerifier_itf) {
                    // Se possono ottenere la diagnosi allora verifico con diagnosi
                    String dia = ((DiagnosiFieldVerifier_itf) fieldVerify).diagnose(value);
                    if (dia != null) {
                        Logger.GetInstance().log3("Diagnosi - " + nameField + " - " + dia);
                        errors.append(nameField).append("||").append(dia).append("$$");
                    }
                } else {
                    // altrimenti utilizzo il tradizionale verify booleano
                    if (fieldVerify.verify(value) == false) {
                        Logger.GetInstance().log3("Sbagliato - " + nameField);
                        if (isNewErrorMsgFormat()) {
                            errors.append(nameField).append("||").append(DIAGNOSI_CAMPO_SBAGLIATO).append("$$");
                        } else {
                            errors.append(" - Sbagliato ").append(nameField);
                        }
                    }
                }
            }
        } catch (AppCrash crash) {
            crash.logContext("LogicalMessageSpec", "Nome campo " + nameField + " - valore campo " + value
                    + " - Errori " + errors.toString());
            throw crash;
        }

        return errors.toString();
    }

    /**
     * Verifica se il messaggio passato rispetta le specifiche. Per il check dei campi viene usato il vocabolario
     * passato nei parametri.
     *
     * @param msgReader MsgReader_itf
     * @param vocabulary Vocabulary
     *
     * @return String Messaggio di errore
     *
     * @exception AppCrash
     */
    public String verifyMessage(MsgReader_itf msgReader, Vocabulary vocabulary) throws AppCrash {

        // Fields obbligatori:
        String result = verify(getMandatoryFields(), msgReader, vocabulary, true);

        // Se result è null non ci sono stati errori
        if ((result == null) || result.equals("")) {

            // Fields facoltativi
            result = verify(getFacoltativeFields(), msgReader, vocabulary, false);
        }

        return result;
    }

    /**
     * @return Ritorna il campo newErrorMsgFormat.
     */
    protected boolean isNewErrorMsgFormat() {

        return _newErrorMsgFormat;
    }

}