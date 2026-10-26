package net.projectsrl.wera.registrazioni;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;

/**
 * Registro eventi (registrazioni.eventi, append-only). Ogni riga e' scritta in una transazione propria,
 * fuori da quella del chiamante, cosi' restano registrati anche i blocchi e gli errori. Un errore qui
 * non blocca mai l'operazione.
 */
final class Eventi {

    private static final String SQL_INSERISCI = "INSERT INTO registrazioni.eventi "
            + "(tipo_evento, id_richiesta, id_utente, codice_profilo, ip, dettaglio) VALUES (?, ?, ?, ?, ?, ?)";

    private Eventi() {

    }

    static void registra(String tipo, Integer idRichiesta, Integer idUtente, String profilo, String ip,
            String dettaglio) {

        DBTransaction tx = null;
        try {
            tx = new DBTransaction();
            Db.esegui(tx, SQL_INSERISCI, tipo, Db.intero(idRichiesta), Db.intero(idUtente),
                    Db.testo(profilo == null ? null : (profilo.length() > 3 ? profilo.substring(0, 3) : profilo)),
                    Db.testo(ip), Db.testo(dettaglio));
            tx.commit();
        } catch (Throwable t) {
            try {
                if (tx != null) {
                    tx.rollBack();
                }
            } catch (AppCrash ignorata) {
                // gia' in errore
            }
            AppCrash ac = (t instanceof AppCrash) ? (AppCrash) t : new AppCrash(t);
            ac.logContext(Eventi.class.getName(), "Evento non registrato: " + tipo + " richiesta " + idRichiesta);
        } finally {
            try {
                if (tx != null) {
                    tx.end();
                }
            } catch (AppCrash ignorata) {
                // connessione gia' rilasciata
            }
        }
    }
}
