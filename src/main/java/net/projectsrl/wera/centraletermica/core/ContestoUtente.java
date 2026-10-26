package net.projectsrl.wera.centraletermica.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;
import net.projectsrl.wera.utils.WeraUtils;

/**
 * Utente della richiesta visto dalla Centrale termica: profilo, azienda, perimetro dei condomini e
 * permessi per sezione. Tutti i controlli sono lato server: il perimetro e' quello calcolato al login
 * da BOWAuthenticationProvider (WHERECONDITION_AZIENDE / WHERECONDITION_PROFILI in sessione).
 */
public class ContestoUtente {

    private final Integer             _idUtente;
    private final String              _profilo;
    private final Integer             _idAzienda;
    private final String              _whereAziende;
    private final String              _whereProfili;
    private final Map<String, String> _cacheParametri = new HashMap<String, String>();

    public ContestoUtente(WebAppUserSecurityInfo<?> userInfo) throws AppCrash {

        Object id = userInfo.getIdUtente();
        _idUtente = (id instanceof Number) ? Integer.valueOf(((Number) id).intValue()) : null;
        _profilo = codiceProfilo(userInfo.getField(WebAppConstants_itf.PROFILO));
        _idAzienda = (_idUtente == null) ? null : WeraUtils.trovaIdAziendaUtente(_idUtente.toString());
        _whereAziende = userInfo.getField(DafneCostanti_itf.WHERECONDITION_AZIENDE);
        _whereProfili = userInfo.getField(DafneCostanti_itf.WHERECONDITION_PROFILI);
    }

    /** Profilo principale fra quelli contenuti nel campo di sessione, in ordine di precedenza. */
    private static String codiceProfilo(String profiloSessione) {

        if (Util.IsEmpty(profiloSessione)) {
            return null;
        }
        for (String codice : CentraleTermicaCostanti.PROFILI_PRECEDENZA) {
            if (profiloSessione.contains(codice)) {
                return codice;
            }
        }
        return null;
    }

    public Integer getIdUtente() {

        return _idUtente;
    }

    public String getProfilo() {

        return _profilo;
    }

    public Integer getIdAzienda() {

        return _idAzienda;
    }

    /** Vero se il profilo e' fra quelli del parametro ACCESSO.PROFILI e il perimetro e' in sessione. */
    public boolean isAbilitato() throws AppCrash {

        if (_idUtente == null || _profilo == null || Util.IsEmpty(_whereAziende) || Util.IsEmpty(_whereProfili)) {
            return false;
        }
        return elenco(parametro(CentraleTermicaCostanti.PAR_ACCESSO_PROFILI, _idAzienda)).contains(_profilo);
    }

    /** Vero se il profilo e' fra quelli del parametro CONFIGURAZIONE.PROFILI (default: solo superuser). */
    public boolean puoConfigurare() throws AppCrash {

        return isAbilitato()
                && elenco(parametro(CentraleTermicaCostanti.PAR_CONFIGURAZIONE_PROFILI, _idAzienda)).contains(_profilo);
    }

    /** Parametri comuni a tutti i dataset nuovi: perimetro e profilo, mai dal browser. */
    Map<String, String> parametriBase() {

        Map<String, String> param = new HashMap<String, String>();
        param.put(DafneCostanti_itf.WHERECONDITION_AZIENDE, _whereAziende);
        param.put(DafneCostanti_itf.WHERECONDITION_PROFILI, _whereProfili);
        param.put("CODICE_PROFILO", _profilo);
        return param;
    }

    /** Condominio se e' nel perimetro dell'utente, altrimenti null. */
    public Map<String, Object> condominio(int idCondominio) throws AppCrash {

        Map<String, String> param = parametriBase();
        param.put("ID_CONDOMINIO", Integer.toString(idCondominio));
        return LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_CONDOMINIO, param, "ID_CONDOMINIO", "ID_AZIENDA",
                "DENOMINAZIONE", "INDIRIZZO", "LOCALITA", "PROVINCIA");
    }

    /**
     * Permesso del profilo sulla sezione per un condominio dell'azienda indicata: null se la sezione non
     * esiste, e' disattivata, e' di un'altra azienda o il profilo non ha righe in SEZIONI_PROFILI.
     */
    public Map<String, Object> permessoSezione(int idSezione, Integer idAziendaCondominio) throws AppCrash {

        Map<String, String> param = parametriBase();
        param.put("ID_SEZIONE", Integer.toString(idSezione));
        Map<String, Object> permesso = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_PERMESSO_SEZIONE, param,
                "ID_SEZIONE", "ID_AZIENDA", "TIPO", "FL_LETTURA", "FL_SCRITTURA");
        if (permesso == null) {
            return null;
        }
        Integer aziendaSezione = LetturaDataset.intero(permesso, "ID_AZIENDA");
        if (aziendaSezione != null && !aziendaSezione.equals(idAziendaCondominio)) {
            return null;
        }
        return permesso;
    }

    public boolean puoLeggere(int idSezione, Integer idAziendaCondominio) throws AppCrash {

        return LetturaDataset.vero(permessoSezione(idSezione, idAziendaCondominio), "FL_LETTURA");
    }

    public boolean puoScrivere(int idSezione, Integer idAziendaCondominio) throws AppCrash {

        return LetturaDataset.vero(permessoSezione(idSezione, idAziendaCondominio), "FL_SCRITTURA");
    }

    /** Valore effettivo del parametro (override dell'azienda, poi default); "" se assente. */
    public String parametro(String chiave, Integer idAzienda) throws AppCrash {

        String cacheKey = chiave + "|" + idAzienda;
        if (_cacheParametri.containsKey(cacheKey)) {
            return _cacheParametri.get(cacheKey);
        }
        Map<String, String> param = new HashMap<String, String>();
        param.put("CHIAVE", chiave);
        param.put("ID_AZIENDA", idAzienda == null ? "NULL" : idAzienda.toString());
        Map<String, Object> riga = LetturaDataset.leggiUna(CentraleTermicaCostanti.DS_PARAMETRO, param, "VALORE");
        String valore = LetturaDataset.testo(riga, "VALORE");
        if (valore == null) {
            valore = "";
        }
        _cacheParametri.put(cacheKey, valore);
        return valore;
    }

    public int parametroIntero(String chiave, Integer idAzienda, int predefinito) throws AppCrash {

        String valore = parametro(chiave, idAzienda);
        if (valore.matches("[0-9]{1,9}")) {
            return Integer.parseInt(valore);
        }
        return predefinito;
    }

    static List<String> elenco(String valore) {

        if (Util.IsEmpty(valore)) {
            return Arrays.asList(new String[0]);
        }
        String[] voci = valore.split(",");
        for (int i = 0; i < voci.length; i++) {
            voci[i] = voci[i].trim().toUpperCase();
        }
        return Arrays.asList(voci);
    }
}
