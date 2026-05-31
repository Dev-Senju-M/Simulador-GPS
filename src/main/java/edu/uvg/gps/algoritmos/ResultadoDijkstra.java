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

    public String getPaso(int index) {
        if (index < 0 || index >= totalPasos) return null;
        return camino[index];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Periodo    : ").append(periodo != null ? periodo.etiqueta() : "N/A").append("\n");
        sb.append("Costo total: ").append(String.format("%.1f", costoTotal))
                .append(esTiempo ? " min" : " km").append("\n");
        sb.append("Recorrido  :\n");
        for (int i = 0; i < totalPasos; i++) {
            sb.append("  Paso ").append(i + 1).append(": ").append(camino[i]).append("\n");
        }
        return sb.toString();
    }
}