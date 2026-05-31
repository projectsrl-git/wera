package net.projectsrl.wera.importazioni.core;

public class UtilsCampoDB {
    public String nome;
    public String tipo; // es. "STRING", "INTEGER"
    public int maxLength;
    public int precisione;      // per DECIMAL
    public int scala;           // per DECIMAL

    
    public UtilsCampoDB(String nome, String tipo) {
        this.nome = nome;
        this.tipo = tipo;
        this.maxLength = 0; // 0 = nessun limite
    }
    
    public UtilsCampoDB(String nome, String tipo, int maxLength) {
        this.nome = nome;
        this.tipo = tipo;
        this.maxLength = maxLength;
    }
    
    // Costruttore per DECIMAL
    public UtilsCampoDB(String nome, String tipo, int precisione, int scala) {
        this.nome = nome;
        this.tipo = tipo;
        this.precisione = precisione;
        this.scala = scala;
    }
}