package edu.uvg.gps.ui;

import javafx.beans.property.SimpleStringProperty;

public class ComparacionFila {
    private final SimpleStringProperty ruta;
    private final SimpleStringProperty km;
    private final SimpleStringProperty tiempoBase;
    private final SimpleStringProperty tiempoActual;
    private final SimpleStringProperty estado;

    public ComparacionFila(String ruta, String km, String tiempoBase, String tiempoActual, String estado) {
        this.ruta = new SimpleStringProperty(ruta);
        this.km = new SimpleStringProperty(km);
        this.tiempoBase = new SimpleStringProperty(tiempoBase);
        this.tiempoActual = new SimpleStringProperty(tiempoActual);
        this.estado = new SimpleStringProperty(estado);
    }

    public SimpleStringProperty rutaProperty() {
        return ruta;
    }
    public SimpleStringProperty kmProperty() {
        return km;
    }
    public SimpleStringProperty tiempoBaseProperty() {
        return tiempoBase;
    }
    public SimpleStringProperty tiempoActualProperty() {
        return tiempoActual;
    }
    public SimpleStringProperty estadoProperty() {
        return estado;
    }
}