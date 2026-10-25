package net.projectsrl.wera.centraletermica.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

/**
 * FUNCTIONID=CentraleTermicaDownload (GET).
 * <ul>
 * <li>ID_VERSIONE=n : quella versione</li>
 * <li>ID_DOCUMENTO=n : la versione corrente del documento</li>
 * <li>ID_FILE_WERA=n : un documento caricato da /wera (sola lettura)</li>
 * <li>SELEZIONE=D12,V7,W45 : zip di documenti (D), versioni (V) e documenti di /wera (W)</li>
 * <li>APRI=S : visualizza nel browser (solo pdf e immagini) invece di scaricare</li>
 * </ul>
 * Ogni file passa dai controlli di perimetro e di permesso sulla sezione; i documenti annullati solo
 * a chi puo' scrivere nella sezione; i file di /wera sovrascritti da un caricamento piu' recente non si
 * scaricano (su disco c'e' un altro documento). Ogni download e' registrato in EVENTI.
 */
public class FunctionCentraleTermicaDownload extends FunctionCentraleTermica_base {

    private static final String[] TIPI_APRIBILI = { "application/pdf", "image/jpeg", "image/png", "image/gif" };

    /** Un file autorizzato e pronto da inviare. */
    private static final class Voce {

        File    file;
        String  nome;
        String  tipoMime;
        Integer idCondominio;
        Integer idDocumento;
        Integer idVersione;
        Integer idFileWera;
    }

    /** Esito della risoluzione: voce, oppure codice HTTP e messaggio. */
    private static final class Risoluzione {

        Voce    voce;
        int     status;
        String  messaggio;
        Integer idCondominio;
        boolean negato;

        static Risoluzione ok(Voce voce) {

            Risoluzione r = new Risoluzione();
            r.voce = voce;
            return r;
        }

        static Risoluzione errore(int status, String messaggio, Integer idCondominio, boolean negato) {

            Risoluzione r = new Risoluzione();
            r.status = status;
            r.messaggio = messaggio;
            r.idCondominio = idCondominio;
            r.negato = negato;
            return r;
        }
    }

    public FunctionCentraleTermicaDownload() {

        super();
    }

    public FunctionCentraleTermicaDownload(ApplicationServices_itf applServices, String functionID,
            String functionName) {

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

        String selezione = req.getParameter("SELEZIONE");
        if (Util.IsNotEmpty(selezione)) {
            scaricaZip(ctx, selezione, res);
            return;
        }

        Risoluzione r;
        Integer id;
        if ((id = intero(req.getParameter("ID_VERSIONE"))) != null) {
            r = risolviVersione(ctx, id, null);
        } else if ((id = intero(req.getParameter("ID_DOCUMENTO"))) != null) {
            r = risolviVersione(ctx, null, id);
        } else if ((id = intero(req.getParameter("ID_FILE_WERA"))) != null) {
            r = risolviFileWera(ctx, id);
        } else {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST,
                    "Indicare ID_VERSIONE, ID_DOCUMENTO, ID_FILE_WERA o SELEZIONE");
            return;
        }

