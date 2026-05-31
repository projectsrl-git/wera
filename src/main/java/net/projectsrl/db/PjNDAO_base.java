
package net.projectsrl.db;

import java.io.BufferedReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.project.dataset.Row_itf;
import net.project.db.DBTransaction;
import net.project.db.NDAO_base;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.SsbServletRequest;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.webapp.security.WebAppUserSecurityInfo;
import project.misc.Utils;

public abstract class PjNDAO_base extends NDAO_base {

    protected static final String     DB_NAME_PROPERTY = "DBEntity.NomeDB";

    private WebAppUserSecurityInfo<?> _userInfo;

    private Map<String, Class<?>>     _noStringField;
    private String                    _tableName       = "";

    public PjNDAO_base(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

        _tableName = tableName;
    }

    public PjNDAO_base(String tableName, String configName) throws AppCrash {

        super(tableName, configName);

        _tableName = tableName;
    }

    public PjNDAO_base(String tableName) throws AppCrash {

        super(tableName);

        _tableName = tableName;
    }

    protected void addNoStringField(String attribID, Class<?> fieldType) {

        if (_noStringField == null) {
            _noStringField = new HashMap<String, Class<?>>();
        }

        attribID = attribID.toUpperCase();

        _noStringField.put(attribID, fieldType);
    }

    /**
     * Aggiunge l'uguaglianza ad un valore di un campo in AND alla WhereCondition del NDAO
     * 
     * @param String fieldName
     * @param WhereCondition whereCondition
     * @throws AppCrash
     */
    protected void appendField(String fieldName, WhereCondition whereCondition) throws AppCrash {

        if (whereCondition == null) {
            return;
        }

        if (getAttribute(fieldName) != null) {
            if (whereCondition.getNames().size() > 0) whereCondition.append(" AND ");
            whereCondition.append(fieldName);
            whereCondition.append(" = ");
            whereCondition.appendFieldValue(fieldName);
        }

    }

    public Class<?> getNoStringField(String attribID) {

        if (_noStringField == null) {
            return null;
        }

        attribID = attribID.toUpperCase();

        return _noStringField.get(attribID);
    }

    /**
     * Popola il PjDAO_base con tuti i campi/valori provenienti dalla SsbServletRequest req (in input) aventi gli stessi
     * nomi dei campi del dao
     * 
     * @param SsbServletRequest req
     * @throws AppCrash
     */
    public void setAttributesFromRequest(SsbServletRequest req) throws AppCrash {

        // condizioni iniziali sui parametri
        //
        ErrDetector.GetInstance().preCond(req != null, "setAttributesFromRequest - req!=null");

        Iterator<?> daoFields = iterator();

        while (daoFields.hasNext()) {

            String fieldName = (String) daoFields.next();

            if (DbConstants_itf.ID_UTENTE_INS.equals(fieldName)) {

                WebAppUserSecurityInfo<?> userInfo = getWebAppUserSecurityInfo(req);

                if (userInfo != null) {
                    setAttribute(fieldName, userInfo.getIdUtente());
                    continue;
                }

            }

            String value = req.getField(fieldName.toUpperCase());
            if (Util.IsEmpty(value)) {
                value = req.getField(fieldName.toLowerCase());
            }

            setAttribute(fieldName, value);
        }

    }

    /**
     * setAttribute
     * 
     * @param attribID
     * @param value
     * @throws AppCrash
     * 
     * @see net.project.db.NDAO_base#setAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    public void setAttribute(String attribID, Object value) throws AppCrash {

        attribID = attribID.toUpperCase();

        if (value instanceof String) {

            setNoStringAttribute(attribID, value);

        } else {

            super.setAttribute(attribID, value);

        }

    }

    private void setNoStringAttribute(String attribID, Object value) throws AppCrash {

        String stringValue = (String) value;

        try {
            Class<?> fieldType = getNoStringField(attribID);

            if (fieldType == BigDecimal.class) {

                if (!Util.IsEmpty(stringValue)) {
                    super.setAttribute(attribID, new BigDecimal(stringValue));
                }

            } else if (fieldType == Timestamp.class) {

                if (!Util.IsEmpty(stringValue)) {
                    super.setAttribute(attribID, Timestamp.valueOf(stringValue));
                }

            } else if (fieldType == Float.class) {

                if (!Util.IsEmpty(stringValue)) {
                    super.setAttribute(attribID, Float.parseFloat(stringValue));
                }
            } else if (fieldType == Integer.class) {

                if (!Util.IsEmpty(stringValue)) {
                    super.setAttribute(attribID, Integer.parseInt(stringValue));
                }

            } else if (fieldType == UUID.class) {

                if (!Util.IsEmpty(stringValue)) {
                    super.setAttribute(attribID, UUID.fromString(stringValue));
                }

            } else if (fieldType == Clob.class) {

                if (value instanceof String) {
                    if (!Util.IsEmpty(stringValue)) {
                        super.setAttribute(attribID, stringValue);
                    }
                }

            } else if (fieldType == Boolean.class) {

                Boolean flag = Boolean.FALSE;

                if (!Util.IsEmpty(stringValue)) {
                    flag = new Boolean(stringValue);

                    if (stringValue.equalsIgnoreCase("ON")) {
                        flag = Boolean.TRUE;
                    }

                }

                super.setAttribute(attribID, flag);

            } else if (fieldType == null) {

                // se � una data ed � " / /"
                // la vuoto
                //
                if (stringValue.trim().length() == 6 || stringValue.trim().length() == 4) {
                    if (stringValue.contains("/  /")) {
                        stringValue = "";
                    }
                }

                // verifica se il campo � un data dritta e in tal caso la ribalta
                //
                if (stringValue.length() == 10) {
                    if ((stringValue.substring(2, 3) + stringValue.substring(5, 6)).equals("//")) {
                        stringValue = Utils.ribaltaData(stringValue);
                    }
                }

                super.setAttribute(attribID, stringValue);
            }
        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "attribID:" + attribID + " - value:" + value);

        }
    }

    public boolean isValidField(String fieldName) throws AppCrash {

        ErrDetector.GetInstance().param(Util.IsNotEmpty(fieldName), "fieldName is empty");

        return checkFieldName(fieldName.toUpperCase());

    }

    /**
     * Popola il PjNDAO_base dao in input con tuti i campi/valori provenienti dalla Map<String,String> (in input) aventi
     * gli stessi nomi dei campi del dao
     * 
     * @param templateData
     * 
     * @throws AppCrash
     */
    public void setAttributesFromMap(Map<String, Object> templateData) throws AppCrash {

        ErrDetector.GetInstance().preCond(templateData != null, "templateData is null");

        Iterator<?> daoFields = this.iterator();

        while (daoFields != null && daoFields.hasNext()) {
            String element = (String) daoFields.next();
            if (element.startsWith("?")) {
                element = element.substring(2);
            }

            Object value = templateData.get(element);
            if (value != null) {
                setAttribute(element, value);
            }
        }
    }

