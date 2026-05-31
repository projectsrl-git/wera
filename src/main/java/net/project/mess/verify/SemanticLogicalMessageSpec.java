/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Simone Z.

  Note:

 */

package net.project.mess.verify;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.project.errors.AppCrash;
import net.project.mess.MsgReader_itf;

/**
 * Questa classe rappresenta la specifica logica di un messaggio in grado di effettuare non solo controlli di tipo
 * sintattico ma anche di tipo semantico. E' ovviamente una sottoclasse di LogicalMessageSpec alla quale demanda il
 * controllo sintattico.Il check semantico viene effettuato solo se la verifica sintattica e' andata a buon fine. Il
 * formato dei file di specifica dei messaggi utilizzato da questa classe e' una estensione di quello utilizzato dalla
 * sua classe madre. Complessivamente abbiamo: colonna 1: nome del campo
 * <P>
 * colonna 2: O/F obbligatorio-facoltativo
 * <P>
 * colonna 3: classe da istanziare per il controllo semantico
 * <P>
 *
 * Su tutte le classi indicate come semantic check viene richiamato il metodo check() passando il MsgReader_itf sotto
 * esame. Questo a prescindere dal fatto che il campo sulla cui riga e' indicata la classe esista oppure no.
 *
 * @author sim
 */
public class SemanticLogicalMessageSpec extends LogicalMessageSpec {

    Set _semanticCheckers;

    /**
     * Costruisce una specifica a partire dal nome (tipo) del messaggio. Legge la proprieta'
     * <code>verifier.messaggeDir</code> per recuperare la directory dove si trovano i file di specifica. Questa
     * proprieta' deve esistere e non essere vuota. Il nome del file della specifica per il messaggio viene letto dalla
     * proprieta' <code>verifier.(messageType).messagge</code>
     *
     * @param messageType String tipo messaggio
     * @throws AppCrash
     */
    public SemanticLogicalMessageSpec(String messageType) throws AppCrash {

        super(messageType);
    }

    /**
     * Verifica se il messaggio passato rispetta le specifiche. Per il check sintattico dei campi viene usato il
     * vocabolario passato nei parametri. Il check semantico viene effettuato solo se la verifica sintattica e' andata a
     * buon fine.
     * <p>
     * La stringa di errore restituita e' nel seguente formato:
     * <p>
     * semantic1||diagnosi$$semantic2||diagnosi2$$...
     * <p>
     * <p>
     *
     * @param msgReader MsgReader_itf
     * @param vocabulary Vocabulary
     *
     * @return String Messaggio di errore
     *
     * @exception AppCrash
     */
    @Override
    public String verifyMessage(MsgReader_itf msgReader, Vocabulary vocabulary) throws AppCrash {

        // Verifica sintattica a carico di LogicalMessageSpec
        String result = super.verifyMessage(msgReader, vocabulary);

        // Se result è null non ci sono stati errori eseguo la verifica semantica
        if ((result == null) || result.equals("")) {

            Iterator iter = _semanticCheckers.iterator();

            int j = 1;
            while (iter.hasNext()) {

                SemanticCheck_itf checker = (SemanticCheck_itf) iter.next();
                String checkResult = checker.check(msgReader);
                if (checkResult.length() > 0) {
                    if (isNewErrorMsgFormat()) {
                        result = result + "semantic" + Integer.toString(j) + "||" + checkResult + "$$";
                    } else {
                        result = result + checkResult;
                    }
                }
                j++;
            }
        }

        return result;
    }

    /**
     * Questo metodo serve per elaborare la terza colonna del file di specifica dei messaggi se esiste. La terza colonna
     * e' il nome della classe da istanziare per effettuare la verifica semantica. Il metodo controlla l'esistenza della
     * solo terza colonna
     *
     * @param element Enumeration con le prime due colonne gia' lette.
     *
     * @throws AppCrash
     */
    @Override
    protected void processCols(Enumeration element) throws AppCrash {

        String checkerClass = null;

        if (_semanticCheckers == null) {
            _semanticCheckers = new HashSet();
        }

        try {

            if (element.hasMoreElements()) {
                checkerClass = (String) element.nextElement();

                SemanticCheck_itf checker = (SemanticCheck_itf) Class.forName(checkerClass).newInstance();
                _semanticCheckers.add(checker);
            }
        } catch (Throwable t) {

            AppCrash ac = new AppCrash(t);
            ac.logContext("SemanticLogicalMessageSpec", "Errore istanziando checker: " + checkerClass + "-" + element);
            throw ac;
        }
    }
}