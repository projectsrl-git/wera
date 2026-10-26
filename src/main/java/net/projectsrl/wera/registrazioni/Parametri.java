package net.projectsrl.wera.registrazioni;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.project.errors.AppCrash;

/**
 * Parametri dello schema registrazioni (valori di default, id_azienda NULL), letti tutti insieme una volta
 * per richiesta HTTP. Chiave assente o valore vuoto = default del chiamante.
 */
final class Parametri {

    private static final String SQL_TUTTI = "SELECT x.chiave, registrazioni.valore_parametro(x.chiave, NULL) AS valore "
            + "FROM (SELECT DISTINCT chiave FROM registrazioni.parametri) x";

    private final Map<String, String> _valori = new HashMap<String, String>();

    private Parametri() {

    }

    static Parametri carica() throws AppCrash {

        Parametri p = new Parametri();
        for (Map<String, Object> riga : Db.leggi(SQL_TUTTI)) {
            Object v = riga.get("valore");
            p._valori.put((String) riga.get("chiave"), v == null ? "" : v.toString().trim());
        }
        return p;
    }

    /** Valore del parametro, stringa vuota se manca. */
    String testo(String chiave) {

        String v = _valori.get(chiave);
        return v == null ? "" : v;
    }

    String testo(String chiave, String predefinito) {

        String v = testo(chiave);
        return v.length() == 0 ? predefinito : v;
    }

    int intero(String chiave, int predefinito) {

        String v = testo(chiave);
        if (v.matches("[0-9]{1,9}")) {
            return Integer.parseInt(v);
        }
        return predefinito;
    }

    /** BOOLEANO S/N: true solo con S (maiuscolo o minuscolo); vuoto = predefinito. */
    boolean vero(String chiave, boolean predefinito) {

        String v = testo(chiave);
        return v.length() == 0 ? predefinito : v.equalsIgnoreCase("S");
    }

    /** ELENCO separato da virgole, voci ripulite, vuote scartate. */
    List<String> elenco(String chiave) {

        List<String> voci = new ArrayList<String>();
        for (String voce : testo(chiave).split(",")) {
            if (voce.trim().length() > 0) {
                voci.add(voce.trim());
            }
        }
        return voci;
    }

    /** Elenco in maiuscolo (profili, estensioni). */
    List<String> elencoMaiuscolo(String chiave) {

        List<String> voci = new ArrayList<String>();
        for (String voce : elenco(chiave)) {
            voci.add(voce.toUpperCase());
        }
        return voci;
    }
}
