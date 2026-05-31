
package net.projectsrl.wm.db;

import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.projectsrl.db.PjDAO_base;

public class ParaDAO extends PjDAO_base {

    private static final String NOME_TABELLA = "PARA";

    public static final String  CODICE	= "CODICE";
    public static final String  DESCRI	= "DESCRI";
    public static final String  LIBERA	= "LIBERA";
    public static final String  AZIENDA	= "AZIENDA";
    public static final String  ORDINE	= "ORDINE";
    public static final String  TIPO	= "TIPO";
    public static final String  NORMATIVA	= "NORMATIVA";
    public static final String  DOC	= "DOC";
    public static final String  ORE_MINIME	= "ORE_MINIME";
    public static final String  REG_APPROVAZIONE	= "REG_APPROVAZIONE";
    public static final String  DOC_PREVENTIVI	= "DOC_PREVENTIVI";
    public static final String  DOC_CONSUNTIVI	= "DOC_CONSUNTIVI";
    public static final String  GR_AUTORIZZAZ	= "GR_AUTORIZZAZ";
    
    public static final String  DIP	= "DIP";
    public static final String  RESP	= "RESP";
    public static final String  AMM	= "AMM";
    
    public static final String  LIV1_1	= "LIV1_1";
    public static final String  LIV1_2	= "LIV1_2";
    public static final String  LIV1_3	= "LIV1_3";
    public static final String  LIV1_4	= "LIV1_4";
    public static final String  LIV1_5	= "LIV1_5";
    
    public static final String  LIV2_1	= "LIV2_1";
    public static final String  LIV2_2	= "LIV2_2";
    public static final String  LIV2_3	= "LIV2_3";
    public static final String  LIV2_4	= "LIV2_4";
    public static final String  LIV2_5	= "LIV2_5";
    
    public static final String  LIV3_1	= "LIV3_1";
    public static final String  LIV3_2	= "LIV3_2";
    public static final String  LIV3_3	= "LIV3_3";
    public static final String  LIV3_4	= "LIV3_4";
    public static final String  LIV3_5	= "LIV3_5";
    
    public static final String  LIV4_1	= "LIV4_1";
    public static final String  LIV4_2	= "LIV4_2";
    public static final String  LIV4_3	= "LIV4_3";
    public static final String  LIV4_4	= "LIV4_4";
    public static final String  LIV4_5	= "LIV4_5";
    
    public static final String  LIV5_1	= "LIV5_1";
    public static final String  LIV5_2	= "LIV5_2";
    public static final String  LIV5_3	= "LIV5_3";
    public static final String  LIV5_4	= "LIV5_4";
    public static final String  LIV5_5	= "LIV5_5";

    public ParaDAO() throws AppCrash {

        super(NOME_TABELLA);
		setUniqueIdentifier(CODICE);
    }

    public ParaDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty("DBEntity.NomeDB") + NOME_TABELLA);
		setUniqueIdentifier(CODICE);
    }

    public ParaDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
		setUniqueIdentifier(CODICE);

    }
}
