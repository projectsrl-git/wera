/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.project.servlet.gui;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Importo;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Questa classe serve per rendere disponibile all'interno dei template di freemarker un metodo di formattazione per i
 * vari tipi di dato
 *
 * @author zorzetti
 */
public class FreemarkerFormat implements TemplateMethodModelEx {

    /**
     * Esegue la formattazione del TemplateModel passato secondo il formato indicato. I parametri attesi sono due:
     * l'oggetto da formattare ed il formato da usare.
     *
     * <p>
     * Nel caso di importi (indicati dal formato I) si attende un terzo parametro: la divisa nel formato iso 380,978
     * </p>
     * <p>
     * Il metodo richiama dei metodi protetti a seconda del tipo di dato da formattare. Questo metodi sono ovviamente
     * ridefinibili.
     * </p>
     *
     * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
     */
    @Override
    public Object exec(List arg) throws TemplateModelException {

        try {
            Object objDaFormattare = arg.get(0);

            if (objDaFormattare == null) {
                return new SimpleScalar("");
            }

            SimpleScalar comeFormattare = (SimpleScalar) arg.get(1);
            String formato = comeFormattare.getAsString();

            if (formato.equalsIgnoreCase("I")) {
                String divisa = ((SimpleScalar) arg.get(2)).getAsString();
                Number num = ((SimpleNumber) objDaFormattare).getAsNumber();
                String result = formatImporto(num.longValue(), divisa);

                return result;
            }

            if (objDaFormattare instanceof SimpleDate) {
                String result = formatDate(((SimpleDate) objDaFormattare).getAsDate(), formato);

                return result;
            }

            if (objDaFormattare instanceof SimpleNumber) {
                String result = formatNumber(((SimpleNumber) objDaFormattare).getAsNumber(), formato);

                return result;
            }

            if (objDaFormattare instanceof SimpleScalar) {
                String result = formatString(((SimpleScalar) objDaFormattare).getAsString(), formato);

                return result;
            }
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
        }

        return "err";
    }

    /**
     * Questo metodo esegue la formattazione della data passata con il formato indicato
     *
     * @param data la data da formattare
     * @param formato formato da usare
     *
     * @return la data formattata
     */
    protected String formatDate(Date data, String formato) {

        String resp = "err";

        try {
            SimpleDateFormat format = new SimpleDateFormat(formato);
            resp = format.format(data);
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("FreemarkerFormat", "Format di " + data + " Formato:" + formato);
        }

        return resp;
    }

    /**
     * Questo metodo provvede alla formattazione del valore numerico passato con il formato indicato
     *
     * @param num numero da formattare
     * @param formato formato da usare
     *
     * @return DOCUMENT ME!
     */
    protected String formatNumber(Number num, String formato) {

        String resp = "err";

        try {
            DecimalFormat format = new DecimalFormat(formato);
            resp = format.format(num);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("FreemarkerFormat", "Format di " + num + " Formato:" + formato);
        }

        return resp;
    }

    /**
     * Questo metodo formatta le stringhe passate secondo il formato passato. Riconosce il formato V come valuta
     * (divisa) e traduce il valore passato (380,978) nel corrispondente valore stringa contenuto nelle proprieta' di
     * configurazione. Se il formato e' diverso da V viene restituita la stessa stringa avuta in ingresso
     *
     * <p>
     * Importo.Valuta.Lire per 380
     * </p>
     *
     * <p>
     * Importo.Valuta.Euro per 978
     * </p>
     *
     * <p>
     * </p>
     *
     * @param str stringa da formattare
     * @param formato formato
     *
     * @return la stringa formattata
     */
    protected String formatString(String str, String formato) {

        if (formato.equalsIgnoreCase("V")) {
            if (str.equals("380")) {
                return Config.GetInstance().getProperty("Importo.Valuta.Lire");
            }

            if (str.equals("978")) {
                return Config.GetInstance().getProperty("Importo.Valuta.Euro");
            }
        }

        return str;
    }

    /**
     * Questo metodo formatta gli importi con la divisa passata: per ora euro o lire
     *
     * @param num importo da formattare
     * @param divisa divisa (978, 380) euro o lire
     *
     * @return l'importo formattato
     */
    protected String formatImporto(long num, String divisa) {

        String resp = "err";

        try {
            String str = Long.toString(num);

            Importo imp = new Importo(str, divisa);
            resp = imp.getImporto();
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("FreemarkerFormat", "Format di " + num + " Divisa:" + divisa);
        }

        return resp;
    }
}
