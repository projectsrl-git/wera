
package net.projectsrl.bow.configuration.db;

import java.util.HashMap;
import java.util.Map;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.db.DBTransaction;
import net.project.db.WhereCondition;
import net.project.errors.AppCrash;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.frame.SsbServletRequest;

import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.DeleteQueryRows;
import net.projectsrl.webapp.core.InsertMultipleReferences;
import project.misc.Utils;

public class ProfiliDAO extends PjNDAO_base {

    private static final String TABLE_NAME         = "PROFILI";
    private static final String DS_PROFILI_AZIENDA = "DSProfiliAzienda";

    public static final String  ID_PROFILO         = "ID_PROFILO";
    public static final String  CODICE             = "CODICE";
    public static final String  DESCRIZIONE        = "DESCRIZIONE";

    private String              _menuList          = null;
    private String              _listaAziende      = null;

    private class DeleteProfiliAzienda extends DeleteQueryRows<ProfiliAziendaDAO, Integer> {

        public DeleteProfiliAzienda() {

            super(DS_PROFILI_AZIENDA, ProfiliAziendaDAO.ID_PROFILO, ProfiliAziendaDAO.ID_PROFILO_AZIENDA);

        }

    }

    private class InsertProfiliAzienda extends InsertMultipleReferences<ProfiliAziendaDAO> {

        public InsertProfiliAzienda() {

            super(DS_PROFILI_AZIENDA, ID_PROFILO, ProfiliAziendaDAO.ID_AZIENDA);
        }

    }

    public ProfiliDAO() throws AppCrash {

        super(TABLE_NAME);
    }

    public ProfiliDAO(DBTransaction transact) throws AppCrash {

        super(transact, Config.GetInstance().getProperty(DB_NAME_PROPERTY) + TABLE_NAME);
    }

    public ProfiliDAO(DBTransaction transact, String tableName) throws AppCrash {

        super(transact, tableName);

    }

    /**
     * Questo metodo
     * 
     * @return
     * @throws AppCrash
     * 
     * @see net.ssb.db.NDAO_base#whereCondition()
     */
    @Override
    protected WhereCondition whereCondition() throws AppCrash {

        WhereCondition whereCondition = new WhereCondition(this);

        if (Util.IsNotEmpty(getAttribute(ID_PROFILO))) {
            appendField(ID_PROFILO, whereCondition);
        } else if (Util.IsNotEmpty(getAttribute(CODICE))) {
            appendField(CODICE, whereCondition);
        }

        return whereCondition;

    }

    @Override
    protected void init() throws AppCrash {

        super.init();

        addNoStringField(ID_PROFILO, Integer.class);

    }

    @Override
    public void insert() throws AppCrash {

        super.insert();

        retrieve();

        updateProfili();
        updateProfiliAzienda();

    }

    @Override
    public void update() throws AppCrash {

        super.update();

        updateProfili();
        updateProfiliAzienda();

    }

    private void updateProfili() throws AppCrash {

        if (_menuList != null) {

            String currentMenuList = getMenuList();

            if (_menuList.equals(currentMenuList)) {
                return;
            }

            // TODO: transaction
            deleteCurrentMenuList();

            String[] menuListArray = _menuList.split(";");
            for (int i = 0; i < menuListArray.length; i++) {
                MenuProfiliDAO menuProfiliDAO = new MenuProfiliDAO();
                menuProfiliDAO.setAttribute(MenuProfiliDAO.ID_MENU, menuListArray[i]);
                menuProfiliDAO.setAttribute(MenuProfiliDAO.ID_PROFILO, getAttribute(ID_PROFILO));
                menuProfiliDAO.insert();
            }

        }
    }

    public void setMenuList(String menuList) {

        _menuList = menuList;
    }

    public String getMenuList() throws AppCrash {

        Integer idProfilo = (Integer) getAttribute(ID_PROFILO);

        if (getAttribute(ID_PROFILO) == null) {
            return null;
        }

        DataSet_itf dataSet = null;
        String dsName = "DSListaMenuProfilo";
        String menuList = "";

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            Map<String, String> params = new HashMap<String, String>();
            params.put("ID_PROFILO", idProfilo.toString());
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow == null) {
                    continue;
                }

                Integer idMenu = (Integer) dbRow.getField("ID_MENU");

                if (idMenu == null) {
                    continue;
                }

                if (!menuList.equals("")) {
                    menuList += ";";
                }
                menuList += idMenu.toString();

            }

        } catch (AppCrash ac) {
            ac.logContext("getMenuList", "Errore nell'esecuzione del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("getMenuList", "Errore nella close del dataset " + dsName);
                }
            }
        }

        return menuList;
    }

    public void deleteCurrentMenuList() throws AppCrash {

        Integer idProfilo = (Integer) getAttribute(ID_PROFILO);

        if (getAttribute(ID_PROFILO) == null) {
            return;
        }

        DataSet_itf dataSet = null;
        String dsName = "DSMenuProfili";

        DataSetFactory dsFactory = DataSetFactory.getInstance();

        try {
            dataSet = dsFactory.makeDataSet("", dsName);

            Map<String, String> params = new HashMap<String, String>();
            params.put("ID_PROFILO", idProfilo.toString());
            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();

                if (dbRow == null) {
                    continue;
                }

                Integer id = (Integer) dbRow.getField("ID_MENU_PROFILI");

                if (id == null) {
                    continue;
                }

                MenuProfiliDAO dao = new MenuProfiliDAO();
                dao.setAttribute(MenuProfiliDAO.ID_MENU_PROFILI, id);
                dao.delete();

            }

        } catch (AppCrash ac) {
            ac.logContext("deleteCurrentMenuList", "Errore nell'esecuzione del dataset " + dsName);
            throw ac;
        } finally {
            // chiude il dataset per il conteggio degli elementi trovati
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (Throwable t) {
                    AppCrash ac = new AppCrash(t);
                    ac.logContext("deleteCurrentMenuList", "Errore nella close del dataset " + dsName);
                }
            }
        }

    }

    @Override
    public void setAttributesFromRequest(SsbServletRequest req) throws AppCrash {

        super.setAttributesFromRequest(req);

        String listaAziende = req.getField("AZIENDE_MULTIPLE");
        if (Utils.IsNotEmpty(listaAziende)) {
            _listaAziende = listaAziende;
        }

    }


    private void updateProfiliAzienda() throws AppCrash {

        Integer idProfilo = getPkValue();

        new DeleteProfiliAzienda().delete(idProfilo.toString());

        if (Utils.IsNotEmpty(_listaAziende)) {
            new InsertProfiliAzienda().insert(_listaAziende, idProfilo);
        }
    }

    @Override
    public void setMapFromAttributes(Map<String, Object> map) throws AppCrash {

        Integer idProfilo = getPkValue();

        super.setMapFromAttributes(map);

        map.put("AZIENDE_MULTIPLE", new InsertProfiliAzienda().getSelectedCodeList(idProfilo.toString()));
    }

    private Integer getPkValue() throws AppCrash {

        Integer id = (Integer) getAttribute(ID_PROFILO);
        if (id == null) {
            retrieve();
            id = (Integer) getAttribute(ID_PROFILO);
        }
        return id;
    }

}