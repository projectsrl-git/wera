
package net.projectsrl.wera.comunicazioni.core;

import java.util.Map;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.dafne.core.DafneCostanti_itf;
import net.projectsrl.dafne.db.NewsAziendeDAO;
import net.projectsrl.dafne.db.NewsDAO;
import net.projectsrl.dafne.db.NewsProfiliDAO;
import net.projectsrl.db.PjNDAO_base;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;

/**
 * FunctionInserimentoNews
 */
public class FunctionInserimentoNews extends FunctionAjaxForm_base<NewsDAO> {

    public FunctionInserimentoNews(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    public void mostra(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        Map<String, Object> templateData = createMapFromRequest(req, userInfo);

        if (isAnInsert(req)) {
            templateData.put(NewsDAO.ID_NEWS, "");
        } else {
            PjNDAO_base news = new NewsDAO();
            String idNews = req.getField(NewsDAO.ID_NEWS);
            news.setAttribute(NewsDAO.ID_NEWS, idNews);
            ErrDetector.GetInstance().preCond(news.retrieve(), NewsDAO.ID_NEWS + " not found");
            news.setMapFromAttributes(templateData);
            templateData.put(DafneCostanti_itf.PROFILO_MULTIPLO, new NewsProfiliDAO().getSelectedCodeList(idNews));
            templateData.put(DafneCostanti_itf.AZIENDE_MULTIPLE, new NewsAziendeDAO().getSelectedCodeList(idNews));
        }

        _applicationSrv.displayPage(getPageName(), templateData, setPageDatasetParam(getPageName(), req, templateData),
                res);
    }


    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(NewsDAO.ID_NEWS));
    }

}
