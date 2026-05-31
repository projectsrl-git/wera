
package net.projectsrl.dafne.core;

public enum ProfiliUtente {
    SUPER_ADMINISTRATOR("ADM"),
    INSTALLATORE("INS"),
    AMMINISTRATORE_CONDOMINIO("AMM"),
    CONDOMINO("CON");

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