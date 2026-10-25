package net.projectsrl.wera.centraletermica.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;

/**
 * File della Centrale termica su disco. I file nuovi stanno sotto
 * directory.external_files + parametro FILE.CARTELLA, con percorso UNIVOCO
 * <azienda>/<condominio>/<anno>/<uuid>.<estensione>: nessun caricamento sovrascrive un altro.
 * I file di /wera si leggono (solo lettura) da directory.external_files + cartella.upload.filemanager.
 */
final class ArchivioFile {

    private ArchivioFile() {

    }

    /** Esito della scrittura di un file: percorso relativo, dimensione e impronta. */
    static final class FileScritto {

        final String percorsoRelativo;
        final File   file;
        final long   dimensione;
        final String sha256;

        FileScritto(String percorsoRelativo, File file, long dimensione, String sha256) {

            this.percorsoRelativo = percorsoRelativo;
            this.file = file;
            this.dimensione = dimensione;
            this.sha256 = sha256;
        }
    }

    static String radice(String root) {

        String radice = Config.GetInstance().getProperty("directory.external_files", root);
        if (!radice.endsWith("/") && !radice.endsWith("\\")) {
            radice = radice + "/";
        }
        return radice;
    }

    static File cartellaCentraleTermica(String root, String parametroCartella) throws AppCrash {

        String cartella = Util.IsEmpty(parametroCartella) ? "centrale_termica/" : parametroCartella.trim();
        // la cartella dei nuovi file non puo' coincidere con quella di /wera ne' uscire dalla radice
        if (cartella.contains("..") || cartella.startsWith("/") || cartella.startsWith("\\")
                || cartella.matches("(?i)^upload/?$")) {
            throw new AppCrash("Parametro FILE.CARTELLA non valido: " + cartella);
        }
        return new File(radice(root) + cartella);
    }

    static File cartellaWera(String root) {

        return new File(radice(root) + Config.GetInstance().getProperty("cartella.upload.filemanager", "upload/"));
    }

    /** Solo il nome, senza percorso del client (IE manda C:\...\nome.pdf). */
    static String nomeSemplice(String nomeClient) {

        if (nomeClient == null) {
            return "";
        }
        String nome = nomeClient;
        int slash = Math.max(nome.lastIndexOf('/'), nome.lastIndexOf('\\'));
        if (slash >= 0) {
            nome = nome.substring(slash + 1);
        }
        return nome.replaceAll("[\\p{Cntrl}]", "").trim();
    }

    static String estensione(String nome) {

        int punto = nome.lastIndexOf('.');
        if (punto < 0 || punto == nome.length() - 1) {
            return "";
        }
        return nome.substring(punto + 1).toLowerCase();
    }

    static boolean estensioneAmmessa(String nome, List<String> ammesse) {

        String ext = estensione(nome);
        return ext.length() > 0 && ammesse.contains(ext.toUpperCase());
    }

    /**
     * Scrive lo stream in un file nuovo e univoco calcolando dimensione e SHA-256. Se supera
     * maxByte cancella il file parziale (non e' ancora referenziato da nessuna riga) e fallisce.
     */
    static FileScritto scrivi(InputStream in, File cartellaBase, int idAzienda, int idCondominio, String nomeOriginale,
            long maxByte) throws AppCrash {

        String anno = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        String ext = estensione(nomeOriginale);
        String nomeSalvato = UUID.randomUUID().toString() + (ext.length() > 0 ? "." + ext : "");
        String percorsoRelativo = idAzienda + "/" + idCondominio + "/" + anno + "/" + nomeSalvato;

        File destinazione = new File(cartellaBase, percorsoRelativo);
        File cartella = destinazione.getParentFile();
        if (!cartella.isDirectory() && !cartella.mkdirs()) {
            throw new AppCrash("Impossibile creare la cartella " + cartella.getAbsolutePath());
        }
        if (destinazione.exists()) {
            throw new AppCrash("File gia' esistente (uuid ripetuto?): " + destinazione.getAbsolutePath());
        }

        OutputStream out = null;
        long totale = 0;
        boolean completato = false;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            out = new FileOutputStream(destinazione);
            byte[] buffer = new byte[8192];
            int letti;
            while ((letti = in.read(buffer)) != -1) {
                totale += letti;
                if (totale > maxByte) {
                    throw new AppCrash("File oltre la dimensione massima: " + nomeOriginale);
                }
                digest.update(buffer, 0, letti);
                out.write(buffer, 0, letti);
            }
            out.close();
            out = null;
            completato = true;
            return new FileScritto(percorsoRelativo, destinazione, totale, esadecimale(digest.digest()));
        } catch (AppCrash ac) {
            throw ac;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(ArchivioFile.class.getName(), "Errore nella scrittura di " + destinazione.getAbsolutePath());
            throw ac;
        } finally {
            chiudi(out);
            if (!completato) {
                elimina(destinazione);
            }
        }
    }

    /** Rimuove un file appena scritto e mai registrato (rollback del caricamento). */
    static void elimina(File file) {

        if (file != null && file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
    }

    /** File dentro la cartella base, o null se il percorso esce dalla cartella o il file non c'e'. */
    static File risolvi(File cartellaBase, String percorsoRelativo) throws AppCrash {

        if (Util.IsEmpty(percorsoRelativo)) {
            return null;
        }
        try {
            File base = cartellaBase.getCanonicalFile();
            File file = new File(base, percorsoRelativo).getCanonicalFile();
            if (!file.getPath().startsWith(base.getPath() + File.separator)) {
                return null;
            }
            return file.isFile() ? file : null;
        } catch (IOException e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(ArchivioFile.class.getName(), "Percorso non risolvibile: " + percorsoRelativo);
            throw ac;
        }
    }

    static String tipoMime(String nome) {

        String ext = estensione(nome);
        if (ext.equals("pdf")) return "application/pdf";
        if (ext.equals("p7m")) return "application/pkcs7-mime";
        if (ext.equals("jpg") || ext.equals("jpeg")) return "image/jpeg";
        if (ext.equals("png")) return "image/png";
        if (ext.equals("gif")) return "image/gif";
        if (ext.equals("txt")) return "text/plain";
        if (ext.equals("doc")) return "application/msword";
        if (ext.equals("docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (ext.equals("xls")) return "application/vnd.ms-excel";
        if (ext.equals("xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (ext.equals("odt")) return "application/vnd.oasis.opendocument.text";
        if (ext.equals("ods")) return "application/vnd.oasis.opendocument.spreadsheet";
        if (ext.equals("zip")) return "application/zip";
        return "application/octet-stream";
    }

    private static String esadecimale(byte[] bytes) {

        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", Integer.valueOf(b & 0xff)));
        }
        return sb.toString();
    }

    static void chiudi(java.io.Closeable c) {

        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // chiusura best effort
            }
        }
    }
}
