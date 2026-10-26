package net.projectsrl.wera.registrazioni;

import java.util.Map;

import net.project.misc.Config;
import net.projectsrl.mail.SendSMTPMail;

/**
 * Mail delle registrazioni: oggetto e testo dai parametri <prefisso>.OGGETTO / <prefisso>.TESTO con i
 * segnaposto {NOME} ecc. Con MAIL.ATTIVA = N non parte nulla (in PROD l'SMTP di wera2026 e' il
 * mailcatcher). Ogni tentativo finisce nel registro eventi con l'esito: INVIATA, NON_INVIATA o ERRORE.
 * Nessuna mail contiene password.
 */
final class Posta {

    static final String INVIATA     = "INVIATA";
    static final String NON_INVIATA = "NON_INVIATA";
    static final String ERRORE      = "ERRORE";

    private Posta() {

    }

    /** Esito dell'invio (INVIATA, NON_INVIATA, ERRORE); null se non c'era nessun destinatario. */
    static String invia(Parametri p, String prefisso, String destinatari, Map<String, String> segnaposto,
            Integer idRichiesta, Integer idUtente, String ip) {

        if (destinatari == null || destinatari.trim().length() == 0) {
            return null;
        }
        String oggetto = ascii(sostituisci(p.testo(prefisso + ".OGGETTO", "WERA"), segnaposto)).replaceAll("[\\r\\n]+", " ");
        String testo = ascii(sostituisci(p.testo(prefisso + ".TESTO"), segnaposto));
        String descrizione = prefisso + " a " + destinatari.trim();

        if (!p.vero(RegistrazioniCostanti.PAR_MAIL_ATTIVA, true)) {
            Eventi.registra(RegistrazioniCostanti.EVENTO_MAIL, idRichiesta, idUtente, null, ip,
                    NON_INVIATA + " (MAIL.ATTIVA=N) " + descrizione);
            return NON_INVIATA;
        }
        try {
            String mittente = p.testo(RegistrazioniCostanti.PAR_MAIL_MITTENTE);
            if (mittente.length() == 0) {
                mittente = Config.GetInstance().getProperty("ResetPassword.Mail.Address.From",
                        Config.GetInstance().getProperty("mail.from.service"));
            }
            SendSMTPMail mail = new SendSMTPMail();
            mail.setFrom(mittente);
            mail.setTo(destinatari.trim());
            mail.setCc("");
            mail.setBcc("");
            mail.setSubject(oggetto);
            mail.setBody(testo);
            mail.setServer(Config.GetInstance().getProperty("mail.SMTPHost"));
            mail.setUsername(mittente);
            mail.prepareMail();
            mail.sendMail();
            Eventi.registra(RegistrazioniCostanti.EVENTO_MAIL, idRichiesta, idUtente, null, ip,
                    INVIATA + " " + descrizione);
            return INVIATA;
        } catch (Throwable t) {
            String messaggio = t.getMessage() == null ? t.getClass().getName() : t.getMessage();
            Eventi.registra(RegistrazioniCostanti.EVENTO_MAIL, idRichiesta, idUtente, null, ip,
                    ERRORE + " " + descrizione + ": " + (messaggio.length() > 300 ? messaggio.substring(0, 300) : messaggio));
            return ERRORE;
        }
    }

    /**
     * Solo ASCII: SendSMTPMail dichiara charset us-ascii, quindi un accento arriverebbe rovinato. Vocali
     * accentate all'italiana (o'), altre lettere senza segni diacritici, il resto diventa "?".
     */
    static String ascii(String testo) {

        String t = testo.replaceAll("[\u00e0\u00e1]", "a'").replaceAll("[\u00e8\u00e9]", "e'").replace("\u00ec", "i'")
                .replace("\u00f2", "o'").replace("\u00f9", "u'").replaceAll("[\u00c0\u00c1]", "A'")
                .replaceAll("[\u00c8\u00c9]", "E'").replace("\u00cc", "I'").replace("\u00d2", "O'")
                .replace("\u00d9", "U'").replace("\u2019", "'").replace("\u20ac", "EUR");
        t = java.text.Normalizer.normalize(t, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return t.replaceAll("[^\\x09\\x0A\\x0D\\x20-\\x7E]", "?");
    }

    /** Sostituisce {CHIAVE} con il valore (senza parentesi angolari); i segnaposto sconosciuti restano. */
    static String sostituisci(String testo, Map<String, String> segnaposto) {

        String risultato = testo == null ? "" : testo;
        for (Map.Entry<String, String> e : segnaposto.entrySet()) {
            String valore = e.getValue() == null ? "" : e.getValue().replaceAll("[<>]", "");
            risultato = risultato.replace("{" + e.getKey() + "}", valore);
        }
        return risultato;
    }
}
