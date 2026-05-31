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

    public void setOnMapaCargado(Runnable callback) { this.onMapaCargado = callback; }
    public void setGrafoService(GrafoService grafoService) { this.grafoService = grafoService; }
    public void setOnProblemaReportado(Runnable callback) { this.onProblemaReportado = callback; }

    public void inicializarMapa(String recursoHtml) {
        String url = getClass().getResource(recursoHtml).toExternalForm();
        engine.load(url);
    }

    public void mostrarNodos(Grafo grafo) {
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                conectarJavaConnector();
                agregarMarcadoresAlMapa(grafo);
                dibujarTodasLasRutas(grafo);
                engine.executeScript("ajustarVista()");
                if (onMapaCargado != null) Platform.runLater(onMapaCargado);
            }
        });
        if (engine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            conectarJavaConnector();
            agregarMarcadoresAlMapa(grafo);
            dibujarTodasLasRutas(grafo);
            engine.executeScript("ajustarVista()");
            if (onMapaCargado != null) Platform.runLater(onMapaCargado);
        }
    }

    private void conectarJavaConnector() {
        javaConnector = new JavaConnector();
        JSObject window = (JSObject) engine.executeScript("window");
        window.setMember("javaConnector", javaConnector);
    }

    private void agregarMarcadoresAlMapa(Grafo grafo) {
        NodoGrafo nodo = grafo.getHead();
        while (nodo != null) {
            Ciudad c = nodo.getCiudad();
            // Solo mostrar marcadores de origen y destino
            if ("inicio".equals(c.getTipo()) || "destino".equals(c.getTipo())) {
                String script = String.format(java.util.Locale.US,
                        "agregarMarcador(%f, %f, '%s', '%s')",
                        c.getLatitud(), c.getLongitud(),
                        c.getNombre().replace("'", "\\'"), c.getTipo());
                engine.executeScript(script);
            }
            nodo = nodo.getSiguiente();
        }
    }

    public void dibujarTodasLasRutas(Grafo grafo) {
        java.util.Set<String> trazados = new java.util.HashSet<>();
        NodoGrafo nodo = grafo.getHead();
        while (nodo != null) {
            Ciudad origen = nodo.getCiudad();
            NodoAdyacencia ady = nodo.getListaAdyacencia();
            while (ady != null) {
                Ciudad destino = grafo.obtenerCiudad(ady.getCiudadDestino());
                if (destino != null) {
                    String clave = Math.min(origen.getId(), destino.getId()) + "-" + Math.max(origen.getId(), destino.getId());
                    if (trazados.add(clave)) {
                        String coords = String.format(java.util.Locale.US,
                                "[[%f,%f],[%f,%f]]",
                                origen.getLatitud(), origen.getLongitud(),
                                destino.getLatitud(), destino.getLongitud());
                        engine.executeScript("dibujarRutaAzul('" + coords + "')");
                    }
                }
                ady = ady.getSiguiente();
            }
            nodo = nodo.getSiguiente();
        }
    }

    public void dibujarRutaOptima(String[] camino, int totalPasos, Grafo grafo) {
        StringBuilder coords = new StringBuilder("[");
        boolean primero = true;
        for (int i = 0; i < totalPasos; i++) {
            Ciudad c = grafo.obtenerCiudad(camino[i]);
            if (c == null) continue;
            if (!primero) coords.append(",");
            coords.append(String.format(java.util.Locale.US, "[%f,%f]", c.getLatitud(), c.getLongitud()));
            primero = false;
        }
        coords.append("]");
        engine.executeScript("dibujarRutaOptima('" + coords + "')");
    }

    public void dibujarRuta(String[] camino, int totalPasos, Grafo grafo) {
        dibujarRutaOptima(camino, totalPasos, grafo);
    }

    public void dibujarCorredor(int indice, String coordsJson, String color) {
        engine.executeScript(String.format("dibujarCorredor(%d, '%s', '%s')", indice, coordsJson, color));
    }

    public void mostrarCorredores() { engine.executeScript("mostrarCorredores()"); }

    public void mostrarSoloCorredor(int indice) {
        engine.executeScript("mostrarSoloCorredor(" + indice + ")");
    }

    public void limpiarRuta() { engine.executeScript("limpiarRuta()"); }
    public void limpiarTodo() { engine.executeScript("limpiarTodo()"); }

    public void limpiarIconosProblemas() {
        engine.executeScript("limpiarIconosProblemas()");
    }

    public class JavaConnector {
        public void reportarProblema(String nombreCiudad, String tipoEtiqueta) {
            if (grafoService == null) return;
            TipoProblema tipo = buscarTipo(tipoEtiqueta);
            grafoService.reportarProblemaNodo(nombreCiudad, tipo);
            System.out.println("Problema: " + nombreCiudad + " — " + tipo.getEtiqueta());
            if (onProblemaReportado != null) Platform.runLater(onProblemaReportado);
        }

        public void limpiarProblema(String nombreCiudad) {
            if (grafoService == null) return;
            grafoService.limpiarProblemaNodo(nombreCiudad);
            if (onProblemaReportado != null) Platform.runLater(onProblemaReportado);
        }

        private TipoProblema buscarTipo(String etiqueta) {
            String normalizado = etiqueta.toLowerCase()
                    .replace("ó","o").replace("ú","u").replace("á","a").replace("é","e").replace("í","i");
            for (TipoProblema tp : TipoProblema.values()) {
                String tpNorm = tp.getEtiqueta().toLowerCase()
                        .replace("ó","o").replace("ú","u").replace("á","a").replace("é","e").replace("í","i");
                if (tpNorm.equals(normalizado)) return tp;
            }
            return TipoProblema.ACCIDENTE;
        }
    }
}