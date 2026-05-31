
package net.project.mess.verify.datatype;

/*
 FieldUser_itf.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 17/05/2001

 Autore: Assunta Ciervo

 Note:

 Modifiche:

 */
import net.project.errors.AppCrash;

/**
 * Questa interfaccia definisce le caratteristiche di una classe in grado di eseguire la verifica di un campo di un
 * messaggio. Viene utilizzata per rappresentare i tipi degli elementi presenti in un vocabolario
 * 
 */
public interface FieldVerifier_itf {

    /**
     * Verifica se il campo soddisfi i requisiti. Ritorna true se tutto OK, altrimenti false
     * 
     * @param field Object valore del campo
     * @return boolean true se tutto ok, false altrimenti
     */
    public boolean verify(Object value) throws AppCrash;
}
