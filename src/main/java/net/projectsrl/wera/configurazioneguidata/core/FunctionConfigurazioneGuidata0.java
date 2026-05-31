
package net.projectsrl.wera.configurazioneguidata.core;

import java.util.Map;

import net.project.servlet.frame.ApplicationServices_itf;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.importazioni.core.FunctionImportDatiAnagraficaUtenze;

public class FunctionConfigurazioneGuidata0 extends FunctionImportDatiAnagraficaUtenze {

    public FunctionConfigurazioneGuidata0(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }


    @Override
    protected String getPathDescription(Map<String, Object> map) {

        String pathDescri = "";

        try {
            MenuItem selectedMenuItem = (MenuItem) map.get(WebAppConstants_itf.SELECTED_MENU_ITEM);
            if (selectedMenuItem != null) {
                if (selectedMenuItem.getPathDescri() != null) {
                    pathDescri = selectedMenuItem.getPathDescri();
                }
            }

        } catch (ClassCastException e) {

            String menuId = "11";
            String menuIdSup = "2";
            String languageISO = "it";
            String label = "Anagrafica Utenze";
            String function = "ImportDatiAnagraficaUtenze";
            String link = "astro?FUNCTIONID=ImportDatiAnagraficaUtenze";
            int itemLevel = 2;
            String path = "1520";
            String linkChain = "astro?FUNCTIONID=Home;#;astro?FUNCTIONID=ImportDatiAnagraficaUtenze";
            boolean readOnly = false;
            int nrOfChildren = 0;
            String icon = "";

            pathDescri = "Home / Configurazione guidata / Importazione anagrafica utenze";

            MenuItem selectedMenuItem = new MenuItem(menuId, menuIdSup, languageISO, label, function, link, itemLevel,
                    path, pathDescri, linkChain, readOnly, nrOfChildren, icon);

            map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, selectedMenuItem);

            map.put(WebAppConstants_itf.INCLUDED_MENU, "include/included_menu.include");
            map.put(WebAppConstants_itf.SELECTED_PATH, path);

        }

        return pathDescri;

    }
	

}
