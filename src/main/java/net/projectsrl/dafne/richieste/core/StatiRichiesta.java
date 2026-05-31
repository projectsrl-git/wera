
package net.projectsrl.dafne.richieste.core;

public enum StatiRichiesta {

    DRAFT("DRA"), WAITNG_FOR_FIRST_APPROVAL("WAI"), INTERMEDIATE_APPROVAL("INT"), APPROVED("APP"), REJECTED("REJ");

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

    public StatiRichiesta getApproveNextCode() {

        StatiRichiesta nextStatoRichiesta = this;

        if (this.equals(DRAFT)) {
            nextStatoRichiesta = WAITNG_FOR_FIRST_APPROVAL;
        } else if (this.equals(WAITNG_FOR_FIRST_APPROVAL)) {
            nextStatoRichiesta = APPROVED;
        }

        return nextStatoRichiesta;
    }
}