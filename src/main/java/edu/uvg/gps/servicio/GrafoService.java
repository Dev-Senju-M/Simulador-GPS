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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GrafoService {

    private Grafo grafo;
    private Dijkstra dijkstra;
    private Grafo[] grafosPorRuta;

    private static final int ID_TEMPORAL_BASE = 99000;
    private int contadorTemporal = 0;
    private List<Integer> nodosTemporales = new ArrayList<>();

    public GrafoService() {
        this.grafo = new Grafo();
    }

    public boolean cargarDataset(String recurso) {
        try {
            List<String> lineas = LectorArchivo.leerLineas(recurso);
            if (lineas.isEmpty()) return false;
            grafo = GrafoIO.construirGrafo(lineas);
            dijkstra = new Dijkstra(grafo);
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
        return calcularRuta(origen, destino, hora, LocalDate.now().getDayOfWeek());
    }

    public ResultadoDijkstra calcularRuta(String origen, String destino,
                                          LocalTime hora, DayOfWeek dia) {
        if (dijkstra == null) return null;
        return dijkstra.calcularConTiempo(origen, destino, hora, dia);
    }

    public String agregarNodoTemporal(double lat, double lon, TipoProblema problema) {
       AristaCercana mejor = encontrarAristaMasCercana(lat, lon);
        if (mejor == null) return null;

        int idTemp = ID_TEMPORAL_BASE + contadorTemporal++;
        String nombreTemp = "⚠ Accidente #" + idTemp;
        Ciudad ciudadTemp = new Ciudad(idTemp, nombreTemp, lat, lon, 0,
                new double[]{0, 0, 0, 0, 0});
        ciudadTemp.setTipo("temporal");
        ciudadTemp.reportarProblema(problema);
        grafo.agregarCiudad(ciudadTemp);
        nodosTemporales.add(idTemp);
        double distA = haversine(lat, lon,
                mejor.ciudadA.getLatitud(), mejor.ciudadA.getLongitud());
        double distB = haversine(lat, lon,
                mejor.ciudadB.getLatitud(), mejor.ciudadB.getLongitud());

        double penalizacion = problema.getMultiplicadorExtra();
        double tiempoA = (distA / 0.4) * penalizacion;
        double tiempoB = (distB / 0.4) * penalizacion;

        double[] multAlto = {1.0, penalizacion * 2, penalizacion * 2,
                penalizacion * 2, penalizacion * 2};

        grafo.agregarRutaDirecta(idTemp,
                mejor.ciudadA.getId(), distA, tiempoA, 10, multAlto);
        grafo.agregarRutaDirecta(idTemp,
                mejor.ciudadB.getId(), distB, tiempoB, 10, multAlto);

        aplicarProblemaTramoEnGrafo(grafo,
                mejor.ciudadA.getNombre(), mejor.ciudadB.getNombre(), problema);

        if (grafosPorRuta != null) {
            for (Grafo g : grafosPorRuta) {
                if (g != null) {
                    Ciudad cA = g.obtenerCiudad(mejor.ciudadA.getNombre());
                    Ciudad cB = g.obtenerCiudad(mejor.ciudadB.getNombre());
                    if (cA != null && cB != null) {
                        Ciudad ct = new Ciudad(idTemp, nombreTemp, lat, lon, 0,
                                new double[]{0,0,0,0,0});
                        ct.setTipo("temporal");
                        g.agregarCiudad(ct);
                        g.agregarRutaDirecta(idTemp, cA.getId(), distA, tiempoA, 10, multAlto);
                        g.agregarRutaDirecta(idTemp, cB.getId(), distB, tiempoB, 10, multAlto);
                        aplicarProblemaTramoEnGrafo(g, cA.getNombre(), cB.getNombre(), problema);
                    }
                }
            }
        }

        dijkstra = new Dijkstra(grafo);
        System.out.println("Nodo temporal creado: " + nombreTemp +
                " entre " + mejor.ciudadA.getNombre() + " y " + mejor.ciudadB.getNombre());
        return nombreTemp;
    }

    private static class AristaCercana {
        Ciudad ciudadA, ciudadB;
        double distancia;
        AristaCercana(Ciudad a, Ciudad b, double d) {
            ciudadA = a; ciudadB = b; distancia = d;
        }
    }

    private AristaCercana encontrarAristaMasCercana(double lat, double lon) {
        AristaCercana mejor = null;
        java.util.Set<String> revisadas = new java.util.HashSet<>();

        NodoGrafo nodo = grafo.getHead();
        while (nodo != null) {
            Ciudad cA = nodo.getCiudad();
            if (cA.getId() >= ID_TEMPORAL_BASE) { nodo = nodo.getSiguiente(); continue; }

            NodoAdyacencia ady = nodo.getListaAdyacencia();
            while (ady != null) {
                Ciudad cB = grafo.obtenerCiudad(ady.getCiudadDestino());
                if (cB != null && cB.getId() < ID_TEMPORAL_BASE) {
                    String clave = Math.min(cA.getId(), cB.getId()) + "-" +
                            Math.max(cA.getId(), cB.getId());
                    if (revisadas.add(clave)) {
                        double dist = distanciaASegmento(lat, lon,
                                cA.getLatitud(), cA.getLongitud(),
                                cB.getLatitud(), cB.getLongitud());
                        if (mejor == null || dist < mejor.distancia) {
                            mejor = new AristaCercana(cA, cB, dist);
                        }
                    }
                }
                ady = ady.getSiguiente();
            }
            nodo = nodo.getSiguiente();
        }
        return mejor;
    }
    private double distanciaASegmento(double px, double py,
                                      double ax, double ay,
                                      double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        if (dx == 0 && dy == 0) return haversine(px, py, ax, ay);
        double t = ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        return haversine(px, py, ax + t * dx, ay + t * dy);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0;
        double dlat = Math.toRadians(lat2 - lat1);
        double dlon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dlat/2) * Math.sin(dlat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dlon/2) * Math.sin(dlon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    public void limpiarNodosTemporales() {
        for (int id : nodosTemporales) {
            grafo.eliminarCiudad(id);
            if (grafosPorRuta != null) {
                for (Grafo g : grafosPorRuta) {
                    if (g != null) g.eliminarCiudad(id);
                }
            }
        }
        nodosTemporales.clear();
        contadorTemporal = 0;
        dijkstra = new Dijkstra(grafo);
        System.out.println("Nodos temporales eliminados.");
    }

    public boolean reportarProblemaTramo(String ciudadOrigen, String ciudadDestino, TipoProblema problema) {
        boolean encontrado = aplicarProblemaTramoEnGrafo(grafo, ciudadOrigen, ciudadDestino, problema);
        if (grafosPorRuta != null) {
            for (Grafo g : grafosPorRuta) {
                if (g != null) aplicarProblemaTramoEnGrafo(g, ciudadOrigen, ciudadDestino, problema);
            }
        }
        return encontrado;
    }

    private boolean aplicarProblemaTramoEnGrafo(Grafo g, String ciudadOrigen,
                                                String ciudadDestino, TipoProblema problema) {
        boolean encontrado = false;
        NodoAdyacencia ady = g.obtenerAdyacencias(ciudadOrigen);
        while (ady != null) {
            if (ady.getCiudadDestino().equalsIgnoreCase(ciudadDestino)) {
                ady.reportarProblema(problema); encontrado = true; break;
            }
            ady = ady.getSiguiente();
        }
        NodoAdyacencia adyInv = g.obtenerAdyacencias(ciudadDestino);
        while(adyInv != null){
            if (adyInv.getCiudadDestino().equalsIgnoreCase(ciudadOrigen)) {
                adyInv.reportarProblema(problema); break;
            }
            adyInv = adyInv.getSiguiente();
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
        limpiarNodosTemporales();
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
            while (ady != null) { ady.limpiarProblema(); ady = ady.getSiguiente(); }
            nodo = nodo.getSiguiente();
        }
    }

    public Grafo[] getGrafosPorRuta() { return grafosPorRuta; }
    public String[] obtenerNombresCiudades() { return grafo.obtenerNombresNodos(); }
    public Grafo getGrafo() { return grafo; }
}