package net.projectsrl.wera.centraletermica.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.project.errors.AppCrash;

/**
 * Dati della pagina di configurazione della Centrale termica (solo profili di CONFIGURAZIONE.PROFILI):
 * sezioni con i permessi per profilo, profili, tipi di scadenza, parametri (default + eccezioni per
 * azienda), corrispondenze delle categorie di /wera, aziende del perimetro.
 */
final class DatiConfigurazione {

    private DatiConfigurazione() {

    }

    static Map<String, Object> leggi(ContestoUtente ctx) throws AppCrash {

        Map<String, String> param = ctx.parametriBase();
        Map<String, Object> config = new LinkedHashMap<String, Object>();

        List<Map<String, Object>> sezioni = LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_SEZIONI, param,
                "ID_SEZIONE", "ID_AZIENDA", "RAGSOC", "CODICE", "TITOLO", "SOTTOTITOLO", "ICONA", "TIPO", "ORDINE",
                "FL_SCADENZA_RICHIESTA", "FL_DISATTIVO", "NR_DOCUMENTI");
        List<Map<String, Object>> permessi = LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_PERMESSI, param,
                "ID_SEZIONE", "CODICE_PROFILO", "FL_LETTURA", "FL_SCRITTURA");
        for (Map<String, Object> s : sezioni) {
            Map<String, Object> perSezione = new LinkedHashMap<String, Object>();
            for (Map<String, Object> p : permessi) {
                if (s.get("ID_SEZIONE") != null && s.get("ID_SEZIONE").equals(p.get("ID_SEZIONE"))) {
                    String livello = LetturaDataset.vero(p, "FL_SCRITTURA") ? "RW"
                            : (LetturaDataset.vero(p, "FL_LETTURA") ? "R" : "-");
                    perSezione.put(LetturaDataset.testo(p, "CODICE_PROFILO"), livello);
                }
            }
            s.put("PERMESSI", perSezione);
        }
        config.put("sezioni", sezioni);
        config.put("profili", LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_PROFILI, param, "CODICE",
                "DESCRIZIONE"));
        config.put("tipi", LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_TIPI, param, "ID_TIPO_SCADENZA",
                "ID_AZIENDA", "RAGSOC", "CODICE", "DESCRIZIONE", "ID_SEZIONE", "TITOLO_SEZIONE", "PERIODICITA_MESI",
                "GIORNI_PREAVVISO", "RIFERIMENTO_NORMATIVO", "ORDINE", "FL_DISATTIVO", "NR_SCADENZE"));

        // parametri: una voce per chiave con il default e le eccezioni per azienda
        List<Map<String, Object>> righe = LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_PARAMETRI, param,
                "ID_PARAMETRO", "ID_AZIENDA", "RAGSOC", "CHIAVE", "VALORE", "TIPO_VALORE", "DESCRIZIONE");
        Map<String, Map<String, Object>> perChiave = new LinkedHashMap<String, Map<String, Object>>();
        for (Map<String, Object> r : righe) {
            String chiave = LetturaDataset.testo(r, "CHIAVE");
            Map<String, Object> voce = perChiave.get(chiave);
            if (voce == null) {
                voce = new LinkedHashMap<String, Object>();
                voce.put("CHIAVE", chiave);
                voce.put("ECCEZIONI", new ArrayList<Map<String, Object>>());
                perChiave.put(chiave, voce);
            }
            if (r.get("ID_AZIENDA") == null) {
                voce.put("VALORE", r.get("VALORE") == null ? "" : r.get("VALORE"));
                voce.put("TIPO_VALORE", r.get("TIPO_VALORE"));
                voce.put("DESCRIZIONE", r.get("DESCRIZIONE"));
            } else {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> eccezioni = (List<Map<String, Object>>) voce.get("ECCEZIONI");
                Map<String, Object> e = new LinkedHashMap<String, Object>();
                e.put("ID_AZIENDA", r.get("ID_AZIENDA"));
                e.put("RAGSOC", r.get("RAGSOC"));
                e.put("VALORE", r.get("VALORE") == null ? "" : r.get("VALORE"));
                eccezioni.add(e);
            }
        }
        List<Map<String, Object>> parametri = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> voce : perChiave.values()) {
            if (voce.containsKey("TIPO_VALORE")) { // solo chiavi con un default
                parametri.add(voce);
            }
        }
        config.put("parametri", parametri);

        config.put("categorie", LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_CATEGORIE, param,
                "CODICE_CATEGORIA", "DESCRIZIONE", "ID_SEZIONE", "NR_FILE"));
        config.put("aziende", LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_AZIENDE, param, "ID_AZIENDA",
                "RAGSOC"));
        return config;
    }

    /** Vero se l'azienda e' fra quelle che l'utente puo' configurare. */
    static boolean aziendaAmmessa(ContestoUtente ctx, Integer idAzienda) throws AppCrash {

        if (idAzienda == null) {
            return true;
        }
        for (Map<String, Object> a : LetturaDataset.leggi(CentraleTermicaCostanti.DS_CONFIG_AZIENDE,
                ctx.parametriBase(), "ID_AZIENDA")) {
            if (idAzienda.equals(LetturaDataset.intero(a, "ID_AZIENDA"))) {
                return true;
            }
        }
        return false;
    }
}
