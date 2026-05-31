
package net.project.db.ser;

import java.util.HashMap;
import java.util.Map;

import net.project.errors.AppCrash;
import net.project.misc.Config;

/**
 * Questa classe e' la factory base per la costruzione di serializzatori. E' un multi singleton e definisce i due metodi
 * astratti che devono essere implementati dalle factory concrete.
 * 
 * @author Simone
 */
public abstract class SerializerFactory_base {

    static private Map _Factories = new HashMap();

    /**
     * Questo metodo ritorna l'istanza della classe SerializerFactory_base presente nel sistema con il nome indicato. Se
     * un tale oggetto non esiste ancora ne crea uno. L'oggetto creato appartiene alla classe concreta letta dalla
     * proprieta' di configurazione <b>SerializerFactoryClass.NOME</b>
     *
     * @param name java.lang.String nome della factory
     * @return SerializerFactory_base L'istanza della classe SerializerFactory_base
     * @exception net.project.errors.AppCrash Nel caso vi fossere problemi nell'istanziazione della classe concreta.
     */
    public final static SerializerFactory_base GetInstance(String name) throws AppCrash {

        SerializerFactory_base result = (SerializerFactory_base) _Factories.get(name);
        // questo e' un "Double checked lock" design pattern
        if (result == null) {
            synchronized (SerializerFactory_base.class) {
                result = (SerializerFactory_base) _Factories.get(name);
                if (result == null) {
                    String classe = Config.GetInstance().getProperty("SerializerFactoryClass." + name);
                    try {
                        result = (SerializerFactory_base) Class.forName(classe).newInstance();
                        _Factories.put(name, result);
                    } catch (ClassNotFoundException e) {
                        AppCrash err = new AppCrash(e);
                        err.logContext("SerializerFactory_base", " class not found" + classe);
                    } catch (IllegalAccessException e) {
                        AppCrash err = new AppCrash(e);
                        err.logContext("SerializerFactory_base", " error " + classe);
                    } catch (InstantiationException e) {
                        AppCrash err = new AppCrash(e);
                        err.logContext("SerializerFactory_base", " error" + classe);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Metodo astratto che deve essere implementato dalla factory concreta per creare un serializzatore generico.
     * 
     * @return Serializer_itf
     * @throws AppCrash
     */
    public abstract Serializer_itf makeSerializer() throws AppCrash;

    /**
     * Metodo astratto che deve essere implementato dalla factory concreta per creare un serializzatore ad hoc per il
     * tipo di classe passato.
     * 
     * @param classe classe per la quale costruire il serializzatore
     * @return Serializer_itf
     * @throws AppCrash
     */
    public abstract Serializer_itf makeSerializer(Class classe) throws AppCrash;

}
