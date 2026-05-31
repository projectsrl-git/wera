
package net.projectsrl.dafne.db;

import net.project.errors.AppCrash;

public interface DAOVisitor_itf {

    public void visitString(String fieldName, String fieldValue) throws AppCrash;

    public void visitFloat(String fieldName, Float fieldValue) throws AppCrash;

    public void visitInteger(String fieldName, Integer fieldValue) throws AppCrash;

    public void visitDefault(String fieldName, Object fieldValue) throws AppCrash;
}
