package net.projectsrl.wera.ripartizioniuni2018.db;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDAO_base;

public class RipartizioniUNI2018DAO extends AliModDAO_base {
	private static final String TABLE_NAME = "RIPARTIZIONI_UNI_2018";

	public static final String DATA_DAL = "DATA_DAL";
	public static final String DATA_AL = "DATA_AL";

	public static final String ID_CONDOMINIO = "ID_CONDOMINIO";
	public static final String CONFERMATA = "CONFERMATA";
	public static final String INVIATA = "INVIATA";
	public static final String CONTROLLO_OK = "CONTROLLO_OK";
	
	public static final String TIPOLOGIA_RIPARTIZIONE = "TIPOLOGIA_RIPARTIZIONE";
	public static final String TOTALE_MILLESIMI_RISCALDAMENTO        = "TOTALE_MILLESIMI_RISCALDAMENTO";
    public static final String TOTALE_MILLESIMI_ACS        = "TOTALE_MILLESIMI_ACS";
    public static final String RENDIMENTO_CALDAIA        = "RENDIMENTO_CALDAIA";
    public static final String TIPO_CONTABILIZZAZIONE = "TIPO_CONTABILIZZAZIONE";
    public static final String TIPO_RIPARTIZIONE = "TIPO_RIPARTIZIONE";
    
    public static final String FABBISOGNO_ANNUO_CLIMA = "FABBISOGNO_ANNUO_CLIMA";
    public static final String FABBISOGNO_ANNUO_ACS = "FABBISOGNO_ANNUO_ACS";
    public static final String PERDITE_IMPIANTO = "PERDITE_IMPIANTO";
    
	public static final String TIPO_RIPARTIZIONE_PC = "TIPO_RIPARTIZIONE_PC";

	public static final String SG_SCM_TOTALE_PREV="SG_SCM_TOTALE_PREV";
	public static final String SG_SCR_QTA_PREV = "SG_SCR_QTA_PREV";
	public static final String SG_SCR_IMPORTO_PREV = "SG_SCR_IMPORTO_PREV";
	public static final String SG_SCR_TOTALE_PREV = "SG_SCR_TOTALE_PREV";
	public static final String SG_SG_TOTALE_PREV = "SG_SG_TOTALE_PREV";
	public static final String SG_SCM_TOTALE_CONS="SG_SCM_TOTALE_CONS";
	public static final String SG_SCR_QTA_CONS = "SG_SCR_QTA_CONS";
	public static final String SG_SCR_IMPORTO_CONS = "SG_SCR_IMPORTO_CONS";
	public static final String SG_SCR_TOTALE_CONS = "SG_SCR_TOTALE_CONS";
	public static final String SG_SG_TOTALE_CONS = "SG_SG_TOTALE_CONS";
	
	public static final String VET_COMB_UM = "VET_COMB_UM";
	public static final String VET_COMB_Q_VE_H_PREV = "VET_COMB_Q_VE_H_PREV";
	public static final String VET_COMB_Q_VE_W_PREV = "VET_COMB_Q_VE_W_PREV";
	public static final String VET_COMB_C_VE_PREV = "VET_COMB_C_VE_PREV";
	public static final String VET_COMB_Q_VE_H_CONS = "VET_COMB_Q_VE_H_CONS";
	public static final String VET_COMB_Q_VE_W_CONS = "VET_COMB_Q_VE_W_CONS";
	public static final String VET_COMB_C_VE_CONS = "VET_COMB_C_VE_CONS";
	public static final String VET_ELETTR_UM = "VET_ELETTR_UM";
	public static final String VET_ELETTR_Q_VE_H_PREV = "VET_ELETTR_Q_VE_H_PREV";
	public static final String VET_ELETTR_Q_VE_W_PREV = "VET_ELETTR_Q_VE_W_PREV";
	public static final String VET_ELETTR_C_VE_PREV = "VET_ELETTR_C_VE_PREV";
	public static final String VET_ELETTR_Q_VE_H_CONS = "VET_ELETTR_Q_VE_H_CONS";
	public static final String VET_ELETTR_Q_VE_W_CONS = "VET_ELETTR_Q_VE_W_CONS";
	public static final String VET_ELETTR_C_VE_CONS = "VET_ELETTR_C_VE_CONS";
	
