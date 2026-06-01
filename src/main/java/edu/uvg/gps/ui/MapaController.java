package edu.uvg.gps.ui;

import edu.uvg.gps.grafo.Grafo;
import edu.uvg.gps.model.Ciudad;
import edu.uvg.gps.model.NodoAdyacencia;
import edu.uvg.gps.model.NodoGrafo;
import edu.uvg.gps.model.TipoProblema;
import edu.uvg.gps.servicio.GrafoService;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class MapaController {

    private final WebEngine engine;
    private GrafoService grafoService;
    private Runnable onProblemaReportado;
    private Runnable onMapaCargado;
    private JavaConnector javaConnector;

    public MapaController(WebView webView) {
        this.engine = webView.getEngine();
    }

    public void setOnMapaCargado(Runnable r)       { this.onMapaCargado = r; }
    public void setGrafoService(GrafoService gs)   { this.grafoService = gs; }
    public void setOnProblemaReportado(Runnable r) { this.onProblemaReportado = r; }

    public void inicializarMapa(String recurso) {
        engine.load(getClass().getResource(recurso).toExternalForm());
    }

    public void mostrarNodos(Grafo grafo) {
        engine.getLoadWorker().stateProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo == Worker.State.SUCCEEDED) {
                setup(grafo);
            }
        });
        if (engine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            setup(grafo);
        }
    }

    private void setup(Grafo grafo) {
        conectarJavaConnector();
        agregarMarcadores(grafo);
        dibujarTodasLasRutas(grafo);
        engine.executeScript("ajustarVista()");
        if (onMapaCargado != null) Platform.runLater(onMapaCargado);
    }

    private void conectarJavaConnector() {
        javaConnector = new JavaConnector();
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("javaConnector", javaConnector);
    }

    private void agregarMarcadores(Grafo grafo) {
        NodoGrafo nodo = grafo.getHead();
        while (nodo != null) {
            Ciudad c = nodo.getCiudad();
            if ("inicio".equals(c.getTipo()) || "destino".equals(c.getTipo())) {
                engine.executeScript(String.format(java.util.Locale.US,
                        "agregarMarcador(%f, %f, '%s', '%s')",
                        c.getLatitud(), c.getLongitud(),
                        c.getNombre().replace("'", "\\'"), c.getTipo()));
            }
            nodo = nodo.getSiguiente();
        }
    }

    public void dibujarTodasLasRutas(Grafo grafo) {
        java.util.Set<String> vistos = new java.util.HashSet<>();
        NodoGrafo nodo = grafo.getHead();
        while (nodo != null) {
            Ciudad a = nodo.getCiudad();
            NodoAdyacencia ady = nodo.getListaAdyacencia();
            while (ady != null) {
                Ciudad b = grafo.obtenerCiudad(ady.getCiudadDestino());
                if (b != null) {
                    String clave = Math.min(a.getId(), b.getId()) + "-" + Math.max(a.getId(), b.getId());
                    if (vistos.add(clave)) {
                        engine.executeScript(String.format(java.util.Locale.US,
                                "dibujarRutaAzul('[[%f,%f],[%f,%f]]')",
                                a.getLatitud(), a.getLongitud(), b.getLatitud(), b.getLongitud()));
                    }
                }
                ady = ady.getSiguiente();
            }
            nodo = nodo.getSiguiente();
        }
    }

    public void dibujarRutaOptima(String[] camino, int pasos, Grafo grafo) {
        engine.executeScript("dibujarRutaOptima('" + buildCoords(camino, pasos, grafo) + "')");
    }

    public void dibujarRuta(String[] camino, int pasos, Grafo grafo) {
        dibujarRutaOptima(camino, pasos, grafo);
    }

    public void dibujarCorredor(int i, String coords, String color) {
        engine.executeScript(String.format("dibujarCorredor(%d, '%s', '%s')", i, coords, color));
    }

    public void mostrarSoloCorredor(int i) {
        engine.executeScript("mostrarSoloCorredor(" + i + ")");
    }

    public void limpiarRuta()              { engine.executeScript("limpiarRuta()"); }
    public void limpiarIconosProblemas()   { engine.executeScript("limpiarIconosProblemas()"); }
    public void ocultarNodos()             { engine.executeScript("limpiarNodos()"); }
    public void limpiarCaminoBusqueda()    { engine.executeScript("limpiarCaminoBusqueda()"); }

    public void mostrarTodosLosNodos(Grafo grafo) {
        engine.executeScript("limpiarNodos()");
        NodoGrafo nodo = grafo.getHead();
        while (nodo != null) {
            Ciudad c = nodo.getCiudad();
            if (c.getId() < 99000) {
                engine.executeScript(String.format(java.util.Locale.US,
                        "agregarNodoIntermedio(%f, %f, '%s')",
                        c.getLatitud(), c.getLongitud(),
                        c.getNombre().replace("'", "\\'").replace("\n", "")));
            }
            nodo = nodo.getSiguiente();
        }
    }

    public void dibujarCaminoBusqueda(String[] camino, int pasos, Grafo grafo) {
        engine.executeScript("dibujarCaminoBusqueda('" + buildCoords(camino, pasos, grafo) + "')");
    }

    public void agregarMarcadorTemporal(double lat, double lon, String nombre) {
        engine.executeScript(String.format(java.util.Locale.US,
                "agregarMarcadorTemporal(%f, %f, '%s')", lat, lon, nombre.replace("'", "\\'")));
    }

    private String buildCoords(String[] camino, int pasos, Grafo grafo) {
        StringBuilder sb = new StringBuilder("[");
        boolean primero = true;
        for (int i = 0; i < pasos; i++) {
            Ciudad c = grafo.obtenerCiudad(camino[i]);
            if (c == null) continue;
            if (!primero) sb.append(",");
            sb.append(String.format(java.util.Locale.US, "[%f,%f]", c.getLatitud(), c.getLongitud()));
            primero = false;
        }
        return sb.append("]").toString();
    }

    public class JavaConnector {

        public void reportarProblema(String nombre, String tipo) {
            if (grafoService == null) return;
            grafoService.reportarProblemaNodo(nombre, buscarTipo(tipo));
            if (onProblemaReportado != null) Platform.runLater(onProblemaReportado);
        }

        public void limpiarProblema(String nombre) {
            if (grafoService == null) return;
            grafoService.limpiarProblemaNodo(nombre);
            if (onProblemaReportado != null) Platform.runLater(onProblemaReportado);
        }

        public void reportarAccidenteEnCoordenadas(double lat, double lon, String tipo) {
            if (grafoService == null) return;
            String nombreTemp = grafoService.agregarNodoTemporal(lat, lon, buscarTipo(tipo));
            if (nombreTemp != null) {
                final double fLat = lat, fLon = lon;
                final String fNombre = nombreTemp;
                Platform.runLater(() -> agregarMarcadorTemporal(fLat, fLon, fNombre));
            }
            if (onProblemaReportado != null) Platform.runLater(onProblemaReportado);
        }

        public void limpiarTodosLosProblemas() {
            if (grafoService == null) return;
            grafoService.limpiarTodosLosProblemas();
            if (onProblemaReportado != null) Platform.runLater(onProblemaReportado);
        }

        private TipoProblema buscarTipo(String etiqueta) {
            String n = normalizar(etiqueta);
            for (TipoProblema tp : TipoProblema.values())
                if (normalizar(tp.getEtiqueta()).equals(n)) return tp;
            return TipoProblema.ACCIDENTE;
        }

        private String normalizar(String s) {
            return s.toLowerCase()
                    .replace("ó","o").replace("ú","u")
                    .replace("á","a").replace("é","e").replace("í","i");
        }
    }
}