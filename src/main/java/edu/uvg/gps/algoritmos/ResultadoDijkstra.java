package edu.uvg.gps.algoritmos;

public class ResultadoDijkstra {
    private String[] camino;
    private int totalPasos;
    private double costoTotal;
    private boolean esTiempo;

    public ResultadoDijkstra(String[] camino, int totalPasos, double costoTotal, boolean esTiempo) {
        this.camino = camino;
        this.totalPasos = totalPasos;
        this.costoTotal = costoTotal;
        this.esTiempo = esTiempo;
    }

    public String[] getCamino() { return camino; }
    public int getTotalPasos() { return totalPasos; }
    public double getCostoTotal() { return costoTotal; }
    public boolean isEsTiempo() { return esTiempo; }
}