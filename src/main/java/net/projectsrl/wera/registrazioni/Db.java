package net.projectsrl.wera.registrazioni;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;

/**
 * Query parametriche (PreparedStatement) sullo schema registrazioni e sulle tabelle utenti di /wera.
 * Nessun valore della richiesta finisce concatenato nell'SQL. I null vanno tipizzati (Nullo): il DAO del
 * framework li manderebbe come varchar.
 */
final class Db {

    static final class Nullo {

        final int tipo;

        Nullo(int tipo) {

            this.tipo = tipo;
        }
    }

    static final Nullo NULL_INTERO = new Nullo(Types.INTEGER);
    static final Nullo NULL_TESTO  = new Nullo(Types.VARCHAR);

    private Db() {

    }

    /** Esegue in una transazione propria una sola istruzione di lettura e restituisce le righe. */
    static List<Map<String, Object>> leggi(String sql, Object... valori) throws AppCrash {

        DBTransaction tx = new DBTransaction();
        try {
            return righe(tx, sql, valori);
        } catch (Throwable t) {
            throw errore(t, sql);
        } finally {
            tx.end();
        }
    }

    static int esegui(DBTransaction tx, String sql, Object... valori) throws SQLException, AppCrash {

        PreparedStatement ps = tx.prepareStatement(sql);
        try {
            lega(ps, valori);
            return ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    /** Prima colonna della prima riga (null se nessuna riga). Va bene anche per INSERT ... RETURNING. */
    static Object valore(DBTransaction tx, String sql, Object... valori) throws SQLException, AppCrash {

        PreparedStatement ps = tx.prepareStatement(sql);
        try {
            lega(ps, valori);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getObject(1) : null;
        } finally {
            ps.close();
        }
    }

    static Integer intero(DBTransaction tx, String sql, Object... valori) throws SQLException, AppCrash {

        Object v = valore(tx, sql, valori);
        return (v instanceof Number) ? Integer.valueOf(((Number) v).intValue()) : null;
    }

    /** Righe come mappe colonna (minuscolo, come le restituisce PostgreSQL) -> valore. */
    static List<Map<String, Object>> righe(DBTransaction tx, String sql, Object... valori)
            throws SQLException, AppCrash {

        PreparedStatement ps = tx.prepareStatement(sql);
        try {
            lega(ps, valori);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            List<Map<String, Object>> elenco = new ArrayList<Map<String, Object>>();
            while (rs.next()) {
                Map<String, Object> riga = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    riga.put(md.getColumnLabel(i).toLowerCase(), rs.getObject(i));
                }
                elenco.add(riga);
            }
            return elenco;
        } finally {
            ps.close();
        }
    }

    /** true se l'eccezione (o una causa) e' una violazione di vincolo univoco di PostgreSQL. */
    static boolean violazioneUnivoca(Throwable t) {

        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof SQLException && "23505".equals(((SQLException) c).getSQLState())) {
                return true;
            }
        }
        return false;
    }

    static AppCrash errore(Throwable t, String contesto) {

        AppCrash ac = (t instanceof AppCrash) ? (AppCrash) t : new AppCrash(t);
        ac.logContext(Db.class.getName(), contesto);
        return ac;
    }

    static Object intero(Integer v) {

        return v == null ? NULL_INTERO : v;
    }

    static Object testo(String v) {

        return v == null ? NULL_TESTO : v;
    }

    private static void lega(PreparedStatement ps, Object[] valori) throws SQLException {

        for (int i = 0; i < valori.length; i++) {
            Object v = valori[i];
            int n = i + 1;
            if (v == null) {
                ps.setNull(n, Types.VARCHAR);
            } else if (v instanceof Nullo) {
                ps.setNull(n, ((Nullo) v).tipo);
            } else if (v instanceof String) {
                ps.setString(n, (String) v);
            } else if (v instanceof Integer) {
                ps.setInt(n, ((Integer) v).intValue());
            } else if (v instanceof Long) {
                ps.setLong(n, ((Long) v).longValue());
            } else if (v instanceof Boolean) {
                ps.setBoolean(n, ((Boolean) v).booleanValue());
            } else if (v instanceof Timestamp) {
                ps.setTimestamp(n, (Timestamp) v);
            } else {
                throw new IllegalArgumentException("Tipo non gestito: " + v.getClass().getName());
            }
        }
    }
}
