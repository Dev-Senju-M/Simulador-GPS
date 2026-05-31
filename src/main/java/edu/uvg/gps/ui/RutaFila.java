package edu.uvg.gps.ui;

import javafx.beans.property.SimpleStringProperty;

public class RutaFila {
    private final SimpleStringProperty origen;
    private final SimpleStringProperty destino;
    private final SimpleStringProperty distancia;
    private final SimpleStringProperty tiempo;
    private final SimpleStringProperty velocidad;

    public RutaFila(String origen, String destino, String distancia,
                    String tiempo, String velocidad) {
        this.origen    = new SimpleStringProperty(origen);
        this.destino   = new SimpleStringProperty(destino);
        this.distancia = new SimpleStringProperty(distancia);
        this.tiempo    = new SimpleStringProperty(tiempo);
        this.velocidad = new SimpleStringProperty(velocidad);
    }

    public SimpleStringProperty origenProperty()    { return origen; }
    public SimpleStringProperty destinoProperty()   { return destino; }
    public SimpleStringProperty distanciaProperty() { return distancia; }
    public SimpleStringProperty tiempoProperty()    { return tiempo; }
    public SimpleStringProperty velocidadProperty() { return velocidad; }
}