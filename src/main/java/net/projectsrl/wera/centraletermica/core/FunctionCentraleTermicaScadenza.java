package net.projectsrl.wera.centraletermica.core;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

/**
 * FUNCTIONID=CentraleTermicaScadenza (solo POST), scadenziario di un condominio.
 * <ul>
 * <li>AZIONE=crea: ID_CONDOMINIO, ID_TIPO_SCADENZA, DATA_SCADENZA, DESCRIZIONE, PERIODICITA_MESI,
 * GIORNI_PREAVVISO, NOTA (facoltativi gli ultimi quattro)</li>
 * <li>AZIONE=chiudi: ID_CONDOMINIO, ID_SCADENZA, DATA_ESECUZIONE (default oggi), ID_DOCUMENTO
 * (facoltativo, documento dello stesso condominio), NOTA. Se il tipo (o la scadenza) e' periodico crea la
 * successiva: data di esecuzione + periodicita, collegata con id_scadenza_precedente. DATA_PROSSIMA (facoltativa)
 * impone la data della successiva (anche per i tipi non periodici).</li>
 * <li>AZIONE=annulla: ID_CONDOMINIO, ID_SCADENZA, NOTA</li>
 * </ul>
 * Serve il permesso di scrittura sulla sezione del tipo di scadenza. Le scadenze non si cancellano mai.
 */
public class FunctionCentraleTermicaScadenza extends FunctionCentraleTermica_base {

    private static final String SQL_PROSSIMA     = "SELECT NEXTVAL('CENTRALE_TERMICA.SCADENZE_ID_SCADENZA_SEQ')";
    private static final String SQL_INSERISCI    = "INSERT INTO CENTRALE_TERMICA.SCADENZE (ID_SCADENZA, ID_AZIENDA, "
            + "ID_CONDOMINIO, ID_TIPO_SCADENZA, DESCRIZIONE, DATA_SCADENZA, PERIODICITA_MESI, GIORNI_PREAVVISO, "
            + "STATO, ID_SCADENZA_PRECEDENTE, NOTA, ID_UTENTE_INS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'APERTA', ?, ?, ?)";
    private static final String SQL_BLOCCA       = "SELECT X.STATO FROM CENTRALE_TERMICA.SCADENZE X "
            + "WHERE X.ID_SCADENZA = ? AND X.ID_CONDOMINIO = ? FOR UPDATE";
    private static final String SQL_CHIUDI       = "UPDATE CENTRALE_TERMICA.SCADENZE SET STATO = 'CHIUSA', "
            + "DATA_ESECUZIONE = ?, ID_DOCUMENTO_CHIUSURA = ?, TS_CHIUSURA = ?, ID_UTENTE_CHIUSURA = ?, "
            + "NOTA = COALESCE(?, NOTA), TS_MOD = ?, ID_UTENTE_MOD = ? WHERE ID_SCADENZA = ?";
    private static final String SQL_ANNULLA      = "UPDATE CENTRALE_TERMICA.SCADENZE SET STATO = 'ANNULLATA', "
            + "TS_CHIUSURA = ?, ID_UTENTE_CHIUSURA = ?, NOTA = COALESCE(?, NOTA), TS_MOD = ?, ID_UTENTE_MOD = ? "
            + "WHERE ID_SCADENZA = ?";

    public FunctionCentraleTermicaScadenza() {

        super();
    }

    public FunctionCentraleTermicaScadenza(ApplicationServices_itf applServices, String functionID,
            String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        rispondiErrore(res, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Usare POST");
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        ContestoUtente ctx = contesto(userInfo, res);
        if (ctx == null) {
            return;
        }
        Integer idCondominio = intero(req.getParameter("ID_CONDOMINIO"));
        if (idCondominio == null) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "ID_CONDOMINIO mancante o non valido");
            return;
        }
        Map<String, Object> condominio = ctx.condominio(idCondominio.intValue());
        if (condominio == null) {
            negato(ctx, res, "Condominio fuori dal perimetro dell'utente", idCondominio, "scadenze");
            return;
        }
        Integer idAzienda = LetturaDataset.intero(condominio, "ID_AZIENDA");

