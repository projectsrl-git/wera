/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.projectsrl.webapp.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.project.dataset.DataSetFactory;
import net.project.dataset.DataSet_itf;
import net.project.dataset.Row_itf;
import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Config;
import net.project.misc.Util;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.WebAppConstants_itf;

/**
 * Questa
 */
public class WebAppUserSecurityInfo<U> extends UserSecurityInfo {

    private static final String LANGUAGE_PROPERTIES_FILE      = "WEB-INF/config/language.properties";
    private static final String TEMPLATE_DATA_PROPERTIES_FILE = "WEB-INF/config/template-data.properties";
    private static final String BOOLEAN_FALSE                 = "f";
    private U                   _userUnuqueIdentifier;
    private Map<String, Object> _sessionMap                   = new HashMap<String, Object>();
    private String              _configName                   = "";

    public final static String  MENU_LIST                     = "menu_list";
    private final static String MENU_DATASET                  = "DSMenu";
    private static final String USERNAME                      = "USERNAME";

    public void setUserUniqueIdentifier(U userUniqueIdentifier) {

        _userUnuqueIdentifier = userUniqueIdentifier;
    }

    public U getIdUtente() {

        return _userUnuqueIdentifier;
    }

    public Map<String, Object> getSessionMap() {

        return _sessionMap;
    }

    @SuppressWarnings("unchecked")
    public List<MenuItem> getMenuList() throws AppCrash {

        ErrDetector.GetInstance().preCond(_sessionMap != null, "session map is null");

        List<MenuItem> menuList = (List<MenuItem>) _sessionMap.get(MENU_LIST);

        return menuList;
    }

    /**
     * List contenente le voci di menu in ordine di gerarchia
     * 
     * @param userInfo
     * @return
     * @throws AppCrash
     */
    public void addMenuListToSessionMap(String isoLanguage) throws AppCrash {

        List<MenuItem> menuList = new ArrayList<MenuItem>();

        DataSet_itf dataSet = null;

        try {

            ErrDetector.GetInstance().postCond(Util.IsNotEmpty(getUserId()), "user is empty");

            DataSetFactory dsFactory = DataSetFactory.getInstance();
            dsFactory = DataSetFactory.getInstance();
            dataSet = dsFactory.makeDataSet(_configName, MENU_DATASET);

            HashMap<String, String> params = new HashMap<String, String>();
            params.put(USERNAME, getUserId());
            params.put(WebAppConstants_itf.CURRENT_SELECTED_ISO_LANGUAGE, isoLanguage);

            dataSet.setParam(params);
            dataSet.open();

            while (dataSet.hasMoreElements()) {
                Row_itf dbRow = (Row_itf) dataSet.nextElement();
                U menuId = (U) dbRow.getField(MenuItem.ID_MENU);
                U menuIdSup = (U) dbRow.getField(MenuItem.ID_MENU_SUP);
                String menuIdSupString = "";
                if (menuIdSup != null) {
                    menuIdSupString = menuIdSup.toString();
                }
                String label = (String) dbRow.getField(MenuItem.LABEL);
                String function = (String) dbRow.getField(MenuItem.ALIAS);
                String link = (String) dbRow.getField(MenuItem.LINK);
                int itemLevel = (Integer) dbRow.getField(MenuItem.ITEM_LEVEL);
                String path = (String) dbRow.getField(MenuItem.PATH);
                String pathDescri = (String) dbRow.getField(MenuItem.PATH_DESCRI);
                String linkChain = (String) dbRow.getField(MenuItem.LINK_CHAIN);
                String icon = (String) dbRow.getField(MenuItem.ICON);
                // boolean readOnly = BOOLEAN_FALSE.equalsIgnoreCase((String) dbRow.getField(MenuItem.READONLY));
                boolean readOnly = false;
                int nrOfChildren = (Integer) dbRow.getField(MenuItem.NR_CHILDREN);

                MenuItem menuItem = new MenuItem(menuId.toString(), menuIdSupString, isoLanguage, label, function, link,
                        itemLevel, path, pathDescri, linkChain, readOnly, nrOfChildren, icon);
                menuList.add(menuItem);
            }

        } catch (Throwable t) {
            AppCrash ac = new AppCrash(t);
            ac.logContext(this.getClass().getName(), "errore nella costruzione del menu - user:" + getUserId());
            throw ac;
        } finally {
            if (dataSet != null) {
                try {
                    dataSet.close();
                } catch (AppCrash ac) {
                    ac.logContext(this.getClass().getName(), "Errore nella close del dataset");
                }
            }
        }

        _sessionMap.put(MENU_LIST, menuList);

    }

    public void setTemplateDataProperties() {

        Properties prop = new Properties();
        InputStream input = null;
        String fileName = TEMPLATE_DATA_PROPERTIES_FILE;

        try {

            fileName = Config.GetInstance().makeAbsolutePath(fileName);

            if (!(new File(fileName)).exists()) {
                return;
            }

            input = new FileInputStream(fileName);

            // load a properties file
            prop.load(input);

            Set<Object> keys = prop.keySet();
            for (Object k : keys) {
                String labelKey = (String) k;
                String labelValue = prop.getProperty(labelKey);
                setField(labelKey, labelValue);
            }

        } catch (Throwable ex) {
            AppCrash ac = new AppCrash();
            ac.logContext(this.getClass().getName(), "error reading template data properties file " + fileName);

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void setLanguageLabels(String isoLanguage) {

        Properties prop = new Properties();
        InputStream input = null;
        String fileName = LANGUAGE_PROPERTIES_FILE;

        try {

            fileName = Config.GetInstance().makeAbsolutePath(fileName);

            if (!(new File(fileName)).exists()) {
                return;
            }

            input = new FileInputStream(fileName);

            // load a properties file
            prop.load(input);

            Set<Object> keys = prop.keySet();
            for (Object k : keys) {
                String labelKey = (String) k;

                if (labelKey.startsWith(isoLanguage + ".")) {

                    String labelValue = prop.getProperty(labelKey);

                    if (Util.IsNotEmpty(labelValue)) {
                        setField(labelKey.substring(3), labelValue);
                    }

                }

            }

        } catch (Throwable ex) {
            AppCrash ac = new AppCrash();
            ac.logContext(this.getClass().getName(), "error reading language properties file " + fileName);

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * valore di un campo della map dell'utente
     * 
     * @param String fieldName
     * @return String
     */
    public String getField(String fieldName) {

        String fieldValue = (String) _sessionMap.get(fieldName);

        if (fieldValue == null) {
            return "";
        }

        return fieldValue;
    }

    public void setField(String key, Object value) {

        _sessionMap.put(key, value);

    }

    public String getSelectedPathTag(MenuItem currentMenuItem) {

        String path = "0";
        if (Util.IsNotEmpty(currentMenuItem.getPath())) {
            path = currentMenuItem.getPath().substring(0, 1);
        }
        return path;
    }
}
