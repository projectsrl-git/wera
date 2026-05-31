
package it.project.iride.core;

import java.util.HashMap;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;

/**
 * FunctionMostraPaginaConDataset
 * 
 * descrive il metodo mostra() standard per una pagina con dataset
 * 
 * il nome della pagina (Page.template.name) su file di configurazione deve essere uguale al nome funzione nel
 * function_list.txt
 * 
 */
public class FunctionMostraPaginaConDataset extends FunctionIride_base {

    public FunctionMostraPaginaConDataset() {

        super();
    }

    public FunctionMostraPaginaConDataset(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        HashMap<String, Object> templateData = setTemplateDataFromRequest(setCommonTags(req, userInfo), req);

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        mostra(req, res, userInfo);

    }

    protected String getPageName() {

        return _functionName.toLowerCase();

    }

    @Override
    protected HashMap<String, String> prepareWhereCondition(HashMap<String, String> templateData,
            HashMap<String, String> queryParameter) {

        return templateData;
    }
}
