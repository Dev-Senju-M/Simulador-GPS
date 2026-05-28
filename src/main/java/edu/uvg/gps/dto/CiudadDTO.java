package edu.uvg.gps.dto;

public class CiudadDTO {
    private int id;
    private String nombre;
    private double latitud;
    private double longitud;
    private double altitud;
    private String tipo;

    public CiudadDTO(int id, String nombre, double latitud, double longitud, double altitud, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public double getLatitud() {
        return latitud;
    }
    public double getLongitud() {
        return longitud;
    }
    public double getAltitud() {return altitud;
    }
    public String getTipo() {return tipo;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nombre + " (" + tipo + ")";
    }
}