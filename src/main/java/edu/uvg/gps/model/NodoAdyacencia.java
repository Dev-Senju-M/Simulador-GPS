package edu.uvg.gps.model;

public class NodoAdyacencia {
    private String ciudadDestino;
    private double distancia;
    private double tiempoBase;
    private double[] multiplicadores;
    private NodoAdyacencia siguiente;

    public NodoAdyacencia(String ciudadDestino, double distancia, double tiempo) {
        this.ciudadDestino = ciudadDestino;
        this.distancia = distancia;
        this.tiempoBase = tiempo;
        this.multiplicadores = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
        this.siguiente = null;
    }

    public NodoAdyacencia(String ciudadDestino, double distancia, double tiempoBase, double[] multiplicadores) {
        this.ciudadDestino = ciudadDestino;
        this.distancia = distancia;
        this.tiempoBase = tiempoBase;
        this.multiplicadores = multiplicadores;
        this.siguiente = null;
    }

    public double getTiempoEfectivo(int indicePeriodo) {
        return tiempoBase * multiplicadores[indicePeriodo];
    }

    public String getCiudadDestino() { return ciudadDestino; }
    public void setCiudadDestino(String ciudadDestino) { this.ciudadDestino = ciudadDestino; }

    public double getDistancia() { return distancia; }
    public void setDistancia(double distancia) { this.distancia = distancia; }

    public double getTiempo() { return tiempoBase; }
    public void setTiempo(double tiempo) { this.tiempoBase = tiempo; }

    public double getTiempoBase() { return tiempoBase; }
    public double[] getMultiplicadores() { return multiplicadores; }

    public NodoAdyacencia getSiguiente() { return siguiente; }
    public void setSiguiente(NodoAdyacencia siguiente) { this.siguiente = siguiente; }

    @Override
    public String toString() {
        return "-> " + ciudadDestino +
                " (" + String.format("%.2f", distancia) + " km" +
                " | " + String.format("%.1f", tiempoBase) + " min base)";
    }
}
