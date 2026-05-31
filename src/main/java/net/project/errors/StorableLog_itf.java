
package net.project.errors;

/**
 * Questa interfaccia rappresenta un oggetto di log memorizzabile. Su tutti gli oggetti di questo tipo presenti nella
 * Logger.Info viene chiamato il metodo store() quando si esegue Logger.dumpInfo().
 * 
 * @author Simone
 */
public interface StorableLog_itf {

    /**
     * Questo metodo serve per memorizzare l'oggetto che implementa questa interfaccia Il metodo viene chiamato su tutti
     * gli oggetti di questo tipo presenti nella Logger.Info quando viene chiamato il metodo Logger.dumpInfo(). (ad
     * esempio nella finally di doPost e doGet delle servlet application)
     * 
     */
    public void store();
}
