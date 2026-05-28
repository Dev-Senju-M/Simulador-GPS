package edu.uvg.gps.dto;

public class RutaDTO {
    private int idOrigen;
    private String nombreOrigen;
    private int idDestino;
    private String nombreDestino;
    private double distancia;
    private double tiempo;

    public RutaDTO(int idOrigen, String nombreOrigen, int idDestino,
                   String nombreDestino, double distancia, double tiempo) {
        this.idOrigen = idOrigen;
        this.nombreOrigen = nombreOrigen;
        this.idDestino = idDestino;
        this.nombreDestino = nombreDestino;
        this.distancia = distancia;
        this.tiempo = tiempo;
    }

    public int getIdOrigen() { return idOrigen; }
    public String getNombreOrigen() { return nombreOrigen; }
    public int getIdDestino() { return idDestino; }
    public String getNombreDestino() { return nombreDestino; }
    public double getDistancia() { return distancia; }
    public double getTiempo() { return tiempo; }

    @Override
    public String toString() {
        return "[" + idOrigen + "] " + nombreOrigen + " -> " +
                "[" + idDestino + "] " + nombreDestino +
                " | " + String.format("%.2f", distancia) + " km" +
                " | " + String.format("%.1f", tiempo) + " min";
    }
}