
package project.db;

import net.project.db.DAO_base;
import net.project.db.DBTransaction;
import net.project.errors.AppCrash;
import project.misc.Utils;

public abstract class PjDAO_base extends DAO_base {

    private String _uniqueIdentifier = "";

    public PjDAO_base(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);
    }

    public PjDAO_base(String tableName, String configName) throws AppCrash {

        super(tableName, configName);
    }

    public PjDAO_base(String tableName) throws AppCrash {

        super(tableName);
    }

    @Override
    public void setField(String name, String stringValue) throws AppCrash {

        if (stringValue == null) {

            super.setField(name, stringValue);
        }

        // se è una data ed è "  /  /"
        // la vuoto
        //
        if (stringValue.trim().length() == 6 || stringValue.trim().length() == 4) {
            if (stringValue.contains("/  /")) {
                stringValue = "";
            }
        }

        // verifica se il campo è un data dritta e in tal caso la ribalta
        //
        if (stringValue.length() == 10) {
            if ((stringValue.substring(2, 3) + stringValue.substring(5, 6)).equals("//")) {
                stringValue = Utils.ribaltaData(stringValue);
            }
        }

        super.setField(name, stringValue);
    }

    @Override
    public String getField(String name) throws AppCrash {

        String stringValue = super.getField(name);

        if (stringValue == null) {

            return stringValue;
        }

        // verifica se il campo è un data ribaltata e in tal caso la raddrizza
        //
        if (stringValue.length() == 10) {
            if ((stringValue.substring(4, 5) + stringValue.substring(7, 8)).equals("//")) {
                stringValue = Utils.raddrizzaData(stringValue);
            }
        }

        if (stringValue.contains("\"")) {
            stringValue = stringValue.replaceAll("\"", "&quot;");
        }

        return stringValue.trim();

    }

    public String getFieldHtml(String name) throws AppCrash {

        String stringValue = getField(name);

        if (stringValue.contains("\"")) {
            stringValue = stringValue.replaceAll("\"", "&quot;");
        }

        return stringValue;

    }

    @Override
    public void update() throws AppCrash {

        _whereCondition = whereCondition();
        super.update();
    }

    public void deleteByUniqueIdentifier(String idUniqueIdentifier) throws AppCrash {

        if (idUniqueIdentifier.equals("")) {
            return;
        }

        setField(_uniqueIdentifier, idUniqueIdentifier);
        retrieve();
        delete();

    }

    public void setUniqueIdentifier(String identifier) {

        _uniqueIdentifier = identifier;
    }

    public String getUniqueIdentifier() {

        return _uniqueIdentifier;
    }

    @Override
    protected String whereCondition() throws AppCrash {

        String whereCondition = null;
        StringBuffer tempBuffer = new StringBuffer();

        String uniqueIdentifier = getField(_uniqueIdentifier);
        if (!uniqueIdentifier.equals("")) {
            tempBuffer.append(" where " + _uniqueIdentifier + "  = '").append(uniqueIdentifier).append("'");
        }

        whereCondition = tempBuffer.toString();
        return whereCondition;
    }

}
