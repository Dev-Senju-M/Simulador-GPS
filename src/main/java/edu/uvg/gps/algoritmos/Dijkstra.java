package edu.uvg.gps.algoritmos;

import edu.uvg.gps.estructuras.ColaPrioridad;
import edu.uvg.gps.estructuras.NodoHeap;
import edu.uvg.gps.estructuras.Pila;
import edu.uvg.gps.grafo.Grafo;
import edu.uvg.gps.model.Ciudad;
import edu.uvg.gps.model.NodoAdyacencia;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class Dijkstra {

    private Grafo grafo;

    public Dijkstra(Grafo grafo) {
        this.grafo = grafo;
    }

    public ResultadoDijkstra calcularConTiempo(String origen, String destino, LocalTime horaSalida) {
        return calcularConTiempo(origen, destino, horaSalida, DayOfWeek.MONDAY);
    }

    public ResultadoDijkstra calcularConTiempo(String origen, String destino,
                                               LocalTime horaSalida, DayOfWeek dia) {
        PeriodoHorario periodo = PeriodoHorario.desdeHora(horaSalida);
        int indicePeriodo = periodo.ordinal();
        double factorDia = PeriodoHorario.factorPorDia(dia);

        String[] nodos = grafo.obtenerNombresNodos();
        int total = nodos.length;
        double[] distancias = new double[total];
        String[] anteriores = new String[total];
        boolean[] visitados = new boolean[total];

        for (int i = 0; i < total; i++) {
            distancias[i] = Double.MAX_VALUE;
            anteriores[i] = null;
            visitados[i] = false;
        }

        int indiceOrigen = buscarIndice(nodos, origen);
        if (indiceOrigen == -1) { System.out.println("Ciudad origen no encontrada: " + origen); return null; }
        distancias[indiceOrigen] = 0;

        ColaPrioridad cola = new ColaPrioridad(total * total);
        cola.insertar(origen, 0);

        while (!cola.estaVacia()) {
            NodoHeap actual = cola.extraerMinimo();
            String ciudadActual = actual.getCiudad();
            int indiceActual = buscarIndice(nodos, ciudadActual);
            if (indiceActual == -1 || visitados[indiceActual]) continue;
            visitados[indiceActual] = true;

            NodoAdyacencia ady = grafo.obtenerAdyacencias(ciudadActual);
            while (ady != null) {
                int indiceVecino = buscarIndice(nodos, ady.getCiudadDestino());
                if (indiceVecino != -1 && !visitados[indiceVecino]) {
                    Ciudad ciudadVecino = grafo.obtenerCiudad(ady.getCiudadDestino());
                    double pesoArista = ady.getTiempoEfectivo(indicePeriodo, factorDia);
                    double demora = (ciudadVecino != null)
                            ? ciudadVecino.getDemoraCongestión(indicePeriodo) * factorDia
                            : 0;
                    double nuevaDist = distancias[indiceActual] + pesoArista + demora;
                    if (nuevaDist < distancias[indiceVecino]) {
                        distancias[indiceVecino] = nuevaDist;
                        anteriores[indiceVecino] = ciudadActual;
                        cola.insertar(ady.getCiudadDestino(), nuevaDist);
                    }
                }
                ady = ady.getSiguiente();
            }
        }

        int indiceDestino = buscarIndice(nodos, destino);
        if (indiceDestino == -1 || distancias[indiceDestino] == Double.MAX_VALUE) {
            System.out.println("No existe camino entre " + origen + " y " + destino);
            return null;
        }

        Pila pila = new Pila();
        String cur = destino;
        while (cur != null) {
            pila.push(cur);
            cur = anteriores[buscarIndice(nodos, cur)];
        }
        String[] camino = new String[total];
        int i = 0;
        while (!pila.estaVacia()) camino[i++] = pila.pop();

        return new ResultadoDijkstra(camino, i, distancias[indiceDestino], periodo);
    }

    public ResultadoDijkstra calcular(String origen, String destino, boolean usarTiempo) {
        String[] nodos = grafo.obtenerNombresNodos();
        int total = nodos.length;
        double[] distancias = new double[total];
        String[] anteriores = new String[total];
        boolean[] visitados = new boolean[total];

        for (int i = 0; i < total; i++) {
            distancias[i] = Double.MAX_VALUE;
            anteriores[i] = null;
            visitados[i] = false;
        }

        int indiceOrigen = buscarIndice(nodos, origen);
        if (indiceOrigen == -1) { System.out.println("Ciudad origen no encontrada: " + origen); return null; }
        distancias[indiceOrigen] = 0;

        ColaPrioridad cola = new ColaPrioridad(total * total);
        cola.insertar(origen, 0);

        while (!cola.estaVacia()) {
            NodoHeap actual = cola.extraerMinimo();
            String ciudadActual = actual.getCiudad();
            int indiceActual = buscarIndice(nodos, ciudadActual);
            if (indiceActual == -1 || visitados[indiceActual]) continue;
            visitados[indiceActual] = true;

            NodoAdyacencia ady = grafo.obtenerAdyacencias(ciudadActual);
            while (ady != null) {
                int indiceVecino = buscarIndice(nodos, ady.getCiudadDestino());
                if (indiceVecino != -1 && !visitados[indiceVecino]) {
                    double peso = usarTiempo ? ady.getTiempo() : ady.getDistancia();
                    double nuevaDist = distancias[indiceActual] + peso;
                    if (nuevaDist < distancias[indiceVecino]) {
                        distancias[indiceVecino] = nuevaDist;
                        anteriores[indiceVecino] = ciudadActual;
                        cola.insertar(ady.getCiudadDestino(), nuevaDist);
                    }
                }
                ady = ady.getSiguiente();
            }
        }

        int indiceDestino = buscarIndice(nodos, destino);
        if (indiceDestino == -1 || distancias[indiceDestino] == Double.MAX_VALUE) {
            System.out.println("No existe camino entre " + origen + " y " + destino);
            return null;
        }

        Pila pila = new Pila();
        String cur = destino;
        while (cur != null) { pila.push(cur); cur = anteriores[buscarIndice(nodos, cur)]; }
        String[] camino = new String[total];
        int i = 0;
        while (!pila.estaVacia()) camino[i++] = pila.pop();
        return new ResultadoDijkstra(camino, i, distancias[indiceDestino], usarTiempo);
    }

    public void mostrarRuta(String origen, String destino, boolean usarTiempo) {
        ResultadoDijkstra r = calcular(origen, destino, usarTiempo);
        if (r == null) return;
        System.out.println("\n===== RUTA MAS CORTA =====");
        System.out.println("Origen  : " + origen);
        System.out.println("Destino : " + destino);
        for (int i = 0; i < r.getTotalPasos(); i++)
            System.out.println("Paso " + (i + 1) + ": " + r.getCamino()[i]);
        if (usarTiempo)
            System.out.println("Tiempo total : " + String.format("%.1f", r.getCostoTotal()) + " min");
        else
            System.out.println("Distancia total: " + String.format("%.2f", r.getCostoTotal()) + " km");
        System.out.println("==========================\n");
    }

    private int buscarIndice(String[] nodos, String nombre) {
        for (int i = 0; i < nodos.length; i++) {
            if (nodos[i] != null && nodos[i].equalsIgnoreCase(nombre)) return i;
        }
        return -1;
    }
}