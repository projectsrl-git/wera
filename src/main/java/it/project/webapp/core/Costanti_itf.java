
package it.project.webapp.core;

import net.project.misc.Config;

/**
 * Codici usati nell'applicazione ASTROWEB
 */
@Deprecated
public interface Costanti_itf {

    public static final String OPZIONE_INSERIMENTO_MODIFICA                = "OPZIONE_INSERIMENTO_MODIFICA";

    public static final String OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI       = "OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI";

    public static final String OPZIONE_INSERIMENTO_MODIFICA_EVENTI         = "OPZIONE_INSERIMENTO_MODIFICA_EVENTI";

    public static final String OPZIONE_INSERIMENTO                         = "INSERIMENTO";

    public static final String OPZIONE_MODIFICA                            = "MODIFICA";

    public static final String OPZIONE_CANCELLA                            = "CANCELLA";

    public static final String OPZIONE_VISUALIZZA                          = "VISUALIZZA";

    public static final String OPZIONE_DUPLICA                             = "DUPLICA";

    public static final String OPZIONE_REFRESH                             = "REFRESH";

    public static final String OPZIONE_REFRESH_INSERIMENTO                 = "REFRESH_INSERIMENTO";

    public static final String OPZIONE_REFRESH_MODIFICA                    = "REFRESH_MODIFICA";

    public static final String NESSUNA_OPZIONE                             = "";

    public static final String NO_MESSAGE                                  = "";

    public static final String TAG_MESSAGE                                 = "MESSAGE";

    public static final String TAG_HTTP_METHOD                             = "HTTP_METHOD";

    public static final String PAGE_MESSAGE                                = "message";

    public static final String FUNCTIONID                                  = Config.GetInstance()
            .getProperty("Servlet.FunctionField", "FUNCTIONID");

    public static final String OPZIONE_INSERIMENTO_MODIFICA_RIFERIMENTI    = "OPZIONE_INSERIMENTO_MODIFICA_RIFERIMENTI";

    public static final String OPZIONE_INSERIMENTO_MODIFICA_OFFERTE        = "OPZIONE_INSERIMENTO_MODIFICA_OFFERTE";

    public static final String OPZIONE_INSERIMENTO_DETTAGLI_DA_PRELIEVO    = "OPZIONE_INSERIMENTO_DETTAGLI_DA_PRELIEVO";

    public static final String OPZIONE_VISUALIZZA_BROWSE                   = "OPZIONE_VISUALIZZA_BROWSE";

    public static final String RICHIESTA_CHIUSA_POSITIVAMENTE              = "300";

    public static final String TAG_MODULO                                  = "MODULO";

    public static final String MODULO_STAMPA_RICHIESTA_RIDOTTA_INTERNA     = "RIDOTTA_INTERNA";

    public static final String MODULO_STAMPA_RICHIESTA_RIDOTTA_ESTERNA     = "RIDOTTA_ESTERNA";

    public static final String MODULO_STAMPA_RICHIESTA_COMPLETA            = "COMPLETA";

    public static final String PROJECT                                     = "PROJECT";

    public static final String EUROPEO                                     = "EUROPEO";

    public static final String ALTRAN                                      = "ALTRAN";

    public static final String MODULO_PRIVACY                              = "MODULO_PRIVACY";

    public static final String MODULO_STAMPA_RICHIESTA_RIDOTTA_INTERNA_PDF = "RIDOTTA_INTERNA";

    public static final String MODULO_STAMPA_RICHIESTA_RIDOTTA_ESTERNA_PDF = "RIDOTTA_ESTERNA";

    public static final String MODULO_STAMPA_RICHIESTA_COMPLETA_PDF        = "COMPLETA";

    public static final String ATTACHMENT                                  = "ATTACHMENT";

    public static final String PREFISSO_PARAMETRO                          = "PREFISSO_PARAMETRO";

    public static final String PREVIOUS_REQUEST_HASH                       = "PREVIOUS_REQUEST_HASH";

    public static final String TIPO_EVENTO_RICHIESTA                       = "RICHIESTA";
    public static final String TIPO_EVENTO_TRATTATIVA                      = "TRATTATIVA";
    public static final String TIPO_EVENTO_RISORSA_UMANA                   = "RISORSA_UMANA";
    public static final String TIPO_EVENTO_CV                              = "CV";
    public static final String TIPO_EVENTO_TICKET                          = "TICKET";
    public static final String DESCRIZIONE_PARAMETRO                       = "DESCRIZIONE_PARAMETRO";

    public static final String OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI_1     = "OPZIONE_INSERIMENTO_MODIFICA_DETTAGLI_1";

    public static final String COD_UTENTE_INSD                             = "COD_UTENTE_INSD";

}