    public void setMapFromAttributes(Map<String, Object> map) throws AppCrash {

        ErrDetector.GetInstance().param(map != null, "map is null");

        Iterator<?> daoFields = this.iterator();

        while (daoFields != null && daoFields.hasNext()) {

            String fieldName = (String) daoFields.next();
            String elementValue = getAttributeAsString(fieldName);

            if (elementValue != null) {
                putFieldInTemplateMap(map, fieldName, elementValue);
            }

        }

    }
    
    
    public void setMapForDuplicateDAO(Map<String, Object> map) throws AppCrash {

        ErrDetector.GetInstance().param(map != null, "map is null");

        Iterator<?> daoFields = this.iterator();

        while (daoFields != null && daoFields.hasNext()) {

            String fieldName = (String) daoFields.next();

            if (getAttribute(fieldName) != null) {
                map.put(fieldName, getAttribute(fieldName));
            }

        }

    }

    protected void putFieldInTemplateMap(Map<String, Object> map, String fieldName, String notNullFieldValue) {

        map.put(fieldName, notNullFieldValue.trim());
    }

    public void setMapFromRow(Map<String, Object> map, Row_itf row) throws AppCrash {

        ErrDetector.GetInstance().param(map != null, "map is null");
        ErrDetector.GetInstance().param(row != null, "row is null");

        Iterator<?> daoFields = this.iterator();

        while (daoFields != null && daoFields.hasNext()) {

            String fieldName = (String) daoFields.next();
            putFieldInMapFromRow(map, row, fieldName);

        }

    }

    public void putFieldInMapFromRow(Map<String, Object> data, Row_itf dbRow, String fieldName) throws AppCrash {

        Object fieldValue = dbRow.getField(fieldName);
        if (fieldValue instanceof String) {
            data.put(fieldName, (String) dbRow.getField(fieldName));
        } else if (fieldValue instanceof Boolean) {
            data.put(fieldName, (Boolean) dbRow.getField(fieldName));
        }
    }

    protected String getTableName() {

        return _tableName;
    }

    @Override
    public Object getAttribute(String attribID) throws AppCrash {

        Object attr = super.getAttribute(attribID);

        if (attr == null) {
            return null;
        }

        if (attr instanceof String) {

            String stringValue = (String) attr;

            // verifica se il campo � un data ribaltata e in tal caso la raddrizza
            //
            if (stringValue.length() == 10) {
                if ((stringValue.substring(4, 5) + stringValue.substring(7, 8)).equals("//")) {
                    stringValue = Utils.raddrizzaData(stringValue);
                    return stringValue;
                }
            }

        } else if (attr instanceof Clob) {
            String stringValue = clobToString((Clob) attr);
            return stringValue;
        }

        return attr;
    }

    public WebAppUserSecurityInfo<?> getWebAppUserSecurityInfo(SsbServletRequest req) throws AppCrash {

        WebAppUserSecurityInfo<?> userInfo = null;

        try {
            userInfo = (WebAppUserSecurityInfo<?>) req.getSession(false)
                    .getAttribute(WebAppConstants_itf.SESSION_USER_SECURITY_INFO);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "error in cast (WebAppUserSecurityInfo) userInfo:" + userInfo);
        }

        return userInfo;

    }

    public void setWebAppUserSecurityInfo(WebAppUserSecurityInfo<?> userInfo) throws AppCrash {

        _userInfo = userInfo;

    }

    public WebAppUserSecurityInfo<?> getWebAppUserSecurityInfo() {

        return _userInfo;
    }

    /*********************************************************************************************
     * From CLOB to String
     * 
     * @return string representation of clob
     *********************************************************************************************/
    private String clobToString(Clob data) {

        final StringBuilder sb = new StringBuilder();

        try {
            final Reader reader = data.getCharacterStream();
            final BufferedReader br = new BufferedReader(reader);

            int b;
            while (-1 != (b = br.read())) {
                sb.append((char) b);
            }

            br.close();
        } catch (Throwable e) {
            AppCrash ac = new AppCrash();
            ac.logContext(this.getClass().getName(), "SQL. Could not convert CLOB to string");
            return e.toString();
        }

        return sb.toString();
    }

}
