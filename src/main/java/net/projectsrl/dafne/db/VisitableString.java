
package net.projectsrl.dafne.db;

import net.project.errors.AppCrash;

public class VisitableString implements VisitableDAOField_itf {

    private String _value;

    public VisitableString(String fieldValue) {
        _value = fieldValue;
    }

    @Override
    public void accept(String fieldName, DAOVisitor_itf visitor)  throws AppCrash {

        visitor.visitString(fieldName, _value);
    }
}
