package net.projectsrl.wera.centraletermica.core;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

/**
 * FUNCTIONID=CentraleTermicaConfigurazioneSalva (solo POST, profili di CONFIGURAZIONE.PROFILI).
 * OGGETTO = sezione | tipo | parametro | categoria; AZIONE = salva (default) | elimina (solo per
 * l'eccezione di un parametro per azienda). Le sezioni e i tipi non si cancellano: si disattivano.
 * Ogni modifica e' registrata in EVENTI (CONFIGURAZIONE).
 */
public class FunctionCentraleTermicaConfigurazioneSalva extends FunctionCentraleTermica_base {

    private static final String SQL_ID_SEZIONE      = "SELECT NEXTVAL('CENTRALE_TERMICA.SEZIONI_ID_SEZIONE_SEQ')";
    private static final String SQL_ESISTE_SEZIONE  = "SELECT COUNT(*) FROM CENTRALE_TERMICA.SEZIONI "
            + "WHERE COALESCE(ID_AZIENDA, 0) = COALESCE(?, 0) AND CODICE = ?";
    private static final String SQL_NUOVA_SEZIONE   = "INSERT INTO CENTRALE_TERMICA.SEZIONI (ID_SEZIONE, ID_AZIENDA, "
            + "CODICE, TITOLO, SOTTOTITOLO, ICONA, TIPO, ORDINE, FL_SCADENZA_RICHIESTA, FL_DISATTIVO, ID_UTENTE_INS) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SEZIONE         = "UPDATE CENTRALE_TERMICA.SEZIONI SET TITOLO = ?, SOTTOTITOLO = ?, "
            + "ICONA = ?, TIPO = ?, ORDINE = ?, FL_SCADENZA_RICHIESTA = ?, FL_DISATTIVO = ?, TS_MOD = ?, "
            + "ID_UTENTE_MOD = ? WHERE ID_SEZIONE = ?";
    private static final String SQL_VIA_PERMESSI    = "DELETE FROM CENTRALE_TERMICA.SEZIONI_PROFILI WHERE ID_SEZIONE = ?";
    private static final String SQL_PERMESSO        = "INSERT INTO CENTRALE_TERMICA.SEZIONI_PROFILI (ID_SEZIONE, "
            + "CODICE_PROFILO, FL_LETTURA, FL_SCRITTURA, ID_UTENTE_INS) VALUES (?, ?, TRUE, ?, ?)";

    private static final String SQL_ID_TIPO         = "SELECT NEXTVAL('CENTRALE_TERMICA.TIPI_SCADENZA_ID_TIPO_SCADENZA_SEQ')";
    private static final String SQL_ESISTE_TIPO     = "SELECT COUNT(*) FROM CENTRALE_TERMICA.TIPI_SCADENZA "
            + "WHERE COALESCE(ID_AZIENDA, 0) = COALESCE(?, 0) AND CODICE = ?";
    private static final String SQL_NUOVO_TIPO      = "INSERT INTO CENTRALE_TERMICA.TIPI_SCADENZA (ID_TIPO_SCADENZA, "
            + "ID_AZIENDA, CODICE, DESCRIZIONE, ID_SEZIONE, PERIODICITA_MESI, GIORNI_PREAVVISO, RIFERIMENTO_NORMATIVO, "
            + "ORDINE, FL_DISATTIVO, ID_UTENTE_INS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_TIPO            = "UPDATE CENTRALE_TERMICA.TIPI_SCADENZA SET DESCRIZIONE = ?, "
            + "ID_SEZIONE = ?, PERIODICITA_MESI = ?, GIORNI_PREAVVISO = ?, RIFERIMENTO_NORMATIVO = ?, ORDINE = ?, "
            + "FL_DISATTIVO = ?, TS_MOD = ?, ID_UTENTE_MOD = ? WHERE ID_TIPO_SCADENZA = ?";

