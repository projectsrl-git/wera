
package net.project.dataset;

import net.project.errors.AppCrash;

/**
 * Questa classe che estende DBDataSet rappresenta un dataset in grado di aprirsi automaticamente quando viene
 * richiamato il metodo rewind() e di chiudersi quando viene letto l'ultimo record presente. Il metodo open() non fa
 * nulla.
 * <P>
 * Questo tipo di dataset e' stato introdotto per essere utilizzato in Freemarker la' dove in una pagina vi fossero una
 * grande quantita' di dataset: i dataset vreati dalle page_itf vengono tutti aperti prima di incominciare la display e
 * percio' occupano ciascuno una connessione ad DB. Nel caso vi fossero 30 dataset in una pagina cio' non sarebbe sano.
 * Con i dataset AutomaticDBDataSet invece i dataset sono effettivamente aperti e chiusi direttamente da freemarker
 * quando occorrono uno alla volta (freemarker chiama rewind prima di usare una dataset).
 * <p>
 * Questa esigenza e' nata ad esempio nella reportistica del gestionale Bankpass.
 * 
 * @author Simone
 */
public class AutomaticDBDataSet extends DBDataSet {

    private boolean _opened = false;

    /**
     * Costruttore.
     * 
     * @roseuid 3A5B327E0326
     */
    public AutomaticDBDataSet(String configName, String dsName) throws AppCrash {

        super(configName, dsName);

    }

    /**
     * Questo metodo non fa assolutamente nulla
     * 
     * @return void
     */
    @Override
    public void open() throws AppCrash {

        return;
    }

    /**
     * Posiziona il cursore del ResultSet davanti alla prima riga. Una chiamata a questo metodo provoca anche la open()
     * vera e propria del dataset.
     * 
     * @return void
     * @exception net.project.errors.AppCrash in caso di SQLException o di Resultset = null
     * @roseuid 3A5D7D5200D4
     */
    @Override
    public void rewind() throws AppCrash {

        close();
        super.open();
        _opened = true;

    }

    /**
     * Controlla se il ResultSet contiene altri elementi.Se non vi sono ulteriori elementi presenti nel dataset viene
     * richiamato automaticamente il metodo close().
     * 
     * @return boolean true se il ResultSet contiene altri elementi, false altrimenti
     * @exception RuntimeException: in caso di SQLException N.B. viene lanciata una RuntimeException, anziché
     *                un'eccezione di net.project.errors, in quanto il presente metodo deriva dall'interfaccia
     *                Enumeration (implementata indirettamente tramite DataSet_itf), e pertanto la sua signature (che
     *                non prevede eccezioni) non può essere cambiata.
     */
    @Override
    public boolean hasMoreElements() {

        if (!_opened) return false;

        boolean hasMore = super.hasMoreElements();

        if (!hasMore) {
            try {
                close();
            } catch (AppCrash t) {
                throw new RuntimeException("AutomaticDBDataSet Error - " + toString());
            }
        }

        return hasMore;
    }

}
