
package net.projectsrl.alibow.core;

public enum ProfiliUtente {
    AGENZIA("AGE")
    ;

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