package net.projectsrl.wera.centraletermica.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

/**
 * FUNCTIONID=CentraleTermicaDati (GET), risposta JSON per la pagina della Centrale termica.
 * <p>
 * OPERAZIONE = condomini | parametri | riepilogo | sezioni | documenti | versioni | scadenze.
 * Dal browser arrivano solo id numerici e scelte da elenchi chiusi: perimetro, profilo e filtri SQL
 * li costruisce il server (a differenza di JSONDataSet, nessuna WHERECONDITION dal client).
 */
public class FunctionCentraleTermicaDati extends FunctionCentraleTermica_base {

    private static final String[] COLONNE_CONDOMINIO = { "ID_CONDOMINIO", "ID_AZIENDA", "DENOMINAZIONE", "INDIRIZZO",
            "LOCALITA", "PROVINCIA" };
    private static final String[] COLONNE_SEZIONE    = { "ID_SEZIONE", "CODICE", "TITOLO", "SOTTOTITOLO", "ICONA",
            "TIPO", "ORDINE", "FL_SCADENZA_RICHIESTA", "FL_SCRITTURA", "NR_DOCUMENTI", "NR_ARCHIVIATI",
            "TS_ULTIMO_AGGIORNAMENTO", "PROSSIMA_SCADENZA", "SEMAFORO" };
    private static final String[] COLONNE_DOCUMENTO  = { "ORIGINE", "ID_DOCUMENTO", "ID_FILE_WERA", "ID_SEZIONE",
            "CODICE_SEZIONE", "TITOLO_SEZIONE", "TIPO_SEZIONE", "TITOLO", "DESCRIZIONE", "STATO", "FL_SOLA_LETTURA",
            "DATA_DOCUMENTO", "ID_VERSIONE", "NUMERO_VERSIONE", "NOME_FILE", "TIPO_MIME", "DIMENSIONE_BYTE",
            "TS_CARICAMENTO", "CARICATO_DA" };
    private static final String[] COLONNE_VERSIONE   = { "ID_VERSIONE", "NUMERO_VERSIONE", "NOME_FILE_ORIGINALE",
            "TIPO_MIME", "DIMENSIONE_BYTE", "NOTA", "TS_INS", "CARICATO_DA", "FL_CORRENTE" };
    private static final String[] COLONNE_SCADENZA   = { "ID_SCADENZA", "CODICE_TIPO", "DESCRIZIONE", "ID_SEZIONE",
            "TITOLO_SEZIONE", "DATA_SCADENZA", "GIORNI_MANCANTI", "GIORNI_PREAVVISO", "SEMAFORO",
            "RIFERIMENTO_NORMATIVO" };
    private static final String[] COLONNE_RIEPILOGO  = { "NR_DOCUMENTI", "PROSSIMA_SCADENZA",
            "DESCR_PROSSIMA_SCADENZA", "SEMAFORO_PROSSIMA_SCADENZA", "ULTIMO_INTERVENTO", "NR_SEZIONI",
            "NR_SEZIONI_CON_DOCUMENTI", "NR_SCADUTE", "NR_IN_SCADENZA" };

    public FunctionCentraleTermicaDati() {

        super();
    }

    public FunctionCentraleTermicaDati(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        mostra(req, res, userInfo);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        ContestoUtente ctx = contesto(userInfo, res);
        if (ctx == null) {
            return;
        }

        String operazione = req.getParameter("OPERAZIONE");
        if ("condomini".equals(operazione)) {
            rispondi(res, "condomini", LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONDOMINI, ctx.parametriBase(),
                    COLONNE_CONDOMINIO));
            return;
        }
        if ("parametri".equals(operazione)) {
            Map<String, Object> parametri = new LinkedHashMap<String, Object>();
            for (String chiave : CentraleTermicaCostanti.PARAMETRI_PAGINA) {
                parametri.put(chiave, ctx.parametro(chiave, ctx.getIdAzienda()));
            }
            parametri.put("PROFILO", ctx.getProfilo());
            rispondi(res, "parametri", parametri);
            return;
        }

