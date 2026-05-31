
package net.projectsrl.bow.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import net.project.errors.AppCrash;
import net.project.servlet.frame.ApplicationServices_itf;
import net.project.servlet.frame.SsbServletRequest;
import net.project.servlet.frame.SsbServletResponse;
import net.project.servlet.security.UserSecurityInfo;
import net.projectsrl.pdf.CreatePDF;

public abstract class FunctionStampaPDFGeneric_base<P extends CreatePDF> extends FunctionStampaPDF_base {

    public FunctionStampaPDFGeneric_base() {

        super();

    }

    public FunctionStampaPDFGeneric_base(ApplicationServices_itf applServices, String functionID, String functionName) {

        super(applServices, functionID, functionName);

    }

    @Override
    public void elabora(SsbServletRequest req, SsbServletResponse res, UserSecurityInfo userInfo) throws AppCrash {

        String directory = _applicationSrv.getRoot() + OUTPUT_PATH;

        String filename = creaFilePdf(req, userInfo);

        File file = new File(filename);
        String contentType = getContentType(filename);
        System.out.println(contentType);
        res.setContentType(contentType);
        res.setHeader("Content-Disposition", "attachment; filename=" + filename.replace(directory, ""));
        int length = (int) file.length();

        if (length > Integer.MAX_VALUE) {
        }

        byte[] bytes = new byte[length];

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(file);

            fin.read(bytes);

            ServletOutputStream os = res.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (Throwable ac) {
            new AppCrash(ac);
        }

    }

    @SuppressWarnings("unchecked")
    private P makeTypeParameterInstance(Map<String, Object> map) {

        P pdfCreator = null;

        try {
            Type type = getClass().getGenericSuperclass();
            ParameterizedType paramType = (ParameterizedType) type;
            Class<P> typeParameterClass = (Class<P>) paramType.getActualTypeArguments()[0];


            Class[] cArg = new Class[2]; 
            cArg[0] = String.class; //First argument is of *object* type Long
            cArg[1] = Map.class; //Second argument is of *object* type String

            String directory = _applicationSrv.getRoot() + OUTPUT_PATH;
            
            pdfCreator = (P) typeParameterClass.getDeclaredConstructor(cArg).newInstance(directory,map);

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "error instatiating type parameter class");
        }

        return pdfCreator;

    }

    @Override
    protected String creaFilePdf(SsbServletRequest req, UserSecurityInfo userInfo) throws AppCrash {


        String nomeFileCompleto = null;

        Map<String, Object> map = new HashMap<String, Object>();

        setData(req, map);

        P pdfCreator = makeTypeParameterInstance(map);
        nomeFileCompleto = pdfCreator.createPDF();

        return nomeFileCompleto;
    }

    protected abstract void setData(SsbServletRequest req, Map<String, Object> data) throws AppCrash;


}
