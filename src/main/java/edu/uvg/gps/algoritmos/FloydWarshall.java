package edu.uvg.gps.algoritmos;

import edu.uvg.gps.grafo.Grafo;
import edu.uvg.gps.model.Ciudad;
import edu.uvg.gps.model.NodoAdyacencia;
import java.time.LocalTime;

public class FloydWarshall {

    private Grafo grafo;
    private double[][] matrizDistancias;
    private double[][] matrizTiempos;
    private int[][] siguiente;
    private String[] nodos;
    private int total;
    private PeriodoHorario ultimoPeriodo;

    public FloydWarshall(Grafo grafo) {
        this.grafo = grafo;
    }

    // Calcular sin trafico — usa tiempoBase
    public void calcular() {
        calcularConTiempo(LocalTime.of(10, 0)); // DIA — sin pico
    }

    // Calcular con trafico segun hora
    public void calcularConTiempo(LocalTime horaSalida) {
        ultimoPeriodo = PeriodoHorario.desdeHora(horaSalida);
        int indicePeriodo = ultimoPeriodo.ordinal();

        nodos = grafo.obtenerNombresNodos();
        total = nodos.length;

        matrizDistancias = new double[total][total];
        matrizTiempos    = new double[total][total];
        siguiente        = new int[total][total];

        // Inicializar matrices
        for (int i = 0; i < total; i++) {
            for (int j = 0; j < total; j++) {
                if (i == j) {
                    matrizDistancias[i][j] = 0;
                    matrizTiempos[i][j]    = 0;
                } else {
                    matrizDistancias[i][j] = Double.MAX_VALUE / 2;
                    matrizTiempos[i][j]    = Double.MAX_VALUE / 2;
                }
                siguiente[i][j] = -1;
            }
        }

        // Llenar con aristas existentes
        for (int i = 0; i < total; i++) {
            NodoAdyacencia ady = grafo.obtenerAdyacencias(nodos[i]);
            while (ady != null) {
                int j = buscarIndice(ady.getCiudadDestino());
                if (j != -1) {
                    matrizDistancias[i][j] = ady.getDistancia();

                    // Tiempo efectivo segun periodo + demora de congestion del nodo destino
                    Ciudad ciudadDestino = grafo.obtenerCiudad(ady.getCiudadDestino());
                    double demora = (ciudadDestino != null) ? ciudadDestino.getDemoraCongestión(indicePeriodo) : 0;
                    matrizTiempos[i][j] = ady.getTiempoEfectivo(indicePeriodo) + demora;

                    siguiente[i][j] = j;
                }
                ady = ady.getSiguiente();
            }
        }

        // Algoritmo Floyd-Warshall
        for (int k = 0; k < total; k++) {
            for (int i = 0; i < total; i++) {
                for (int j = 0; j < total; j++) {
                    if (matrizDistancias[i][k] + matrizDistancias[k][j] < matrizDistancias[i][j]) {
                        matrizDistancias[i][j] = matrizDistancias[i][k] + matrizDistancias[k][j];
                        matrizTiempos[i][j]    = matrizTiempos[i][k]    + matrizTiempos[k][j];
                        siguiente[i][j]        = siguiente[i][k];
                    }
                }
            }
        }

        System.out.println("Floyd-Warshall calculado para " + total + " nodos | Periodo: " + ultimoPeriodo.etiqueta());
    }

    // Obtiene camino entre dos ciudades
    public String[] obtenerCamino(String origen, String destino) {
        int i = buscarIndice(origen);
        int j = buscarIndice(destino);
        if (i == -1 || j == -1 || siguiente[i][j] == -1) return null;

        String[] camino = new String[total];
        int totalPasos = 0;
        camino[totalPasos++] = nodos[i];
        while (i != j) {
            i = siguiente[i][j];
            camino[totalPasos++] = nodos[i];
        }
        return camino;
    }

    // Retorna ResultadoDijkstra para compatibilidad con la UI
    public ResultadoDijkstra obtenerResultado(String origen, String destino) {
        String[] camino = obtenerCamino(origen, destino);
        if (camino == null) return null;

        int totalPasos = 0;
        while (totalPasos < camino.length && camino[totalPasos] != null) totalPasos++;

        int i = buscarIndice(origen);
        int j = buscarIndice(destino);
        double costo = matrizTiempos[i][j];

        return new ResultadoDijkstra(camino, totalPasos, costo, ultimoPeriodo);
    }

    public void mostrarRuta(String origen, String destino) {
        int i = buscarIndice(origen);
        int j = buscarIndice(destino);
        if (i == -1 || j == -1) { System.out.println("Ciudad no encontrada."); return; }

        String[] camino = obtenerCamino(origen, destino);
        if (camino == null) { System.out.println("No existe camino entre " + origen + " y " + destino); return; }

        System.out.println("\n===== FLOYD-WARSHALL =====");
        System.out.println("Origen   : " + origen);
        System.out.println("Destino  : " + destino);
        System.out.println("Periodo  : " + (ultimoPeriodo != null ? ultimoPeriodo.etiqueta() : "N/A"));
        System.out.println("--------------------------");
        int paso = 1;
        for (String ciudad : camino) {
            if (ciudad != null) System.out.println("Paso " + paso++ + ": " + ciudad);
        }
        System.out.println("--------------------------");
        System.out.println("Distancia total: " + String.format("%.2f", matrizDistancias[buscarIndice(origen)][buscarIndice(destino)]) + " km");
        System.out.println("Tiempo total   : " + String.format("%.1f", matrizTiempos[buscarIndice(origen)][buscarIndice(destino)]) + " min");
        System.out.println("==========================\n");
    }

    public void mostrarMatrizDistancias() {
        System.out.println("\n===== MATRIZ DE DISTANCIAS =====");
        System.out.printf("%-25s", "");
        for (String nodo : nodos) System.out.printf("%-12s", nodo != null ? nodo.substring(0, Math.min(10, nodo.length())) : "");
        System.out.println();
        for (int i = 0; i < total; i++) {
            System.out.printf("%-25s", nodos[i] != null ? nodos[i].substring(0, Math.min(23, nodos[i].length())) : "");
            for (int j = 0; j < total; j++) {
                if (matrizDistancias[i][j] >= Double.MAX_VALUE / 2)
                    System.out.printf("%-12s", "INF");
                else
                    System.out.printf("%-12s", String.format("%.1f", matrizDistancias[i][j]));
            }
            System.out.println();
        }
        System.out.println("================================\n");
    }

    public double getDistancia(String origen, String destino) {
        int i = buscarIndice(origen);
        int j = buscarIndice(destino);
        if (i == -1 || j == -1) return -1;
        return matrizDistancias[i][j];
    }

    public double getTiempo(String origen, String destino) {
        int i = buscarIndice(origen);
        int j = buscarIndice(destino);
        if (i == -1 || j == -1) return -1;
        return matrizTiempos[i][j];
    }
    public PeriodoHorario getUltimoPeriodo() { return ultimoPeriodo; }
    private int buscarIndice(String nombre) {
        for (int i = 0; i < total; i++) {
            if (nodos[i] != null && nodos[i].equalsIgnoreCase(nombre)) return i;
        }
        return -1;
    }
}