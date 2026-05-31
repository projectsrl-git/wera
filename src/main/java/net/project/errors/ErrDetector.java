/*
  ErrDetector.java

  Copyright (c) by SSB spa Societa' per i Servizi Bancari 

  Data creazione: 14/05/1999

  Autore: Simone Z.

  Note:

  Modifiche:	08/10/99	TL16 ristrutturazione eccezioni
				02/10/2000	Anna L.		Aggiunta interfaccia.

 */

package net.project.errors;

/**
 * Classe base per la rilevazione degli errori. E' un singleton.
 */

public class ErrDetector implements ErrDetector_itf {

    static private ErrDetector_itf _Instance = null;

    /**
     * Metodo di accesso al singleton restituisce l'istanza dell'oggetto.
     *
     * @return L'istanza dell'oggetto.
     */
    public static ErrDetector_itf GetInstance() {

        // questo e' un "Double checked lock" design pattern
        if (_Instance == null) {
            synchronized (ErrDetector.class) {
                if (_Instance == null) {
                    _Instance = new ErrDetector();
                }
            }
        }

        return _Instance;
    }

    /**
     * Controlla la validita' di un parametro.
     *
     * @param cond boolean La condizione booleana da verificare.
     * @exception net.project.errors.ParamCrash Se la condizione è falsa.
     */
    @Override
    public void param(boolean cond) throws ParamCrash {

        if (!cond) {
            ParamCrash err = new ParamCrash();
            throw err;
        }
    }

    /**
     * Controlla la validita' di un parametro.
     *
     * @param cond boolean La condizione booleana da verificare.
     * @param messaggio java.lang.String Il messaggio da loggare al verificarsi del crash.
     * @exception net.project.errors.ParamCrash Se la condizione è falsa.
     */
    @Override
    public void param(boolean cond, String messaggio) throws ParamCrash {

        if (!cond) {
            ParamCrash err = new ParamCrash(messaggio);
            throw err;
        }
    }

    /**
     * Controlla che una oggetto non sia null
     *
     * @param obj java.lang.Object L'oggetto da controllare.
     * @exception net.project.errors.ParamCrash Se l'oggetto è null.
     */
    @Override
    public void param(Object obj) throws ParamCrash {

        if (obj == null) {
            ParamCrash err = new ParamCrash("Param NULL error");
            throw err;
        }
    }

    /**
     * Controlla che una stringa non sia vuota
     *
     * @param str java.lang.String La stringa da controllare.
     * @exception net.project.errors.ParamCrash Se la stringa è vuota.
     */
    @Override
    public void param(String str) throws ParamCrash {

        if (str == null) {
            ParamCrash err = new ParamCrash("Param STRING empty error");
            throw err;
        }
        if (str.length() == 0) {
            ParamCrash err = new ParamCrash("Param STRING empty error");
            throw err;
        }
    }

    /**
     * Controlla l'esistenza di una precondizione
     *
     * @param cond boolean La precondizione da verificare.
     * @exception net.project.errors.AppCrash Se la precondizione è falsa.
     */
    @Override
    public void preCond(boolean cond) throws AppCrash {

        check(cond, "");
    }

    /**
     * Controlla l'esistenza di una precondizione
     *
     * @param cond boolean La precondizione da verificare.
     * @param messaggio java.lang.String Il messaggio di errore.
     * @exception net.project.errors.AppCrash Se la precondizione è falsa.
     */
    @Override
    public void preCond(boolean cond, String messaggio) throws AppCrash {

        check(cond, messaggio);
    }

    /**
     * Controlla l'esistenza di una postcondizione
     *
     * @param cond boolean La postcondizione da verificare.
     * @exception net.project.errors.AppCrash Se la postcondizione è falsa.
     */
    @Override
    public void postCond(boolean cond) throws AppCrash {

        check(cond, "");
    }

    /**
     * Controlla l'esistenza di una postcondizione
     *
     * @param cond boolean La postcondizione da verificare.
     * @param messaggio java.lang.String Il messaggio di errore.
     * @exception net.project.errors.AppCrash Se la postcondizione è falsa.
     */
    @Override
    public void postCond(boolean cond, String messaggio) throws AppCrash {

        check(cond, messaggio);
    }

    /**
     * Controlla la validita' di un invariante
     *
     * @param cond boolean L'invariante da verificare.
     * @exception net.project.errors.AppCrash Se l'invariante è falsa.
     */
    @Override
    public void invariant(boolean cond) throws AppCrash {

        check(cond, "");
    }

    /**
     * Controlla la validita' di un invariante
     *
     * @param cond boolean L'invariante da verificare.
     * @param messaggio java.lang.String Il messaggio di errore.
     * @exception net.project.errors.AppCrash Se l'invariante è falsa.
     */
    @Override
    public void invariant(boolean cond, String messaggio) throws AppCrash {

        check(cond, messaggio);
    }

    /**
     * Controlla la validità di una condizione.
     *
     * @param cond boolean La condizione da verificare.
     * @param mess java.lang.String Il messaggio di errore.
     * @exception net.project.errors.AppCrash Se la condizione non è verificata.
     */
    private void check(boolean cond, String mess) throws AppCrash {

        if (!cond) {
            InvariantCrash err = new InvariantCrash(mess);
            throw err;
        }
    }

}
