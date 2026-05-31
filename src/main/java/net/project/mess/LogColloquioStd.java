/*

  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Simone Z.

  Note:

 */

package net.project.mess;

import net.project.errors.Logger;
import net.project.errors.Logger_itf;

/**
 * Questa sottoclasse di LogColloquio semplicemente contiene una implementazione del metodo store() che esegue il log
 * sul file di debug di tutte le proprieta' dell'oggetto standard LogColloquio con delle chiamate a log3().
 * 
 * @author zorzetti
 */
public class LogColloquioStd extends LogColloquio {

    /**
     * Creates a new LogColloquioStd object.
     *
     * @param tipoColloquio tipo di colloquio
     */
    public LogColloquioStd(String tipoColloquio) {

        super(tipoColloquio);

    }

    /**
     * Questo metodo serve per memorizzare il presente log del colloquio. Le sottoclassi devono ridefinirlo in modo da
     * effettuare le operazioni opportune. Il framework lo invoca su tutti gli oggetti LogColloquio presenti in
     * Logger.Info.
     * <P>
     * Di default il metodo esegue un logApplication() di tutti i campi.
     */
    @Override
    public void store() {

        Logger_itf logger = Logger.GetInstance();

        String diagnosi = "";
        String tipo = getTipoMsg();

        VerifiedMessage vm = getVerifiedMessage(tipo);
        if (vm != null) {
            diagnosi = vm.getDiagnosi();
        }

        logger.log3("-------------------------------------------");
        logger.log3("Protocollo: " + getProtocollo());
        logger.log3("Sorgente: " + getSorgente());
        logger.log3("Destinazione: " + getDestinazione());
        logger.log3("Tipo msg: " + tipo);
        logger.log3("Diagnosi: " + diagnosi);
        logger.log3("App.msg. ID: " + getAppMsgId());
        logger.log3("TS richiesta: " + getTsRichiesta());
        logger.log3("Richiesta: " + getRichiesta());
        logger.log3("TS risposta: " + getTsRisposta());
        logger.log3("Risposta: " + getRisposta());

    }

}
