
package net.projectsrl.wera.ripartizioniuni2018.db;

import java.math.BigDecimal;
import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.wera.base.db.AliModDettagliDAO_base;
import net.projectsrl.wera.ripartizioni.db.RipartizioniDettaglioDAO;

public class RipartizioniUNI2018DettagliDAO extends AliModDettagliDAO_base {
	private static final String TABLE_NAME = "RIPARTIZIONI_UNI_2018_DETTAGLIO";
	public static final String ID_AZIENDA = "ID_AZIENDA";
	public static final String ID_CONDOMINO = "ID_CONDOMINO";

	public static final String DENOMINAZIONE = "DENOMINAZIONE";
	public static final String NUMERO_RIPARTITORI = "NUMERO_RIPARTITORI";
	public static final String MILLESIMI_CLIMA = "MILLESIMI_CLIMA";
	public static final String MILLESIMI_ACS = "MILLESIMI_ACS";
	public static final String MILLESIMI = "MILLESIMI";
	public static final String  LETTURA   = "LETTURA";
    public static final String  LETTURA_ACS   = "LETTURA_ACS";

	public static final String TS_INS = "TS_INS";
	public static final String ID_UTENTE_INS = "ID_UTENTE_INS";
	public static final String TS_MOD = "TS_MOD";
	public static final String ID_UTENTE_MOD = "ID_UTENTE_MOD";

		public static final String Q_H = "Q_H";
	public static final String Q_W = "Q_W";
	public static final String UR = "UR";
	public static final String CONSUMO_RISCALDAMENTO_Q_VOL = "CONSUMO_RISCALDAMENTO_Q_VOL";
	public static final String CONSUMO_RISCALDAMENTO_Q_INV = "CONSUMO_RISCALDAMENTO_Q_INV";
	public static final String CONSUMO_RISCALDAMENTO_Q_OBB = "CONSUMO_RISCALDAMENTO_Q_OBB";
	public static final String CONSUMO_RISCALDAMENTO_Q_TOT = "CONSUMO_RISCALDAMENTO_Q_TOT";
	public static final String CONSUMO_ACS_Q_VOL = "CONSUMO_ACS_Q_VOL";
	public static final String CONSUMO_ACS_Q_INV = "CONSUMO_ACS_Q_INV";
	public static final String CONSUMO_ACS_Q_OBB = "CONSUMO_ACS_Q_OBB";
	public static final String CONSUMO_ACS_Q_TOT = "CONSUMO_ACS_Q_TOT";

	public static final String SPESE_RISCALDAMENTO_S_VOL = "SPESE_RISCALDAMENTO_S_VOL";
	public static final String SPESE_RISCALDAMENTO_S_INV = "SPESE_RISCALDAMENTO_S_INV";
	public static final String SPESE_RISCALDAMENTO_S_OBB = "SPESE_RISCALDAMENTO_S_OBB";
	public static final String SPESE_RISCALDAMENTO_S_E = "SPESE_RISCALDAMENTO_S_E";
	public static final String SPESE_RISCALDAMENTO_S_G = "SPESE_RISCALDAMENTO_S_G";
	public static final String SPESE_RISCALDAMENTO_S_C = "SPESE_RISCALDAMENTO_S_C";
	public static final String SPESE_RISCALDAMENTO_S_P = "SPESE_RISCALDAMENTO_S_P";
	public static final String SPESE_RISCALDAMENTO_S_UC = "SPESE_RISCALDAMENTO_S_UC";
	public static final String SPESE_RISCALDAMENTO_S_TOT = "SPESE_RISCALDAMENTO_S_TOT";
	public static final String SPESE_ACS_S_VOL = "SPESE_ACS_S_VOL";
	public static final String SPESE_ACS_S_INV = "SPESE_ACS_S_INV";
	public static final String SPESE_ACS_S_OBB = "SPESE_ACS_S_OBB";
	public static final String SPESE_ACS_S_E = "SPESE_ACS_S_E";
	public static final String SPESE_ACS_S_G = "SPESE_ACS_S_G";
	public static final String SPESE_ACS_S_C = "SPESE_ACS_S_C";
	public static final String SPESE_ACS_S_P = "SPESE_ACS_S_P";
	public static final String SPESE_ACS_S_UC = "SPESE_ACS_S_UC";
	public static final String SPESE_ACS_S_TOT = "SPESE_ACS_S_TOT";
	public static final String SPESE_TOTALI_S_H_TOT = "SPESE_TOTALI_S_H_TOT";
	public static final String SPESE_TOTALI_S_W_TOT = "SPESE_TOTALI_S_W_TOT";
	public static final String SPESE_TOTALI_S_GL_TOT = "SPESE_TOTALI_S_GL_TOT";
	
