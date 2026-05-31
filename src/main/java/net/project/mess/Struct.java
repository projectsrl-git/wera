/*
  Copyright (c) by SSB spa Societa' per i Servizi Bancari

  Autore: Simone Z.

  Note:


 */

package net.project.mess;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.project.errors.AppCrash;

/**
 * Questa classe rappresenta una generica struttura dati. Un insieme di campi. E' ottenuta estendendo MsgWriterEx
 * <P>
 * I nomi dei campi permessi sono ricavati dalle costanti dischiarate di tipo static public final String che iniziano
 * per C_ dalle classi che la estendono
 * <P>
 * <P>
 * Per il suo uso concreto si deve creare una classe che la estende e che definisce i nomi dei campi contenuti nella
 * struttura dati
 * 
 * @author Simone
 */
public class Struct extends MsgWriterEx {

    /**
     * Crea un nuovo oggetto di tipo Struct. Il tipo della struttura restituito da getType viene impostato con il nome
     * passato.
     * 
     * @param type il tipo da impostare come valore per getType()
     */
    public Struct(String type) throws AppCrash {

        super(type);
    }

    /**
     * Crea un nuovo oggetto di tipo Struct. Il tipo viene impostato al valore "struct"
     */
    public Struct() throws AppCrash {

        this("struct");
    }

    /**
     * Questo metodo serve per impostare il valore di un campo della struttura. Se il campo richiesto non fa parte della
     * struttura viene generata una eccezione
     * 
     * @param name name nome del campo.
     * @param value value valore del campo.
     * 
     * @exception AppCrash
     */
    @Override
    public void setField(String name, String value) throws AppCrash {

        if (value == null) value = "";
        super.setField(name, value);
    }

    /**
     * Questo metodo oggi non e' supportato. E' presente perche' viene implementata l'interfaccia MsgWriter_itf
     * 
     * @return byte[] un array di byte che contiene il messaggio costruito
     * 
     * @throws AppCrash
     */
    @Override
    public byte[] getMessage() throws AppCrash {

        throw new AppCrash("Metodo non supportato");
    }

    /**
     * Questo metodo recupera l'elenco dei campi che fanno parte di questa struttura ricercando tutti gli attributi
     * dichiarati come public static final di tipo String e che incominciano per "C_". I valori assegnati a tali
     * attributi rappresentano i nomi dei campi.
     * 
     * @return un iterator con i nomi dei campi permessi
     * 
     * @throws AppCrash
     */
    @Override
    protected Iterator getFieldList() throws AppCrash {

        Field[] fields = this.getClass().getFields();
        Set campi = new HashSet();

        int j = 0;
        String nome = null;

        try {
            for (j = 0; j < fields.length; j++) {

                nome = fields[j].getName();

                // Se il nome non inizia con C_ allora non e' un campo
                if (nome.startsWith("C_") == false) {
                    continue;
                }

                Class tipo = fields[j].getType();

                // se non e' di tipo stringa lo ignoro
                if (!tipo.getName().equals("java.lang.String")) {
                    continue;
                }

                int modif = fields[j].getModifiers();

                // Se a questo punto il campo e' una stringa public static final allora il suo valore fa parte dei nomi
                // campi accettati
                if (Modifier.isStatic(modif) && Modifier.isPublic(modif) && Modifier.isFinal(modif)) {

                    campi.add(fields[j].get(this));
                }

            }
        } catch (IllegalAccessException e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext("Struct", "Errore sul campo " + j + " - " + nome);
        }

        return campi.iterator();
    }
}
