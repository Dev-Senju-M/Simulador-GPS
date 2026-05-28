package edu.uvg.gps.estructuras;

public class NodoHeap {
    private String ciudad;
    private double distancia;

    public NodoHeap(String ciudad, double distancia) {
        this.ciudad = ciudad;
        this.distancia = distancia;
    }

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
}
