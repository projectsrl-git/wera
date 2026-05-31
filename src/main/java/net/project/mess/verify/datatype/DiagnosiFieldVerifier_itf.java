/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.mess.verify.datatype;

/**
 * Questa interfaccia estende il field verifier aggiungendo un metodo per ottere la diagnosi del difetto nel campo
 * controllato.
 * <P>
 * La diagnosi dovrebbe essere un codice che descrive il tipo di errore o violazione che e' stata rilevata. Questa
 * diagonsi puo' essere poi utilizzata per recuperare un messaggio di errore ad essa associata per campo/form
 */
public interface DiagnosiFieldVerifier_itf extends FieldVerifier_itf {

    /**
     * Questo metodo verifica il campo passato e restuisce stringa che indica la diagnosi.
     * <p>
     * Se tutto e' ok deve essere restituito null.
     * <p>
     * La diagnosi deve essere un codice.
     *
     * @return
     */
    public String diagnose(String value);

}
