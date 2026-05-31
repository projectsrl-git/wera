/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.mess.iso8583;

/*
 DataSource8583Ext_base.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 31/03/2003

 Autore: Rosella V.

 Note:

 Modifiche:

 */
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

/**
 * Classe astratta che gestisce la creazione dei messaggi ISO8583.
 */
public abstract class DataSource8583Ext_base extends DataSource8583_base {

    /**
     * Costruttore.
     *
     * @param messageType java.lang.String Tipo del messaggio.
     */
    protected DataSource8583Ext_base(String messageType) {

        super(messageType);
    }

    /**
     * Restituisce il valore del campo ISO8583.
     *
     * @param dataElementNum int Indice del campo ISO8583.
     *
     * @return java.lang.String
     *
     * @throws AppCrash
     */
    @Override
    public String getElement(int dataElementNum) throws AppCrash {

        Class classe = this.getClass();
        String nomeMetodo = "getCampo" + dataElementNum;
        Method metodo = null;
        Object valCampo = null;

        try {
            metodo = classe.getMethod(nomeMetodo, new Class[] {});
            valCampo = metodo.invoke(this, new Object[] {});
        } catch (NoSuchMethodException e) {
            AppCrash app = new AppCrash(e);
            throw app;
        } catch (InvocationTargetException e) {
            AppCrash app = new AppCrash(e);
            throw app;
        } catch (IllegalAccessException e) {
            AppCrash app = new AppCrash(e);
            throw app;
        }

        if (Logger.GetInstance().getLogLevel() >= 3) {
            Logger.GetInstance().log3("---element" + dataElementNum + " = " + valCampo);
        }

        if (valCampo == null) {
            return null;
        }
        return valCampo.toString();

    }

    /**
     * Inizializza il DataSource che lo implementa.
     *
     * @throws AppCrash
     */
    @Override
    protected abstract void startInit() throws AppCrash;
}
