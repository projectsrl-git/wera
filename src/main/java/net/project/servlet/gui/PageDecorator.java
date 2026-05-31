/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.servlet.gui;

import java.io.PrintWriter;
import java.util.Map;

import net.project.dataset.DataSet_itf;
import net.project.errors.AppCrash;

/**
 * Questa classe e' un decorator per oggetti di tipo Page_itf; tutte le richieste vengono inoltrate all'oggetto che
 * decora
 */
public class PageDecorator implements Page_itf {

    private Page_itf _thePage;

    public PageDecorator(Page_itf page) {

        _thePage = page;
    }

    /**
     * Questo metodo inoltra la richiesta al metodo getName() della Page_itf decorata
     *
     * @return
     *
     * @see net.project.servlet.gui.Page_itf#getName()
     */
    @Override
    public String getName() {

        return _thePage.getName();
    }

    /**
     * Questo metodo inoltra la richiesta al metodo setDataSourceParam() della Page_itf decorata
     *
     * @param param
     * @param dsNum
     * @throws AppCrash
     *
     * @see net.project.servlet.gui.Page_itf#setDataSourceParam(java.util.Map, int)
     */
    @Override
    public void setDataSourceParam(Map param, int dsNum) throws AppCrash {

        _thePage.setDataSourceParam(param, dsNum);

    }

    /**
     * Questo metodo
     *
     * @param paramHash
     * @throws AppCrash
     *
     * @see net.project.servlet.gui.Page_itf#setPageRootData(java.util.Map)
     */
    @Override
    public void setPageRootData(Map paramHash) throws AppCrash {

        _thePage.setPageRootData(paramHash);

    }

    /**
     * Questo metodo inoltra la richiesta al metodo setDataSourceParam() della Page_itf decorata
     *
     * @param ds
     * @param name
     * @throws AppCrash
     *
     * @see net.project.servlet.gui.Page_itf#setPageRootData(net.project.dataset.DataSet_itf, java.lang.String)
     */
    @Override
    public void setPageRootData(DataSet_itf ds, String name) throws AppCrash {

        _thePage.setPageRootData(ds, name);

    }

    /**
     * Questo metodo inoltra la richiesta al metodo display() della Page_itf decorata
     *
     * @param pw
     * @throws AppCrash
     *
     * @see net.project.servlet.gui.Page_itf#display(java.io.PrintWriter)
     */
    @Override
    public void display(PrintWriter pw) throws AppCrash {

        _thePage.display(pw);

    }

    /**
     * Questo metodo inoltra la richiesta al metodo getConfigName() della Page_itf decorata
     *
     * @return String nome della configurazione
     */
    @Override
    public String getConfigName() {

        return _thePage.getConfigName();
    }

    protected Page_itf getOriginalPage() {

        return _thePage;
    }
}
