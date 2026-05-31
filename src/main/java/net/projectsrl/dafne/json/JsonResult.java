
package net.projectsrl.dafne.json;

import com.fasterxml.jackson.core.JsonGenerator;

import net.project.errors.AppCrash;
import net.project.errors.ErrDetector;
import net.projectsrl.dafne.db.DAOVisitor_itf;
import net.projectsrl.dafne.db.VisitableDAOField_itf;

public class JsonResult extends JsonString_base implements DAOVisitor_itf {

    private boolean _result;
    private String  _title;
    private String  _message;

    public JsonResult(boolean result, String title, String message) {

        super();

        try {

            ErrDetector.GetInstance().postCond(title != null, "title is null");
            ErrDetector.GetInstance().postCond(message != null, "message is null");
            _result = result;
            _title = title;
            _message = message;

        } catch (Throwable e) {
            new AppCrash(e);
        }
    }

    @Override
    protected void addRootObject(JsonGenerator jsonGenerator) throws AppCrash {

        try {

            jsonGenerator.writeBooleanField("result", _result);
            jsonGenerator.writeStringField("title", _title);
            jsonGenerator.writeStringField("message", _message);

        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), this.toString());
        }

    }

    @Override
    public String toString() {

        return "JsonResult [_result=" + _result + ", _title=" + _title + ", _message=" + _message + "]";
    }

    @Override
    protected void writeAdditionalObjects(JsonGenerator jsonGenerator) throws AppCrash {

        // TODO Auto-generated method stub

    }

    protected void acceptFieldValue(String fieldName, Object fieldValue) {

        if (fieldValue == null) {
            return;
        }

        try {
            String typeName = fieldValue.getClass().getName();
            typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
            Object instance = Class.forName("net.projectsrl.dafne.db.Visitable" + typeName)
                    .getConstructor(fieldValue.getClass()).newInstance(fieldValue);

            if (instance instanceof VisitableDAOField_itf) ((VisitableDAOField_itf) instance).accept(fieldName, this);

        } catch (Throwable th) {
            AppCrash ac = new AppCrash(th);
            ac.logContext(this.getClass().getName(), "fieldName:" + fieldName + " - fieldValue:" + fieldValue);

        }
    }

    @Override
    public void visitString(String fieldName, String fieldValue) throws AppCrash {

        try {
            getJsonGenerator().writeStringField(fieldName, (String) fieldValue);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "fieldName:" + fieldName + " fieldValue:" + fieldValue);
        }

    }

    @Override
    public void visitFloat(String fieldName, Float fieldValue) throws AppCrash {

        try {
            getJsonGenerator().writeNumberField(fieldName, (Float) fieldValue);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "fieldName:" + fieldName + " fieldValue:" + fieldValue);
        }

    }

    @Override
    public void visitInteger(String fieldName, Integer fieldValue) throws AppCrash {

        try {
            getJsonGenerator().writeNumberField(fieldName, (Integer) fieldValue);
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "fieldName:" + fieldName + " fieldValue:" + fieldValue);
        }

    }

    @Override
    public void visitDefault(String fieldName, Object fieldValue) throws AppCrash {

        try {
            getJsonGenerator().writeStringField(fieldName, fieldValue.toString());
        } catch (Throwable e) {
            AppCrash ac = new AppCrash(e);
            ac.logContext(this.getClass().getName(), "fieldName:" + fieldName + " fieldValue:" + fieldValue);
        }

    }

}
