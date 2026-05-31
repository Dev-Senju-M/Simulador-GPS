package edu.uvg.gps.ui;

import javafx.beans.property.SimpleStringProperty;

public class CiudadFila {
    private final SimpleStringProperty id;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty lat;
    private final SimpleStringProperty lon;
    private final SimpleStringProperty alt;
    private final SimpleStringProperty tipo;

    public CiudadFila(String id, String nombre, String lat,
                      String lon, String alt, String tipo) {
        this.id     = new SimpleStringProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.lat    = new SimpleStringProperty(lat);
        this.lon    = new SimpleStringProperty(lon);
        this.alt    = new SimpleStringProperty(alt);
        this.tipo   = new SimpleStringProperty(tipo);
    }

    public SimpleStringProperty idProperty()     { return id; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty latProperty()    { return lat; }
    public SimpleStringProperty lonProperty()    { return lon; }
    public SimpleStringProperty altProperty()    { return alt; }
    public SimpleStringProperty tipoProperty()   { return tipo; }
}