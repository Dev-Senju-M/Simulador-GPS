package edu.uvg.gps.model;

public enum TipoProblema {
    NINGUNO(1.0,"Sin problemas"),
    ACCIDENTE(1.8,"Accidente"),
    TORMENTA(1.6,"Tormenta"),
    CONSTRUCCION(1.4, "Construccion"),
    PARADA_POLICIAL(1.3, "Parada policial"),
    MANIFESTACION(2.0, "Manifestacion");


    private final double multiplicadorExtra;
    private final String etiqueta;

    TipoProblema(double multiplicadorExtra, String etiqueta) {
        this.multiplicadorExtra = multiplicadorExtra;
        this.etiqueta = etiqueta;
    }

    public double getMultiplicadorExtra() { return multiplicadorExtra; }
    public String getEtiqueta() { return etiqueta; }
}