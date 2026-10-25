package net.projectsrl.wera.centraletermica.core;

import java.io.File;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.wera.centraletermica.db.DocumentiDAO;
import net.projectsrl.wera.centraletermica.db.VersioniDAO;

/**
 * FUNCTIONID=CentraleTermicaUpload (solo POST multipart).
 * <p>
 * Campi: ID_CONDOMINIO (obbligatorio); ID_SEZIONE per documenti nuovi, oppure ID_DOCUMENTO per caricare
 * una NUOVA VERSIONE (un solo file); TITOLO, DESCRIZIONE, DATA_DOCUMENTO (gg/mm/aaaa o aaaa-mm-gg), NOTA
 * facoltativi. Con piu' file e senza ID_DOCUMENTO ogni file diventa un documento (titolo dal nome se
 * TITOLO manca o se i file sono piu' di uno).
 * <p>
 * Per ogni file: scrittura su disco con nome univoco e SHA-256, poi una transazione (documento,
 * versione, versione corrente). Se il DB fallisce il file appena scritto viene rimosso. Risposta JSON
 * con l'esito file per file.
 */
@SuppressWarnings("deprecation")
public class FunctionCentraleTermicaUpload extends FunctionCentraleTermica_base {

    private static final String SQL_PROSSIMO_DOCUMENTO = "SELECT NEXTVAL('CENTRALE_TERMICA.DOCUMENTI_ID_DOCUMENTO_SEQ')";
    private static final String SQL_PROSSIMA_VERSIONE  = "SELECT NEXTVAL('CENTRALE_TERMICA.VERSIONI_ID_VERSIONE_SEQ')";
    private static final String SQL_BLOCCA_DOCUMENTO   = "SELECT STATO FROM CENTRALE_TERMICA.DOCUMENTI WHERE ID_DOCUMENTO = ? FOR UPDATE";
    private static final String SQL_NUMERO_VERSIONE    = "SELECT COALESCE(MAX(NUMERO_VERSIONE), 0) + 1 FROM CENTRALE_TERMICA.VERSIONI WHERE ID_DOCUMENTO = ?";

    private static final long   MEGABYTE               = 1024L * 1024L;

    public FunctionCentraleTermicaUpload() {

        super();
    }

