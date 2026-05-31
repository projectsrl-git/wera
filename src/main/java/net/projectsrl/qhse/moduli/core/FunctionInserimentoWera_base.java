
package net.projectsrl.qhse.moduli.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import net.project.errors.AppCrash;
import net.project.misc.Base64;
import net.project.misc.Util;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.alibow.core.Constants_itf;
import net.projectsrl.webapp.core.FunctionAjaxForm_base;
import net.projectsrl.webapp.core.ServletApplication;
import net.projectsrl.wera.anagrafiche.db.CondominiDAO;
import net.projectsrl.wera.base.db.WeraDAO_base;
import net.projectsrl.wm.utils.Utils;

public abstract class FunctionInserimentoWera_base<D extends WeraDAO_base> extends FunctionAjaxForm_base<D> {

    public FunctionInserimentoWera_base(ApplicationServices_itf applServices, String functionID,
            String functionName) {

        super(applServices, functionID, functionName);
    }

    @Override
    protected boolean isAnInsert(SsbServletRequest req) {

        return Util.IsEmpty(req.getField(CondominiDAO.ID_MODULO));
    }

    @Override
    protected void sendResponseJSON(SsbServletResponse res, boolean result, String title, String message, D formDao) {

        try {
            PrintWriter out = res.getWriter();

            Integer id = (Integer) formDao.getAttribute(WeraDAO_base.ID_MODULO);
            String nr = "";
            String stato = (String) formDao.getAttribute(WeraDAO_base.STATO);
            Integer idAzienda = 0;
            if (formDao.getAttribute(WeraDAO_base.ID_AZIENDA)!=null){
            	idAzienda = Integer.parseInt(formDao.getAttribute(WeraDAO_base.ID_AZIENDA).toString());
            }
            
            
            String resultString = "{\"result\":" + result + ",\"title\":'" + title + "',\"message\":'" + message
                    + "',\"id\":" + id + ",\"azienda\":" + idAzienda + ",\"nr\":'" + nr + "',\"stato\":'" + stato + "'}";
            out.println(resultString);
            out.close();

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "errore writing succesful response");
        }
    }



    @Override
    protected void update(SsbServletRequest req, D formDao, UserSecurityInfo userInfo) throws AppCrash {

        String idModulo = req.getField(WeraDAO_base.ID_MODULO);
        String stato = req.getField(WeraDAO_base.STATO);

        formDao.setAttribute(WeraDAO_base.ID_MODULO, idModulo);

        formDao.retrieve();

       
        if (stato == null || stato.equals("")) {
            stato = (String) formDao.getAttribute(WeraDAO_base.STATO);
        }

        formDao.setAttributesFromRequest(req);

        formDao.setAttribute(WeraDAO_base.STATO, stato);

        formDao.update();
    }

    


    protected String creaFirma(String fileName, String signFromPage) throws AppCrash {
    
        String imageCode = signFromPage;
    
        String[] imageString = imageCode.split(",");
        // tokenize the data
        
        if (imageString.length<2) {
            return null;
        }
    
        // create a buffered image
        BufferedImage image = new BufferedImage(350, 200, BufferedImage.TYPE_BYTE_GRAY);
        byte[] imageByte = null;
    
        File outputfile = null;
    
        String signImageFileName = null;
    
        try {
            imageByte = Base64.decode(imageString[1]);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
    
            // write the image to a file
            outputfile = new File(_applicationSrv.getRoot() + "/Output/" + fileName);
            ImageIO.write(image, "png", outputfile);
            signImageFileName = outputfile.toString();
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "error creating digital sign - signImageFileName:"+signImageFileName+ " - imageCode:"+imageCode);
        }
    
        return signImageFileName;
    
    }

    @Override
    protected void onSuccess(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo, D formDao)
            throws AppCrash {
        
        String workFlowAction = req.getField(Constants_itf.WORKFLOW_ACTION);
        req.setField(Constants_itf.MODULO, formDao.getTableName());
        
        if (Utils.IsNotEmpty(workFlowAction)) {
            ((ServletApplication) _applicationSrv).invokeProcessPost("ApprovalWorkFlow", req, res);
        }
        
    }
    
}