        String azione = req.getParameter("AZIONE");
        try {
            if ("crea".equals(azione)) {
                crea(ctx, req, res, idCondominio, idAzienda);
            } else if ("chiudi".equals(azione) || "annulla".equals(azione)) {
                chiudiOAnnulla(ctx, req, res, idCondominio, idAzienda, "chiudi".equals(azione));
            } else {
                rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "AZIONE non valida");
            }
        } catch (ParseException e) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Data non valida (gg/mm/aaaa)");
        }
    }

    private void crea(ContestoUtente ctx, SsbServletRequest req, SsbServletResponse res, Integer idCondominio,
            Integer idAzienda) throws AppCrash, ParseException {

        Integer idTipo = intero(req.getParameter("ID_TIPO_SCADENZA"));
        java.sql.Date dataScadenza = dataSql(req.getParameter("DATA_SCADENZA"));
        Integer periodicita = interoOpzionale(req, "PERIODICITA_MESI");
        Integer preavviso = interoOpzionale(req, "GIORNI_PREAVVISO");
        if (idTipo == null || dataScadenza == null) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Tipo e data di scadenza obbligatori");
            return;
        }
        if ((periodicita != null && (periodicita.intValue() < 1 || periodicita.intValue() > 240))
                || (preavviso != null && (preavviso.intValue() < 0 || preavviso.intValue() > 365))) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST,
                    "Periodicita' 1-240 mesi, preavviso 0-365 giorni");
            return;
        }
        Map<String, Object> tipo = tipo(ctx, idTipo, idAzienda);
        if (tipo == null) {
            rispondiErrore(res, HttpServletResponse.SC_NOT_FOUND, "Tipo di scadenza non disponibile");
            return;
        }
        if (!LetturaDataset.vero(tipo, "FL_SCRITTURA")) {
            negato(ctx, res, "Profilo senza permesso sulla sezione della scadenza", idCondominio, "crea " + idTipo);
            return;
        }

        Integer idScadenza = null;
        DBTransaction tx = new DBTransaction();
        try {
            idScadenza = Sql.intero(tx, SQL_PROSSIMA);
            Sql.esegui(tx, SQL_INSERISCI, idScadenza, idAzienda, idCondominio, idTipo,
                    Sql.testo(campo(req, "DESCRIZIONE", 200)), dataScadenza, Sql.intero(periodicita),
                    Sql.intero(preavviso), Sql.NULL_INTERO, Sql.testo(campo(req, "NOTA", 2000)), ctx.getIdUtente());
            tx.commit();
        } catch (Throwable t) {
            tx.rollBack();
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Creazione scadenza fallita");
            throw ac;
        } finally {
            tx.end();
        }
        registraScadenza(ctx, CentraleTermicaCostanti.EVENTO_SCADENZA_CREATA, idCondominio, idScadenza, null,
                LetturaDataset.testo(tipo, "CODICE") + " al " + dataScadenza);
        esito(res, idScadenza, null);
    }

    private void chiudiOAnnulla(ContestoUtente ctx, SsbServletRequest req, SsbServletResponse res,
            Integer idCondominio, Integer idAzienda, boolean chiudi) throws AppCrash, ParseException {

        Integer idScadenza = intero(req.getParameter("ID_SCADENZA"));
        if (idScadenza == null) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "ID_SCADENZA mancante o non valido");
            return;
        }
        Map<String, String> param = ctx.parametriBase();
        param.put("ID_SCADENZA", idScadenza.toString());
        param.put("ID_CONDOMINIO", idCondominio.toString());
        Map<String, Object> scadenza = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_SCADENZA, param,
                "ID_SCADENZA", "ID_TIPO_SCADENZA", "STATO", "DATA_SCADENZA", "DESCRIZIONE", "PERIODICITA_MESI",
                "PERIODICITA_EFFETTIVA", "GIORNI_PREAVVISO", "CODICE_TIPO", "FL_SCRITTURA");
        if (scadenza == null) {
            rispondiErrore(res, HttpServletResponse.SC_NOT_FOUND, "Scadenza non trovata");
            return;
        }
        if (!LetturaDataset.vero(scadenza, "FL_SCRITTURA")) {
            negato(ctx, res, "Profilo senza permesso sulla sezione della scadenza", idCondominio,
                    "scadenza " + idScadenza);
            return;
        }
        if (!"APERTA".equals(LetturaDataset.testo(scadenza, "STATO"))) {
            rispondiErrore(res, HttpServletResponse.SC_CONFLICT, "La scadenza non e' piu' aperta");
            return;
        }

        String nota = campo(req, "NOTA", 2000);
        java.sql.Date esecuzione = null;
        java.sql.Date dataProssima = null;
        Integer idDocumento = null;
        if (chiudi) {
            esecuzione = dataSql(req.getParameter("DATA_ESECUZIONE"));
            if (esecuzione == null) {
                esecuzione = new java.sql.Date(System.currentTimeMillis());
            }
            dataProssima = dataSql(req.getParameter("DATA_PROSSIMA"));
            if (dataProssima != null && !dataProssima.after(esecuzione)) {
                rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "La prossima scadenza deve seguire l'esecuzione");
                return;
            }
            idDocumento = intero(req.getParameter("ID_DOCUMENTO"));
            if (idDocumento != null) {
                Map<String, String> pd = ctx.parametriBase();
                pd.put("ID_DOCUMENTO", idDocumento.toString());
                Map<String, Object> documento = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_DOCUMENTO, pd,
                        "ID_DOCUMENTO", "ID_CONDOMINIO", "ID_SEZIONE", "STATO");
                if (documento == null || !idCondominio.equals(LetturaDataset.intero(documento, "ID_CONDOMINIO"))) {
                    rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Documento non del condominio");
                    return;
                }
            }
        }

        Timestamp adesso = new Timestamp(System.currentTimeMillis());
        Integer periodicita = LetturaDataset.intero(scadenza, "PERIODICITA_EFFETTIVA");
        java.sql.Date prossima = null;
        Integer idSuccessiva = null;
        DBTransaction tx = new DBTransaction();
        try {
            String stato = (String) Sql.valore(tx, SQL_BLOCCA, idScadenza, idCondominio);
            if (!"APERTA".equals(stato)) {
                throw new AppCrash("Scadenza " + idScadenza + " non piu' aperta: " + stato);
            }
            if (chiudi) {
                Sql.esegui(tx, SQL_CHIUDI, esecuzione, Sql.intero(idDocumento), adesso, ctx.getIdUtente(),
                        Sql.testo(nota), adesso, ctx.getIdUtente(), idScadenza);
                if (dataProssima != null || (periodicita != null && periodicita.intValue() > 0)) {
                    if (dataProssima != null) {
                        prossima = dataProssima;
                    } else {
                        Calendar c = Calendar.getInstance();
                        c.setTime(esecuzione);
                        c.add(Calendar.MONTH, periodicita.intValue());
                        prossima = new java.sql.Date(c.getTimeInMillis());
                    }
                    idSuccessiva = Sql.intero(tx, SQL_PROSSIMA);
                    Sql.esegui(tx, SQL_INSERISCI, idSuccessiva, idAzienda, idCondominio,
                            LetturaDataset.intero(scadenza, "ID_TIPO_SCADENZA"),
                            Sql.testo(LetturaDataset.testo(scadenza, "DESCRIZIONE")), prossima,
                            Sql.intero(LetturaDataset.intero(scadenza, "PERIODICITA_MESI")),
                            Sql.intero(LetturaDataset.intero(scadenza, "GIORNI_PREAVVISO")), idScadenza,
                            Sql.NULL_TESTO, ctx.getIdUtente());
                }
            } else {
                Sql.esegui(tx, SQL_ANNULLA, adesso, ctx.getIdUtente(), Sql.testo(nota), adesso, ctx.getIdUtente(),
                        idScadenza);
            }
            tx.commit();
        } catch (Throwable t) {
            tx.rollBack();
            AppCrash ac = (t instanceof AppCrash) ? (AppCrash) t : new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Aggiornamento scadenza " + idScadenza + " fallito");
            throw ac;
        } finally {
            tx.end();
        }

        String codice = LetturaDataset.testo(scadenza, "CODICE_TIPO");
        if (chiudi) {
            registraScadenza(ctx, CentraleTermicaCostanti.EVENTO_SCADENZA_CHIUSA, idCondominio, idScadenza,
                    idDocumento, codice + " eseguita il " + esecuzione + (nota == null ? "" : ": " + nota));
            if (idSuccessiva != null) {
                registraScadenza(ctx, CentraleTermicaCostanti.EVENTO_SCADENZA_CREATA, idCondominio, idSuccessiva, null,
                        codice + " al " + prossima + " (rinnovo di " + idScadenza + ")");
            }
        } else {
            registraScadenza(ctx, CentraleTermicaCostanti.EVENTO_SCADENZA_ANNULLATA, idCondominio, idScadenza, null,
                    codice + (nota == null ? "" : ": " + nota));
        }
        esito(res, idScadenza, idSuccessiva == null ? null : new Object[] { idSuccessiva, prossima.toString() });
    }

    /** Tipo di scadenza visibile per l'azienda, con il permesso di scrittura del profilo sulla sua sezione. */
    private Map<String, Object> tipo(ContestoUtente ctx, Integer idTipo, Integer idAzienda) throws AppCrash {

        Map<String, String> param = ctx.parametriBase();
        param.put("ID_AZIENDA", idAzienda == null ? "0" : idAzienda.toString());
        for (Map<String, Object> t : LetturaDataset.leggi(CentraleTermicaCostanti.DS_TIPI_SCADENZA, param,
                "ID_TIPO_SCADENZA", "CODICE", "FL_SCRITTURA")) {
            if (idTipo.equals(LetturaDataset.intero(t, "ID_TIPO_SCADENZA"))) {
                return t;
            }
        }
        return null;
    }

    private void registraScadenza(ContestoUtente ctx, String tipo, Integer idCondominio, Integer idScadenza,
            Integer idDocumento, String dettaglio) {

        registraEvento(ctx, tipo, idCondominio, idDocumento, null, null, "scadenza " + idScadenza + ": " + dettaglio,
                idScadenza);
    }

    private void esito(SsbServletResponse res, Integer idScadenza, Object[] successiva) throws AppCrash {

        Map<String, Object> esito = new LinkedHashMap<String, Object>();
        esito.put("esito", "OK");
        esito.put("id_scadenza", idScadenza);
        if (successiva != null) {
            esito.put("id_scadenza_successiva", successiva[0]);
            esito.put("data_scadenza_successiva", successiva[1]);
        }
        rispondiJson(res, HttpServletResponse.SC_OK, esito);
    }
}