        if (r.voce == null) {
            esitoNegativo(ctx, res, r);
            return;
        }
        boolean apri = "S".equalsIgnoreCase(req.getParameter("APRI")) && apribile(r.voce.tipoMime);
        invia(r.voce, apri, res);
        registraEvento(ctx, CentraleTermicaCostanti.EVENTO_DOWNLOAD, r.voce.idCondominio, r.voce.idDocumento,
                r.voce.idVersione, r.voce.idFileWera, (apri ? "apri " : "") + r.voce.nome);
    }

    private void esitoNegativo(ContestoUtente ctx, SsbServletResponse res, Risoluzione r) throws AppCrash {

        if (r.negato) {
            negato(ctx, res, r.messaggio, r.idCondominio, "download");
        } else {
            rispondiErrore(res, r.status, r.messaggio);
        }
    }

    // ---------------------------------------------------------------------------------------- risoluzione

    private Risoluzione risolviVersione(ContestoUtente ctx, Integer idVersione, Integer idDocumento) throws AppCrash {

        Map<String, String> param = ctx.parametriBase();
        param.put("ID_VERSIONE", idVersione == null ? "NULL" : idVersione.toString());
        param.put("ID_DOCUMENTO", idDocumento == null ? "NULL" : idDocumento.toString());
        Map<String, Object> riga = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_VERSIONE, param, "ID_VERSIONE",
                "ID_DOCUMENTO", "NOME_FILE_ORIGINALE", "PERCORSO_FILE", "TIPO_MIME", "ID_CONDOMINIO", "ID_SEZIONE",
                "STATO");
        if (riga == null) {
            return Risoluzione.errore(HttpServletResponse.SC_NOT_FOUND, "Documento non trovato", null, false);
        }

        Integer idCondominio = LetturaDataset.intero(riga, "ID_CONDOMINIO");
        Map<String, Object> condominio = ctx.condominio(idCondominio.intValue());
        if (condominio == null) {
            return Risoluzione.errore(HttpServletResponse.SC_FORBIDDEN, "Condominio fuori dal perimetro dell'utente",
                    idCondominio, true);
        }
        Integer idAzienda = LetturaDataset.intero(condominio, "ID_AZIENDA");
        Map<String, Object> permesso = ctx.permessoSezione(LetturaDataset.intero(riga, "ID_SEZIONE").intValue(),
                idAzienda);
        boolean annullato = CentraleTermicaCostanti.STATO_ANNULLATO.equals(LetturaDataset.testo(riga, "STATO"));
        if (!LetturaDataset.vero(permesso, "FL_LETTURA") || (annullato && !LetturaDataset.vero(permesso,
                "FL_SCRITTURA"))) {
            return Risoluzione.errore(HttpServletResponse.SC_FORBIDDEN, "Sezione non accessibile al profilo",
                    idCondominio, true);
        }

        File cartella = ArchivioFile.cartellaCentraleTermica(_applicationSrv.getRoot(),
                ctx.parametro(CentraleTermicaCostanti.PAR_FILE_CARTELLA, idAzienda));
        File file = ArchivioFile.risolvi(cartella, LetturaDataset.testo(riga, "PERCORSO_FILE"));
        if (file == null) {
            return Risoluzione.errore(HttpServletResponse.SC_NOT_FOUND, "File non presente sul server", idCondominio,
                    false);
        }

        Voce voce = new Voce();
        voce.file = file;
        voce.nome = LetturaDataset.testo(riga, "NOME_FILE_ORIGINALE");
        voce.tipoMime = LetturaDataset.testo(riga, "TIPO_MIME");
        voce.idCondominio = idCondominio;
        voce.idDocumento = LetturaDataset.intero(riga, "ID_DOCUMENTO");
        voce.idVersione = LetturaDataset.intero(riga, "ID_VERSIONE");
        return Risoluzione.ok(voce);
    }

    private Risoluzione risolviFileWera(ContestoUtente ctx, Integer idFileWera) throws AppCrash {

        Map<String, String> param = ctx.parametriBase();
        param.put("ID_FILE_WERA", idFileWera.toString());
        Map<String, Object> riga = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_FILE_WERA, param,
                "ID_FILE_WERA", "ID_CONDOMINIO", "ID_SEZIONE", "NOME_FILE", "FL_FILE_SOVRASCRITTO");
        if (riga == null) {
            return Risoluzione.errore(HttpServletResponse.SC_NOT_FOUND, "Documento non trovato", null, false);
        }

        Integer idCondominio = LetturaDataset.intero(riga, "ID_CONDOMINIO");
        Map<String, Object> condominio = ctx.condominio(idCondominio.intValue());
        if (condominio == null) {
            return Risoluzione.errore(HttpServletResponse.SC_FORBIDDEN, "Condominio fuori dal perimetro dell'utente",
                    idCondominio, true);
        }
        Integer idAzienda = LetturaDataset.intero(condominio, "ID_AZIENDA");
        if (!"S".equalsIgnoreCase(ctx.parametro(CentraleTermicaCostanti.PAR_DOCUMENTI_WERA_VISIBILI, idAzienda))) {
            return Risoluzione.errore(HttpServletResponse.SC_NOT_FOUND, "Documenti di WERA non visibili", idCondominio,
                    false);
        }
        if (!ctx.puoLeggere(LetturaDataset.intero(riga, "ID_SEZIONE").intValue(), idAzienda)) {
            return Risoluzione.errore(HttpServletResponse.SC_FORBIDDEN, "Sezione non accessibile al profilo",
                    idCondominio, true);
        }
        if (LetturaDataset.vero(riga, "FL_FILE_SOVRASCRITTO")) {
            return Risoluzione.errore(HttpServletResponse.SC_GONE,
                    "File sostituito su WERA da un caricamento piu' recente con lo stesso nome", idCondominio, false);
        }

        String nome = LetturaDataset.testo(riga, "NOME_FILE");
        File file = ArchivioFile.risolvi(ArchivioFile.cartellaWera(_applicationSrv.getRoot()), nome);
        if (file == null) {
            return Risoluzione.errore(HttpServletResponse.SC_NOT_FOUND, "File non presente sul server", idCondominio,
                    false);
        }

        Voce voce = new Voce();
        voce.file = file;
        voce.nome = nome;
        voce.tipoMime = ArchivioFile.tipoMime(nome);
        voce.idCondominio = idCondominio;
        voce.idFileWera = idFileWera;
        return Risoluzione.ok(voce);
    }

    // ---------------------------------------------------------------------------------------- invio

    private void scaricaZip(ContestoUtente ctx, String selezione, SsbServletResponse res) throws AppCrash {

        int maxFile = ctx.parametroIntero(CentraleTermicaCostanti.PAR_DOWNLOAD_ZIP_MAX_FILE, ctx.getIdAzienda(), 50);
        String[] codici = selezione.split(",");
        if (codici.length > maxFile) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Massimo " + maxFile + " file per volta");
            return;
        }

        // tutto o niente: se un solo file non e' autorizzato o manca, lo zip non parte
        List<Voce> voci = new ArrayList<Voce>();
        Set<String> giaVisti = new HashSet<String>();
        for (String codice : codici) {
            String c = codice.trim().toUpperCase();
            if (c.length() < 2 || !giaVisti.add(c)) {
                continue;
            }
            Integer id = intero(c.substring(1));
            Risoluzione r;
            if (id == null) {
                r = Risoluzione.errore(HttpServletResponse.SC_BAD_REQUEST, "Selezione non valida: " + c, null, false);
            } else if (c.charAt(0) == 'D') {
                r = risolviVersione(ctx, null, id);
            } else if (c.charAt(0) == 'V') {
                r = risolviVersione(ctx, id, null);
            } else if (c.charAt(0) == 'W') {
                r = risolviFileWera(ctx, id);
            } else {
                r = Risoluzione.errore(HttpServletResponse.SC_BAD_REQUEST, "Selezione non valida: " + c, null, false);
            }
            if (r.voce == null) {
                r.messaggio = r.messaggio + " (" + c + ")";
                esitoNegativo(ctx, res, r);
                return;
            }
            voci.add(r.voce);
        }
        if (voci.isEmpty()) {
            rispondiErrore(res, HttpServletResponse.SC_BAD_REQUEST, "Selezione vuota");
            return;
        }

        String nomeZip = "centrale_termica_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".zip";
        StringBuilder dettaglio = new StringBuilder(nomeZip).append(":");
        Set<String> nomiUsati = new HashSet<String>();
        ZipOutputStream zip = null;
        try {
            res.setContentType("application/zip");
            impostaNome(res, nomeZip, false);
            zip = new ZipOutputStream(res.getOutputStream());
            for (Voce voce : voci) {
                ZipEntry entry = new ZipEntry(nomeUnico(voce.nome, nomiUsati));
                entry.setTime(voce.file.lastModified());
                zip.putNextEntry(entry);
                copia(voce.file, zip);
                zip.closeEntry();
                dettaglio.append(' ').append(voce.idVersione != null ? "V" + voce.idVersione : "W" + voce.idFileWera);
            }
            zip.finish();
            zip.flush();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Errore nella creazione dello zip " + nomeZip);
            throw ac;
        } finally {
            ArchivioFile.chiudi(zip);
        }

        registraEvento(ctx, CentraleTermicaCostanti.EVENTO_DOWNLOAD_ZIP, voci.get(0).idCondominio, null, null, null,
                dettaglio.toString());
    }

    private void invia(Voce voce, boolean apri, SsbServletResponse res) throws AppCrash {

        OutputStream out = null;
        try {
            res.setContentType(voce.tipoMime != null ? voce.tipoMime : "application/octet-stream");
            res.setHeader("X-Content-Type-Options", "nosniff");
            res.setHeader("Cache-Control", "private, no-store");
            impostaNome(res, voce.nome, apri);
            res.setContentLength((int) voce.file.length());
            out = res.getOutputStream();
            copia(voce.file, out);
            out.flush();
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "Errore nell'invio di " + voce.file.getAbsolutePath());
            throw ac;
        } finally {
            ArchivioFile.chiudi(out);
        }
    }

    private static void copia(File file, OutputStream out) throws Exception {

        InputStream in = new FileInputStream(file);
        try {
            byte[] buffer = new byte[8192];
            int letti;
            while ((letti = in.read(buffer)) != -1) {
                out.write(buffer, 0, letti);
            }
        } finally {
            in.close();
        }
    }

    /** Content-Disposition con nome ASCII di ripiego e nome UTF-8 (RFC 6266 / 5987). */
    private static void impostaNome(SsbServletResponse res, String nome, boolean apri) throws Exception {

        String ascii = nome.replaceAll("[^A-Za-z0-9._ ()-]", "_");
        String utf8 = URLEncoder.encode(nome, "UTF-8").replace("+", "%20");
        res.setHeader("Content-Disposition", (apri ? "inline" : "attachment") + "; filename=\"" + ascii
                + "\"; filename*=UTF-8''" + utf8);
    }

    private static String nomeUnico(String nome, Set<String> usati) {

        String candidato = nome;
        int n = 2;
        while (!usati.add(candidato.toLowerCase())) {
            int punto = nome.lastIndexOf('.');
            candidato = (punto > 0) ? nome.substring(0, punto) + " (" + n + ")" + nome.substring(punto)
                    : nome + " (" + n + ")";
            n++;
        }
        return candidato;
    }

    private static boolean apribile(String tipoMime) {

        for (String tipo : TIPI_APRIBILI) {
            if (tipo.equals(tipoMime)) {
                return true;
            }
        }
        return false;
    }
}
