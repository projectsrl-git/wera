package net.projectsrl.wera.centraletermica.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;

import net.project.db.DBTransaction;

/**
 * Statement parametrizzati dentro una DBTransaction, con i NULL tipizzati. Serve dove i DAO del
 * framework non bastano: NDAO_base scrive i null con setString (varchar) e Postgres li rifiuta sulle
 * colonne integer/date ("column is of type ... but expression is of type character varying").
 * Valori ammessi: null, String, Integer, Long, Boolean, java.sql.Date, Timestamp.
 */
final class Sql {

    /** NULL tipizzato: new Nullo(Types.INTEGER) ecc. */
    static final class Nullo {

        final int tipo;

        Nullo(int tipo) {

            this.tipo = tipo;
        }
    }

    static final Nullo NULL_INTERO = new Nullo(Types.INTEGER);
    static final Nullo NULL_TESTO  = new Nullo(Types.VARCHAR);
    static final Nullo NULL_DATA   = new Nullo(Types.DATE);

    private Sql() {

    }

    static int esegui(DBTransaction tx, String sql, Object... valori) throws Exception {

        PreparedStatement ps = tx.prepareStatement(sql);
        try {
            lega(ps, valori);
            return ps.executeUpdate();
        } finally {
            ps.close();
        }
    }

    /** Prima colonna della prima riga (null se nessuna riga). */
    static Object valore(DBTransaction tx, String sql, Object... valori) throws Exception {

        PreparedStatement ps = tx.prepareStatement(sql);
        try {
            lega(ps, valori);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getObject(1) : null;
        } finally {
            ps.close();
        }
    }

    static Integer intero(DBTransaction tx, String sql, Object... valori) throws Exception {

        Object v = valore(tx, sql, valori);
        return (v instanceof Number) ? Integer.valueOf(((Number) v).intValue()) : null;
    }

    private static void lega(PreparedStatement ps, Object[] valori) throws Exception {

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
            } else if (v instanceof java.sql.Date) {
                ps.setDate(n, (java.sql.Date) v);
            } else if (v instanceof Timestamp) {
                ps.setTimestamp(n, (Timestamp) v);
            } else {
                throw new IllegalArgumentException("Tipo non gestito: " + v.getClass().getName());
            }
        }
    }

    // --- conversioni per i campi delle richieste (null = assente o vuoto)

    static Object intero(Integer v) {

        return v == null ? NULL_INTERO : v;
    }

    static Object testo(String v) {

        return v == null ? NULL_TESTO : v;
    }

    static Object data(java.sql.Date v) {

        return v == null ? NULL_DATA : v;
    }
}
