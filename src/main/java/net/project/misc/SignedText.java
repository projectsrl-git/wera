/*
  SignedText.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 26/06/2002

  Autore: Andrea B.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.security.PublicKey;
import java.security.Signature;

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta un testo firmato del quale si vuole verificare la firma
 */
public class SignedText {

    private byte[] _text;
    private String _original;

    /**
     * Costruttore. Viene passato il testo del messaggio del quale verificare la firma. Il messaggio viene convertito
     * internamente in ASCII.
     */
    public SignedText(String text) throws AppCrash {

        _original = text;
        _text = text.getBytes();
        _text = Converter.convertOut(_text, "8859_1");
    }

    /**
     * Questo metodo verifica che la firma MD5withRSA sul messaggio passato sia valida.
     * 
     * @param java.java.security.publicKey chiave publica ottenuta dal certificato
     * @param java.lang.Byte Firma da verificare.
     * @return boolean true se la verifica ha avuto esito positivo , false altrimenti.
     */
    public boolean verifySignature(PublicKey publickey, byte[] messSign) throws AppCrash {

        try {
            String algor = Config.GetInstance().getProperty("SignedText.algoritmo", "MD5withRSA");
            Signature signature = Signature.getInstance(algor);
            signature.initVerify(publickey);
            signature.update(_text);

            if (signature.verify(messSign)) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable io) {
            AppCrash ac = new AppCrash(io);
            ac.logContext("SigndText", "Errore nella verifica della firma -" + _original);
            throw ac;
        }
    }

}
