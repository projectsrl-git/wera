
package net.project.servlet.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;

import net.project.dataset.DataSet_itf;
import net.project.errors.AppCrash;
import net.project.errors.Logger;
import net.project.mess.LogColloquio;
import net.project.misc.Config;

/**
 * Questa classe permette di decorare una Page_itf per eseguire il log della risposta generata tramite il meccanismo dei
 * LogColloquio.
 * 
 * @author Simone
 */
public class LoggingPageDecorator implements Page_itf {

    private Page_itf _page;
    private String   _cfName;
    private String   _logEnabled;

    /**
     * Costruttore. Prende in ingresso la pagina da decorare. Legge la proprieta' di configurazione "Page." + name +
     * ".log". Il valore di defualt per a proprieta' e' true
     * 
     * @param Page_itf pagina da decorare
     * @param java.lang.String cfName nome della configurazione
     * @exception net.project.errors.AppCrash
     */
    public LoggingPageDecorator(Page_itf page, String cfName) throws AppCrash {

        _page = page;
        _cfName = cfName;
        _logEnabled = Config.GetInstance().getProperty("Page." + page.getName() + ".log", "true");
    }

    /**
     * Restituisce il nome della pagina.
     * 
     * @return java.lang.String
     * @exception
     */
    @Override
    public String getName() {

        return _page.getName();
    }

    /**
     * Valorizza i parametri di uno dei DataSet che serviranno a costruire la pagina.
     * 
     * @param param java.util.Hashtable hashtable dei parametri del DataSet
     * @param dsNum int identificativo del DataSet
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void setDataSourceParam(Map param, int dsNum) throws AppCrash {

        _page.setDataSourceParam(param, dsNum);
    }

    /**
     * Se gli oggetti contenuti come valori nella hashtable sono istanze di DataSet_itf, allora richiama su ognuno di
     * essi il metodo setPageRootData(DataSet_itf, String) assumendo che le rispettive chiavi siano stringhe contenenti
     * il nome del nodo della _modelRoot; altrimenti carica nella _modelRoot tanti oggetti SimpleScalar quanti sono gli
     * elementi della hashtable stessa
     * 
     * @param param java.util.Hashtable paramHash hashtable contenente i parametri passati dal client
     * @return void
     * @exception net.project.errors.AppCrash
     */
    @Override
    public void setPageRootData(Map paramHash) throws AppCrash {

        _page.setPageRootData(paramHash);
    }

    /**
     * Riceve un dataset e carica in un nodo della _modelRoot un oggetto DataSetToSimpleListAdapter (ottenuto dal
     * dataset stesso)
     * 
     * @param ds net.project.dataset.DataSet_itf il dataset in ingresso
     * @param name java.lang.String il nome del nodo della _modelRoot cui assegnare l'oggetto DataSetToSimpleListAdapter
     * @return void
     * @exception net.project.errors.ParamCrash
     */
    @Override
    public void setPageRootData(DataSet_itf ds, String name) throws AppCrash {

        _page.setPageRootData(ds, name);
    }

    /**
     * Mostra la pagina creata ed esegue il log se necessario: verifica se la proprieta' di configurazione
     * Page.+name+.log e' uguale a true.(default true)
     * 
     * @param out java.io.PrintWriter l'oggetto sul quale mostrare la pagina
     * @return void
     */
    @Override
    public void display(PrintWriter pw) throws AppCrash {

        LogColloquio log = null;
        Hashtable info = Logger.GetInstance().getInfo();

        if (info != null) {
            log = (LogColloquio) info.get(LogColloquio.RQ_HTTP_ENTRANTE);
        }

        // Se non devo loggare o non c'e' un oggetto LogColloquio nel quale mettere il risultato
        // eseguo direttamente la display
        if (_logEnabled.equalsIgnoreCase("false") || log == null) {
            _page.display(pw);
            return;
        }

        // Se devo loggare faccio creare l'output in un bytearray locale e poi lo mando al cliente
        // e lo memorizzo
        String output;
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream(1000);
            PrintWriter localPW = new PrintWriter(bao);

            _page.display(localPW);
            localPW.flush();
            localPW.close();

            output = bao.toString();
            bao.close();
            pw.print(output);
            pw.flush();

        } catch (AppCrash e) {
            throw e;
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            throw ac;
        }

        log.setRisposta(output);

    }

    /**
     * Questo metodo ritorna il nome della configurazione usata per creare la pagina
     *
     * @return String nome della configurazione
     */
    @Override
    public String getConfigName() {

        return _cfName;
    }

}
