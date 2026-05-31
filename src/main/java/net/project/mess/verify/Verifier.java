/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Assunta Ciervo

  Note:

 */

package net.project.mess.verify;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.errors.Logger;
import net.project.mess.MsgReader_itf;
import net.project.mess.VerifiedMessage;
import net.project.misc.Config;

/**
 * Classe singleton utilizzata per la verifica del messaggio. Per rappresentare la specifica logica di ogni singolo
 * messaggio viene utilizzata la classe indicata nella proprieta' di configurazione
 * <code>verifier.LogicalMessageSpec</code> se presente, altrimenti la classe
 * net.project.mess.verify.LogicalMessageSpec. Per specifica si intende l'insieme dei campi che formano il messaggio
 * divisi in obbligatori e facoltativi. I nomi dei campi sono presi da un vocabolario. Tutte le caratteristiche dei
 * campi sono contenute nel vocabolario.
 * <P>
 * La verifica di un messaggio consiste in due o tre passi logici:
 * <P>
 * verifica che tutti i campi obbligatori siano presenti
 * <P>
 * verifica che il valore dei campi presenti (obbl. e facolt.) sia conforme a quando dettato dal vocabolario
 * <P>
 * eventuale verifica di relazioni particolari fra i campi presenti. Questo avviene se come classe per le specifiche
 * logica si utilizza SemanticLogicalMessageSpec
 * <P>
 * <P>
 * Il vocabolario da utilizzare per validare i campi di un messaggio puo' differire da messaggio a messaggio.
 *
 */
public class Verifier {

    public static final String MESSAGGIO_ESATTO = "MESSAGGIO ESATTO";

    private static Verifier    _Instance;
    private static Map         _vocabolaries;
    private static Map         _messages;
    private static String      _messageSpecClass;

    /**
     * Verifier constructor.
     */
    private Verifier() {

        super();

        // Istanzio Hashtable dei vocabolari e dei message
        _vocabolaries = new HashMap();
        _messages = new HashMap();
        _messageSpecClass = Config.GetInstance().getProperty("verifier.LogicalMessageSpec",
                "net.project.mess.verify.LogicalMessageSpec");
    }

    /**
     * Ritorna l'istanza della classe. Il singleton server per memorizzare le specifiche logiche dei messaggi che, una
     * volta lette da file, vengono mantenute in memoria.
     *
     * @return Verifier.
     *
     * @exception AppCrash
     */
    public static Verifier GetInstance() throws AppCrash {

        // questo e' un "Double checked lock" design pattern
        if (_Instance == null) {
            synchronized (Verifier.class) {

                if (_Instance == null) {
                    _Instance = new Verifier();
                }
            }
        }

        return _Instance;
    }

    /**
     * Recupera la specifica logica del tipo di messaggio indicato.
     *
     * @param msgType String tipo di messaggio per il recupero della classe che gestisce la verifica del messagio
     *
     * @return la specifica del messaggio
     *
     * @exception AppCrash
     */
    public LogicalMessageSpec getLogicalMessageSpec(String msgType) throws AppCrash {

        LogicalMessageSpec message = (LogicalMessageSpec) _messages.get(msgType);

        if (message == null) {

            try {
                Class tempClass = Class.forName(_messageSpecClass);
                Constructor constr = tempClass.getConstructor(new Class[] { String.class });
                message = (LogicalMessageSpec) constr.newInstance(new Object[] { msgType });

                // Put in hashtable il vocabolario
                _messages.put(msgType, message);
            } catch (Throwable t) {
                AppCrash ac = new AppCrash(t);
                ac.logContext("Verifier", "Istanziazione specifica: " + _messageSpecClass);
                throw ac;
            }
        }

        return message;
    }

    /**
     * Recupera il vocabolario da utilizzare per validare il tipo di messaggio indicato.
     *
     * @param msgType String tipo di messaggio
     *
     * @return vocabulary Vocabulary
     *
     * @exception AppCrash
     */
    public Vocabulary getVocabulary(String msgType) throws AppCrash {

        String vocabularyName = Config.GetInstance().getProperty("verifier." + msgType + ".vocabolario");
        ErrDetector.GetInstance().param(vocabularyName);

        Vocabulary vocabolary = (Vocabulary) _vocabolaries.get(vocabularyName);

        if (vocabolary == null) {
            vocabolary = new Vocabulary(vocabularyName);

            // Leggo il vocabolario
            vocabolary.readDocument();

            // Put in hashtable il vocabolario
            _vocabolaries.put(vocabularyName, vocabolary);
        }

        return vocabolary;
    }