	public static final String ST_RISC_SE_PREV = "ST_RISC_SE_PREV";
	public static final String ST_RISC_SG_PREV = "ST_RISC_SG_PREV";
	public static final String ST_RISC_ST_PREV = "ST_RISC_ST_PREV";
	public static final String ST_RISC_SE_CONS = "ST_RISC_SE_CONS";
	public static final String ST_RISC_SG_CONS = "ST_RISC_SG_CONS";
	public static final String ST_RISC_ST_CONS = "ST_RISC_ST_CONS";
	public static final String ST_ACS_SE_PREV = "ST_ACS_SE_PREV";
	public static final String ST_ACS_SG_PREV = "ST_ACS_SG_PREV";
	public static final String ST_ACS_ST_PREV = "ST_ACS_ST_PREV";
	public static final String ST_ACS_SE_CONS = "ST_ACS_SE_CONS";
	public static final String ST_ACS_SG_CONS = "ST_ACS_SG_CONS";
	public static final String ST_ACS_ST_CONS = "ST_ACS_ST_CONS";
	
	public static final String ST_SE_TOTALE_PREV = "ST_SE_TOTALE_PREV";
	public static final String ST_SG_TOTALE_PREV = "ST_SG_TOTALE_PREV";
	public static final String ST_ST_TOTALE_PREV = "ST_ST_TOTALE_PREV";
	public static final String ST_SE_TOTALE_CONS = "ST_SE_TOTALE_CONS";
	public static final String ST_SG_TOTALE_CONS = "ST_SG_TOTALE_CONS";
	public static final String ST_ST_TOTALE_CONS = "ST_ST_TOTALE_CONS";
	
	
	
	public static final String GEN_TIPO = "GEN_TIPO";
	public static final String GEN_QGN_H_PREV = "GEN_QGN_H_PREV";
	public static final String GEN_QGN_W_PREV = "GEN_QGN_W_PREV";
	public static final String GEN_QGN_H_CONS = "GEN_QGN_H_CONS";
	public static final String GEN_QGN_W_CONS = "GEN_QGN_W_CONS";

	public static final String COMMITTENTE_COGNOME = "COMMITTENTE_COGNOME";
	public static final String COMMITTENTE_NOME = "COMMITTENTE_NOME";
	public static final String ID_TECNICO = "ID_TECNICO";
	public static final String ID_AMMINISTRATORE = "ID_AMMINISTRATORE";
	public static final String RESPONSABILE_IMPIANTO = "RESPONSABILE_IMPIANTO";
	public static final String ID_SOFTWARE_CALCOLO = "ID_SOFTWARE_CALCOLO";
	
	//OUTPUT
	public static final String CONSUMI_RISC_Q_VOL = "CONSUMI_RISC_Q_VOL";
	public static final String CONSUMI_RISC_Q_INV = "CONSUMI_RISC_Q_INV";
	public static final String CONSUMI_RISC_Q_OBB = "CONSUMI_RISC_Q_OBB";
	public static final String CONSUMI_RISC_Q_TOT = "CONSUMI_RISC_Q_TOT";
	public static final String CONSUMI_RISC_F_INV = "CONSUMI_RISC_F_INV";
	public static final String CONSUMI_ACS_Q_VOL = "CONSUMI_ACS_Q_VOL";
	public static final String CONSUMI_ACS_Q_INV = "CONSUMI_ACS_Q_INV";
	public static final String CONSUMI_ACS_Q_OBB = "CONSUMI_ACS_Q_OBB";
	public static final String CONSUMI_ACS_Q_TOT = "CONSUMI_ACS_Q_TOT";
	public static final String CONSUMI_ACS_F_INV = "CONSUMI_ACS_F_INV";
	
	public static final String COSTI_ETU_CLIMA_INVERN_CH = "COSTI_ETU_CLIMA_INVERN_CH";
	public static final String COSTI_ETU_ACS_CW = "COSTI_ETU_ACS_CW";

	public static final String SPESE_RISC_S_VOL = "SPESE_RISC_S_VOL";
	public static final String SPESE_RISC_S_INV = "SPESE_RISC_S_INV";
	public static final String SPESE_RISC_S_OBB = "SPESE_RISC_S_OBB";
	public static final String SPESE_RISC_S_E = "SPESE_RISC_S_E";
	public static final String SPESE_RISC_S_G = "SPESE_RISC_S_G";
	public static final String SPESE_RISC_S_C = "SPESE_RISC_S_C";
	public static final String SPESE_RISC_S_P = "SPESE_RISC_S_P";
	public static final String SPESE_RISC_S_UC = "SPESE_RISC_S_UC";
	public static final String SPESE_RISC_S_TOT = "SPESE_RISC_S_TOT";
	public static final String SPESE_ACS_S_VOL = "SPESE_ACS_S_VOL";
	public static final String SPESE_ACS_S_INV = "SPESE_ACS_S_INV";
	public static final String SPESE_ACS_S_OBB = "SPESE_ACS_S_OBB";
	public static final String SPESE_ACS_S_E = "SPESE_ACS_S_E";
	public static final String SPESE_ACS_S_G = "SPESE_ACS_S_G";
	public static final String SPESE_ACS_S_C = "SPESE_ACS_S_C";
	public static final String SPESE_ACS_S_P = "SPESE_ACS_S_P";
	public static final String SPESE_ACS_S_UC = "SPESE_ACS_S_UC";
	public static final String SPESE_ACS_S_TOT = "SPESE_ACS_S_TOT";
	public static final String SPESE_TOTALI_S_GL_TOT = "SPESE_TOTALI_S_GL_TOT";
	
