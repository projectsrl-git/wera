package net.projectsrl.wera.registrazioni;

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
 * Foto allegate alle richieste. Stanno sotto directory.external_files + parametro FILE.CARTELLA con percorso
 * UNIVOCO <anno>/<id_richiesta>/<uuid>.<estensione>; il nome del client resta solo nel DB. Si accettano
 * solo immagini vere (firma dei primi byte), non basta l'estensione.
 */
final class Allegati {

    private Allegati() {

    }

    /** Esito della scrittura: percorso relativo, file, dimensione, impronta SHA-256, tipo MIME. */
    static final class FileScritto {

        final String percorsoRelativo;
        final File   file;
        final long   dimensione;
        final String sha256;
        final String tipoMime;

        FileScritto(String percorsoRelativo, File file, long dimensione, String sha256, String tipoMime) {

            this.percorsoRelativo = percorsoRelativo;
            this.file = file;
            this.dimensione = dimensione;
            this.sha256 = sha256;
            this.tipoMime = tipoMime;
        }
    }

    static File cartella(String root, String parametroCartella) throws AppCrash {

        String radice = Config.GetInstance().getProperty("directory.external_files", root);
        if (!radice.endsWith("/") && !radice.endsWith("\\")) {
            radice = radice + "/";
        }
        String cartella = Util.IsEmpty(parametroCartella) ? "registrazioni/" : parametroCartella.trim();
        // non puo' uscire dalla radice ne' coincidere con la cartella piatta degli upload di /wera
        if (cartella.contains("..") || cartella.startsWith("/") || cartella.startsWith("\\")
                || cartella.matches("(?i)^upload/?$")) {
            throw new AppCrash("Parametro FILE.CARTELLA non valido: " + cartella);
        }
        return new File(radice + cartella);
    }

    /** Solo il nome, senza percorso del client e caratteri di controllo. */
    static String nomeSemplice(String nomeClient) {

        if (nomeClient == null) {
            return "";
        }
        String nome = nomeClient;
        int slash = Math.max(nome.lastIndexOf('/'), nome.lastIndexOf('\\'));
        if (slash >= 0) {
            nome = nome.substring(slash + 1);
        }
        nome = nome.replaceAll("[\\p{Cntrl}<>\"]", "").trim();
        return nome.length() > 255 ? nome.substring(nome.length() - 255) : nome;
    }

    static String estensione(String nome) {

        int punto = nome.lastIndexOf('.');
        if (punto < 0 || punto == nome.length() - 1) {
            return "";
        }
        return nome.substring(punto + 1).toLowerCase();
    }

    static boolean estensioneAmmessa(String nome, List<String> ammesseMaiuscole) {

        String ext = estensione(nome);
        return ext.length() > 0 && ammesseMaiuscole.contains(ext.toUpperCase());
    }

    /**
     * Tipo MIME dalla firma dei primi byte: JPEG, PNG, WebP, HEIC/HEIF. Null se il contenuto non e' una di
     * queste immagini (qualunque sia l'estensione).
     */
    static String tipoImmagine(byte[] t, int n) {

        if (n >= 3 && (t[0] & 0xff) == 0xFF && (t[1] & 0xff) == 0xD8 && (t[2] & 0xff) == 0xFF) {
            return "image/jpeg";
        }
        if (n >= 8 && (t[0] & 0xff) == 0x89 && t[1] == 'P' && t[2] == 'N' && t[3] == 'G' && t[4] == 0x0D
                && t[5] == 0x0A && t[6] == 0x1A && t[7] == 0x0A) {
            return "image/png";
        }
        if (n >= 12 && t[0] == 'R' && t[1] == 'I' && t[2] == 'F' && t[3] == 'F' && t[8] == 'W' && t[9] == 'E'
                && t[10] == 'B' && t[11] == 'P') {
            return "image/webp";
        }
        if (n >= 12 && t[4] == 'f' && t[5] == 't' && t[6] == 'y' && t[7] == 'p') {
            String marca = new String(t, 8, 4, java.nio.charset.Charset.forName("US-ASCII"));
            if (marca.startsWith("hei") || marca.startsWith("hev") || marca.equals("mif1") || marca.equals("msf1")) {
                return "image/heic";
            }
        }
        return null;
    }

    /**
     * Scrive lo stream in un file nuovo e univoco, calcolando dimensione e SHA-256 e controllando la firma
     * dell'immagine. Se il contenuto non e' un'immagine o supera maxByte il file parziale viene cancellato.
     */
    static FileScritto scrivi(InputStream in, File cartellaBase, int idRichiesta, String nomeOriginale, long maxByte)
            throws AppCrash {

        String anno = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        String ext = estensione(nomeOriginale);
        String percorsoRelativo = anno + "/" + idRichiesta + "/" + UUID.randomUUID().toString()
                + (ext.length() > 0 ? "." + ext : "");
        File destinazione = new File(cartellaBase, percorsoRelativo);
        File cartella = destinazione.getParentFile();
        if (!cartella.isDirectory() && !cartella.mkdirs()) {
            throw new AppCrash("Impossibile creare la cartella " + cartella.getAbsolutePath());
        }

        OutputStream out = null;
        long totale = 0;
        boolean completato = false;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] testa = new byte[16];
            int letti = leggiTesta(in, testa);
            String tipo = tipoImmagine(testa, letti);
            if (tipo == null) {
                throw new FileNonValido("Il file non e' un'immagine JPG, PNG, WebP o HEIC");
            }
            out = new FileOutputStream(destinazione);
            digest.update(testa, 0, letti);
            out.write(testa, 0, letti);
            totale = letti;
            byte[] buffer = new byte[8192];
            while ((letti = in.read(buffer)) != -1) {
                totale += letti;
                if (totale > maxByte) {
                    throw new FileNonValido("Il file supera la dimensione massima");
                }
                digest.update(buffer, 0, letti);
                out.write(buffer, 0, letti);
            }
            out.close();
            out = null;
            completato = true;
            return new FileScritto(percorsoRelativo, destinazione, totale, esadecimale(digest.digest()), tipo);
        } catch (AppCrash ac) {
            throw ac;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(Allegati.class.getName(), "Errore nella scrittura di " + destinazione.getAbsolutePath());
            throw ac;
        } finally {
            chiudi(out);
            if (!completato) {
                elimina(destinazione);
            }
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
            ac.logContext(Allegati.class.getName(), "Percorso non risolvibile: " + percorsoRelativo);
            throw ac;
        }
    }

    /** Rimuove un file scritto e non registrato (rollback dell'invio). */
    static void elimina(File file) {

        if (file != null && file.exists() && !file.delete()) {
            file.deleteOnExit();
        }
        // cartella della richiesta rimasta vuota (invio annullato): delete riesce solo se e' vuota
        if (file != null && file.getParentFile() != null) {
            file.getParentFile().delete();
        }
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

    /** Errore dovuto al file inviato (non al server): il messaggio va all'utente. */
    static final class FileNonValido extends AppCrash {

        private static final long serialVersionUID = 1L;

        /** Testo per l'utente (AppCrash non lo restituisce da getMessage). */
        final String testo;

        FileNonValido(String messaggio) {

            super(messaggio);
            this.testo = messaggio;
        }
    }

    private static int leggiTesta(InputStream in, byte[] testa) throws IOException {

        int n = 0;
        while (n < testa.length) {
            int r = in.read(testa, n, testa.length - n);
            if (r < 0) {
                break;
            }
            n += r;
        }
        return n;
    }

    private static String esadecimale(byte[] bytes) {

        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", Integer.valueOf(b & 0xff)));
        }
        return sb.toString();
    }
}
