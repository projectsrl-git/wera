
package net.projectsrl.wera.ripartizioni.db;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class RipartizioniDAO extends AliModDAO_base {
	private static final String TABLE_NAME          = "RIPARTIZIONI";
	
	public static final String  ID_CONDOMINIO = "ID_CONDOMINIO";
    public static final String  DATA_DAL   = "DATA_DAL";
    public static final String  DATA_AL = "DATA_AL";

    public static final String  CONFERMATA           = "CONFERMATA";
    public static final String  INVIATA           = "INVIATA";
    public static final String TIPO_RIPARTIZIONE = "TIPO_RIPARTIZIONE";
    public static final String  CC_AC_QTA              = "CC_AC_QTA";
    public static final String  CC_FM_QTA           = "CC_FM_QTA";
    public static final String  CC_SI_QTA             = "CC_SI_QTA";
    public static final String  CC_AC_LORDO        = "CC_AC_LORDO";
    public static final String  CC_FM_LORDO        = "CC_FM_LORDO";
    public static final String  CC_SI_LORDO        = "CC_SI_LORDO";
    public static final String  CC_AC_TOTALE        = "CC_AC_TOTALE";
    public static final String  CC_FM_TOTALE        = "CC_FM_TOTALE";
    public static final String  CC_SI_TOTALE        = "CC_SI_TOTALE";
    public static final String  CC_TC_TOTALE        = "CC_TC_TOTALE";
    public static final String  CMI_CIS_QTA        = "CMI_CIS_QTA";
    public static final String  CMI_CCT_QTA        = "CMI_CCT_QTA";
    public static final String  CMI_CL_QTA        = "CMI_CL_QTA";
    public static final String  CMI_CIS_UM        = "CMI_CIS_UM";
    public static final String  CMI_CCT_UM        = "CMI_CCT_UM";
    public static final String  CMI_CL_UM        = "CMI_CL_UM";
    public static final String  CMI_CIS_LORDO        = "CMI_CIS_LORDO";
    public static final String  CMI_CCT_LORDO        = "CMI_CCT_LORDO";
    public static final String  CMI_CL_LORDO        = "CMI_CL_LORDO";
    public static final String  CMI_CIS_TOTALE        = "CMI_CIS_TOTALE";
    public static final String  CMI_CCT_TOTALE         = "CMI_CCT_TOTALE";
    public static final String  CMI_CL_TOTALE        = "CMI_CL_TOTALE";
    public static final String  CMI_CT_TOTALE        = "CMI_CT_TOTALE";
    public static final String  TC_CC_RIP        = "TC_CC_RIP";
    public static final String  TC_CCT_RIP        = "TC_CCT_RIP";
    public static final String  TC_CFM_RIP        = "TC_CFM_RIP";
    public static final String  TC_CL_RIP        = "TC_CL_RIP";
    public static final String  TC_CC_NOTE        = "TC_CC_NOTE";
    public static final String  TC_CCT_NOTE        = "TC_CCT_NOTE";
    public static final String  TC_CFM_NOTE        = "TC_CFM_NOTE";
    public static final String  TC_CL_NOTE        = "TC_CL_NOTE";
    public static final String  TC_TC_NOTE        = "TC_TC_NOTE";
    public static final String  TC_CC_TOTALE        = "TC_CC_TOTALE";
    public static final String  TC_CCT_TOTALE        = "TC_CCT_TOTALE";
    public static final String  TC_CFM_TOTALE        = "TC_CFM_TOTALE";
    public static final String  TC_CL_TOTALE        = "TC_CL_TOTALE";
    public static final String  TC_TC_TOTALE        = "TC_TC_TOTALE";
    public static final String  CR_PF_RIP        = "CR_PF_RIP";
    public static final String  CR_PR_RIP        = "CR_PR_RIP";
    public static final String  CR_TC_RIP        = "CR_TC_RIP";
    public static final String  CR_PF_NOTE        = "CR_PF_NOTE";
    public static final String  CR_PR_NOTE        = "CR_PR_NOTE";
    public static final String  CR_TC_NOTE        = "CR_TC_NOTE";
    public static final String  CR_PF_TOTALE        = "CR_PF_TOTALE";
    public static final String  CR_PR_TOTALE        = "CR_PR_TOTALE";
    public static final String  CR_TC_TOTALE        = "CR_TC_TOTALE";
    public static final String  PU_CPF        = "PU_CPF";
    public static final String  PU_CR        = "PU_CR";
    public static final String  PU_CPF_TOTALE        = "PU_CPF_TOTALE";
    public static final String  PU_CR_TOTALE        = "PU_CR_TOTALE";
    public static final String  TC_AC_RIP        = "TC_AC_RIP";
    public static final String  TC_AC_NOTE        = "TC_AC_NOTE";
    public static final String  TC_AC_TOTALE        = "TC_AC_TOTALE";
    
    public static final String  CC_AC_MILL        = "CC_AC_MILL";
    public static final String  CC_AC_CONS        = "CC_AC_CONS";
    public static final String  CMI_CCT_ACCONTO        = "CMI_CCT_ACCONTO";
    public static final String  CMI_CCT_SALDO        = "CMI_CCT_SALDO";
    public static final String SALVATAGGIO_IN_CORSO = "SALVATAGGIO_IN_CORSO";
    
    public static final String  TS_INS        = "TS_INS";
    public static final String  ID_UTENTE_INS        = "ID_UTENTE_INS";
    public static final String  TS_MOD        = "TS_MOD";
    public static final String  ID_UTENTE_MOD        = "ID_UTENTE_MOD";
 
    
    public RipartizioniDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public RipartizioniDAO(DBTransaction transact) throws AppCrash {

        super(transact, TABLE_NAME);
    }

    public RipartizioniDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_MODULO, Integer.class);
        addNoStringField(ID_CONDOMINIO, Integer.class);

        addNoStringField(CONFERMATA, Boolean.class);
        addNoStringField(INVIATA, Boolean.class);
        addNoStringField(CC_AC_QTA, BigDecimal.class);
        addNoStringField(CC_FM_QTA, BigDecimal.class);
        addNoStringField(TC_CL_TOTALE, BigDecimal.class);
        addNoStringField(TC_CC_TOTALE, BigDecimal.class);
        addNoStringField(TC_CCT_TOTALE, BigDecimal.class);
        addNoStringField(CC_SI_QTA, BigDecimal.class);
        addNoStringField(CC_AC_LORDO, BigDecimal.class);
        addNoStringField(CC_FM_LORDO, BigDecimal.class);
        addNoStringField(CC_SI_LORDO, BigDecimal.class);
        addNoStringField(CC_AC_TOTALE, BigDecimal.class);
        addNoStringField(CC_FM_TOTALE, BigDecimal.class);
        addNoStringField(CC_SI_TOTALE, BigDecimal.class);
        addNoStringField(CC_TC_TOTALE, BigDecimal.class);
        addNoStringField(CMI_CIS_QTA, BigDecimal.class);
        addNoStringField(CMI_CCT_QTA, BigDecimal.class);
        addNoStringField(CMI_CL_QTA, BigDecimal.class);
        addNoStringField(CMI_CIS_LORDO, BigDecimal.class);
        addNoStringField(TC_CCT_RIP, BigDecimal.class);
        addNoStringField(CR_PF_RIP, BigDecimal.class);
        addNoStringField(TC_CCT_RIP, BigDecimal.class);
        addNoStringField(CMI_CCT_LORDO, BigDecimal.class);
        addNoStringField(CMI_CL_LORDO, BigDecimal.class);
        addNoStringField(CMI_CIS_TOTALE, BigDecimal.class);
        addNoStringField(CMI_CCT_TOTALE, BigDecimal.class);
        addNoStringField(CMI_CL_TOTALE, BigDecimal.class);
        addNoStringField(CMI_CT_TOTALE, BigDecimal.class);
        addNoStringField(TC_TC_TOTALE, BigDecimal.class);
        addNoStringField(TC_CFM_TOTALE, BigDecimal.class);
        addNoStringField(TC_CC_RIP, BigDecimal.class);
        addNoStringField(TC_CFM_RIP, BigDecimal.class);
        addNoStringField(TC_CL_RIP, BigDecimal.class);
        addNoStringField(CR_PF_TOTALE, BigDecimal.class);
        addNoStringField(CR_PR_TOTALE, BigDecimal.class);
        addNoStringField(CR_TC_TOTALE, BigDecimal.class);
        addNoStringField(PU_CPF, BigDecimal.class);
        addNoStringField(PU_CR, BigDecimal.class);
        addNoStringField(PU_CPF_TOTALE, BigDecimal.class);
        addNoStringField(PU_CR_TOTALE, BigDecimal.class);
        addNoStringField(TC_AC_RIP, BigDecimal.class);
        addNoStringField(TC_AC_TOTALE, BigDecimal.class);
        addNoStringField(TS_INS, Timestamp.class);
        addNoStringField(ID_UTENTE_INS, Integer.class);
        addNoStringField(TS_MOD, Timestamp.class);
        addNoStringField(ID_UTENTE_MOD, Integer.class);
        addNoStringField(CR_PR_RIP, BigDecimal.class);
        addNoStringField(CR_TC_RIP, Integer.class);
        
        addNoStringField(CC_AC_MILL, BigDecimal.class);
        addNoStringField(CC_AC_CONS, BigDecimal.class);
        addNoStringField(CMI_CCT_ACCONTO, BigDecimal.class);
        addNoStringField(CMI_CCT_SALDO, BigDecimal.class);
        
        addNoStringField(SALVATAGGIO_IN_CORSO,Boolean.class);
        
    }

    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_MODULO))) {
            appendField(ID_MODULO, whereCondition);
        }else if (Util.IsNotEmpty(getAttribute(NR_MODULO))) {
            appendField(NR_MODULO, whereCondition);
        }
        
        return whereCondition;
    }
    
    @Override
    public void delete() throws AppCrash {
		 super.delete();
	}

}
