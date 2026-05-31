
package net.project.db.ser;

import net.project.errors.AppCrash;

/**
 * Questa interfaccia rappresenta una entita' capace di serializzare e deserializzare oggetti Java in un formato
 * qualsiasi da e verso una stringa
 * 
 * @author Simone
 */
public interface Serializer_itf {

    /**
     * Questo metodo serve per deserializzare un oggetto contenuto in una stringa
     * 
     * @param str la stringa che contiene l'oggetto
     * @return l óggetto
     * @throws AppCrash
     */
    public Object deserializeFromString(String str) throws AppCrash;

    /**
     * Questo metodo serve per serializzare un oggetto in una string
     *
     * @param obj l'oggetto da serializzare
     * @return la stringa contenente l'oggetto
     * @throws AppCrash
     */
    public String serializeToString(Object obj) throws AppCrash;
}