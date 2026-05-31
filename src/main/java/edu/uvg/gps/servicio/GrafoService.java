package edu.uvg.gps.servicio;

import edu.uvg.gps.algoritmos.Dijkstra;
import edu.uvg.gps.algoritmos.ResultadoDijkstra;
import edu.uvg.gps.grafo.Grafo;
import edu.uvg.gps.model.Ciudad;
import edu.uvg.gps.model.NodoAdyacencia;
import edu.uvg.gps.model.NodoGrafo;
import edu.uvg.gps.model.TipoProblema;
import edu.uvg.gps.persistencia.GrafoIO;
import edu.uvg.gps.persistencia.LectorArchivo;

import java.time.LocalTime;
import java.util.List;

public class GrafoService {

    private Grafo grafo;
    private Dijkstra dijkstra;
    private Grafo[] grafosPorRuta;

    public GrafoService() {
        this.grafo = new Grafo();
    }

    public boolean cargarDataset(String recurso) {
        try {
            List<String> lineas = LectorArchivo.leerLineas(recurso);
            if (lineas.isEmpty()) {
                System.out.println("Archivo vacio o no encontrado: " + recurso);
                return false;
            }
            grafo = GrafoIO.construirGrafo(lineas);
            dijkstra = new Dijkstra(grafo);
            System.out.println("Dataset cargado: " + grafo.getTotalNodos() + " ciudades");
            return true;
        } catch (Exception e) {
            System.out.println("Error al cargar dataset: " + e.getMessage());
            return false;
        }
    }

    public void cargarMultiplesDatasets(String[] recursos) {
        grafosPorRuta = new Grafo[recursos.length];
        for (int i = 0; i < recursos.length; i++) {
            List<String> lineas = LectorArchivo.leerLineas(recursos[i]);
            if (!lineas.isEmpty()) {
                GrafoIO.agregarAlGrafo(lineas, grafo);
                grafosPorRuta[i] = GrafoIO.construirGrafo(lineas);
                System.out.println("Dataset agregado: " + recursos[i]);
            }
        }
        dijkstra = new Dijkstra(grafo);
    }

    public ResultadoDijkstra calcularRuta(String origen, String destino, LocalTime hora) {
        if (dijkstra == null) return null;
        return dijkstra.calcularConTiempo(origen, destino, hora);
    }

    public boolean reportarProblemaTramo(String ciudadOrigen, String ciudadDestino, TipoProblema problema) {
        boolean encontrado = false;

        // Aplicar en grafo principal
        encontrado = aplicarProblemaTramoEnGrafo(grafo, ciudadOrigen, ciudadDestino, problema);

        // Aplicar en grafos separados donde exista ese tramo
        if (grafosPorRuta != null) {
            for (Grafo g : grafosPorRuta) {
                if (g != null) aplicarProblemaTramoEnGrafo(g, ciudadOrigen, ciudadDestino, problema);
            }
        }
        return encontrado;
    }

    private boolean aplicarProblemaTramoEnGrafo(Grafo g, String ciudadOrigen, String ciudadDestino, TipoProblema problema) {
        boolean encontrado = false;
        NodoAdyacencia ady = g.obtenerAdyacencias(ciudadOrigen);
        while (ady != null) {
            if (ady.getCiudadDestino().equalsIgnoreCase(ciudadDestino)) {
                ady.reportarProblema(problema);
                encontrado = true;
                break;
            }
            ady = ady.getSiguiente();
        }
        NodoAdyacencia adyInverso = g.obtenerAdyacencias(ciudadDestino);
        while (adyInverso != null) {
            if (adyInverso.getCiudadDestino().equalsIgnoreCase(ciudadOrigen)) {
                adyInverso.reportarProblema(problema);
                break;
            }
            adyInverso = adyInverso.getSiguiente();
        }
        return encontrado;
    }

