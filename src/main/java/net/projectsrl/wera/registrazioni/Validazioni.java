package net.projectsrl.wera.registrazioni;

import java.util.regex.Pattern;

/**
 * Controlli formali dei dati fiscali e di contatto (lato server; il JS della pagina ripete gli stessi per
 * dare l'errore subito). Nessuna verifica online: la verifica vera la fa chi approva la richiesta.
 */
final class Validazioni {

    private static final Pattern EMAIL      = Pattern.compile("^[^\\s@<>\"',;]+@[^\\s@<>\"',;]+\\.[^\\s@<>\"',;.]{2,}$");
    private static final Pattern CF_PERSONA = Pattern.compile(
            "^[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]$");
    /** Valori dei caratteri in posizione dispari (1a, 3a, ...) per il carattere di controllo del CF. */
    private static final int[]   CF_DISPARI = { 1, 0, 5, 7, 9, 13, 15, 17, 19, 21, 2, 4, 18, 20, 11, 3, 6, 8, 12, 14, 16,
            10, 22, 25, 24, 23 };

    private Validazioni() {

    }

    /** Partita IVA italiana: 11 cifre con cifra di controllo (non tutta zeri). */
    static boolean partitaIva(String v) {

        if (v == null || !v.matches("[0-9]{11}") || v.equals("00000000000")) {
            return false;
        }
        int somma = 0;
        for (int i = 0; i < 10; i++) {
            int c = v.charAt(i) - '0';
            if (i % 2 == 1) {
                c = c * 2;
                if (c > 9) {
                    c -= 9;
                }
            }
            somma += c;
        }
        return (10 - somma % 10) % 10 == v.charAt(10) - '0';
    }

    /** Codice fiscale di persona fisica (16 caratteri, omocodie comprese) con carattere di controllo. */
    static boolean codiceFiscalePersona(String v) {

        if (v == null || !CF_PERSONA.matcher(v).matches()) {
            return false;
        }
        int somma = 0;
        for (int i = 0; i < 15; i++) {
            char c = v.charAt(i);
            int k = Character.isDigit(c) ? c - '0' : c - 'A';
            somma += (i % 2 == 0) ? CF_DISPARI[k] : k;
        }
        return v.charAt(15) == (char) ('A' + somma % 26);
    }

    /** Codice fiscale: di persona (16) oppure numerico di societa' (11 cifre, stesso controllo della P.IVA). */
    static boolean codiceFiscale(String v, boolean ammessoNumerico) {

        if (v != null && v.length() == 11) {
            return ammessoNumerico && partitaIva(v);
        }
        return codiceFiscalePersona(v);
    }

    static boolean email(String v) {

        return v != null && v.length() <= 100 && EMAIL.matcher(v).matches();
    }

    /** Codice destinatario SDI: 7 caratteri alfanumerici (facoltativo: null = valido). */
    static boolean codiceSdi(String v) {

        return v == null || v.matches("[A-Z0-9]{7}");
    }

    static boolean cap(String v) {

        return v != null && v.matches("[0-9]{5}");
    }

    static boolean provincia(String v) {

        return v != null && v.matches("[A-Z]{2}");
    }

    /** Telefono: cifre con +, spazi, punti, trattini o barre; almeno 6 cifre. */
    static boolean telefono(String v) {

        return v != null && v.matches("\\+?[0-9 ./()-]{6,30}") && v.replaceAll("[^0-9]", "").length() >= 6;
    }

    /** Testo libero accettabile: niente parentesi angolari (finisce nelle mail HTML e nelle pagine). */
    static boolean testoPulito(String v) {

        return v == null || (v.indexOf('<') < 0 && v.indexOf('>') < 0);
    }

    /** Maiuscolo senza spazi interni (CF, P.IVA, SDI, provincia). Null resta null. */
    static String compatto(String v) {

        return v == null ? null : v.replaceAll("\\s+", "").toUpperCase();
    }

    /** P.IVA senza prefisso IT (chi la copia dalla fattura lo mette spesso). */
    static String partitaIvaNormalizzata(String v) {

        String c = compatto(v);
        if (c != null && c.startsWith("IT") && c.length() == 13) {
            c = c.substring(2);
        }
        return c;
    }
}
