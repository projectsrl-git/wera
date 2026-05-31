package net.projectsrl.wera.importazioni.db;

import java.sql.Timestamp;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.projectsrl.wera.base.db.WeraDAO_base;

public class ScaricoDAO extends WeraDAO_base {
	

	private static final String TABLE_NAME = "SCARICO";

	public static final String ID_MODULO = "ID_MODULO";
	public static final String ID_CONDOMINIO = "ID_CONDOMINIO";
	
	public static final String NOME_FILE = "NOME_FILE";
	public static final String DATA_IMPORT = "DATA_IMPORT";
	public static final String ORA_IMPORT = "ORA_IMPORT";
	public static final String N_CLIENTE_NETWORK = "N_CLIENTE_NETWORK";
	public static final String N_FABBRICA_NETWORK = "N_FABBRICA_NETWORK";
	public static final String VERSIONE_SOFTWARE_I = "VERSIONE_SOFTWARE_I";
	public static final String DATA_DI_LETTURA_I = "DATA_DI_LETTURA_I";
	public static final String GIORNO_SETTIMANA = "GIORNO_SETTIMANA";
	public static final String ORA_DI_LETTURA_I = "ORA_DI_LETTURA_I";
	public static final String OPERAT_HOURS = "OPERAT_HOURS";
	public static final String ANOMALIA_I = "ANOMALIA_I";
	public static final String DATA_ANOMALIA_I = "DATA_ANOMALIA_I";
	public static final String ORA_ANOMALIA_I = "ORA_ANOMALIA_I";
	public static final String DISP_RADIO_TROVATI = "DISP_RADIO_TROVATI";
	public static final String MODELLO_ANTENNA = "MODELLO_ANTENNA";

	public static final String N_PROGRESSIVO_DISPOSITIVO = "N_PROGRESSIVO_DISPOSITIVO";
	public static final String ANTENNA_DI_RIFERIMENTO = "ANTENNA_DI_RIFERIMENTO";
	public static final String DATA_DI_LETTURA = "DATA_DI_LETTURA";
	public static final String ORA_DI_LETTURA = "ORA_DI_LETTURA";
	public static final String N_FABBRICA_DISPOSITIVO = "N_FABBRICA_DISPOSITIVO";
	public static final String CODICE_DI_PRODUZIONE = "CODICE_DI_PRODUZIONE";
	public static final String VERSIONE_SOFTWARE = "VERSIONE_SOFTWARE";
	public static final String ANOMALIA = "ANOMALIA";
	public static final String DATA_ANOMALIA = "DATA_ANOMALIA";
	public static final String ORA_ANOMALIA = "ORA_ANOMALIA";
	public static final String LETTURA_ATTUALE = "LETTURA_ATTUALE";
	public static final String UNITA_DI_MISURA = "UNITA_DI_MISURA";
	public static final String VOLUME_ATTUALE = "VOLUME_ATTUALE";
	public static final String UNITA_DI_MISURA1 = "UNITA_DI_MISURA1";
	public static final String FATTORE_ENERGIA = "FATTORE_ENERGIA";
	public static final String LETTURA_A_DATA_DI_SCARICO = "LETTURA_A_DATA_DI_SCARICO";
	public static final String UNITA_DI_MISURA2 = "UNITA_DI_MISURA2";
	public static final String DATA_DI_SCARICO = "DATA_DI_SCARICO";
	public static final String DATA_INIZIO_STATISTICA = "DATA_INIZIO_STATISTICA";
	public static final String STAT_VALUE1 = "STAT_VALUE1";
	public static final String STAT_VALUE2 = "STAT_VALUE2";
	public static final String STAT_VALUE3 = "STAT_VALUE3";
	public static final String STAT_VALUE4 = "STAT_VALUE4";
	public static final String STAT_VALUE5 = "STAT_VALUE5";
	public static final String STAT_VALUE6 = "STAT_VALUE6";
	public static final String STAT_VALUE7 = "STAT_VALUE7";
	public static final String STAT_VALUE8 = "STAT_VALUE8";
	public static final String STAT_VALUE9 = "STAT_VALUE9";
	public static final String STAT_VALUE10 = "STAT_VALUE10";
	public static final String STAT_VALUE11 = "STAT_VALUE11";
	public static final String STAT_VALUE12 = "STAT_VALUE12";
	public static final String STAT_VALUE13 = "STAT_VALUE13";
	public static final String STAT_VALUE14 = "STAT_VALUE14";
	public static final String STAT_VALUE15 = "STAT_VALUE15";
	public static final String STAT_VALUE16 = "STAT_VALUE16";
	public static final String STAT_VALUE17 = "STAT_VALUE17";
	public static final String STAT_VALUE18 = "STAT_VALUE18";
	public static final String UNIT_OF_STAT = "UNIT_OF_STAT";
	public static final String CURRVALUE_OF_TARIFF1 = "CURRVALUE_OF_TARIFF1";
	public static final String UNIT_OF_TARIFF1 = "UNIT_OF_TARIFF1";
	public static final String SET_DAY_VAL_OF_TARIFF1 = "SET_DAY_VAL_OF_TARIFF1";
	public static final String SET_DAY_UNIT_OF_TARIFF1 = "SET_DAY_UNIT_OF_TARIFF1";

