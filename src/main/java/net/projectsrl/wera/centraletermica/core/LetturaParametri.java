package net.projectsrl.wera.centraletermica.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.project.errors.AppCrash;

/**
 * Lettura dei parametri della Centrale termica senza un utente in sessione (motore degli avvisi):
 * override dell'azienda, poi default (funzione centrale_termica.valore_parametro). Cache per istanza.
 */
final class LetturaParametri {

    private final Map<String, String> _cache = new HashMap<String, String>();

    String valore(String chiave, Integer idAzienda) throws AppCrash {

        String k = chiave + "|" + idAzienda;
        if (_cache.containsKey(k)) {
            return _cache.get(k);
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put("CHIAVE", chiave);
        param.put("ID_AZIENDA", idAzienda == null ? "NULL" : idAzienda.toString());
        String v = LetturaDataset.testo(LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_PARAMETRO, param, "VALORE"),
                "VALORE");
        v = v == null ? "" : v;
        _cache.put(k, v);
        return v;
    }

    boolean vero(String chiave, Integer idAzienda) throws AppCrash {

        return "S".equalsIgnoreCase(valore(chiave, idAzienda));
    }

    /** Elenco di interi positivi ordinato (es. "90,60,30" -> [30,60,90]); voci non valide ignorate. */
    List<Integer> interi(String chiave, Integer idAzienda) throws AppCrash {

        List<Integer> out = new ArrayList<Integer>();
        for (String s : valore(chiave, idAzienda).split(",")) {
            String t = s.trim();
            if (t.matches("[0-9]{1,4}") && !out.contains(Integer.valueOf(t))) {
                out.add(Integer.valueOf(t));
            }
        }
        Collections.sort(out);
        return out;
    }
}
