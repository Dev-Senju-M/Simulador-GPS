package edu.uvg.gps.algoritmos;

public class ResultadoDijkstra {
    private String[] camino;
    private int totalPasos;
    private double costoTotal;
    private boolean esTiempo;
    private PeriodoHorario periodo;

    public ResultadoDijkstra(String[] camino, int totalPasos, double costoTotal, boolean esTiempo) {
        this.camino = camino;
        this.totalPasos = totalPasos;
        this.costoTotal = costoTotal;
        this.esTiempo = esTiempo;
        this.periodo = null;
    }

    public ResultadoDijkstra(String[] camino, int totalPasos, double costoTotal, PeriodoHorario periodo) {
        this.camino = camino;
        this.totalPasos = totalPasos;
        this.costoTotal = costoTotal;
        this.esTiempo = true;
        this.periodo = periodo;
    }

    public String[] getCamino() { return camino; }
    public int getTotalPasos() { return totalPasos; }
    public double getCostoTotal() { return costoTotal; }
    public boolean isEsTiempo() { return esTiempo; }
    public PeriodoHorario getPeriodo() { return periodo; }
}
