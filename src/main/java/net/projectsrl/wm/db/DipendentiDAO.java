
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.db.PjDAO_base;

/**
 * Classe che rappresenta la tabella DIPENDENTI Proprietà lette dal file di configurazione:
 * <p>
 * DATABASE.NAME = nome del database
 */
public class DipendentiDAO extends PjDAO_base {

    private static final String NOME_TABELLA        = "DIPENDENTI";

    public static final String  ID_DIPENDENTE       = "ID_DIPENDENTE";
    public static final String  ID_UTENTE           = "ID_UTENTE";
    public static final String  MATRICOLA           = "MATRICOLA";
    public static final String  NOMINATIVO          = "NOMINATIVO";
    public static final String  DATA_ASSUNZIONE     = "DATA_ASSUNZIONE";
    public static final String  DATA_CESSAZIONE     = "DATA_CESSAZIONE";
    public static final String  MATR_MECCANOG       = "MATR_MECCANOG";
    public static final String  MATR_INPS           = "MATR_INPS";
    public static final String  MATR_PROVVISORIA    = "MATR_PROVVISORIA";
    public static final String  AZIENDA             = "AZIENDA";
    public static final String  FILIALE             = "FILIALE";
    public static final String  SESSO               = "SESSO";
    public static final String  TEL_FISSO           = "TEL_FISSO";
    public static final String  TEL_CELLULARE       = "TEL_CELLULARE";
    public static final String  COD_FISCALE         = "COD_FISCALE";
    public static final String  GRUPPO_APPARTENENZA = "GRUPPO_APPARTENENZA";
    public static final String  MANSIONE            = "MANSIONE";
    public static final String  INDIR_DOMICILIO     = "INDIR_DOMICILIO";
    public static final String  CITTA_DOMICILIO     = "CITTA_DOMICILIO";
    public static final String  CAP_DOMICILIO       = "CAP_DOMICILIO";
    public static final String  PROV_DOMICILIO      = "PROV_DOMICILIO";
    public static final String  INDIR_RESIDENZA     = "INDIR_RESIDENZA";
    public static final String  CITTA_RESIDENZA     = "CITTA_RESIDENZA";
    public static final String  CAP_RESIDENZA       = "CAP_RESIDENZA";
    public static final String  PROV_RESIDENZA      = "PROV_RESIDENZA";
    public static final String  DATA_NASCITA        = "DATA_NASCITA";
    public static final String  CITTA_NASCITA       = "CITTA_NASCITA";
    public static final String  PROV_NASCITA        = "PROV_NASCITA";
    public static final String  GRUPPO_SANGUE       = "GRUPPO_SANGUE";
    public static final String  NOME_BANCA          = "NOME_BANCA";
    public static final String  CODICE_ABI          = "CODICE_ABI";
    public static final String  CODICE_CAB          = "CODICE_CAB";
    public static final String  CONTO_CORRENTE      = "CONTO_CORRENTE";
    public static final String  CODICE_IBAN         = "CODICE_IBAN";
    public static final String  MAIL                = "MAIL";
    public static final String  GR_AUTORIZZAZ       = "GR_AUTORIZZAZ";
    public static final String  NOTE                = "NOTE";
    public static final String  NOTE_BIS            = "NOTE_BIS";
    public static final String  CITTADINO_EXTRACEE  = "CITTADINO_EXTRACEE";
    public static final String  PERM_SOG_NUMERO     = "PERM_SOG_NUMERO";
    public static final String  PERM_SOG_SCADENZA   = "PERM_SOG_SCADENZA";
    public static final String  PROFILO_ORARIO      = "PROFILO_ORARIO";

    public DipendentiDAO() throws AppCrash {

        super(NOME_TABELLA);
        setUniqueIdentifier(ID_DIPENDENTE);
    }

    public DipendentiDAO(DBTransaction transact) throws AppCrash {

        super(transact, NOME_TABELLA);
        setUniqueIdentifier(ID_DIPENDENTE);
    }

    public DipendentiDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
        setUniqueIdentifier(ID_DIPENDENTE);

    }

}
