
package net.projectsrl.bow.core;

public enum ProfiliUtente {
    ADMINISTRATOR("ADM"),
    APPROVER("APP"),
    RESPONSABILE("RES");

    private String code;

    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    private ProfiliUtente(String code) {
        this.code = code;
    }
}