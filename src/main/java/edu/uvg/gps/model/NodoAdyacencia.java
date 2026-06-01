package edu.uvg.gps.model;

public class NodoAdyacencia {
    private String ciudadDestino;
    private double distancia;
    private double tiempoBase;
    private double[] multiplicadores;
    private int velocidadMaxima;
    private TipoProblema problemaActual;
    private NodoAdyacencia siguiente;

    public NodoAdyacencia(String ciudadDestino, double distancia, double tiempo) {
        this.ciudadDestino = ciudadDestino;
        this.distancia = distancia;
        this.tiempoBase = tiempo;
        this.multiplicadores = new double[]{1.0, 1.0, 1.0, 1.0, 1.0};
        this.velocidadMaxima = 60;
        this.problemaActual = TipoProblema.NINGUNO;
        this.siguiente = null;
    }

    public NodoAdyacencia(String ciudadDestino, double distancia, double tiempoBase, double[] multiplicadores) {
        this.ciudadDestino = ciudadDestino;
        this.distancia = distancia;
        this.tiempoBase = tiempoBase;
        this.multiplicadores = multiplicadores;
        this.velocidadMaxima = 60;
        this.problemaActual = TipoProblema.NINGUNO;
        this.siguiente = null;
    }

    public NodoAdyacencia(String ciudadDestino, double distancia, double tiempoBase,
                          int velocidadMaxima, double[] multiplicadores) {
        this.ciudadDestino = ciudadDestino;
        this.distancia = distancia;
        this.tiempoBase = tiempoBase;
        this.multiplicadores = multiplicadores;
        this.velocidadMaxima = velocidadMaxima;
        this.problemaActual = TipoProblema.NINGUNO;
        this.siguiente = null;
    }

    public double getTiempoEfectivo(int indicePeriodo) {
        return tiempoBase * multiplicadores[indicePeriodo] * problemaActual.getMultiplicadorExtra();
    }

    public double getTiempoEfectivo(int indicePeriodo, double factorDia) {
        return tiempoBase * multiplicadores[indicePeriodo] * factorDia * problemaActual.getMultiplicadorExtra();
    }

    public void reportarProblema(TipoProblema problema) {
        this.problemaActual = problema;
        System.out.println("Problema en tramo hacia " + ciudadDestino + ": " + problema.getEtiqueta());
    }

    public void limpiarProblema() { this.problemaActual = TipoProblema.NINGUNO; }

    public String getCiudadDestino() { return ciudadDestino; }
    public void setCiudadDestino(String ciudadDestino) { this.ciudadDestino = ciudadDestino; }
    public double getDistancia() { return distancia; }
    public void setDistancia(double distancia) { this.distancia = distancia; }
    public double getTiempo() { return tiempoBase; }
    public void setTiempo(double tiempo) { this.tiempoBase = tiempo; }
    public double getTiempoBase() { return tiempoBase; }
    public double[] getMultiplicadores() { return multiplicadores; }
    public int getVelocidadMaxima() { return velocidadMaxima; }
    public void setVelocidadMaxima(int velocidadMaxima) { this.velocidadMaxima = velocidadMaxima; }
    public TipoProblema getProblemaActual() { return problemaActual; }
    public NodoAdyacencia getSiguiente() { return siguiente; }
    public void setSiguiente(NodoAdyacencia siguiente) { this.siguiente = siguiente; }

    @Override
    public String toString() {
        String problema = problemaActual != TipoProblema.NINGUNO ? " ⚠ " + problemaActual.getEtiqueta() : "";
        return "-> " + ciudadDestino +
                " (" + String.format("%.2f", distancia) + " km" +
                " | " + String.format("%.1f", tiempoBase) + " min base" +
                " | max " + velocidadMaxima + " km/h" + problema + ")";
    }
}