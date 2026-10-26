package net.projectsrl.wera.registrazioni;

/**
 * Costanti delle registrazioni self-service di wera2026 (schema PostgreSQL "registrazioni").
 * Disegno: .claude/2026-10-26-disegno-registrazione-amministratore.md.
 */
final class RegistrazioniCostanti {

    private RegistrazioniCostanti() {

    }

    // --- parametri (registrazioni.parametri)
    static final String PAR_REGISTRAZIONE_ATTIVA      = "REGISTRAZIONE.ATTIVA";
    static final String PAR_REGISTRAZIONE_CHIUSA      = "REGISTRAZIONE.CHIUSA.TESTO";
    static final String PAR_AZIENDA_CODICE            = "ATTIVAZIONE.AZIENDA_CODICE";
    static final String PAR_PROFILO                   = "ATTIVAZIONE.PROFILO";
    static final String PAR_APPROVAZIONE_PROFILI      = "APPROVAZIONE.PROFILI";
    static final String PAR_PASSWORD_MIN              = "PASSWORD.LUNGHEZZA_MIN";
    static final String PAR_PASSWORD_MAX              = "PASSWORD.LUNGHEZZA_MAX";
    static final String PAR_PASSWORD_REGOLA           = "PASSWORD.REGOLA";
    static final String PAR_PASSWORD_REGOLA_TESTO     = "PASSWORD.REGOLA.TESTO";
    static final String PAR_PASSWORD_GIORNI           = "PASSWORD.GIORNI_VALIDITA";
    static final String PAR_FASCE_CONDOMINI           = "CONDOMINI.FASCE";
    static final String PAR_FILE_CARTELLA             = "FILE.CARTELLA";
    static final String PAR_FILE_ESTENSIONI           = "FILE.ESTENSIONI";
    static final String PAR_FILE_MAX_MB               = "FILE.DIMENSIONE_MAX_MB";
    static final String PAR_FILE_MAX_NUMERO           = "FILE.MAX_PER_RICHIESTA";
    static final String PAR_ANTIABUSO_SECONDI         = "ANTIABUSO.SECONDI_MINIMI";
    static final String PAR_ANTIABUSO_MAX_IP          = "ANTIABUSO.MAX_RICHIESTE_IP_24H";
    static final String PAR_ANTIABUSO_PROXY           = "ANTIABUSO.PROXY_FIDATI";
    static final String PAR_MAIL_ATTIVA               = "MAIL.ATTIVA";
    static final String PAR_MAIL_MITTENTE             = "MAIL.MITTENTE";
    static final String PAR_MAIL_NOTIFICA_DESTINATARI = "MAIL.NOTIFICA_DESTINATARI";
    static final String PAR_LINK_ACCESSO              = "LINK.ACCESSO";
    static final String PAR_TEMPI_RISPOSTA            = "TESTO.TEMPI_RISPOSTA";
    static final String PAR_PAGINA_TITOLO             = "PAGINA.TITOLO";
    static final String PAR_PAGINA_SOTTOTITOLO        = "PAGINA.SOTTOTITOLO";
    static final String PAR_PAGINA_INFO_VERIFICA      = "PAGINA.INFO_VERIFICA";
    static final String PAR_PRIVACY_TESTO             = "PRIVACY.TESTO";
    static final String PAR_PRIVACY_LINK              = "PRIVACY.LINK";
    static final String PAR_ASSISTENZA_EMAIL          = "ASSISTENZA.EMAIL";
    static final String PAR_ASSISTENZA_TELEFONO       = "ASSISTENZA.TELEFONO";

    // --- mail (prefisso dei parametri .OGGETTO / .TESTO)
    static final String MAIL_RICEVUTA                 = "MAIL.RICEVUTA";
    static final String MAIL_NOTIFICA                 = "MAIL.NOTIFICA";
    static final String MAIL_APPROVATA                = "MAIL.APPROVATA";
    static final String MAIL_RIFIUTATA                = "MAIL.RIFIUTATA";

    // --- valori di dominio (vincoli CHECK del DDL 003)
    static final String TIPO_AMMINISTRATORE           = "AMMINISTRATORE";
    static final String SOGGETTO_PROFESSIONISTA       = "PROFESSIONISTA";
    static final String SOGGETTO_STUDIO               = "STUDIO";
    static final String STATO_IN_ATTESA               = "IN_ATTESA";
    static final String STATO_APPROVATA               = "APPROVATA";
    static final String STATO_RIFIUTATA               = "RIFIUTATA";
    static final String STATO_ANNULLATA               = "ANNULLATA";
    static final String COMPATIBILITA_DA_VERIFICARE   = "DA_VERIFICARE";

    // --- eventi (registrazioni.eventi.tipo_evento)
    static final String EVENTO_RICHIESTA              = "RICHIESTA";
    static final String EVENTO_BLOCCO                 = "BLOCCO";
    static final String EVENTO_APPROVAZIONE           = "APPROVAZIONE";
    static final String EVENTO_RIFIUTO                = "RIFIUTO";
    static final String EVENTO_ANNULLAMENTO           = "ANNULLAMENTO";
    static final String EVENTO_MODIFICA               = "MODIFICA";
    static final String EVENTO_MAIL                   = "MAIL";
    static final String EVENTO_DOWNLOAD               = "DOWNLOAD";
    static final String EVENTO_ACCESSO_NEGATO         = "ACCESSO_NEGATO";
    static final String EVENTO_ERRORE                 = "ERRORE";

    static final long   MEGABYTE                      = 1024L * 1024L;
}
