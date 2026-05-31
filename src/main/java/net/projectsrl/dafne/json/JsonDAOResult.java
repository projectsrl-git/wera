
package net.projectsrl.dafne.json;

import java.util.Iterator;

import com.fasterxml.jackson.core.JsonGenerator;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.projectsrl.db.PjNDAO_base;

public class JsonDAOResult extends JsonResult {

    private PjNDAO_base _dao;

    public JsonDAOResult(boolean result, String title, String message, PjNDAO_base dao) {

        super(result, title, message);

        try {

            ErrDetector.GetInstance().postCond(dao != null, "dao is null");
            _dao = dao;

        } catch (Throwable e) {
            new AppCrash(e);
        }
    }

    @Override
    protected void writeAdditionalObjects(JsonGenerator jsonGenerator) throws AppCrash {

        try {
            jsonGenerator.writeObjectFieldStart("dao");
            Iterator<?> daoFields = _dao.iterator();

            while (daoFields != null && daoFields.hasNext()) {
                String fieldName = (String) daoFields.next();
                Object fieldValue = _dao.getAttribute(fieldName);
                acceptFieldValue(fieldName, fieldValue);
            }

            jsonGenerator.writeEndObject(); // closing dao
        } catch (Throwable e) {
            AppCrash ac=new AppCrash(e);
            ac.logContext(this.getClass().getName(), "dao:"+_dao);
        }
    }

   
    @Override
    public String toString() {

        return super.toString() + " - JsonDAOResult [_dao=" + _dao + "]";
    }

}
