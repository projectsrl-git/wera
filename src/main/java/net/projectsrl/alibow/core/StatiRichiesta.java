
package net.projectsrl.alibow.core;

public enum StatiRichiesta {

    DRAFT("DRA"), WAITNG_FOR_FIRST_APPROVAL("WAI"),  APPROVED("APP"), COMPLETED("END");

    private String code;

    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    private StatiRichiesta(String code) {
        this.code = code;
    }

}