
package net.project.db.ser;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import net.project.errors.AppCrash;
import net.project.errors.Logger;

import com.skaringa.javaxml.ObjectTransformer;
import com.skaringa.javaxml.ObjectTransformerFactory;

/**
 * Questa classe e' un serializzatore che serializza gli oggetti passati in stringhe.E' ovviamente presente anche il
 * meccanismo di deserializzazione. Per serializzare-deserializzare vengono utilizzate le classi del package open source
 * com.skaringa.javaxml GLi oggetti passati sono serializzati in stringhe che vengono poi compresse tramite
 * java.util.zip e trasformate in base64. Per la deserializzazione devono essere passate le stringhe originalmente
 * restituite dal serializzatore. Affinche' sia possibile serializzare e deserializzare con successo gli oggetti questi
 * devono avere un constructor senza parametri. La stessa regola si applica agli oggetti eventualmente contenuti negli
 * oggetti da serializzare.
 *
 * @author Simone
 */
public class SkaringaXMLSerializer implements Serializer_itf {

    /**
     * Deserializza un oggetto a partire da una stringa che sia stata generata dal metodo serializeToString di questa
     * classe.
     * 
     * @param str La stringa contenente l'oggetto da deserializzare
     * @see net.project.db.ser.Serializer_itf#deserializeFromString(String)
     */
    @Override
    public Object deserializeFromString(String str) throws AppCrash {

        String data = null;
        try {
            data = unzipXML(str);
            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("Deserialize XML obj: " + data);
            }
            // Get an ObjectTransformer.
            ObjectTransformer trans = ObjectTransformerFactory.getInstance().getImplementation();
            Object field = trans.deserializeFromString(data);
            return field;
        } catch (AppCrash e) {
            throw e;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("SkaringaXMLSerializer ", data);
            throw ac;
        }

    }

    /**
     * Serializza l'oggetto passato in una stringa. La stringa restituita e' in base64
     * 
     * @param obj l'oggetto da serializzare
     * @see net.project.db.ser.Serializer_itf#serializeToString(Object)
     */
    @Override
    public String serializeToString(Object obj) throws AppCrash {

        String XML = null;
        try {
            // Get an ObjectTransformer.
            ObjectTransformer trans = ObjectTransformerFactory.getInstance().getImplementation();

            // Serialize the object into a string
            XML = trans.serializeToString(obj);
            if (Logger.GetInstance().getLogLevel() >= 3) {
                Logger.GetInstance().log3("Serialize XML obj: " + XML);
            }
            XML = zipXML(XML);
            return XML;

        } catch (AppCrash e) {
            throw e;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("SkaringaXMLSerializer ", obj.toString());
            throw ac;
        }

    }

    /**
     * Comprime la stringa passata e la converte in Base64
     */
    private String zipXML(String str) throws AppCrash {

        Deflater compressor = null;
        try {
            byte[] input = str.getBytes();

            // Create the compressor with highest level of compression
            compressor = new Deflater();
            compressor.setLevel(Deflater.BEST_COMPRESSION);

            // Give the compressor the data to compress
            compressor.setInput(input);
            compressor.finish();

            // Create an expandable byte array to hold the compressed data.
            // You cannot use an array that's the same size as the orginal because
            // there is no guarantee that the compressed data will be smaller than
            // the uncompressed data.
            ByteArrayOutputStream bos = new ByteArrayOutputStream(input.length);

            // Compress the data
            byte[] buf = new byte[1024];
            while (!compressor.finished()) {
                int count = compressor.deflate(buf);
                bos.write(buf, 0, count);
            }
            bos.close();

            // Get the compressed data
            byte[] compressedData = bos.toByteArray();

            String compressed = net.project.misc.Base64.encode(compressedData);

            return compressed;
        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DAOXML", "Stringa in conversione:" + str);
            throw ac;
        } finally {
            if (compressor != null) {
                compressor.end();
            }
        }
    }

    /**
     * Converte la stringa passata Base64 e la decomprime
     */
    private String unzipXML(String str) throws AppCrash {

        Inflater decompressor = null;
        try {

            byte[] compressedData = net.project.misc.Base64.decode(str);

            // Create the decompressor and give it the data to compress
            decompressor = new Inflater();
            decompressor.setInput(compressedData);

            // Create an expandable byte array to hold the decompressed data
            ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);

            // Decompress the data
            byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }

            bos.close();

            // Get the decompressed data
            byte[] decompressedData = bos.toByteArray();

            byte[] tmpByte = new byte[1];
            tmpByte[0] = decompressedData[0];

            if (new String(tmpByte).equals("<")) {
                return new String(decompressedData);
            }
            return new String(decompressedData, "Cp1047");

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext("DAOXML", "Stringa in conversione:" + str);
            throw ac;
        } finally {
            if (decompressor != null) {
                decompressor.end();
            }
        }
    }

}
