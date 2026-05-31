package net.projectsrl.wera.importazioni.core;

public class UtilsErroreValidazione {
	public String nomeFoglio;
    public String campo;
    public String messaggio;
    public String valore;
    public String tipoPrevisto;
    public String tipoRicevuto;
    public int riga;
    public int colonna;

    public UtilsErroreValidazione(String nomeFoglio, String campo, String messaggio, String valore,
                                  String tipoPrevisto, String tipoRicevuto,
                                  int riga, int colonna) {
    	this.nomeFoglio=nomeFoglio;
        this.campo = campo;
        this.messaggio = messaggio;
        this.valore = valore;
        this.tipoPrevisto = tipoPrevisto;
        this.tipoRicevuto = tipoRicevuto;
        this.riga = riga;
        this.colonna = colonna;
    }

    @Override
    public String toString() {
        return String.format("Errore nel '%s' - campo '%s' (riga %d, colonna %d): %s. Valore: '%s'. Atteso: %s, Ricevuto: %s\n",
            nomeFoglio, campo, riga, colonna, messaggio, valore,
            tipoPrevisto != null ? tipoPrevisto : "-",
            tipoRicevuto != null ? tipoRicevuto : "-");
    }
}