    public boolean reportarProblemaNodo(String nombreCiudad, TipoProblema problema) {
        Ciudad ciudad = grafo.obtenerCiudad(nombreCiudad);
        if (ciudad == null) return false;
        aplicarProblemaNodoEnGrafo(grafo, nombreCiudad, problema);

        if (grafosPorRuta != null) {
            for (Grafo g : grafosPorRuta) {
                if (g != null) aplicarProblemaNodoEnGrafo(g, nombreCiudad, problema);
            }
        }
        return true;
    }

    private static final int RANGO_PROBLEMA = 10;

    private void aplicarProblemaNodoEnGrafo(Grafo g, String nombreCiudad, TipoProblema problema) {
        if (g.obtenerCiudad(nombreCiudad) == null) return;
        recorrerRango(g, nombreCiudad, nodo -> nodo.getCiudad().reportarProblema(problema),
                ady -> ady.reportarProblema(problema));
    }

    public void limpiarProblemaNodo(String nombreCiudad) {
        limpiarRangoEnGrafo(grafo, nombreCiudad);
        if (grafosPorRuta != null) {
            for (Grafo g : grafosPorRuta) {
                if (g != null) limpiarRangoEnGrafo(g, nombreCiudad);
            }
        }
    }

    private void limpiarRangoEnGrafo(Grafo g, String nombreCiudad) {
        recorrerRango(g, nombreCiudad, nodo -> nodo.getCiudad().limpiarProblema(),
                NodoAdyacencia::limpiarProblema);
    }

    private void recorrerRango(Grafo g, String nombreCiudad,
                                java.util.function.Consumer<NodoGrafo> accionNodo,
                                java.util.function.Consumer<NodoAdyacencia> accionArista) {
        java.util.Map<String, Integer> distancia = new java.util.HashMap<>();
        java.util.Queue<String> cola = new java.util.LinkedList<>();
        distancia.put(nombreCiudad, 0);
        cola.add(nombreCiudad);

        while (!cola.isEmpty()) {
            String actual = cola.poll();
            int d = distancia.get(actual);

            NodoGrafo nodoActual = buscarNodoEnGrafo(g, actual);
            if (nodoActual == null) continue;

            accionNodo.accept(nodoActual);

            NodoAdyacencia ady = nodoActual.getListaAdyacencia();
            while (ady != null) {
                accionArista.accept(ady);
                if (d < RANGO_PROBLEMA && !distancia.containsKey(ady.getCiudadDestino())) {
                    distancia.put(ady.getCiudadDestino(), d + 1);
                    cola.add(ady.getCiudadDestino());
                }
                ady = ady.getSiguiente();
            }
        }
    }

    private NodoGrafo buscarNodoEnGrafo(Grafo g, String nombre) {
        NodoGrafo nodo = g.getHead();
        while (nodo != null) {
            if (nodo.getCiudad().getNombre().equalsIgnoreCase(nombre)) return nodo;
            nodo = nodo.getSiguiente();
        }
        return null;
    }

    public void limpiarTodosLosProblemas() {
        limpiarProblemasEnGrafo(grafo);

        // Limpiar en grafos separados
        if (grafosPorRuta != null) {
            for (Grafo g : grafosPorRuta) {
                if (g != null) limpiarProblemasEnGrafo(g);
            }
        }
        System.out.println("Todos los problemas limpiados.");
    }

    private void limpiarProblemasEnGrafo(Grafo g) {
        NodoGrafo nodo = g.getHead();
        while (nodo != null) {
            nodo.getCiudad().limpiarProblema();
            NodoAdyacencia ady = nodo.getListaAdyacencia();
            while (ady != null) {
                ady.limpiarProblema();
                ady = ady.getSiguiente();
            }
            nodo = nodo.getSiguiente();
        }
    }

    public Grafo[] getGrafosPorRuta() { return grafosPorRuta; }
    public String[] obtenerNombresCiudades() { return grafo.obtenerNombresNodos(); }
    public Grafo getGrafo() { return grafo; }
}