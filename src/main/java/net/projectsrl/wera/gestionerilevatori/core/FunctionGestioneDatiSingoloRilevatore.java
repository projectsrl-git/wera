
package net.projectsrl.wera.gestionerilevatori.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.qhse.moduli.core.FunctionInserimentoWera_base;
import net.projectsrl.webapp.authentication.MenuItem;
import net.projectsrl.webapp.core.WebAppConstants_itf;
import net.projectsrl.wera.importazioni.db.DatiRilevatoriDAO;

public class FunctionGestioneDatiSingoloRilevatore extends FunctionInserimentoWera_base<DatiRilevatoriDAO> {

    public FunctionGestioneDatiSingoloRilevatore(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(DatiRilevatoriDAO.ID_MODULO, "");
            templateData.put(DatiRilevatoriDAO.ID_UTENTE_INS, getSpecificUserInfo(userInfo).getIdUtente());

        } else {
            PjNDAO_base rowToUpdate = new DatiRilevatoriDAO();
            String idRow = req.getField(DatiRilevatoriDAO.ID_MODULO);
            rowToUpdate.setAttribute(DatiRilevatoriDAO.ID_MODULO, idRow);
            ErrDetector.GetInstance().preCond(rowToUpdate.retrieve(), DatiRilevatoriDAO.ID_MODULO + " not found");
            rowToUpdate.setMapFromAttributes(templateData);
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
            String menuIdSup = "70";
            String languageISO = "it";
            String label = "Gestione dati rilevatori";
            String function = "GestioneDatiSingoloRilevatore";
            String link = "astro?FUNCTIONID=GestioneDatiSingoloRilevatore";
            int itemLevel = 2;
            String path = "1520";
            String linkChain = "astro?FUNCTIONID=Home;#;astro?FUNCTIONID=GestioneDatiSingoloRilevatore";
            boolean readOnly = false;
            int nrOfChildren = 0;
            String icon = "";

            pathDescri = "Home / Gestione dati rilevatori / Gestione Dati Singolo Rilevatore";

            MenuItem selectedMenuItem = new MenuItem(menuId, menuIdSup, languageISO, label, function, link, itemLevel,
                    path, pathDescri, linkChain, readOnly, nrOfChildren, icon);

            map.put(WebAppConstants_itf.SELECTED_MENU_ITEM, selectedMenuItem);

            map.put(WebAppConstants_itf.INCLUDED_MENU, "include/included_menu.include");
            map.put(WebAppConstants_itf.SELECTED_PATH, path);

        }

        return pathDescri;

    }
    
}
