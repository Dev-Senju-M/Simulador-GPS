package edu.uvg.gps.dto;

public class ResultadoRutaDTO {
    private String[] camino;
    private int totalPasos;
    private double distanciaTotal;
    private double tiempoTotal;
    private String algoritmoUsado;
    private String optimizadoPor;

    public ResultadoRutaDTO(String[] camino, int totalPasos, double distanciaTotal, double tiempoTotal, String algoritmoUsado, String optimizadoPor) {
        this.camino = camino;
        this.totalPasos = totalPasos;
        this.distanciaTotal = distanciaTotal;
        this.tiempoTotal = tiempoTotal;
        this.algoritmoUsado = algoritmoUsado;
        this.optimizadoPor = optimizadoPor;
    }

    public String[] getCamino() { return camino; }
    public int getTotalPasos() { return totalPasos; }
    public double getDistanciaTotal() { return distanciaTotal; }
    public double getTiempoTotal() { return tiempoTotal; }
    public String getAlgoritmoUsado() { return algoritmoUsado; }
    public String getOptimizadoPor() { return optimizadoPor; }

    public String getPaso(int index) {
        if (index < 0 || index >= totalPasos) return null;
        return camino[index];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Algoritmo  : ").append(algoritmoUsado).append("\n");
        sb.append("Optimizado : ").append(optimizadoPor).append("\n");
        sb.append("Distancia  : ").append(String.format("%.2f", distanciaTotal)).append(" km\n");
        sb.append("Tiempo     : ").append(String.format("%.1f", tiempoTotal)).append(" min\n");
        sb.append("Recorrido  : \n");
        for (int i = 0; i < totalPasos; i++) {
            sb.append("  Paso ").append(i + 1).append(": ").append(camino[i]).append("\n");
        }
        return sb.toString();
    }
}