        // tutte le altre operazioni sono per un condominio del perimetro
        Integer idCondominio = intero(req.getParameter("ID_CONDOMINIO"));
        if (idCondominio == null) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "ID_CONDOMINIO mancante o non valido");
            return;
        }
        Map<String, Object> condominio = ctx.condominio(idCondominio.intValue());
        if (condominio == null) {
            negato(ctx, res, "Condominio fuori dal perimetro dell'utente", idCondominio, "dati " + operazione);
            return;
        }
        Integer idAzienda = LetturaDataset.intero(condominio, "ID_AZIENDA");

        Map<String, String> param = ctx.parametriBase();
        param.put("ID_CONDOMINIO", idCondominio.toString());
        param.put("ID_AZIENDA", idAzienda == null ? "0" : idAzienda.toString());
        param.put("DOCUMENTI_WERA", "S".equalsIgnoreCase(ctx.parametro(
                CentraleTermicaCostanti.PAR_DOCUMENTI_WERA_VISIBILI, idAzienda)) ? "S" : "N");
        param.put("GIORNI_PREAVVISO", Integer.toString(ctx.parametroIntero(
                CentraleTermicaCostanti.PAR_GIORNI_PREAVVISO, idAzienda, 30)));
        String sezioneInterventi = ctx.parametro(CentraleTermicaCostanti.PAR_SEZIONE_INTERVENTI, idAzienda);
        param.put("SEZIONE_INTERVENTI", sezioneInterventi.matches("[A-Z0-9_]{1,30}") ? sezioneInterventi : "-");

        if ("riepilogo".equals(operazione)) {
            Map<String, Object> riepilogo = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_RIEPILOGO, param,
                    COLONNE_RIEPILOGO);
            Map<String, Object> dati = new LinkedHashMap<String, Object>();
            dati.put("condominio", condominio);
            dati.put("riepilogo", riepilogo);
            rispondi(res, "dati", dati);
        } else if ("sezioni".equals(operazione)) {
            rispondi(res, "sezioni", LetturaDataset.leggi(CentraleTermicaCostanti.DS_SEZIONI, param, COLONNE_SEZIONE));
        } else if ("documenti".equals(operazione)) {
            documenti(ctx, req, res, param, idAzienda);
        } else if ("versioni".equals(operazione)) {
            versioni(ctx, req, res, param, idCondominio, idAzienda);
        } else if ("scadenze".equals(operazione)) {
            rispondi(res, "scadenze", LetturaDataset.leggi(CentraleTermicaCostanti.DS_SCADENZE, param,
                    COLONNE_SCADENZA));
        } else {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "OPERAZIONE non valida");
        }
    }

    /**
     * Documenti del condominio. ID_SEZIONE facoltativo; STATO = CORRENTE (default) | ARCHIVIATO | TUTTI;
     * ULTIMI=S limita al parametro ULTIMI_DOCUMENTI.NUMERO (riquadro "Ultimi documenti aggiornati").
     */
    private void documenti(ContestoUtente ctx, SsbServletRequest req, SsbServletResponse res,
            Map<String, String> param, Integer idAzienda) throws AppCrash {

        Integer idSezione = intero(req.getParameter("ID_SEZIONE"));
        param.put("FILTRO_SEZIONE", idSezione == null ? "" : " AND D.ID_SEZIONE = " + idSezione);

        String stato = req.getParameter("STATO");
        if ("ARCHIVIATO".equals(stato)) {
            param.put("FILTRO_STATO", " AND D.STATO = 'ARCHIVIATO'");
        } else if ("TUTTI".equals(stato)) {
            param.put("FILTRO_STATO", "");
        } else {
            param.put("FILTRO_STATO", " AND D.STATO = 'CORRENTE'");
        }

        int limite = "S".equalsIgnoreCase(req.getParameter("ULTIMI"))
                ? ctx.parametroIntero(CentraleTermicaCostanti.PAR_ULTIMI_DOCUMENTI, idAzienda, 10) : 1000;
        param.put("LIMITE", Integer.toString(limite));

        rispondi(res, "documenti", LetturaDataset.leggi(CentraleTermicaCostanti.DS_DOCUMENTI, param,
                COLONNE_DOCUMENTO));
    }

    /** Versioni di un documento (piu' recente in cima), solo se il documento e' del condominio e leggibile. */
    private void versioni(ContestoUtente ctx, SsbServletRequest req, SsbServletResponse res,
            Map<String, String> param, Integer idCondominio, Integer idAzienda) throws AppCrash {

        Integer idDocumento = intero(req.getParameter("ID_DOCUMENTO"));
        if (idDocumento == null) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "ID_DOCUMENTO mancante o non valido");
            return;
        }
        param.put("ID_DOCUMENTO", idDocumento.toString());
        Map<String, Object> documento = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_DOCUMENTO, param,
                "ID_DOCUMENTO", "ID_CONDOMINIO", "ID_SEZIONE", "STATO");
        if (documento == null || !idCondominio.equals(LetturaDataset.intero(documento, "ID_CONDOMINIO"))) {
            rispondiErrore(res, HttpServletResponse.SC_NOT_FOUND, "Documento non trovato");
            return;
        }
        Map<String, Object> permesso = ctx.permessoSezione(LetturaDataset.intero(documento, "ID_SEZIONE").intValue(),
                idAzienda);
        boolean annullato = CentraleTermicaCostanti.STATO_ANNULLATO.equals(LetturaDataset.testo(documento, "STATO"));
        if (!LetturaDataset.vero(permesso, "FL_LETTURA") || (annullato && !LetturaDataset.vero(permesso,
                "FL_SCRITTURA"))) {
            negato(ctx, res, "Sezione non accessibile al profilo", idCondominio, "versioni " + idDocumento);
            return;
        }
        rispondi(res, "versioni", LetturaDataset.leggi(CentraleTermicaCostanti.DS_VERSIONI, param, COLONNE_VERSIONE));
    }

    private void rispondi(SsbServletResponse res, String chiave, Object dati) throws AppCrash {

        Map<String, Object> risposta = new LinkedHashMap<String, Object>();
        risposta.put("esito", "OK");
        risposta.put(chiave, dati);
        if (dati instanceof List) {
            risposta.put("totale", Integer.valueOf(((List<?>) dati).size()));
        }
        rispondiJson(res, HttpServletResponse.SC_OK, risposta);
    }
}
