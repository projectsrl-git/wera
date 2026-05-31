
package net.project.mess.atdat;

/*
 ATDAT_esiti_itf.java

 Copyright (c) by SSB spa Societa' per i Servizi Bancari 

 Data creazione: 01/07/1999

 Autore: Rosella V.

 Note:

 Modifiche:	17/02/2000		Anna L.		TL33	Aggiunto esito 0003 per storno online concesso

 */

public interface ATDATEsiti_itf {

    public static final String AUT_CONCESSA            = "0000";
    public static final String AUT_DIFF_ACCETTATA      = "0001";
    public static final String STORNO_CONCESSO         = "0003";
    public static final String AUT_CF_CONCESSA         = "0020";
    public static final String PAG_GSM_PRESO_IN_CARICO = "0010";
    public static final String RICHIESTA_ERRATA        = "0100";
    public static final String PROB_NEGOZIO            = "0101";
    public static final String ORDINE_DUPLICATO        = "0102";
    public static final String PROB_MESS_RICHIESTA     = "0103";
    public static final String SCAD_TERMINI            = "0104";
    public static final String TRANS_ASSENTE           = "0105";
    public static final String NEGATA_DA_TAN_CIRC      = "0200";
    public static final String NUM_CARTA_ERRATO        = "0201";
    public static final String PROB_CIRCUITO           = "0202";
    public static final String PLAFOND_ESAURITO        = "0203";
    public static final String CARTA_BLOCCATA          = "0204";
    public static final String PROB_SERV_EPS           = "0300";
}
