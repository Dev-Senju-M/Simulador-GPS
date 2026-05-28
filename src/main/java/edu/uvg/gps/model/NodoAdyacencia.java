package edu.uvg.gps.model;

public class NodoAdyacencia {
    private String ciudadDestino;
    private double distancia;
    private double tiempo;
    private NodoAdyacencia siguiente;

    public NodoAdyacencia(String ciudadDestino, double distancia, double tiempo) {
        this.ciudadDestino = ciudadDestino;
        this.distancia = distancia;
        this.tiempo = tiempo;
        this.siguiente = null;
    }

    public String getCiudadDestino() { return ciudadDestino; }
    public void setCiudadDestino(String ciudadDestino) { this.ciudadDestino = ciudadDestino; }

    public double getDistancia() { return distancia; }
    public void setDistancia(double distancia) { this.distancia = distancia; }

    public double getTiempo() { return tiempo; }
    public void setTiempo(double tiempo) { this.tiempo = tiempo; }

    public NodoAdyacencia getSiguiente() { return siguiente; }
    public void setSiguiente(NodoAdyacencia siguiente) { this.siguiente = siguiente; }

    @Override
    public String toString() {
        return "-> " + ciudadDestino +
                " (" + String.format("%.2f", distancia) + " km" +
                " | " + String.format("%.1f", tiempo) + " min)";
    }
}