    /**
     * Verifica il messaggio passato utilizzando come tipo di messaggio il parametro msgType. Se il messaggio risulta
     * esatto viene restituita una stringa costante definita da Verifier.MESSAGGIO_ESATTO; se il messaggio contiene
     * degli errori viene restituita una stringa con la diagnosi (spiegazione degli errori incontrati). Dopo la verifica
     * inserisce in Logger.Info un VerifiedMessage che contiene il messaggio ed il risultato della verifica; per
     * l'inserimento usa come chiave il tipo del messaggio ricavato dal LogicalMessageSpec.
     *
     * @param msgType String tipo del messaggio
     * @param msgReader MsgReader_itf messaggio da validare
     *
     * @return String Messaggio di errore
     *
     * @exception AppCrash
     */
    public String verifyMessage(String msgType, MsgReader_itf msgReader) throws AppCrash {

        // msgType per recuperare la classe LogicalMessageSpec
        LogicalMessageSpec message = getLogicalMessageSpec(msgType);

        // Verifica Messaggio
        String result = verifyMessage(message, msgReader);

        return result;
    }

    /**
     * Verifica il messaggio. Come tipo messaggio viene utilizzato quello contenuto in msgReader recuperandolo da esso
     * tramite il metodo getType(). Se il messaggio risulta esatto viene restituita una stringa costante definita da
     * Verifier.MESSAGGIO_ESATTO; se il messaggio contiene degli errori viene restituita una stringa con la diagnosi
     * (spiegazione degli errori incontrati). Dopo la verifica inserisce in Logger.Info un VerifiedMessage che contiene
     * il messaggio ed il risultato della verifica; per l'inserimento usa come chiave il tipo del messaggio ricavato dal
     * LogicalMessageSpec.
     *
     * @param msgReader MsgReader_itf messaggio da verificare
     *
     * @return String Messaggio di errore
     *
     * @exception AppCrash
     */
    public String verifyMessage(MsgReader_itf msgReader) throws AppCrash {

        // A Secondo del msgType recuperare la classe LogicalMessageSpec
        LogicalMessageSpec message = getLogicalMessageSpec(msgReader.getType());

        // Verifica Messaggio
        String result = verifyMessage(message, msgReader);

        return result;
    }

    /**
     * Verifica il messaggio. Dopo la verifica inserisce in Logger.Info un VerifiedMessage che contiene il messaggio ed
     * il risultato della verifica; per l'inserimento usa come chiave il tipo del messaggio ricavato dal
     * LogicalMessageSpec. Se il messaggio risulta esatto viene restituita una stringa costante definita da
     * Verifier.MESSAGGIO_ESATTO; se il messaggio contiene degli errori viene restituita una stringa con la diagnosi
     * (spiegazione degli errori incontrati)
     *
     * @param mess LogicalMessageSpec classe di verifica del messaggio
     * @param msgReader MsgReader_itf messaggio da verificare
     *
     * @return String Messaggio di errore
     *
     * @exception AppCrash
     */
    private String verifyMessage(LogicalMessageSpec mess, MsgReader_itf msgReader) throws AppCrash {

        // Recupero vocabolario legato al messaggio
        Vocabulary vocabulary = getVocabulary(mess.getMessageType());

        String result = mess.verifyMessage(msgReader, vocabulary);

        if ((result == null) || result.equals("")) {
            result = Verifier.MESSAGGIO_ESATTO;
        }

        VerifiedMessage vm = new VerifiedMessage(msgReader, result);

        Logger.GetInstance().addInfo(mess.getMessageType(), vm);

        return result;
    }

