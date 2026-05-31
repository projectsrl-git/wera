
package net.project.db.ser;

import net.project.errors.AppCrash;
import net.project.misc.Config;

/**
 * Questa e' la factory utilizzata per creare i serializer per la memorizzazione degli oggetti e delle strutture
 * variabili nel DB. Usata da DAOXML_base. Sono presenti due metodi per creare serializzatori: uno crea un
 * serializzatore di tipo generico; l'altro crea un serializzatore particolare per la classe passata.Entrambi sono
 * configurati tramite proprieta' di config
 * 
 * @author Simone
 */
public class DBXMLSerializerFactory extends SerializerFactory_base {

    /**
     * Creates a new DBXMLSerializer object.
     */
    protected DBXMLSerializerFactory() {

    }

    /**
     * Questo metodo restituisce un Serializer_itf che serializza in XML. Restituisce una istanza della classe contenuta
     * nella proprieta' <b>DB.DBXMLSerializer</b> del file di configurazione
     *
     * @return il serializer XML
     *
     * @throws AppCrash
     */
    @Override
    public Serializer_itf makeSerializer() throws AppCrash {

        String serializer = Config.GetInstance().getProperty("DB.DBXMLSerializer");
        return makeIt(serializer);
    }

    /**
     * Questo metodo restituisce un Serializer_itf che serializza in XML. Restituisce una istanza della classe contenuta
     * nella proprieta' <b>DB.DBXMLSerializer.+ Class.getName()</b> del file di configurazione. Serve per ottenere un
     * serializer ad hoc classe per classe
     *
     * @param classe Class dell'oggetto da serializzare
     *
     * @return il serializer XML
     *
     * @throws AppCrash
     */
    @Override
    public Serializer_itf makeSerializer(Class classe) throws AppCrash {

        String nome = classe.getName();
        String serializer = Config.GetInstance().getProperty("DB.DBXMLSerializer." + nome);
        return makeIt(serializer);
    }

    private Serializer_itf makeIt(String classe) throws AppCrash {

        try {
            Serializer_itf result = (Serializer_itf) Class.forName(classe).newInstance();

            return result;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DBXMLSerializerFactory", "Errore istanziando: " + classe);
            throw ac;
        }

    }

}