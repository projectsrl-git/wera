/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.mess.atdat;

/*
 ATDATURLRead.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari

 Data creazione: 19/05/1999

 Autore: Simone Z.

 Note:

 Modifiche:

 */
import net.project.mess.MsgReader_itf;

/**
 * Classe per la lettura di un messaggio ATDAT
 */
public class ATDATURLRead implements MsgReader_itf {

    private String _messaggio;

    /**
     * Costruttore presente solo per permettere la serializzazione/deserializzazione di oggetti di questa classe. <b>Non
     * deve essere utilizzato</b>
     */
    public ATDATURLRead() {

    }

    /**
     * Costruttore.
     *
     * @param messaggio DOCUMENT ME!
     */
    public ATDATURLRead(byte[] messaggio) {

        _messaggio = new String(messaggio);
    }

    /**
     * Ritorna il campo corrispondente a nome.
     *
     * @param nome java.lang.String nome del campo.
     *
     * @return String
     */
    @Override
    public String getField(String nome) {

        String campo;
        boolean trovatoCampo = false;
        int indexStart = 0;

        while (!trovatoCampo) {
            indexStart = _messaggio.indexOf(nome, indexStart);

            if (indexStart == -1) {
                break;
            }

            if ((_messaggio.charAt(indexStart + nome.length()) == '=')
                    && ((_messaggio.charAt(indexStart - 1) == '(') || (_messaggio.charAt(indexStart - 1) == ' ') || (_messaggio
                            .charAt(indexStart - 1) == '&'))) {
                trovatoCampo = true;
            } else {
                indexStart++;
            }
        }

        if (indexStart != -1) {
            int indexEnd = _messaggio.indexOf('&', indexStart);

            if (indexEnd != -1) {
                campo = _messaggio.substring(indexStart + nome.length() + 1, indexEnd);

                return campo;
            }
        }

        return "";
    }

    /**
     * Ritorna il tipo di messaggio.
     *
     * @return String
     */
    @Override
    public String getType() {

        String tipo;

        tipo = _messaggio.substring(0, 5);

        return tipo;
    }
}