    private static final String SQL_TIPO_PARAMETRO  = "SELECT TIPO_VALORE FROM CENTRALE_TERMICA.PARAMETRI "
            + "WHERE ID_AZIENDA IS NULL AND CHIAVE = ?";
    private static final String SQL_PARAMETRO_DEF   = "UPDATE CENTRALE_TERMICA.PARAMETRI SET VALORE = ?, TS_MOD = ?, "
            + "ID_UTENTE_MOD = ? WHERE ID_AZIENDA IS NULL AND CHIAVE = ?";
    private static final String SQL_PARAMETRO_AZ    = "UPDATE CENTRALE_TERMICA.PARAMETRI SET VALORE = ?, TS_MOD = ?, "
            + "ID_UTENTE_MOD = ? WHERE ID_AZIENDA = ? AND CHIAVE = ?";
    private static final String SQL_NUOVO_PARAMETRO = "INSERT INTO CENTRALE_TERMICA.PARAMETRI (ID_AZIENDA, CHIAVE, "
            + "VALORE, TIPO_VALORE, ID_UTENTE_INS) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_VIA_PARAMETRO   = "DELETE FROM CENTRALE_TERMICA.PARAMETRI WHERE ID_AZIENDA = ? "
            + "AND CHIAVE = ?";

    private static final String SQL_CATEGORIA       = "UPDATE CENTRALE_TERMICA.CATEGORIE_WERA SET ID_SEZIONE = ?, "
            + "TS_MOD = ?, ID_UTENTE_MOD = ? WHERE ID_AZIENDA IS NULL AND CODICE_CATEGORIA = ?";
    private static final String SQL_NUOVA_CATEGORIA = "INSERT INTO CENTRALE_TERMICA.CATEGORIE_WERA (CODICE_CATEGORIA, "
            + "ID_SEZIONE, ID_UTENTE_INS) VALUES (?, ?, ?)";

    private static final Set<String> TIPI_SEZIONE   = new HashSet<String>(java.util.Arrays.asList("DOCUMENTI", "FOTO",
            "RISERVATA"));

    /** Errore di validazione da restituire all'utente (400/404/409). */
    private static final class Rifiuto extends Exception {

        private static final long serialVersionUID = 1L;
        final int                 status;

        Rifiuto(int status, String messaggio) {

            super(messaggio);
            this.status = status;
        }
    }

    public FunctionCentraleTermicaConfigurazioneSalva() {

        super();
    }

    public FunctionCentraleTermicaConfigurazioneSalva(ApplicationServices_itf applServices, String functionID,
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
        if (!ctx.puoConfigurare()) {
            negato(ctx, res, "Profilo non abilitato alla configurazione", null, "salva configurazione");
            return;
        }

        String oggetto = req.getParameter("OGGETTO");
        DBTransaction tx = new DBTransaction();
        String dettaglio;
        Integer id = null;
        try {
            if ("sezione".equals(oggetto)) {
                Object[] r = salvaSezione(ctx, req, tx);
                id = (Integer) r[0];
                dettaglio = (String) r[1];
            } else if ("tipo".equals(oggetto)) {
                Object[] r = salvaTipo(ctx, req, tx);
                id = (Integer) r[0];
                dettaglio = (String) r[1];
            } else if ("parametro".equals(oggetto)) {
                dettaglio = salvaParametro(ctx, req, tx);
            } else if ("categoria".equals(oggetto)) {
                dettaglio = salvaCategoria(ctx, req, tx);
            } else {
                throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "OGGETTO non valido");
            }
            tx.commit();
        } catch (Rifiuto r) {
            tx.rollBack();
            tx.end();
            rispondiErrore(res, r.status, r.getMessage());
            return;
        } catch (Throwable t) {
            tx.rollBack();
            tx.end();
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Salvataggio configurazione fallito: " + oggetto);
            throw ac;
        }
        tx.end();