	public static final String KDEVADR292 = "KDEVADR292";
	public static final String ZDAT = "ZDAT";
	public static final String ZUHR = "ZUHR";
	public static final String CBS104 = "CBS104";
	public static final String ZFDAUER251 = "ZFDAUER251";
	public static final String VOLUMECUMUL = "VOLUMECUMUL";
	public static final String UNVOLUMECUMUL = "UNVOLUMECUMUL";
	public static final String CVOLSTLVAL108 = "CVOLSTLVAL108";
	public static final String CVOLSTLDIM108 = "CVOLSTLDIM108";
	public static final String CENSTVLVAL111 = "CENSTVLVAL111";
	public static final String CENSTVLDIM111 = "CENSTVLDIM111";
	public static final String CVOLSTVLVAL112 = "CVOLSTVLVAL112";
	public static final String CVOLSTVLDIM112 = "CVOLSTVLDIM112";
	public static final String ZDATSTVL114 = "ZDATSTVL114";
	public static final String CPQMWMAXVAL169 = "CPQMWMAXVAL169";
	public static final String CPQMWMAXDIM169 = "CPQMWMAXDIM169";
	public static final String ZDATMWMAX170 = "ZDATMWMAX170";
	public static final String VALCUMULTAR1 = "VALCUMULTAR1";
	public static final String UMVALCUMULTAR1 = "UMVALCUMULTAR1";
	public static final String CVXTAR2VAL194 = "CVXTAR2VAL194";
	public static final String CVXTAR2DIM194 = "CVXTAR2DIM194";
	public static final String CVXTAR1STLVAL193 = "CVXTAR1STLVAL193";
	public static final String CVXTAR1STLDIM193 = "CVXTAR1STLDIM193";
	public static final String CVXTAR2STLVAL194 = "CVXTAR2STLVAL194";
	public static final String CVXTAR2STLDIM194 = "CVXTAR2STLDIM194";
	public static final String VISIBILE = "VISIBILE";
	public static final String DATA_VIS = "DATA_VIS";
	public static final String ORA_IMPORT_COMPLETA = "ORA_IMPORT_COMPLETA";
	public static final String ERRORE_SISTEMATO = "ERRORE_SISTEMATO";

	// per antenne nuove 2023
	public static final String VOLUME1="VOLUME1";
	public static final String VOLUME2="VOLUME2";
	public static final String DATE1="DATE1";
	public static final String ENERGY1="ENERGY1";
	public static final String ENERGY2="ENERGY2";
	public static final String VOLUME_FLOW="VOLUME_FLOW";
	public static final String POWER="POWER";
	public static final String FLOW_TEMPERATURE="FLOW_TEMPERATURE";
	public static final String RETURN_TEMPERATURE="RETURN_TEMPERATURE";
	
	

	public ScaricoDAO() throws AppCrash {

		super(TABLE_NAME);
	}

	public ScaricoDAO(DBTransaction transact) throws AppCrash {

		super(transact, TABLE_NAME);
	}

	public ScaricoDAO(DBTransaction transact, String tableName) throws AppCrash {

		super(transact, tableName);
	}

	@Override
	protected void init() throws AppCrash {

		super.init();
		addNoStringField(ID_MODULO, Integer.class);
		addNoStringField(ID_AZIENDA, Integer.class);
		addNoStringField(ID_CONDOMINIO, Integer.class);
		addNoStringField(VISIBILE, Boolean.class);
		addNoStringField(ERRORE_SISTEMATO, Boolean.class);
		addNoStringField(TS_INS, Timestamp.class);
        addNoStringField(ID_UTENTE_INS, Integer.class);
		addNoStringField(TS_MOD, Timestamp.class);
        addNoStringField(ID_UTENTE_MOD, Integer.class);

	}

	
}