    public FunctionCentraleTermicaUpload(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        rispondiErrore(res, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Usare POST multipart");
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        ContestoUtente ctx = contesto(userInfo, res);
        if (ctx == null) {
            return;
        }

        int maxMb = ctx.parametroIntero(CentraleTermicaCostanti.PAR_FILE_DIMENSIONE_MAX_MB, ctx.getIdAzienda(), 25);
        int maxFile = ctx.parametroIntero(CentraleTermicaCostanti.PAR_FILE_MAX_PER_CARICAMENTO, ctx.getIdAzienda(), 20);

        Map<String, String> campi = new HashMap<String, String>();
        List<FileItem> files = new ArrayList<FileItem>();
        try {
            DiskFileUpload fu = new DiskFileUpload();
            fu.setHeaderEncoding("UTF-8");
            fu.setSizeMax(maxMb * MEGABYTE * maxFile + MEGABYTE);
            @SuppressWarnings("unchecked")
            List<FileItem> items = fu.parseRequest(req);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    campi.put(item.getFieldName(), item.getString("UTF-8"));
                } else if (Util.IsNotEmpty(item.getName())) {
                    files.add(item);
                }
            }
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Richiesta di caricamento non leggibile");
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST,
                    "Richiesta non valida o troppo grande (massimo " + maxFile + " file da " + maxMb + " MB)");
            return;
        }

        try {
            carica(ctx, campi, files, maxMb, maxFile, res);
        } finally {
            for (FileItem item : files) {
                item.delete();
            }
        }
    }

    private void carica(ContestoUtente ctx, Map<String, String> campi, List<FileItem> files, int maxMb, int maxFile,
            SsbServletResponse res) throws AppCrash {

        // --- condominio nel perimetro
        Integer idCondominio = intero(campi.get("ID_CONDOMINIO"));
        if (idCondominio == null) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "ID_CONDOMINIO mancante o non valido");
            return;
        }
        Map<String, Object> condominio = ctx.condominio(idCondominio.intValue());
        if (condominio == null) {
            negato(ctx, res, "Condominio fuori dal perimetro dell'utente", idCondominio, "upload");
            return;
        }
        Integer idAzienda = LetturaDataset.intero(condominio, "ID_AZIENDA");

        // --- documento esistente (nuova versione) o sezione (documenti nuovi)
        Integer idDocumento = intero(campi.get("ID_DOCUMENTO"));
        Integer idSezione;
        if (idDocumento != null) {
            Map<String, String> param = ctx.parametriBase();
            param.put("ID_DOCUMENTO", idDocumento.toString());
            Map<String, Object> documento = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_DOCUMENTO, param,
                    "ID_DOCUMENTO", "ID_CONDOMINIO", "ID_SEZIONE", "STATO");
            if (documento == null || !idCondominio.equals(LetturaDataset.intero(documento, "ID_CONDOMINIO"))) {
                negato(ctx, res, "Documento non del condominio indicato", idCondominio, "ID_DOCUMENTO=" + idDocumento);
                return;
            }
            if (!CentraleTermicaCostanti.STATO_CORRENTE.equals(LetturaDataset.testo(documento, "STATO"))) {
                rispondiErrore(res, HttpServletResponse.SC_CONFLICT,
                        "Si possono aggiungere versioni solo ai documenti correnti");
                return;
            }
            if (files.size() != 1) {
                rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Una nuova versione richiede un solo file");
                return;
            }
            idSezione = LetturaDataset.intero(documento, "ID_SEZIONE");
        } else {
            idSezione = intero(campi.get("ID_SEZIONE"));
            if (idSezione == null) {
                rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "ID_SEZIONE o ID_DOCUMENTO obbligatorio");
                return;
            }
        }
        if (!ctx.puoScrivere(idSezione.intValue(), idAzienda)) {
            negato(ctx, res, "Profilo senza permesso di caricamento nella sezione", idCondominio,
                    "ID_SEZIONE=" + idSezione);
            return;
        }

        // --- controlli sui campi e sui file
        if (files.isEmpty()) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Nessun file ricevuto");
            return;
        }
        if (files.size() > maxFile) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Massimo " + maxFile + " file per caricamento");
            return;
        }
        Timestamp dataDocumento;
        try {
            dataDocumento = data(campi.get("DATA_DOCUMENTO"));
        } catch (ParseException e) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "DATA_DOCUMENTO non valida (gg/mm/aaaa)");
            return;
        }
        String titolo = tronca(campi.get("TITOLO"), 200);
        String descrizione = tronca(campi.get("DESCRIZIONE"), 4000);
        String nota = tronca(campi.get("NOTA"), 500);

        List<String> estensioni = ContestoUtente.elenco(ctx.parametro(CentraleTermicaCostanti.PAR_FILE_ESTENSIONI,
                idAzienda));
        File cartella = ArchivioFile.cartellaCentraleTermica(_applicationSrv.getRoot(),
                ctx.parametro(CentraleTermicaCostanti.PAR_FILE_CARTELLA, idAzienda));

        // --- un documento (o una versione) per file, ognuno con la sua transazione
        List<Map<String, Object>> esiti = new ArrayList<Map<String, Object>>();
        int riusciti = 0;
        for (FileItem item : files) {
            String nomeFile = tronca(ArchivioFile.nomeSemplice(item.getName()), 255);
            Map<String, Object> esito = new LinkedHashMap<String, Object>();
            esito.put("nome_file", nomeFile);
            esiti.add(esito);

            if (!ArchivioFile.estensioneAmmessa(nomeFile, estensioni)) {
                esito.put("esito", "KO");
                esito.put("messaggio", "Estensione non ammessa");
                continue;
            }
            if (item.getSize() > maxMb * MEGABYTE) {
                esito.put("esito", "KO");
                esito.put("messaggio", "File oltre " + maxMb + " MB");
                continue;
            }

            String titoloFile = (titolo != null && files.size() == 1) ? titolo : titoloDaNome(nomeFile);
            try {
                salva(ctx, item, nomeFile, cartella, idAzienda, idCondominio, idSezione, idDocumento, titoloFile,
                        descrizione, dataDocumento, nota, maxMb * MEGABYTE, esito);
                riusciti++;
            } catch (Throwable t) {
                AppCrash ac = (t instanceof AppCrash) ? (AppCrash) t : new AppCrash(t);
                ac.logContext(this.getClass().getName(), "Caricamento fallito: " + nomeFile);
                esito.put("esito", "KO");
                esito.put("messaggio", "Errore nel salvataggio del file");
            }
        }

        Map<String, Object> risposta = new LinkedHashMap<String, Object>();
        risposta.put("esito", riusciti == files.size() ? "OK" : (riusciti == 0 ? "KO" : "PARZIALE"));
        risposta.put("file", esiti);
        rispondiJson(res, riusciti == 0 ? HttpServletResponse.SC_BAD_REQUEST : HttpServletResponse.SC_OK, risposta);
    }

    private void salva(ContestoUtente ctx, FileItem item, String nomeFile, File cartella, Integer idAzienda,
            Integer idCondominio, Integer idSezione, Integer idDocumentoEsistente, String titolo, String descrizione,
            Timestamp dataDocumento, String nota, long maxByte, Map<String, Object> esito) throws Throwable {

        ArchivioFile.FileScritto scritto;
        InputStream in = item.getInputStream();
        try {
            scritto = ArchivioFile.scrivi(in, cartella, idAzienda.intValue(), idCondominio.intValue(), nomeFile,
                    maxByte);
        } finally {
            ArchivioFile.chiudi(in);
        }

        Timestamp adesso = new Timestamp(System.currentTimeMillis());
        DBTransaction tx = new DBTransaction();
        boolean registrato = false;
        Integer idDocumento = idDocumentoEsistente;
        Integer idVersione;
        int numeroVersione;
        try {
            if (idDocumento == null) {
                idDocumento = Integer.valueOf(valoreIntero(tx, SQL_PROSSIMO_DOCUMENTO, null));
                DocumentiDAO documento = new DocumentiDAO(tx);
                documento.setAttribute(DocumentiDAO.ID_DOCUMENTO, idDocumento);
                documento.setAttribute(DocumentiDAO.ID_AZIENDA, idAzienda);
                documento.setAttribute(DocumentiDAO.ID_CONDOMINIO, idCondominio);
                documento.setAttribute(DocumentiDAO.ID_SEZIONE, idSezione);
                documento.setAttribute(DocumentiDAO.TITOLO, titolo);
                if (descrizione != null) {
                    documento.setAttribute(DocumentiDAO.DESCRIZIONE, descrizione);
                }
                if (dataDocumento != null) {
                    documento.setAttribute(DocumentiDAO.DATA_DOCUMENTO, dataDocumento);
                }
                documento.setAttribute(DocumentiDAO.STATO, CentraleTermicaCostanti.STATO_CORRENTE);
                documento.setAttribute(DocumentiDAO.ID_UTENTE_INS, ctx.getIdUtente());
                documento.insert();
                numeroVersione = 1;
            } else {
                String stato = valoreTesto(tx, SQL_BLOCCA_DOCUMENTO, idDocumento);
                if (!CentraleTermicaCostanti.STATO_CORRENTE.equals(stato)) {
                    throw new AppCrash("Documento " + idDocumento + " non piu' corrente: " + stato);
                }
                numeroVersione = valoreIntero(tx, SQL_NUMERO_VERSIONE, idDocumento);
            }

            idVersione = Integer.valueOf(valoreIntero(tx, SQL_PROSSIMA_VERSIONE, null));
            VersioniDAO versione = new VersioniDAO(tx);
            versione.setAttribute(VersioniDAO.ID_VERSIONE, idVersione);
            versione.setAttribute(VersioniDAO.ID_DOCUMENTO, idDocumento);
            versione.setAttribute(VersioniDAO.NUMERO_VERSIONE, Integer.valueOf(numeroVersione));
            versione.setAttribute(VersioniDAO.NOME_FILE_ORIGINALE, nomeFile);
            versione.setAttribute(VersioniDAO.PERCORSO_FILE, scritto.percorsoRelativo);
            versione.setAttribute(VersioniDAO.TIPO_MIME, ArchivioFile.tipoMime(nomeFile));
            versione.setAttribute(VersioniDAO.DIMENSIONE_BYTE, Long.valueOf(scritto.dimensione));
            versione.setAttribute(VersioniDAO.IMPRONTA_SHA256, scritto.sha256);
            if (nota != null) {
                versione.setAttribute(VersioniDAO.NOTA, nota);
            }
            versione.setAttribute(VersioniDAO.ID_UTENTE_INS, ctx.getIdUtente());
            versione.insert();

            DocumentiDAO corrente = new DocumentiDAO(tx);
            corrente.setAttribute(DocumentiDAO.ID_DOCUMENTO, idDocumento);
            corrente.setAttribute(DocumentiDAO.ID_VERSIONE_CORRENTE, idVersione);
            corrente.setAttribute(DocumentiDAO.TS_MOD, adesso);
            corrente.setAttribute(DocumentiDAO.ID_UTENTE_MOD, ctx.getIdUtente());
            corrente.update();

            tx.commit();
            registrato = true;
        } catch (Throwable t) {
            tx.rollBack();
            throw t;
        } finally {
            tx.end();
            if (!registrato) {
                ArchivioFile.elimina(scritto.file);
            }
        }

        registraEvento(ctx, idDocumentoEsistente == null ? CentraleTermicaCostanti.EVENTO_CARICAMENTO
                : CentraleTermicaCostanti.EVENTO_NUOVA_VERSIONE, idCondominio, idDocumento, idVersione, null,
                nomeFile + " (" + scritto.dimensione + " byte, sha256 " + scritto.sha256 + ")");

        esito.put("esito", "OK");
        esito.put("id_documento", idDocumento);
        esito.put("id_versione", idVersione);
        esito.put("numero_versione", Integer.valueOf(numeroVersione));
        esito.put("dimensione_byte", Long.valueOf(scritto.dimensione));
        esito.put("impronta_sha256", scritto.sha256);
    }

    private static int valoreIntero(DBTransaction tx, String sql, Integer parametro) throws Throwable {

        PreparedStatement ps = tx.prepareStatement(sql);
        try {
            if (parametro != null) {
                ps.setInt(1, parametro.intValue());
            }
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                throw new AppCrash("Nessun valore da: " + sql);
            }
            return rs.getInt(1);
        } finally {
            ps.close();
        }
    }

    private static String valoreTesto(DBTransaction tx, String sql, Integer parametro) throws Throwable {

        PreparedStatement ps = tx.prepareStatement(sql);
        try {
            ps.setInt(1, parametro.intValue());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } finally {
            ps.close();
        }
    }

    /** Data del documento: gg/mm/aaaa (datepicker) o aaaa-mm-gg (input date); vuota = nessuna data. */
    static Timestamp data(String valore) throws ParseException {

        if (Util.IsEmpty(valore) || Util.IsEmpty(valore.trim())) {
            return null;
        }
        String v = valore.trim();
        SimpleDateFormat formato;
        if (v.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
            formato = new SimpleDateFormat("dd/MM/yyyy");
        } else if (v.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            formato = new SimpleDateFormat("yyyy-MM-dd");
        } else {
            throw new ParseException(v, 0);
        }
        formato.setLenient(false);
        return new Timestamp(formato.parse(v).getTime());
    }

    private static String titoloDaNome(String nomeFile) {

        int punto = nomeFile.lastIndexOf('.');
        String titolo = (punto > 0) ? nomeFile.substring(0, punto) : nomeFile;
        titolo = titolo.replace('_', ' ').trim();
        return tronca(titolo.length() == 0 ? nomeFile : titolo, 200);
    }

    private static String tronca(String valore, int lunghezza) {

        if (valore == null) {
            return null;
        }
        String v = valore.trim();
        if (v.length() == 0) {
            return null;
        }
        return v.length() > lunghezza ? v.substring(0, lunghezza) : v;
    }
}