	public static final String TIPO_UTENZA = "TIPO_UTENZA";

	public RipartizioniUNI2018DettagliDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public RipartizioniUNI2018DettagliDAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public RipartizioniUNI2018DettagliDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
	}

	@Override
	protected void init() throws AppCrash {

		super.init();

		addNoStringField(ID_MODULO, Integer.class);
		addNoStringField(ID_CONDOMINO, Integer.class);
		addNoStringField(ID_AZIENDA, Integer.class);

		addNoStringField(TS_INS, Timestamp.class);
		addNoStringField(ID_UTENTE_INS, Integer.class);
		addNoStringField(TS_MOD, Timestamp.class);
		addNoStringField(ID_UTENTE_MOD, Integer.class);

		addNoStringField(Q_H, BigDecimal.class);
		addNoStringField(Q_W, BigDecimal.class);
		addNoStringField(UR, BigDecimal.class);
		addNoStringField(CONSUMO_RISCALDAMENTO_Q_VOL, BigDecimal.class);
		addNoStringField(CONSUMO_RISCALDAMENTO_Q_INV, BigDecimal.class);
		addNoStringField(CONSUMO_RISCALDAMENTO_Q_OBB, BigDecimal.class);
		addNoStringField(CONSUMO_RISCALDAMENTO_Q_TOT, BigDecimal.class);
		addNoStringField(CONSUMO_ACS_Q_VOL, BigDecimal.class);
		addNoStringField(CONSUMO_ACS_Q_INV, BigDecimal.class);
		addNoStringField(CONSUMO_ACS_Q_OBB, BigDecimal.class);
		addNoStringField(CONSUMO_ACS_Q_TOT, BigDecimal.class);

		addNoStringField(SPESE_RISCALDAMENTO_S_VOL, BigDecimal.class);
		addNoStringField(SPESE_RISCALDAMENTO_S_INV, BigDecimal.class);
		addNoStringField(SPESE_RISCALDAMENTO_S_OBB, BigDecimal.class);
		addNoStringField(SPESE_RISCALDAMENTO_S_E, BigDecimal.class);
		addNoStringField(SPESE_RISCALDAMENTO_S_G, BigDecimal.class);
		addNoStringField(SPESE_RISCALDAMENTO_S_C, BigDecimal.class);
		addNoStringField(SPESE_RISCALDAMENTO_S_P, BigDecimal.class);
		addNoStringField(SPESE_RISCALDAMENTO_S_UC, BigDecimal.class);
		addNoStringField(SPESE_RISCALDAMENTO_S_TOT, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_VOL, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_INV, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_OBB, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_E, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_G, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_C, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_P, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_UC, BigDecimal.class);
		addNoStringField(SPESE_ACS_S_TOT, BigDecimal.class);
		addNoStringField(SPESE_TOTALI_S_H_TOT, BigDecimal.class);
		addNoStringField(SPESE_TOTALI_S_W_TOT, BigDecimal.class);
		addNoStringField(SPESE_TOTALI_S_GL_TOT, BigDecimal.class);
		
		addNoStringField(LETTURA, BigDecimal.class);
        addNoStringField(LETTURA_ACS, BigDecimal.class);
		addNoStringField(MILLESIMI, BigDecimal.class);
        addNoStringField(MILLESIMI_CLIMA, BigDecimal.class);
	    addNoStringField(MILLESIMI_ACS, BigDecimal.class);

	}

	@Override
	protected WhereCondition whereCondition() throws AppCrash {

		WhereCondition whereCondition = new WhereCondition(this);

		if (Util.IsNotEmpty(getAttribute(ID_DETTAGLIO))) {
			appendField(ID_DETTAGLIO, whereCondition);
		} else if (Util.IsNotEmpty(getAttribute(NR_DETTAGLIO))) {
			appendField(NR_DETTAGLIO, whereCondition);
		}

		return whereCondition;
	}

	@Override
	public void update() throws AppCrash {

		Integer idDettaglio = (Integer) getAttribute(ID_DETTAGLIO);
		RipartizioniDettaglioDAO daoOld = new RipartizioniDettaglioDAO();
		daoOld.setAttribute(ID_DETTAGLIO, idDettaglio);
		daoOld.retrieve();
		String nr = (String) daoOld.getAttribute(NR_DETTAGLIO);

		if (Util.IsEmpty((String) getAttribute(NR_DETTAGLIO))) {
			setAttribute(NR_DETTAGLIO, nr);
		}

		super.update();
	}

}
