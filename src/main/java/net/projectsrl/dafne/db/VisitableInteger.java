package net.projectsrl.dafne.db;

import net.project.errors.AppCrash;

public class VisitableInteger implements VisitableDAOField_itf {

    private Integer _value;

    public VisitableInteger(Integer fieldValue) {
        _value = fieldValue;
    }


    @Override
    public void accept(String fieldName, DAOVisitor_itf visitor)  throws AppCrash {

        visitor.visitInteger(fieldName,_value);
        
    }

}
