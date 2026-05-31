/*
 Copyright (c) by SSB Spa Societa' per i Servizi Bancari

 Note:

 */

package net.project.db;

import java.util.ArrayList;
import java.util.List;

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta una wherecondition per un NDAO
 */
public class WhereCondition {

    private static final String PLACEHOLDER = "?";
    private StringBuffer        _sql        = new StringBuffer(80);
    private List                _values     = new ArrayList();
    private List                _names      = new ArrayList();
    private NDAO_base           _dao;

    /**
     * Constructor
     *
     * @param dao il dao da usare come sorgente per i campi
     */
    public WhereCondition(NDAO_base dao) {

        _dao = dao;
        _sql.append(" where ");
    }

    /**
     * Questo metodo appende alla wherecond una stringa di testo
     *
     * @param param testo da appendere
     */
    public WhereCondition append(String param) {

        _sql.append(param);
        return this;
    }

    /**
     * Questo metodo appende alla wherecond il valore di un campo: viene appeso alla stringa SQL un ? ed alla lista dei
     * valori il valore del campo
     *
     * @param name nome del campo da appendere
     * @throws AppCrash
     */
    public WhereCondition appendFieldValue(String name) throws AppCrash {

        // Se il nome dell'attributo inizia con ? devo eliminare i primi 2 caratteri
        if (name.startsWith("?")) {
            name = name.substring(2);
        }

        try {
            _sql.append(PLACEHOLDER);
            _values.add(_dao.getAttribute(name));
            _names.add(name);
            return this;
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("WhereCondition", "");
            throw ac;
        }
    }

    /**
     * Questo metodo ritorna la stringa SQL che rappresenta la wherecondition con i placeholder
     *
     * @return
     */
    public String getSQL() {

        return new String(_sql);
    }

    /**
     * Questo metodo ritorna una lista ordinata con i valori appesi alla wherecondition
     *
     * @return List lista dei valori
     */
    public List getValues() {

        return _values;
    }

    /**
     * Questo metodo ritorna una lista ordinata con i nomi delle colonne appesi alla wherecondition
     *
     * @return List lista dei nomi
     */
    public List getNames() {

        return _names;
    }

    /**
     * Questo metodo azzera la wherecondition ripulendo la stringa SQL e la lista dei valori
     *
     */
    public void clear() {

        _sql = new StringBuffer(80);
        _values.clear();
    }

    /**
     * metodo toString ritorna una rappresentazione stringa della class
     *
     * @return la stringa che rappresenta la classe
     * @author
     */
    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("WhereCondition[");
        buffer.append("_sql = ").append(_sql);
        buffer.append(" _values = ").append(_values);
        buffer.append("]");
        return buffer.toString();
    }
}