    /**
     * Questo metodo decodifica la stringa di esito prodotta dal metodo verify nel caso in cui si siano usati dei
     * fieldVerifier di tipo DiagnosiFieldVerifier. Restituisce una Map con nomeCampo e messaggio di errore da
     * visualizzare. Nel caso di errore derivante da semantic check (la sintassi di tutti i campi e' corretta) la map
     * contiene 1 mesaggio1 2 messaggio 2 etc.
     * <p>
     * I messaggi di errore vengono letti dalle proprieta' di configurazione:
     * <p>
     * <code>verifier.msg.NOMECAMPO.DIAGOSI</code>
     *
     * @param esitoVerify esito da decodificare
     * @return Map
     */
    public Map buildErrorsMap(String esitoVerify) {

        return buildErrorsMap("", esitoVerify);
    }

    /**
     * Questo metodo decodifica la stringa di esito prodotta dal metodo verify nel caso in cui si siano usati dei
     * fieldVerifier di tipo DiagnosiFieldVerifier. Restituisce una Map con nomeCampo e messaggio di errore da
     * visualizzare. Nel caso di errore derivante da semantic check (la sintassi di tutti i campi e' corretta) la map
     * contiene 1 mesaggio1 2 messaggio 2 etc.
     * <p>
     * I messaggi di errore sintattico vengono letti dalle proprieta' di configurazione:
     * <p>
     * <code>verifier.msg.FORM.NOMECAMPO.DIAGOSI</code>
     * <p>
     * Se il messaggion di errore non e' presente in configurazione viene restituita la diagnosi stessa.
     *
     * @param form nome della form di provenienza
     * @param esitoVerify esito da decodificare
     * @return Map
     */
    public Map buildErrorsMap(String form, String esitoVerify) {

        Map errorMap = new HashMap();

        // La stringa di esito e' codificata come di segiut:
        // nomecampo||diagnosi$$nomecampo||diagnosi$$....
        String[] errori = esitoVerify.split("$$");

        for (int j = 0; j < errori.length; j++) {
            String[] temp = errori[j].split("||");
            String msg = getErrorMessage(form, temp[0], temp[1]);
            errorMap.put(temp[0], msg);
        }
        return errorMap;
    }

    /**
     * Questo metodo decodifica la stringa di esito prodotta dal metodo verify nel caso in cui si siano usati dei
     * fieldVerifier di tipo DiagnosiFieldVerifier. Restituisce una List con i messaggio di errore sitattico e semantico
     * da visualizzare.
     * <p>
     * I messaggi di errore vengono letti dalle proprieta' di configurazione:
     * <p>
     * <code>verifier.msg.NOMECAMPO.DIAGOSI</code>
     *
     * @param esitoVerify esito da decodificare
     * @return
     */
    public List buildErrorsList(String esitoVerify) {

        return buildErrorsList("", esitoVerify);
    }

    /**
     * Questo metodo decodifica la stringa di esito prodotta dal metodo verify nel caso in cui si siano usati dei
     * fieldVerifier di tipo DiagnosiFieldVerifier. Restituisce una List con i messaggio di errore sitattico e semantico
     * da visualizzare.
     * <p>
     * I messaggi di errore vengono letti dalle proprieta' di configurazione:
     * <p>
     * <code>verifier.msg.FORM.NOMECAMPO.DIAGOSI</code>
     *
     * @param form nome della form di provenienza
     * @param esitoVerify esito da decodificare
     * @return
     */
    public List buildErrorsList(String form, String esitoVerify) {

        List errorList = new ArrayList();

        // La stringa di esito e' codificata come di seguito:
        // nomecampo||diagnosi$$nomecampo||diagnosi$$....
        String[] errori = esitoVerify.split("$$");

        for (int j = 0; j < errori.length; j++) {
            String[] temp = errori[j].split("||");
            String msg = getErrorMessage(form, temp[0], temp[1]);
            errorList.add(msg);
        }
        return errorList;

    }

    // Legge il testo dei messaggi di errore a seguito della diagnosi da proprieta' di configurazione
    private String getErrorMessage(String form, String fieldName, String diagnosi) {

        StringBuffer str = new StringBuffer();
        str.append("verifier.msg.");
        if (!form.equals("")) {
            str.append(form).append(".");
        }
        str.append(fieldName).append(".").append(diagnosi);
        String msg = Config.GetInstance().getProperty(str.toString(), diagnosi);
        return msg;
    }
}