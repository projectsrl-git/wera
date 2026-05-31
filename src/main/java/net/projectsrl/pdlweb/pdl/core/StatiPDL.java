
package net.projectsrl.pdlweb.pdl.core;

public enum StatiPDL {

    APERTO("OPE"), ATTIVO("ACT"), SOSPESO("SUS"), CHIUSO("CLO");

    private String code;

    public String getCode() {

        return code;
    }

    public void setCode(String code) {

        this.code = code;
    }

    private StatiPDL(String code) {

        this.code = code;
    }
}