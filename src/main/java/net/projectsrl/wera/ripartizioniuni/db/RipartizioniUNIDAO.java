
package net.projectsrl.wera.ripartizioniuni.db;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class RipartizioniUNIDAO extends AliModDAO_base {
	private static final String TABLE_NAME = "RIPARTIZIONI_UNI";

	public static final String DATA_DAL = "DATA_DAL";
	public static final String DATA_AL = "DATA_AL";

	public static final String ID_CONDOMINIO = "ID_CONDOMINIO";
	public static final String CONFERMATA = "CONFERMATA";
	public static final String INVIATA = "INVIATA";
	public static final String CONTROLLO_OK = "CONTROLLO_OK";

	public static final String CC_AC_QTA = "CC_AC_QTA";
	public static final String CC_FM_QTA = "CC_FM_QTA";
	public static final String CC_SI_QTA = "CC_SI_QTA";
	public static final String CC_AC_LORDO = "CC_AC_LORDO";
	public static final String CC_FM_LORDO = "CC_FM_LORDO";
	public static final String CC_SI_LORDO = "CC_SI_LORDO";
	public static final String CC_AC_TOTALE = "CC_AC_TOTALE";
	public static final String CC_FM_TOTALE = "CC_FM_TOTALE";
	public static final String CC_SI_TOTALE = "CC_SI_TOTALE";
	public static final String CC_TC_TOTALE = "CC_TC_TOTALE";
	public static final String CMI_CIS_QTA = "CMI_CIS_QTA";
	public static final String CMI_CCT_QTA = "CMI_CCT_QTA";
	public static final String CMI_CL_QTA = "CMI_CL_QTA";
	public static final String CMI_SI_QTA = "CMI_SI_QTA";
	public static final String CMI_CIS_LORDO = "CMI_CIS_LORDO";
	public static final String CMI_CCT_LORDO = "CMI_CCT_LORDO";
	public static final String CMI_CL_LORDO = "CMI_CL_LORDO";
	public static final String CMI_SI_LORDO = "CMI_SI_LORDO";
	public static final String CMI_CIS_TOTALE = "CMI_CIS_TOTALE";
	public static final String CMI_CCT_TOTALE = "CMI_CCT_TOTALE";
	public static final String CMI_CL_TOTALE = "CMI_CL_TOTALE";
	public static final String CMI_SI_TOTALE = "CMI_SI_TOTALE";
	public static final String TC_CC_RIP = "TC_CC_RIP";
	public static final String TC_CCT_RIP = "TC_CCT_RIP";
	public static final String TC_CFM_RIP = "TC_CFM_RIP";
	public static final String TC_CL_RIP = "TC_CL_RIP";
	public static final String TC_AF_RIP = "TC_AF_RIP";
	public static final String TC_CC_TOTALE = "TC_CC_TOTALE";
	public static final String TC_CCT_TOTALE = "TC_CCT_TOTALE";
	public static final String TC_CFM_TOTALE = "TC_CFM_TOTALE";
	public static final String TC_CL_TOTALE = "TC_CL_TOTALE";
	public static final String TC_TC_TOTALE = "TC_TC_TOTALE";
	public static final String TC_AF_TOTALE = "TC_AF_TOTALE";
	public static final String CR_PF_TOTALE = "CR_PF_TOTALE";
	public static final String CR_PR_TOTALE = "CR_PR_TOTALE";
	public static final String CR_TC_TOTALE = "CR_TC_TOTALE";
	public static final String TC_AC_RIP = "TC_AC_RIP";
	public static final String TC_AC_TOTALE = "TC_AC_TOTALE";
	public static final String TC_CIS_TOTALE = "TC_CIS_TOTALE";
	public static final String TIPO_RIPARTIZIONE = "TIPO_RIPARTIZIONE";
	
	

	public static final String SPESA_TOTALE_IMPIANTO = "SPESA_TOTALE_IMPIANTO";
	public static final String SPESA_TOTALE_GESTIONE = "SPESA_TOTALE_GESTIONE";
	public static final String PERDITE_IMPIANTO = "PERDITE_IMPIANTO";
	public static final String FABBISOGNO_ANNUO_CLIMA = "FABBISOGNO_ANNUO_CLIMA";
	public static final String FABBISOGNO_ANNUO_ACS = "FABBISOGNO_ANNUO_ACS";
	public static final String V1_COMBUSTIBILE_LETTURA_INIZIALE = "V1_COMBUSTIBILE_LETTURA_INIZIALE";
	public static final String V1_COMBUSTIBILE_LETTURA_FINALE = "V1_COMBUSTIBILE_LETTURA_FINALE";
	public static final String V1_COMBUSTIBILE_CONSUMO = "V1_COMBUSTIBILE_CONSUMO";
	public static final String V1_COMBUSTIBILE_FABBISOGNO_CLIMA = "V1_COMBUSTIBILE_FABBISOGNO_CLIMA";
	public static final String V1_COMBUSTIBILE_FABBISOGNO_ACS = "V1_COMBUSTIBILE_FABBISOGNO_ACS";
	public static final String V1_COMBUSTIBILE_COSTO_UNITARIO = "V1_COMBUSTIBILE_COSTO_UNITARIO";
	public static final String V2_ENERGIA_ELETTRICA_LETTURA_INIZIALE = "V2_ENERGIA_ELETTRICA_LETTURA_INIZIALE";
	public static final String V2_ENERGIA_ELETTRICA_LETTURA_FINALE = "V2_ENERGIA_ELETTRICA_LETTURA_FINALE";
	public static final String V2_ENERGIA_ELETTRICA_CONSUMO = "V2_ENERGIA_ELETTRICA_CONSUMO";
	public static final String V2_ENERGIA_ELETTRICA_FABBISOGNO_CLIMA = "V2_ENERGIA_ELETTRICA_FABBISOGNO_CLIMA";
	public static final String V2_ENERGIA_ELETTRICA_FABBISOGNO_ACS = "V2_ENERGIA_ELETTRICA_FABBISOGNO_ACS";
	public static final String V2_ENERGIA_ELETTRICA_COSTO_UNITARIO = "V2_ENERGIA_ELETTRICA_COSTO_UNITARIO";
	public static final String GENERATORE_LETTURA_INIZIALE = "GENERATORE_LETTURA_INIZIALE";
	public static final String GENERATORE_LETTURA_FINALE = "GENERATORE_LETTURA_FINALE";
	public static final String GENERATORE_CONSUMO = "GENERATORE_CONSUMO";
	public static final String GENERATORE_CONSUMO_ACS = "GENERATORE_CONSUMO_ACS";
	public static final String GENERATORE_FABBISOGNO_CLIMA = "GENERATORE_FABBISOGNO_CLIMA";
	public static final String GENERATORE_FABBISOGNO_ACS = "GENERATORE_FABBISOGNO_ACS";
	public static final String V1_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO = "V1_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO";
	public static final String V1_COEFFICIENTE_RIPARTIZIONE_ACS = "V1_COEFFICIENTE_RIPARTIZIONE_ACS";
	public static final String V1_CONSUMO_RISCALDAMENTO = "V1_CONSUMO_RISCALDAMENTO";
	public static final String V1_CONSUMO_ACS = "V1_CONSUMO_ACS";
	public static final String V2_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO = "V2_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO";
	public static final String V2_COEFFICIENTE_RIPARTIZIONE_ACS = "V2_COEFFICIENTE_RIPARTIZIONE_ACS";
	public static final String V2_CONSUMO_RISCALDAMENTO = "V2_CONSUMO_RISCALDAMENTO";
	public static final String V2_CONSUMO_ACS = "V2_CONSUMO_ACS";
	public static final String ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO = "ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO";
	public static final String ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_ACS = "ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_ACS";
	public static final String ENERGIA_TERMICA_CONSUMO_RISCALDAMENTO = "ENERGIA_TERMICA_CONSUMO_RISCALDAMENTO";
	public static final String ENERGIA_TERMICA_CONSUMO_ACS = "ENERGIA_TERMICA_CONSUMO_ACS";
	public static final String CONSUMO_TOTALE_RISCALDAMENTO = "CONSUMO_TOTALE_RISCALDAMENTO";
	public static final String CONSUMO_TOTALE_ACS = "CONSUMO_TOTALE_ACS";
	public static final String CONSUMO_TOTALE_RISCALDAMENTO_E_ACS = "CONSUMO_TOTALE_RISCALDAMENTO_E_ACS";
	public static final String SPESA_RISCALDAMENTO = "SPESA_RISCALDAMENTO";
	public static final String SPESA_ACS = "SPESA_ACS";
	public static final String COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO = "COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO";
	public static final String COEFFICIENTE_RIPARTIZIONE_ACS = "COEFFICIENTE_RIPARTIZIONE_ACS";
	public static final String SPESA_CONDUZIONE_RISCALDAMENTO = "SPESA_CONDUZIONE_RISCALDAMENTO";
	public static final String SPESA_CONDUZIONE_ACS = "SPESA_CONDUZIONE_ACS";
	public static final String SPESA_GESTIONE_RISCALDAMENTO = "SPESA_GESTIONE_RISCALDAMENTO";
	public static final String SPESA_GESTIONE_ACS = "SPESA_GESTIONE_ACS";
	public static final String SPESA_TOTALE_RISCALDAMENTO = "SPESA_TOTALE_RISCALDAMENTO";
	public static final String SPESA_TOTALE_ACS = "SPESA_TOTALE_ACS";
	public static final String SPESA_TOTALE_RISCALDAMENTO_ACS = "SPESA_TOTALE_RISCALDAMENTO_ACS";
	public static final String COSTO_UNITARIO_ENERGIA_TERMICA_RISCALDAMENTO = "COSTO_UNITARIO_ENERGIA_TERMICA_RISCALDAMENTO";
	public static final String COSTO_UNITARIO_ENERGIA_TERMICA_ACS = "COSTO_UNITARIO_ENERGIA_TERMICA_ACS";
	public static final String CONSUMO_INVOLONTARIO = "CONSUMO_INVOLONTARIO";
	public static final String TOTALE_ENERGIA_TERMICA_RISCALDAMENTO = "TOTALE_ENERGIA_TERMICA_RISCALDAMENTO";
	public static final String TOTALE_ENERGIA_TERMICA_ACS = "TOTALE_ENERGIA_TERMICA_ACS";
	public static final String TOTALE_POTENZA_TERMICA_RISCALDAMENTO = "TOTALE_POTENZA_TERMICA_RISCALDAMENTO";
	public static final String TOTALE_POTENZA_TERMICA_ACS = "TOTALE_POTENZA_TERMICA_ACS";
	public static final String TOTALE_RISCALDAMENTO = "TOTALE_RISCALDAMENTO";
	public static final String TOTALE_ACS = "TOTALE_ACS";
	public static final String TOTALE_APPARTAMENTO = "TOTALE_APPARTAMENTO";
	public static final String LETTURA = "LETTURA";
	public static final String LETTURA_ACS = "LETTURA_ACS";
	public static final String TIPOLOGIA_RIPARTIZIONE = "TIPOLOGIA_RIPARTIZIONE";
	public static final String TOTALE_MILLESIMI_RISCALDAMENTO        = "TOTALE_MILLESIMI_RISCALDAMENTO";
    public static final String TOTALE_MILLESIMI_ACS        = "TOTALE_MILLESIMI_ACS";
    public static final String RENDIMENTO_CALDAIA        = "RENDIMENTO_CALDAIA";
    public static final String TIPO_CONTABILIZZAZIONE = "TIPO_CONTABILIZZAZIONE";
    public static final String SALVATAGGIO_IN_CORSO = "SALVATAGGIO_IN_CORSO";

	public static final String TS_INS = "TS_INS";
	public static final String ID_UTENTE_INS = "ID_UTENTE_INS";
	public static final String TS_MOD = "TS_MOD";
	public static final String ID_UTENTE_MOD = "ID_UTENTE_MOD";

	public RipartizioniUNIDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public RipartizioniUNIDAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public RipartizioniUNIDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
	}

	@Override
	protected void init() throws AppCrash {

		super.init();

		addNoStringField(ID_MODULO, Integer.class);
		addNoStringField(ID_CONDOMINIO, Integer.class);

		addNoStringField(CONFERMATA, Boolean.class);
		addNoStringField(INVIATA, Boolean.class);
		addNoStringField(CONTROLLO_OK, Boolean.class);
		addNoStringField(CC_AC_QTA, BigDecimal.class);
		addNoStringField(CC_FM_QTA, BigDecimal.class);
		addNoStringField(CC_SI_QTA, BigDecimal.class);
		addNoStringField(CC_AC_LORDO, BigDecimal.class);
		addNoStringField(CC_FM_LORDO, BigDecimal.class);
		addNoStringField(CC_SI_LORDO, BigDecimal.class);
		addNoStringField(CC_AC_TOTALE, BigDecimal.class);
		addNoStringField(CC_FM_TOTALE, BigDecimal.class);
		addNoStringField(CC_SI_TOTALE, BigDecimal.class);
		addNoStringField(CC_TC_TOTALE, BigDecimal.class);
		addNoStringField(CMI_CIS_QTA, Integer.class);
		addNoStringField(CMI_CCT_QTA, Integer.class);
		addNoStringField(CMI_CL_QTA, Integer.class);
		addNoStringField(CMI_SI_QTA, Integer.class);
		addNoStringField(CMI_CIS_LORDO, BigDecimal.class);
		addNoStringField(CMI_CCT_LORDO, BigDecimal.class);
		addNoStringField(CMI_CL_LORDO, BigDecimal.class);
		addNoStringField(CMI_SI_LORDO, BigDecimal.class);
		addNoStringField(CMI_CIS_TOTALE, BigDecimal.class);
		addNoStringField(CMI_CCT_TOTALE, BigDecimal.class);
		addNoStringField(CMI_CL_TOTALE, BigDecimal.class);
		addNoStringField(CMI_SI_TOTALE, BigDecimal.class);
		addNoStringField(TC_CC_RIP, Integer.class);
		addNoStringField(TC_CCT_RIP, Integer.class);
		addNoStringField(TC_CFM_RIP, Integer.class);
		addNoStringField(TC_CL_RIP, Integer.class);
		addNoStringField(TC_AF_RIP, Integer.class);
		addNoStringField(TC_CC_TOTALE, BigDecimal.class);
		addNoStringField(TC_CCT_TOTALE, BigDecimal.class);
		addNoStringField(TC_CFM_TOTALE, BigDecimal.class);
		addNoStringField(TC_CL_TOTALE, BigDecimal.class);
		addNoStringField(TC_TC_TOTALE, BigDecimal.class);
		addNoStringField(TC_AF_TOTALE, BigDecimal.class);
		addNoStringField(CR_PF_TOTALE, BigDecimal.class);
		addNoStringField(CR_PR_TOTALE, BigDecimal.class);
		addNoStringField(CR_TC_TOTALE, BigDecimal.class);
		addNoStringField(TC_AC_RIP, Integer.class);
		addNoStringField(TC_AC_TOTALE, BigDecimal.class);
		addNoStringField(TC_CIS_TOTALE, BigDecimal.class);
		
		addNoStringField(SPESA_TOTALE_IMPIANTO, BigDecimal.class);
		addNoStringField(SPESA_TOTALE_GESTIONE, BigDecimal.class);
		addNoStringField(PERDITE_IMPIANTO, BigDecimal.class);
		addNoStringField(FABBISOGNO_ANNUO_CLIMA, BigDecimal.class);
		addNoStringField(FABBISOGNO_ANNUO_ACS, BigDecimal.class);
		addNoStringField(V1_COMBUSTIBILE_LETTURA_INIZIALE, BigDecimal.class);
		addNoStringField(V1_COMBUSTIBILE_LETTURA_FINALE, BigDecimal.class);
		addNoStringField(V1_COMBUSTIBILE_CONSUMO, BigDecimal.class);
		addNoStringField(V1_COMBUSTIBILE_FABBISOGNO_CLIMA, BigDecimal.class);
		addNoStringField(V1_COMBUSTIBILE_FABBISOGNO_ACS, BigDecimal.class);
		addNoStringField(V1_COMBUSTIBILE_COSTO_UNITARIO, BigDecimal.class);
		addNoStringField(V2_ENERGIA_ELETTRICA_LETTURA_INIZIALE, BigDecimal.class);
		addNoStringField(V2_ENERGIA_ELETTRICA_LETTURA_FINALE, BigDecimal.class);
		addNoStringField(V2_ENERGIA_ELETTRICA_CONSUMO, BigDecimal.class);
		addNoStringField(V2_ENERGIA_ELETTRICA_FABBISOGNO_CLIMA, BigDecimal.class);
		addNoStringField(V2_ENERGIA_ELETTRICA_FABBISOGNO_ACS, BigDecimal.class);
		addNoStringField(V2_ENERGIA_ELETTRICA_COSTO_UNITARIO, BigDecimal.class);
		addNoStringField(GENERATORE_LETTURA_INIZIALE, BigDecimal.class);
		addNoStringField(GENERATORE_LETTURA_FINALE, BigDecimal.class);
		addNoStringField(GENERATORE_CONSUMO, BigDecimal.class);
		addNoStringField(GENERATORE_CONSUMO_ACS, BigDecimal.class);
		addNoStringField(GENERATORE_FABBISOGNO_CLIMA, BigDecimal.class);
		addNoStringField(GENERATORE_FABBISOGNO_ACS, BigDecimal.class);
		addNoStringField(V1_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(V1_COEFFICIENTE_RIPARTIZIONE_ACS, BigDecimal.class);
		addNoStringField(V1_CONSUMO_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(V1_CONSUMO_ACS, BigDecimal.class);
		addNoStringField(V2_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(V2_COEFFICIENTE_RIPARTIZIONE_ACS, BigDecimal.class);
		addNoStringField(V2_CONSUMO_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(V2_CONSUMO_ACS, BigDecimal.class);
		addNoStringField(ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(ENERGIA_TERMICA_COEFFICIENTE_RIPARTIZIONE_ACS, BigDecimal.class);
		addNoStringField(ENERGIA_TERMICA_CONSUMO_RISCALDAMENTO, Integer.class);
		addNoStringField(ENERGIA_TERMICA_CONSUMO_ACS, Integer.class);
		addNoStringField(CONSUMO_TOTALE_RISCALDAMENTO, Integer.class);
		addNoStringField(CONSUMO_TOTALE_ACS, Integer.class);
		addNoStringField(CONSUMO_TOTALE_RISCALDAMENTO_E_ACS, Integer.class);
		addNoStringField(SPESA_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(SPESA_ACS, BigDecimal.class);
		addNoStringField(COEFFICIENTE_RIPARTIZIONE_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(COEFFICIENTE_RIPARTIZIONE_ACS, BigDecimal.class);
		addNoStringField(SPESA_CONDUZIONE_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(SPESA_CONDUZIONE_ACS, BigDecimal.class);
		addNoStringField(SPESA_GESTIONE_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(SPESA_GESTIONE_ACS, BigDecimal.class);
		addNoStringField(SPESA_TOTALE_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(SPESA_TOTALE_ACS, BigDecimal.class);
		addNoStringField(SPESA_TOTALE_RISCALDAMENTO_ACS, BigDecimal.class);
		addNoStringField(COSTO_UNITARIO_ENERGIA_TERMICA_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(COSTO_UNITARIO_ENERGIA_TERMICA_ACS, BigDecimal.class);
		addNoStringField(CONSUMO_INVOLONTARIO, BigDecimal.class);
		addNoStringField(TOTALE_ENERGIA_TERMICA_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(TOTALE_ENERGIA_TERMICA_ACS, BigDecimal.class);
		addNoStringField(TOTALE_POTENZA_TERMICA_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(TOTALE_POTENZA_TERMICA_ACS, BigDecimal.class);
		addNoStringField(TOTALE_RISCALDAMENTO, BigDecimal.class);
		addNoStringField(TOTALE_ACS, BigDecimal.class);
		addNoStringField(TOTALE_APPARTAMENTO, BigDecimal.class);
		addNoStringField(LETTURA, Integer.class);
		addNoStringField(LETTURA_ACS, BigDecimal.class);
        addNoStringField(TOTALE_MILLESIMI_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(TOTALE_MILLESIMI_ACS, BigDecimal.class);
        addNoStringField(RENDIMENTO_CALDAIA, BigDecimal.class);
        addNoStringField(SALVATAGGIO_IN_CORSO,Boolean.class);
		
		addNoStringField(TS_INS, Timestamp.class);
		addNoStringField(ID_UTENTE_INS, Integer.class);
		addNoStringField(TS_MOD, Timestamp.class);
		addNoStringField(ID_UTENTE_MOD, Integer.class);

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
		 super.delete();
	}

}
