package net.projectsrl.wera.centraletermica.core;

/**
 * Costanti della Centrale termica di wera2026: chiavi dei parametri (CENTRALE_TERMICA.PARAMETRI),
 * stati e tipi di evento, nomi dei dataset di config.cfg. Tenute fuori dai DAO perche' il framework
 * tratta come colonna ogni costante public static final String di un DAO.
 */
public final class CentraleTermicaCostanti {

    // profili (public.profili.codice), in ordine di precedenza
    public static final String   PROFILO_SUPERUSER           = "ADM";
    public static final String   PROFILO_INSTALLATORE        = "INS";
    public static final String   PROFILO_AMMINISTRATORE      = "AMM";
    public static final String   PROFILO_CONDOMINO           = "CON";
    static final String[]        PROFILI_PRECEDENZA          = { "ADM", "INS", "AMM", "CON", "CEN" };

    // chiavi dei parametri
    public static final String   PAR_ACCESSO_PROFILI         = "ACCESSO.PROFILI";
    public static final String   PAR_MENU_VOCE_WERA_PROFILI  = "MENU.VOCE_WERA.PROFILI";
    public static final String   PAR_ETICHETTA_TITOLO        = "ETICHETTA.TITOLO";
    public static final String   PAR_FILE_CARTELLA           = "FILE.CARTELLA";
    public static final String   PAR_FILE_ESTENSIONI         = "FILE.ESTENSIONI";
    public static final String   PAR_FILE_DIMENSIONE_MAX_MB  = "FILE.DIMENSIONE_MAX_MB";
    public static final String   PAR_FILE_MAX_PER_CARICAMENTO = "FILE.MAX_PER_CARICAMENTO";
    public static final String   PAR_DOWNLOAD_ZIP_MAX_FILE   = "DOWNLOAD.ZIP_MAX_FILE";
    public static final String   PAR_DOCUMENTI_WERA_VISIBILI = "DOCUMENTI_WERA.VISIBILI";
    public static final String   PAR_GIORNI_PREAVVISO        = "SCADENZE.GIORNI_PREAVVISO";
    public static final String   PAR_ULTIMI_DOCUMENTI        = "ULTIMI_DOCUMENTI.NUMERO";
    public static final String   PAR_SEZIONE_INTERVENTI      = "RIEPILOGO.SEZIONE_INTERVENTI";
    public static final String   PAR_CONFIGURAZIONE_PROFILI  = "CONFIGURAZIONE.PROFILI";

    // parametri che la pagina puo' leggere (nessun dato tecnico: cartelle, estensioni... restano lato server)
    static final String[]        PARAMETRI_PAGINA            = { "ETICHETTA.TITOLO", "ETICHETTA.GESTIONE",
            "ASSISTENZA.TITOLO", "ASSISTENZA.UFFICIO", "ASSISTENZA.EMAIL", "ASSISTENZA.TELEFONO", "ASSISTENZA.ORARI",
            "ASSISTENZA.LINK_SEGNALAZIONE", "SCADENZE.GIORNI_PREAVVISO", "ULTIMI_DOCUMENTI.NUMERO",
            "FILE.ESTENSIONI", "FILE.DIMENSIONE_MAX_MB", "FILE.MAX_PER_CARICAMENTO", "DOWNLOAD.ZIP_MAX_FILE",
            "DOCUMENTI_WERA.VISIBILI",
            "DOCUMENTI_WERA.ETICHETTA" };

    // stati di CENTRALE_TERMICA.DOCUMENTI
    public static final String   STATO_CORRENTE              = "CORRENTE";
    public static final String   STATO_ARCHIVIATO            = "ARCHIVIATO";
    public static final String   STATO_ANNULLATO             = "ANNULLATO";

    // tipi di CENTRALE_TERMICA.EVENTI
    public static final String   EVENTO_CARICAMENTO          = "CARICAMENTO";
    public static final String   EVENTO_NUOVA_VERSIONE       = "NUOVA_VERSIONE";
    public static final String   EVENTO_ARCHIVIAZIONE        = "ARCHIVIAZIONE";
    public static final String   EVENTO_RIPRISTINO           = "RIPRISTINO";
    public static final String   EVENTO_ANNULLAMENTO         = "ANNULLAMENTO";
    public static final String   EVENTO_DOWNLOAD             = "DOWNLOAD";
    public static final String   EVENTO_DOWNLOAD_ZIP         = "DOWNLOAD_ZIP";
    public static final String   EVENTO_ACCESSO_NEGATO       = "ACCESSO_NEGATO";
    public static final String   EVENTO_SCADENZA_CREATA      = "SCADENZA_CREATA";
    public static final String   EVENTO_SCADENZA_CHIUSA      = "SCADENZA_CHIUSA";
    public static final String   EVENTO_SCADENZA_ANNULLATA   = "SCADENZA_ANNULLATA";
    public static final String   EVENTO_CONFIGURAZIONE       = "CONFIGURAZIONE";

    // dataset di config.cfg
    static final String          DS_PARAMETRO                = "DSCentraleTermicaParametro";
    static final String          DS_CONDOMINI                = "DSCentraleTermicaCondomini";
    static final String          DS_CONDOMINIO               = "DSCentraleTermicaCondominio";
    static final String          DS_PERMESSO_SEZIONE         = "DSCentraleTermicaPermessoSezione";
    static final String          DS_SEZIONI                  = "DSCentraleTermicaSezioni";
    static final String          DS_DOCUMENTI                = "DSCentraleTermicaDocumenti";
    static final String          DS_DOCUMENTO                = "DSCentraleTermicaDocumento";
    static final String          DS_VERSIONI                 = "DSCentraleTermicaVersioni";
    static final String          DS_VERSIONE                 = "DSCentraleTermicaVersione";
    static final String          DS_FILE_WERA                = "DSCentraleTermicaFileWera";
    static final String          DS_SCADENZE                 = "DSCentraleTermicaScadenze";
    static final String          DS_RIEPILOGO                = "DSCentraleTermicaRiepilogo";
    static final String          DS_TIPI_SCADENZA            = "DSCentraleTermicaTipiScadenza";
    static final String          DS_SCADENZA                 = "DSCentraleTermicaScadenza";
    static final String          DS_STORICO_SCADENZE         = "DSCentraleTermicaStoricoScadenze";
    static final String          DS_CONFIG_SEZIONI           = "DSCentraleTermicaConfigSezioni";
    static final String          DS_CONFIG_PERMESSI          = "DSCentraleTermicaConfigPermessi";
    static final String          DS_CONFIG_PROFILI           = "DSCentraleTermicaConfigProfili";
    static final String          DS_CONFIG_TIPI              = "DSCentraleTermicaConfigTipi";
    static final String          DS_CONFIG_PARAMETRI         = "DSCentraleTermicaConfigParametri";
    static final String          DS_CONFIG_CATEGORIE         = "DSCentraleTermicaConfigCategorie";
    static final String          DS_CONFIG_AZIENDE           = "DSCentraleTermicaConfigAziende";
    static final String          DS_SCADENZIARIO             = "DSCentraleTermicaScadenziario";
    static final String          DS_INVII                    = "DSCentraleTermicaInvii";
    static final String          DS_AVVISI_DA_INVIARE        = "DSCentraleTermicaAvvisiDaInviare";

    private CentraleTermicaCostanti() {

    }
}
