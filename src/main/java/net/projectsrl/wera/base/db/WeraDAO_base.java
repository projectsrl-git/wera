
package net.projectsrl.wera.base.db;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Util;
import net.projectsrl.db.PjNDAO_base;

public abstract class WeraDAO_base extends PjNDAO_base {

    public static final String  ID_MODULO             = "ID_MODULO";
    
    public static final String  ID_AZIENDA            = "ID_AZIENDA";

    public static final String  DT_MODULO             = "DT_MODULO";
    public static final String  STATO                 = "STATO";
    
    public static final String  TS_INS                = "TS_INS";
    public static final String  ID_UTENTE_INS         = "ID_UTENTE_INS";
    public static final String  TS_MOD                = "TS_MOD";
    public static final String  ID_UTENTE_MOD         = "ID_UTENTE_MOD";

    private String              _tableName;

    public WeraDAO_base(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
        _tableName = tableName;
    }

    public WeraDAO_base(String tableName, String configName) throws AppCrash {

        super(tableName, configName);
        _tableName = tableName;
    }

    public WeraDAO_base(String tableName) throws AppCrash {

        super(tableName);
        _tableName = tableName;
    }



    
    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_MODULO, Integer.class);
        addNoStringField(ID_AZIENDA, Integer.class);
    
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
        } 

        return whereCondition;
    }

    @Override
    public void insert() throws AppCrash {

        setAttribute(STATO, "DRA");
        super.insert();
    }


    @Override
    protected void putFieldInTemplateMap(Map<String, Object> map, String fieldName, String notNullFieldValue) {

        map.put(fieldName, StringEscapeUtils.escapeJavaScript(notNullFieldValue.trim()));
    }

    public String getTableName() {

        return _tableName;
    }

}