	public static final String MILL = "MILL";
	public static final String SERVIZIO_RISCALDAMENTO_UTENZA = "SERVIZIO_RISCALDAMENTO_UTENZA";
	public static final String SERVIZIO_ACS_UTENZA = "SERVIZIO_ACS_UTENZA";
	public static final String SERVIZIO_RISCALDAMENTO_CENTRALE="SERVIZIO_RISCALDAMENTO_CENTRALE";
	public static final String SERVIZIO_ACS_CENTRALE="SERVIZIO_ACS_CENTRALE";
    public static final String SERVIZIO_RISCALDAMENTO_EDIFICI="SERVIZIO_RISCALDAMENTO_EDIFICI";
    public static final String SERVIZIO_ACS_EDIFICI="SERVIZIO_ACS_EDIFICI";
    public static final String SALVATAGGIO_IN_CORSO = "SALVATAGGIO_IN_CORSO";

	public static final String TS_INS = "TS_INS";
	public static final String ID_UTENTE_INS = "ID_UTENTE_INS";
	public static final String TS_MOD = "TS_MOD";
	public static final String ID_UTENTE_MOD = "ID_UTENTE_MOD";

	public RipartizioniUNI2018DAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public RipartizioniUNI2018DAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public RipartizioniUNI2018DAO(DBTransaction transact, String tableName) throws AppCrash {

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
		addNoStringField(TOTALE_MILLESIMI_RISCALDAMENTO, BigDecimal.class);
        addNoStringField(TOTALE_MILLESIMI_ACS, BigDecimal.class);
        addNoStringField(RENDIMENTO_CALDAIA, BigDecimal.class);
		
        addNoStringField(TS_INS, Timestamp.class);
		addNoStringField(ID_UTENTE_INS, Integer.class);
		addNoStringField(TS_MOD, Timestamp.class);
		addNoStringField(ID_UTENTE_MOD, Integer.class);
		
		
		addNoStringField(SG_SCM_TOTALE_PREV, BigDecimal.class);
		addNoStringField(SG_SCR_QTA_PREV, Integer.class);
		addNoStringField(SG_SCR_IMPORTO_PREV, BigDecimal.class);
		addNoStringField(SG_SCR_TOTALE_PREV, BigDecimal.class);
		addNoStringField(SG_SG_TOTALE_PREV, BigDecimal.class);
		addNoStringField(SG_SCM_TOTALE_CONS, BigDecimal.class);
		addNoStringField(SG_SCR_QTA_CONS, BigDecimal.class);
		addNoStringField(SG_SCR_IMPORTO_CONS, BigDecimal.class);
		addNoStringField(SG_SCR_TOTALE_CONS, BigDecimal.class);
		addNoStringField(SG_SG_TOTALE_CONS, BigDecimal.class);
		
		addNoStringField(VET_COMB_Q_VE_H_PREV, BigDecimal.class);
		addNoStringField(VET_COMB_Q_VE_W_PREV, BigDecimal.class);
		addNoStringField(VET_COMB_C_VE_PREV, BigDecimal.class);
		addNoStringField(VET_COMB_Q_VE_H_CONS, BigDecimal.class);
		addNoStringField(VET_COMB_Q_VE_W_CONS, BigDecimal.class);
		addNoStringField(VET_COMB_C_VE_CONS, BigDecimal.class);
		
		addNoStringField(VET_ELETTR_Q_VE_H_PREV, BigDecimal.class);
		addNoStringField(VET_ELETTR_Q_VE_W_PREV, BigDecimal.class);
		addNoStringField(VET_ELETTR_C_VE_PREV, BigDecimal.class);
		addNoStringField(VET_ELETTR_Q_VE_H_CONS, BigDecimal.class);
		addNoStringField(VET_ELETTR_Q_VE_W_CONS, BigDecimal.class);
		addNoStringField(VET_ELETTR_C_VE_CONS, BigDecimal.class);
		
		addNoStringField(ST_RISC_SE_PREV, BigDecimal.class);
		addNoStringField(ST_RISC_SG_PREV, BigDecimal.class);
		addNoStringField(ST_RISC_ST_PREV, BigDecimal.class);
		addNoStringField(ST_RISC_SE_CONS, BigDecimal.class);
		addNoStringField(ST_RISC_SG_CONS, BigDecimal.class);
		addNoStringField(ST_RISC_ST_CONS, BigDecimal.class);
		addNoStringField(ST_ACS_SE_PREV, BigDecimal.class);
		addNoStringField(ST_ACS_SG_PREV, BigDecimal.class);
		addNoStringField(ST_ACS_ST_PREV, BigDecimal.class);
		addNoStringField(ST_ACS_SE_CONS, BigDecimal.class);
		addNoStringField(ST_ACS_SG_CONS, BigDecimal.class);
		addNoStringField(ST_ACS_ST_CONS, BigDecimal.class);
		addNoStringField(ST_SE_TOTALE_PREV, BigDecimal.class);
		addNoStringField(ST_SG_TOTALE_PREV, BigDecimal.class);
		addNoStringField(ST_ST_TOTALE_PREV, BigDecimal.class);
		addNoStringField(ST_SE_TOTALE_CONS, BigDecimal.class);
		addNoStringField(ST_SG_TOTALE_CONS, BigDecimal.class);
		addNoStringField(ST_ST_TOTALE_CONS, BigDecimal.class);

		
		addNoStringField(GEN_QGN_H_PREV, BigDecimal.class);
		addNoStringField(GEN_QGN_W_PREV, BigDecimal.class);
		addNoStringField(GEN_QGN_H_CONS, BigDecimal.class);
		addNoStringField(GEN_QGN_W_CONS, BigDecimal.class);
		
		
		addNoStringField(ID_TECNICO, Integer.class);
		addNoStringField(ID_AMMINISTRATORE, Integer.class);
		addNoStringField(ID_SOFTWARE_CALCOLO, Integer.class);
		
		addNoStringField(CONSUMI_RISC_Q_VOL, BigDecimal.class);
		addNoStringField(CONSUMI_RISC_Q_INV, BigDecimal.class);
		addNoStringField(CONSUMI_RISC_Q_OBB, BigDecimal.class);
		addNoStringField(CONSUMI_RISC_Q_TOT, BigDecimal.class);
		addNoStringField(CONSUMI_RISC_F_INV, BigDecimal.class);
		addNoStringField(CONSUMI_ACS_Q_VOL, BigDecimal.class);
		addNoStringField(CONSUMI_ACS_Q_INV, BigDecimal.class);
		addNoStringField(CONSUMI_ACS_Q_OBB, BigDecimal.class);
		addNoStringField(CONSUMI_ACS_Q_TOT, BigDecimal.class);
		addNoStringField(CONSUMI_ACS_F_INV, BigDecimal.class);
		
		addNoStringField(COSTI_ETU_CLIMA_INVERN_CH, BigDecimal.class);
		addNoStringField(COSTI_ETU_ACS_CW, BigDecimal.class);
		
		addNoStringField(SPESE_RISC_S_VOL, BigDecimal.class);
		addNoStringField(SPESE_RISC_S_INV, BigDecimal.class);
		addNoStringField(SPESE_RISC_S_OBB, BigDecimal.class);
		addNoStringField(SPESE_RISC_S_E, BigDecimal.class);
		addNoStringField(SPESE_RISC_S_G, BigDecimal.class);
		addNoStringField(SPESE_RISC_S_C, BigDecimal.class);
		addNoStringField(SPESE_RISC_S_P, BigDecimal.class);
		addNoStringField(SPESE_RISC_S_UC, BigDecimal.class);
		addNoStringField(SPESE_RISC_S_TOT, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_VOL, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_INV, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_OBB, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_E, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_G, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_C, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_P, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_UC, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_TOT, BigDecimal.class);
		addNoStringField(SPESE_TOTALI_S_GL_TOT, BigDecimal.class);
		
		addNoStringField(FABBISOGNO_ANNUO_CLIMA, BigDecimal.class);
		addNoStringField(FABBISOGNO_ANNUO_ACS, BigDecimal.class);
		addNoStringField(PERDITE_IMPIANTO, BigDecimal.class);
		
		addNoStringField(SALVATAGGIO_IN_CORSO,Boolean.class);

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
