/*
  PemCertificate.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione:

  Autore: Andrea B.

  Note:

  Modifiche:

 */

package net.project.misc;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta un certificato X509 in formato PEM. Puo' essere utilizzata per recuperare la chiave
 * pubblica contenuta nel certificato o il certificato stesso.
 */
public class PemCertificate {

    private X509Certificate _certificate;
    private byte[]          _bytes;

    /**
     * Costruttore. Viene passata una stringa che rappresenta il certificato in formato PEM. La stringa deve contenere
     * gli "a capo" opportuni per la piattaforma sulla quale gira l'applicazione: "\n" per unix "\n\r" per WIN. La
     * stringa viene convertita in ASCII.
     *
     * @param certificato java.lang.String Certificato in formato PEM
     */
    public PemCertificate(String certificato) throws AppCrash {

        byte[] b = certificato.getBytes();
        _bytes = Converter.convertOut(b, "8859_1");
    }

    /**
     * Costruttore. Viene passato un byte array che rappresenta il certificato in formato PEM. Il byte array deve
     * contenere gli "a capo" opportuni per la piattaforma sulla quale gira l'applicazione: "\n" per unix "\n\r" per
     * WIN. OKKIO su tutte le piattaforme il ba deve essere in ASCII!
     *
     * @param bytes java.lang.Byte Certificato passato come array di bytes
     */
    public PemCertificate(byte[] bytes) {

        _bytes = bytes;
    }

    /**
     * Questo metodo di inizializzazione esegue il parsing della stringa passata e traduce il certificato in un oggetto
     * di tipo X509Certificate.
     */
    public void init() throws AppCrash {

        try {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            ByteArrayInputStream bais = new ByteArrayInputStream(_bytes);

            while (bais.available() > 0) {
                _certificate = (X509Certificate) cf.generateCertificate(bais);
            }

        } catch (Throwable io) {
            AppCrash ac = new AppCrash(io);
            ac.logContext("PemCertificate", "Pem - certificato = " + new String(_bytes));
            throw ac;
        }
    }

    /**
     * Questo metodo ritorna un oggetto X509Certificate che rappresenta il certificato.
     * 
     * @return _certificate java.security.cert.X509Certificate ritorna il valore del certificato.
     */
    public X509Certificate getCertificate() {

        return _certificate;
    }

    /**
     * Questo metodo ritorna la chiave publica del certificato.
     * 
     * @return publickey java.security.PublicKey ritorna la chiave publica del certificato.
     */
    public PublicKey getPublicKey() {

        PublicKey publickey = null;
        publickey = _certificate.getPublicKey();
        return publickey;
    }
}
