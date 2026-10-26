package net.projectsrl.wera.registrazioni;

import net.project.misc.Util;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;

/**
 * Utente loggato visto dalle pagine delle richieste: e' abilitato se uno dei suoi profili (campo di
 * sessione PROFILO, codici separati da virgola) e' nel parametro APPROVAZIONE.PROFILI. Controllo lato server.
 */
final class Approvatore {

    private final Integer _idUtente;
    private final String  _profili;
    private final String  _profilo;
    private final boolean _abilitato;

    Approvatore(WebAppUserSecurityInfo<?> userInfo, Parametri p) {

        Object id = userInfo == null ? null : userInfo.getIdUtente();
        _idUtente = (id instanceof Number) ? Integer.valueOf(((Number) id).intValue()) : null;
        _profili = userInfo == null ? null : userInfo.getField(WebAppConstants_itf.PROFILO);
        String trovato = null;
        if (_idUtente != null && Util.IsNotEmpty(_profili)) {
            for (String codice : p.elencoMaiuscolo(RegistrazioniCostanti.PAR_APPROVAZIONE_PROFILI)) {
                for (String suo : _profili.split(",")) {
                    if (codice.equals(suo.trim().toUpperCase())) {
                        trovato = codice;
                        break;
                    }
                }
                if (trovato != null) {
                    break;
                }
            }
        }
        _profilo = trovato;
        _abilitato = trovato != null;
    }

    boolean isAbilitato() {

        return _abilitato;
    }

    Integer getIdUtente() {

        return _idUtente;
    }

    /** Profilo con cui e' abilitato (per il registro eventi), o il primo dei suoi. */
    String getProfilo() {

        if (_profilo != null) {
            return _profilo;
        }
        if (Util.IsEmpty(_profili)) {
            return null;
        }
        String primo = _profili.split(",")[0].trim();
        return primo.length() > 3 ? primo.substring(0, 3) : primo;
    }
}
