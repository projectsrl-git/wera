/*
  Copyright (c) by SSB Spa Societa' per i Servizi Bancari

  Note:

 */

package net.projectsrl.wera.importazioni.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.FunctionProjectWebApp_base;
import net.projectsrl.webapp.core.WebAppConstants_itf;


public class FunctionRiepilogoDatiRilevatori extends FunctionProjectWebApp_base {

    public FunctionRiepilogoDatiRilevatori(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }
    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);
        
        if (req.getSession(false).getAttribute("MSG")!=null){
        	templateData.put("MSG", req.getSession(false).getAttribute("MSG"));
        }
        
        if ((req.getSession(false).getAttribute("DATI_CONDOMINIO")!=null)  && (templateData.get("DATI_CONDOMINIO")==null)) {
        	templateData.put("DATI_CONDOMINIO", req.getSession(false).getAttribute("DATI_CONDOMINIO"));
        }
        
        if ((req.getSession(false).getAttribute("FILE_NAME_CEDOLINO")!=null) && (templateData.get("NOME_FILE")==null)){
        	templateData.put("NOME_FILE", req.getSession(false).getAttribute("FILE_NAME_CEDOLINO").toString().toLowerCase());
        }
        
        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
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
            String label = "Scarico rilevatori";
            String function = "ImportDatiRilevatori";
            String link = "astro?FUNCTIONID=ImportDatiRilevatori";
            int itemLevel = 2;
            String path = "1520";
            String linkChain = "astro?FUNCTIONID=Home;#;astro?FUNCTIONID=ImportDatiRilevatori";
            boolean readOnly = false;
            int nrOfChildren = 0;
            String icon = "";

            pathDescri = "Home / Importazione dati / Riepilogo importazione scarico dati rilevatori";

            MenuItem selectedMenuItem = new MenuItem(menuId, menuIdSup, languageISO, label, function, link, itemLevel,
                    path, pathDescri, linkChain, readOnly, nrOfChildren, icon);

            map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, selectedMenuItem);

            map.put(WebAppConstants_itf.INCLUDED_MENU, "include/included_menu.include");
            map.put(WebAppConstants_itf.SELECTED_PATH, path);

        }

        return pathDescri;

    }
    
}