        registraEvento(ctx, CentraleTermicaCostanti.EVENTO_CONFIGURAZIONE, null, null, null, null, oggetto + " "
                + dettaglio);
        Map<String, Object> esito = new LinkedHashMap<String, Object>();
        esito.put("esito", "OK");
        if (id != null) {
            esito.put("id", id);
        }
        rispondiJson(res, HttpServletResponse.SC_OK, esito);
    }

    // ---------------------------------------------------------------------------------------- sezioni

    private Object[] salvaSezione(ContestoUtente ctx, SsbServletRequest req, DBTransaction tx) throws Exception {

        Integer idSezione = intero(req.getParameter("ID_SEZIONE"));
        String titolo = obbligatorio(campo(req, "TITOLO", 100), "Titolo obbligatorio");
        String sottotitolo = campo(req, "SOTTOTITOLO", 200);
        String icona = campo(req, "ICONA", 50);
        if (icona != null && !icona.matches("fa-[a-z0-9-]{1,40}")) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Icona: una classe Font Awesome 4, es. fa-book");
        }
        String tipo = campo(req, "TIPO", 10);
        if (!TIPI_SEZIONE.contains(tipo)) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Tipo sezione non valido");
        }
        int ordine = numero(req, "ORDINE", 0, 9999, 0);
        boolean scadenza = "S".equals(req.getParameter("FL_SCADENZA_RICHIESTA"));
        boolean disattivo = "S".equals(req.getParameter("FL_DISATTIVO"));
        Map<String, String> permessi = permessi(ctx, req.getParameter("PERMESSI"));
        Timestamp adesso = new Timestamp(System.currentTimeMillis());

        String codice;
        if (idSezione == null) {
            codice = codice(campo(req, "CODICE", 30));
            Integer idAzienda = aziendaNuova(ctx, req);
            if (Sql.intero(tx, SQL_ESISTE_SEZIONE, Sql.intero(idAzienda), codice).intValue() > 0) {
                throw new Rifiuto(HttpServletResponse.SC_CONFLICT, "Esiste gia' una sezione " + codice);
            }
            idSezione = Sql.intero(tx, SQL_ID_SEZIONE);
            Sql.esegui(tx, SQL_NUOVA_SEZIONE, idSezione, Sql.intero(idAzienda), codice, titolo, Sql.testo(sottotitolo),
                    Sql.testo(icona), tipo, Integer.valueOf(ordine), Boolean.valueOf(scadenza),
                    Boolean.valueOf(disattivo), ctx.getIdUtente());
        } else {
            codice = "id " + idSezione;
            if (Sql.esegui(tx, SQL_SEZIONE, titolo, Sql.testo(sottotitolo), Sql.testo(icona), tipo,
                    Integer.valueOf(ordine), Boolean.valueOf(scadenza), Boolean.valueOf(disattivo), adesso,
                    ctx.getIdUtente(), idSezione) != 1) {
                throw new Rifiuto(HttpServletResponse.SC_NOT_FOUND, "Sezione non trovata");
            }
        }

        Sql.esegui(tx, SQL_VIA_PERMESSI, idSezione);
        StringBuilder p = new StringBuilder();
        for (Map.Entry<String, String> e : permessi.entrySet()) {
            Sql.esegui(tx, SQL_PERMESSO, idSezione, e.getKey(), Boolean.valueOf("RW".equals(e.getValue())),
                    ctx.getIdUtente());
            p.append(' ').append(e.getKey()).append(':').append(e.getValue());
        }
        return new Object[] { idSezione, codice + " '" + titolo + "' " + tipo + (disattivo ? " DISATTIVA" : "")
                + " permessi" + p };
    }

    /** PERMESSI = "ADM:RW,INS:RW,AMM:R": profili esistenti, livelli R | RW (il resto = nessun accesso). */
    private Map<String, String> permessi(ContestoUtente ctx, String valore) throws Exception {

        Set<String> profili = new HashSet<String>();
        for (Map<String, Object> p : LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_PROFILI,
                ctx.parametriBase(), "CODICE")) {
            profili.add(LetturaDataset.testo(p, "CODICE"));
        }
        Map<String, String> permessi = new LinkedHashMap<String, String>();
        if (valore == null || valore.trim().length() == 0) {
            return permessi;
        }
        for (String voce : valore.split(",")) {
            String[] kv = voce.trim().split(":");
            if (kv.length != 2 || !profili.contains(kv[0].trim())) {
                throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Permessi non validi: " + voce);
            }
            String livello = kv[1].trim();
            if ("R".equals(livello) || "RW".equals(livello)) {
                permessi.put(kv[0].trim(), livello);
            } else if (!"-".equals(livello)) {
                throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Permessi non validi: " + voce);
            }
        }
        return permessi;
    }

    // ---------------------------------------------------------------------------------------- tipi di scadenza

    private Object[] salvaTipo(ContestoUtente ctx, SsbServletRequest req, DBTransaction tx) throws Exception {

        Integer idTipo = intero(req.getParameter("ID_TIPO_SCADENZA"));
        String descrizione = obbligatorio(campo(req, "DESCRIZIONE", 200), "Descrizione obbligatoria");
        Integer idSezione = intero(req.getParameter("ID_SEZIONE"));
        if (idSezione == null) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Sezione obbligatoria");
        }
        Integer periodicita = opzionale(req, "PERIODICITA_MESI", 1, 240);
        Integer preavviso = opzionale(req, "GIORNI_PREAVVISO", 0, 365);
        String riferimento = campo(req, "RIFERIMENTO_NORMATIVO", 200);
        int ordine = numero(req, "ORDINE", 0, 9999, 0);
        boolean disattivo = "S".equals(req.getParameter("FL_DISATTIVO"));
        Timestamp adesso = new Timestamp(System.currentTimeMillis());

        String codice;
        if (idTipo == null) {
            codice = codice(campo(req, "CODICE", 30));
            Integer idAzienda = aziendaNuova(ctx, req);
            controllaSezione(ctx, idSezione, idAzienda);
            if (Sql.intero(tx, SQL_ESISTE_TIPO, Sql.intero(idAzienda), codice).intValue() > 0) {
                throw new Rifiuto(HttpServletResponse.SC_CONFLICT, "Esiste gia' un tipo " + codice);
            }
            idTipo = Sql.intero(tx, SQL_ID_TIPO);
            Sql.esegui(tx, SQL_NUOVO_TIPO, idTipo, Sql.intero(idAzienda), codice, descrizione, idSezione,
                    Sql.intero(periodicita), Sql.intero(preavviso), Sql.testo(riferimento), Integer.valueOf(ordine),
                    Boolean.valueOf(disattivo), ctx.getIdUtente());
        } else {
            codice = "id " + idTipo;
            controllaSezione(ctx, idSezione, aziendaTipo(ctx, idTipo));
            if (Sql.esegui(tx, SQL_TIPO, descrizione, idSezione, Sql.intero(periodicita), Sql.intero(preavviso),
                    Sql.testo(riferimento), Integer.valueOf(ordine), Boolean.valueOf(disattivo), adesso,
                    ctx.getIdUtente(), idTipo) != 1) {
                throw new Rifiuto(HttpServletResponse.SC_NOT_FOUND, "Tipo di scadenza non trovato");
            }
        }
        return new Object[] { idTipo, codice + " '" + descrizione + "' sezione " + idSezione + " ogni " + periodicita
                + " mesi" + (disattivo ? " DISATTIVO" : "") };
    }

    /** La sezione deve esistere ed essere globale o della stessa azienda del tipo. */
    private void controllaSezione(ContestoUtente ctx, Integer idSezione, Integer idAzienda) throws Exception {

        for (Map<String, Object> s : LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_SEZIONI,
                ctx.parametriBase(), "ID_SEZIONE", "ID_AZIENDA")) {
            if (idSezione.equals(LetturaDataset.intero(s, "ID_SEZIONE"))) {
                Integer az = LetturaDataset.intero(s, "ID_AZIENDA");
                if (az == null || az.equals(idAzienda)) {
                    return;
                }
                throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Sezione di un'altra azienda");
            }
        }
        throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Sezione inesistente");
    }

    private Integer aziendaTipo(ContestoUtente ctx, Integer idTipo) throws Exception {

        for (Map<String, Object> t : LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_TIPI, ctx.parametriBase(),
                "ID_TIPO_SCADENZA", "ID_AZIENDA")) {
            if (idTipo.equals(LetturaDataset.intero(t, "ID_TIPO_SCADENZA"))) {
                return LetturaDataset.intero(t, "ID_AZIENDA");
            }
        }
        throw new Rifiuto(HttpServletResponse.SC_NOT_FOUND, "Tipo di scadenza non trovato");
    }

    // ---------------------------------------------------------------------------------------- parametri

    private String salvaParametro(ContestoUtente ctx, SsbServletRequest req, DBTransaction tx) throws Exception {

        String chiave = campo(req, "CHIAVE", 60);
        String tipoValore = chiave == null ? null : (String) Sql.valore(tx, SQL_TIPO_PARAMETRO, chiave);
        if (tipoValore == null) {
            throw new Rifiuto(HttpServletResponse.SC_NOT_FOUND, "Parametro inesistente");
        }
        Integer idAzienda = intero(req.getParameter("ID_AZIENDA"));
        if (!DatiConfigurazione.aziendaAmmessa(ctx, idAzienda)) {
            throw new Rifiuto(HttpServletResponse.SC_FORBIDDEN, "Azienda non configurabile");
        }
        Timestamp adesso = new Timestamp(System.currentTimeMillis());

        if ("elimina".equals(req.getParameter("AZIONE"))) {
            if (idAzienda == null) {
                throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Il valore predefinito non si elimina");
            }
            controllaConfigurazione(ctx, chiave, idAzienda, null, true);
            Sql.esegui(tx, SQL_VIA_PARAMETRO, idAzienda, chiave);
            return chiave + " azienda " + idAzienda + ": eccezione eliminata";
        }

        String valore = valoreValido(tipoValore.trim(), chiave, req.getParameter("VALORE"));
        controllaConfigurazione(ctx, chiave, idAzienda, valore, false);
        if (idAzienda == null) {
            Sql.esegui(tx, SQL_PARAMETRO_DEF, valore, adesso, ctx.getIdUtente(), chiave);
        } else if (Sql.esegui(tx, SQL_PARAMETRO_AZ, valore, adesso, ctx.getIdUtente(), idAzienda, chiave) == 0) {
            Sql.esegui(tx, SQL_NUOVO_PARAMETRO, idAzienda, chiave, valore, tipoValore.trim(), ctx.getIdUtente());
        }
        return chiave + (idAzienda == null ? " (predefinito)" : " azienda " + idAzienda) + " = '" + valore + "'";
    }

    /** Valida e normalizza il valore secondo il tipo del parametro e alcune chiavi note. */
    static String valoreValido(String tipo, String chiave, String grezzo) throws Rifiuto {

        String v = grezzo == null ? "" : grezzo.replaceAll("[\\p{Cntrl}]", " ").trim();
        if (v.length() > 2000) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Valore troppo lungo");
        }
        boolean ok;
        if ("NUMERO".equals(tipo)) {
            ok = v.matches("[0-9]{1,9}");
        } else if ("BOOLEANO".equals(tipo)) {
            v = v.toUpperCase();
            ok = v.equals("S") || v.equals("N");
        } else if ("EMAIL".equals(tipo)) {
            ok = v.length() == 0 || v.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
        } else if ("URL".equals(tipo)) {
            ok = v.length() == 0 || v.matches("(?i)https?://\\S+") || v.matches("astro\\?\\S+");
        } else if ("TELEFONO".equals(tipo)) {
            ok = v.length() == 0 || v.matches("[0-9 +./()-]{3,30}");
        } else if ("ELENCO".equals(tipo)) {
            v = v.replaceAll("\\s+", "");
            v = chiave.endsWith(".PROFILI") ? v.toUpperCase() : v.toLowerCase();
            ok = v.matches("[A-Za-z0-9_.-]*(,[A-Za-z0-9_.-]+)*");
        } else {
            ok = true;
        }
        if (ok && chiave.equals(CentraleTermicaCostanti.PAR_FILE_CARTELLA)) {
            ok = v.matches("[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+)*/") && !v.matches("(?i)upload/");
        }
        if (ok && chiave.equals(CentraleTermicaCostanti.PAR_SEZIONE_INTERVENTI)) {
            ok = v.matches("[A-Z0-9_]{1,30}");
        }
        if (!ok) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Valore non valido per " + chiave + " (" + tipo + ")");
        }
        return v;
    }

    /** Nessuno puo' togliere a se stesso l'accesso o la configurazione (si resterebbe chiusi fuori). */
    private void controllaConfigurazione(ContestoUtente ctx, String chiave, Integer idAzienda, String valore,
            boolean elimina) throws Exception {

        boolean critica = chiave.equals(CentraleTermicaCostanti.PAR_CONFIGURAZIONE_PROFILI)
                || chiave.equals(CentraleTermicaCostanti.PAR_ACCESSO_PROFILI);
        boolean miaAzienda = idAzienda == null ? true : idAzienda.equals(ctx.getIdAzienda());
        if (!critica || !miaAzienda) {
            return;
        }
        String effettivo = valore;
        if (elimina) {
            effettivo = ctx.parametro(chiave, null); // tolta l'eccezione, vale il predefinito
        } else if (idAzienda == null) {
            // cambia il predefinito: conta solo se l'azienda dell'utente non ha un'eccezione propria
            String proprio = ctx.parametro(chiave, ctx.getIdAzienda());
            String predefinito = ctx.parametro(chiave, null);
            if (!proprio.equals(predefinito)) {
                return;
            }
        }
        if (!ContestoUtente.elenco(effettivo).contains(ctx.getProfilo())) {
            throw new Rifiuto(HttpServletResponse.SC_CONFLICT, "Non puoi togliere il tuo profilo ("
                    + ctx.getProfilo() + ") da " + chiave);
        }
    }

    // ---------------------------------------------------------------------------------------- categorie /wera

    private String salvaCategoria(ContestoUtente ctx, SsbServletRequest req, DBTransaction tx) throws Exception {

        String codice = campo(req, "CODICE_CATEGORIA", 3);
        boolean esiste = false;
        for (Map<String, Object> c : LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_CATEGORIE,
                ctx.parametriBase(), "CODICE_CATEGORIA")) {
            esiste = esiste || (codice != null && codice.equals(LetturaDataset.testo(c, "CODICE_CATEGORIA")));
        }
        if (!esiste) {
            throw new Rifiuto(HttpServletResponse.SC_NOT_FOUND, "Categoria inesistente");
        }
        Integer idSezione = intero(req.getParameter("ID_SEZIONE"));
        if (idSezione != null) {
            controllaSezione(ctx, idSezione, null);
        }
        Timestamp adesso = new Timestamp(System.currentTimeMillis());
        if (Sql.esegui(tx, SQL_CATEGORIA, Sql.intero(idSezione), adesso, ctx.getIdUtente(), codice) == 0) {
            Sql.esegui(tx, SQL_NUOVA_CATEGORIA, codice, Sql.intero(idSezione), ctx.getIdUtente());
        }
        return codice + " -> " + (idSezione == null ? "non mostrata" : "sezione " + idSezione);
    }

    // ---------------------------------------------------------------------------------------- utilita

    private static String obbligatorio(String v, String messaggio) throws Rifiuto {

        if (v == null) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, messaggio);
        }
        return v;
    }

    private static String codice(String v) throws Rifiuto {

        String c = v == null ? "" : v.toUpperCase();
        if (!c.matches("[A-Z0-9_]{1,30}")) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, "Codice: maiuscole, cifre e _ (max 30)");
        }
        return c;
    }

    private static int numero(SsbServletRequest req, String nome, int min, int max, int predefinito) throws Rifiuto {

        Integer v = interoOpzionale(req, nome);
        if (v == null) {
            return predefinito;
        }
        if (v.intValue() < min || v.intValue() > max) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, nome + " fuori intervallo (" + min + "-" + max + ")");
        }
        return v.intValue();
    }

    private static Integer opzionale(SsbServletRequest req, String nome, int min, int max) throws Rifiuto {

        Integer v = interoOpzionale(req, nome);
        if (v != null && (v.intValue() < min || v.intValue() > max)) {
            throw new Rifiuto(HttpServletResponse.SC_BAD_REQUEST, nome + " fuori intervallo (" + min + "-" + max + ")");
        }
        return v;
    }

    /** Azienda di una sezione o di un tipo NUOVO: vuota = tutte; altrimenti deve essere configurabile. */
    private static Integer aziendaNuova(ContestoUtente ctx, SsbServletRequest req) throws Exception {

        Integer idAzienda = intero(req.getParameter("ID_AZIENDA"));
        if (!DatiConfigurazione.aziendaAmmessa(ctx, idAzienda)) {
            throw new Rifiuto(HttpServletResponse.SC_FORBIDDEN, "Azienda non configurabile");
        }
        return idAzienda;
    }
}
