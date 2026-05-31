
package net.projectsrl.dafne.db;

import net.project.errors.AppCrash;

public interface VisitableDAOField_itf {

    public void accept(String fieldName, DAOVisitor_itf visitor) throws AppCrash;
}