
package net.projectsrl.wera.anagrafiche.db;

import java.math.BigDecimal;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class CondominiDAO extends AliModDAO_base {

	private static final String TABLE_NAME       = "CONDOMINI";

    public static final String  ID_MODULO = "ID_MODULO";
    public static final String  DENOMINAZIONE   = "DENOMINAZIONE";
    public static final String  INDIRIZZO           = "INDIRIZZO";
    public static final String  LOCALITA           = "LOCALITA";
    public static final String  PROVINCIA            = "PROVINCIA";
    public static final String  CAP          = "CAP";
    public static final String  SIM           = "SIM";
    public static final String  SCARICO           = "SCARICO";
    public static final String  ID_AMMINISTRATORE              = "ID_AMMINISTRATORE";
    public static final String  CODFISC           = "CODFISC";
    public static final String  DATA_PRIMO_SCARICO             = "DATA_PRIMO_SCARICO";
    public static final String  GEN             = "GEN";
    public static final String  FEB        = "FEB";
    public static final String  MAR        = "MAR";
    public static final String  APR        = "APR";
    public static final String  MAG        = "MAG";
    public static final String  GIU        = "GIU";
    public static final String  LUG        = "LUG";
    public static final String  AGO        = "AGO";
    public static final String  SETT        = "SETT";
    public static final String  OTT        = "OTT";
    public static final String  NOV        = "NOV";
    public static final String  DIC        = "DIC";
    public static final String  PERDITE_IMPIANTO        = "PERDITE_IMPIANTO";
    public static final String  FABBISOGNO_ANNUO_CLIMA        = "FABBISOGNO_ANNUO_CLIMA";
    public static final String  FABBISOGNO_ANNUO_ACS        = "FABBISOGNO_ANNUO_ACS";
    public static final String  RENDIMENTO_CALDAIA        = "RENDIMENTO_CALDAIA";
    public static final String  TIPO        = "TIPO";
    public static final String  TIPO_CONTABILIZZAZIONE = "TIPO_CONTABILIZZAZIONE";
    public static final String  DATA_PRIMA_SEGNALAZIONE     = "DATA_PRIMA_SEGNALAZIONE";
    public static final String  GG_SEGNALAZIONE             = "GG_SEGNALAZIONE";
    public static final String  TIPOLOGIA_IMPIANTO             = "TIPOLOGIA_IMPIANTO";
    public static final String  RESPONSABILE_IMPIANTO             = "RESPONSABILE_IMPIANTO";
    public static final String  ID_SOFTWARE             = "ID_SOFTWARE";
    public static final String  ID_TECNICO             = "ID_TECNICO";
    
    
    
    
    public CondominiDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public CondominiDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public CondominiDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_AMMINISTRATORE, Integer.class);
        addNoStringField(SCARICO, Integer.class);
        addNoStringField(GG_SEGNALAZIONE, Integer.class);
        
        addNoStringField(PERDITE_IMPIANTO, BigDecimal.class);
        addNoStringField(FABBISOGNO_ANNUO_CLIMA, BigDecimal.class);
        addNoStringField(FABBISOGNO_ANNUO_ACS, BigDecimal.class);
        addNoStringField(RENDIMENTO_CALDAIA, BigDecimal.class);
        
        addNoStringField(GEN, Boolean.class);
        addNoStringField(FEB, Boolean.class);
        addNoStringField(MAR, Boolean.class);
        addNoStringField(APR, Boolean.class);
        addNoStringField(MAG, Boolean.class);
        addNoStringField(GIU, Boolean.class);
        addNoStringField(LUG, Boolean.class);
        addNoStringField(AGO, Boolean.class);
        addNoStringField(SETT, Boolean.class);
        addNoStringField(OTT, Boolean.class);
        addNoStringField(NOV, Boolean.class);
        addNoStringField(DIC, Boolean.class);
        
        addNoStringField(ID_SOFTWARE, Integer.class);
        addNoStringField(ID_TECNICO, Integer.class);
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_MODULO))) {
            appendField(ID_MODULO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(NR_MODULO))) {
            appendField(NR_MODULO, whereCondition);
        }
        
        return whereCondition;
    }
    
    @Override
    public void delete() throws AppCrash {

        String idModulo = getAttributeAsString(ID_MODULO);
        String queryDelete = "delete from utenti where id_utente in (select id_utente from utenti_utenze where id_utenza in (select id_modulo from utenze_dettaglio where id_modulo in (select id_modulo from utenze where id_condominio="+idModulo+")))";
        String queryDelete1 = "delete from utenze_dettaglio where id_modulo in (select id_modulo from utenze where id_condominio="+idModulo+")";
        String queryDelete2 = "delete from utenze where id_condominio="+idModulo;
        String queryDelete3 = "delete from network where id_condominio="+idModulo;
        String queryDelete4 = "delete from contatori where id_condominio="+idModulo;
        String queryDelete5 = "delete from scarico where id_condominio="+idModulo;
        String queryDelete6 = "delete from dati_rilevatori where id_condominio="+idModulo;
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete);
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete1);
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete2);
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete3);
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete4);
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete5);
        net.projectsrl.wm.utils.WMUtils.executeQuery(queryDelete6);
        
        super.delete();

    }
    
    @Override
    public void insert() throws AppCrash {
    	super.insert();
    }

}
