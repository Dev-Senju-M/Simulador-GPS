package edu.uvg.gps.ui;

import edu.uvg.gps.algoritmos.Dijkstra;
import edu.uvg.gps.algoritmos.ResultadoDijkstra;
import edu.uvg.gps.grafo.Grafo;
import edu.uvg.gps.model.NodoGrafo;
import edu.uvg.gps.model.TipoProblema;
import edu.uvg.gps.servicio.GrafoService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class MainController {

    private static final String ORIGEN_FIJO  = "Plaza Tigo Torre 3";
    private static final String DESTINO_FIJO = "UMG Antigua Jocotenango";

    private static final String[] NOMBRES_RUTAS = {
            "Ruta 1 — Carretera Panamericana",
            "Ruta 2 — RD-GUA-16 / RN-10 (Bárcenas)",
            "Ruta 3 — Calzada Atanasio Tzul",
            "Ruta 4 — Calzada Aguilar Batres",
            "Ruta 5 — Calzada Roosevelt / Chimaltenango"
    };

    private static final String[] RUTAS_ARCHIVOS = {
            "/edu/uvg/gps/data/ruta1.txt",
            "/edu/uvg/gps/data/ruta2.txt",
            "/edu/uvg/gps/data/ruta3.txt",
            "/edu/uvg/gps/data/ruta4.txt",
            "/edu/uvg/gps/data/ruta5.txt"
    };

    @FXML private ComboBox<String> cbDia;
    @FXML private ComboBox<String> cbHora;
    @FXML private ComboBox<String> cbAlgoritmo;
    @FXML private ComboBox<String> cbVerRuta;
    @FXML private ComboBox<String> cbVerNodos;
    @FXML private Label            lblResultado;
    @FXML private WebView          webView;
    @FXML private ListView<String> listPasos;
    @FXML private Label            lblSimResultado;
    @FXML private TextField        txtBuscarOrigen;
    @FXML private TextField        txtBuscarDestino;
    @FXML private Label            lblCamino;

    @FXML private TableView<ComparacionFila>           tablaComparacion;
    @FXML private TableColumn<ComparacionFila, String> colCompRuta;
    @FXML private TableColumn<ComparacionFila, String> colCompKm;
    @FXML private TableColumn<ComparacionFila, String> colCompTiempoBase;
    @FXML private TableColumn<ComparacionFila, String> colCompTiempoAct;
    @FXML private TableColumn<ComparacionFila, String> colCompEstado;

    @FXML private TableView<CiudadFila>           tablaCiudades;
    @FXML private TableColumn<CiudadFila, String> colCiudadId;
    @FXML private TableColumn<CiudadFila, String> colCiudadNombre;
    @FXML private TableColumn<CiudadFila, String> colCiudadLat;
    @FXML private TableColumn<CiudadFila, String> colCiudadLon;
    @FXML private TableColumn<CiudadFila, String> colCiudadAlt;
    @FXML private TableColumn<CiudadFila, String> colCiudadTipo;

    private GrafoService      grafoService;
    private MapaController    mapaController;
    private ResultadoDijkstra ultimoResultado;

    @FXML
    public void initialize() {
        grafoService   = new GrafoService();
        mapaController = new MapaController(webView);

        mapaController.setGrafoService(grafoService);
        mapaController.setOnProblemaReportado(this::recalcularRutaOptima);
        mapaController.setOnMapaCargado(() -> {
            recalcularRutaOptima();
            mapaController.mostrarSoloCorredor(-1);
        });
        mapaController.inicializarMapa("/edu/uvg/gps/leaflet.html");
        grafoService.cargarMultiplesDatasets(RUTAS_ARCHIVOS);

        for (int h = 0; h < 24; h++)
            cbHora.getItems().add(String.format("%02d:00", h));
        cbHora.setValue(String.format("%02d:00", LocalTime.now().getHour()));

        cbAlgoritmo.getItems().addAll("Dijkstra", "Floyd-Warshall");
        cbAlgoritmo.setValue("Dijkstra");

        cbVerRuta.getItems().add("Todas las rutas");
        cbVerRuta.getItems().addAll(NOMBRES_RUTAS);
        cbVerRuta.setValue("Todas las rutas");

        cbVerNodos.getItems().addAll("Sin nodos", "Con nodos");
        cbVerNodos.setValue("Sin nodos");

        String[] dias = {"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"};
        cbDia.getItems().addAll(dias);
        cbDia.setValue(dias[LocalDate.now().getDayOfWeek().getValue() - 1]);

        configurarTablas();
        mapaController.mostrarNodos(grafoService.getGrafo());
        llenarTablaCiudades();
    }

    private DayOfWeek obtenerDia() {
        switch (cbDia.getValue()) {
            case "Sábado":  return DayOfWeek.SATURDAY;
            case "Domingo": return DayOfWeek.SUNDAY;
            default:        return DayOfWeek.MONDAY;
        }
    }

    private LocalTime obtenerHora() {
        try { return LocalTime.parse(cbHora.getValue()); }
        catch (Exception e) { return LocalTime.now(); }
    }

    private String obtenerInfoDia(DayOfWeek dia) {
        switch (dia) {
            case SATURDAY: return "Sábado — tráfico 15% menor";
            case SUNDAY:   return "Domingo — tráfico 25% menor";
            default:       return "Entre semana";
        }
    }

    private void recalcularRutaOptima() {
        LocalTime hora = obtenerHora();
        DayOfWeek dia  = obtenerDia();
        Grafo[] grafos = grafoService.getGrafosPorRuta();
        if (grafos == null) return;

        int indiceMejor = -1;
        double menorTiempo = Double.MAX_VALUE;
        ResultadoDijkstra[] resultados = new ResultadoDijkstra[grafos.length];
        double[] tiemposAct  = new double[grafos.length];
        double[] tiemposBase = new double[grafos.length];
        double[] distancias  = new double[grafos.length];

        for (int i = 0; i < grafos.length; i++) {
            if (grafos[i] == null) { tiemposAct[i] = -1; continue; }
            Dijkstra d = new Dijkstra(grafos[i]);
            ResultadoDijkstra r    = d.calcularConTiempo(ORIGEN_FIJO, DESTINO_FIJO, hora, dia);
            ResultadoDijkstra rBase = d.calcularConTiempo(ORIGEN_FIJO, DESTINO_FIJO, LocalTime.of(4,0), DayOfWeek.MONDAY);
            resultados[i]   = r;
            tiemposBase[i]  = rBase != null ? rBase.getCostoTotal() : -1;
            if (r != null) {
                tiemposAct[i] = r.getCostoTotal();
                distancias[i] = calcularDistanciaTotal(grafos[i], r.getCamino(), r.getTotalPasos());
                if (tiemposAct[i] < menorTiempo) { menorTiempo = tiemposAct[i]; indiceMejor = i; }
            } else {
                tiemposAct[i] = -1;
            }
        }

        actualizarTabla(grafos, tiemposAct, tiemposBase, distancias, resultados, indiceMejor);

        if (indiceMejor >= 0 && resultados[indiceMejor] != null) {
            ResultadoDijkstra mejor = resultados[indiceMejor];
            ultimoResultado = mejor;
            mapaController.dibujarRutaOptima(mejor.getCamino(), mejor.getTotalPasos(), grafos[indiceMejor]);
            String periodo = mejor.getPeriodo() != null ? mejor.getPeriodo().etiqueta() : "";
            lblResultado.setText(String.format(
                    "Ruta óptima \nAlgoritmo: %s\n%s\nPasos: %d\nTiempo: %.1f min\n%s\n\n%s",
                    cbAlgoritmo.getValue(), obtenerInfoDia(dia),
                    mejor.getTotalPasos(), mejor.getCostoTotal(), periodo,
                    construirCaminoTexto(mejor)));
        } else {
            lblResultado.setText("Sin ruta disponible.");
        }
    }

    private void actualizarTabla(Grafo[] grafos, double[] tiemposAct, double[] tiemposBase,
                                 double[] distancias, ResultadoDijkstra[] resultados, int indiceMejor) {
        for (int i = 0; i < grafos.length; i++) {
            if (resultados[i] == null) continue;
            String coords = obtenerCoordsRuta(grafos[i], resultados[i].getCamino(), resultados[i].getTotalPasos());
            mapaController.dibujarCorredor(i, coords, calcularColorSaturacion(tiemposAct[i], tiemposBase[i]));
        }

        ObservableList<ComparacionFila> filas = FXCollections.observableArrayList();
        for (int i = 0; i < grafos.length; i++) {
            String tAct  = tiemposAct[i]  < 0 ? "Sin ruta" : String.format("%.1f min", tiemposAct[i]);
            String tBase = tiemposBase[i] < 0 ? "Sin ruta" : String.format("%.1f min", tiemposBase[i]);
            String dist  = distancias[i]  <= 0 ? "-"       : String.format("%.1f km",  distancias[i]);
            String estado = (i == indiceMejor) ? "✅ MEJOR RUTA" : (tiemposAct[i] < 0 ? "Sin conexión" : "");
            filas.add(new ComparacionFila(NOMBRES_RUTAS[i], dist, tBase, tAct, estado));
        }
        tablaComparacion.setItems(filas);
        tablaComparacion.setRowFactory(tv -> new TableRow<ComparacionFila>() {
            @Override
            protected void updateItem(ComparacionFila item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.estadoProperty().get().contains("MEJOR"))
                    setStyle("-fx-background-color: #1b5e20;");
                else if (item != null && !empty)
                    setStyle("-fx-background-color: #0d2137;");
                else
                    setStyle("");
            }
        });
    }

    @FXML public void onBuscarRuta()        { recalcularRutaOptima(); }
    @FXML public void onCambiarFiltro()     { recalcularRutaOptima(); }
    @FXML public void onActualizarTiempos() { recalcularRutaOptima(); }

    @FXML
    public void onVerRuta() {
        int indice = cbVerRuta.getSelectionModel().getSelectedIndex() - 1;
        mapaController.mostrarSoloCorredor(indice);
    }

    @FXML
    public void onVerNodos() {
        if ("Con nodos".equals(cbVerNodos.getValue()))
            mapaController.mostrarTodosLosNodos(grafoService.getGrafo());
        else
            mapaController.ocultarNodos();
    }

    @FXML
    public void onVerificarCamino() {
        String textoOrigen  = txtBuscarOrigen.getText().trim();
        String textoDestino = txtBuscarDestino.getText().trim();

        if (textoOrigen.isEmpty() || textoDestino.isEmpty()) {
            setLblCamino("⚠ Escribe origen y destino.", "#ff9800");
            return;
        }

        String nOrigen  = buscarNombreParcial(textoOrigen);
        String nDestino = buscarNombreParcial(textoDestino);

        if (nOrigen == null) {
            setLblCamino("No encontrado:\n\"" + textoOrigen + "\"", "#ef5350");
            return;
        }
        if (nDestino == null) {
            setLblCamino("No encontrado:\n\"" + textoDestino + "\"", "#ef5350");
            return;
        }

        boolean existe = grafoService.getGrafo().existeCamino(nOrigen, nDestino);
        if (existe) {
            setLblCamino("Existe camino\nDe: " + nOrigen + "\nA: " + nDestino, "#a5d6a7");
            ResultadoDijkstra r = grafoService.calcularRuta(nOrigen, nDestino, obtenerHora(), obtenerDia());
            if (r != null)
                mapaController.dibujarCaminoBusqueda(r.getCamino(), r.getTotalPasos(), grafoService.getGrafo());
        } else {
            setLblCamino("No existe camino\nDe: " + nOrigen + "\nA: " + nDestino, "#ef5350");
            mapaController.limpiarCaminoBusqueda();
        }
    }

    private void setLblCamino(String texto, String color) {
        lblCamino.setText(texto);
        lblCamino.setStyle("-fx-font-size:11;-fx-padding:6;-fx-background-color:#0d1b2e;" +
                "-fx-text-fill:" + color + ";-fx-background-radius:6;");
    }

    private String buscarNombreParcial(String texto) {
        String lower = texto.toLowerCase();
        NodoGrafo nodo = grafoService.getGrafo().getHead();
        while (nodo != null) {
            if (nodo.getCiudad().getNombre().toLowerCase().contains(lower))
                return nodo.getCiudad().getNombre();
            nodo = nodo.getSiguiente();
        }
        return null;
    }

    @FXML
    public void onGuardarGrafo() {
        edu.uvg.gps.persistencia.EscritorArchivo escritor =
                new edu.uvg.gps.persistencia.EscritorArchivo(grafoService.getGrafo());
        boolean ok = escritor.guardarArchivo("grafo_guardado.txt");
        if (ok) mostrarInfo("Grafo guardado en grafo_guardado.txt");
        else mostrarAlerta("Error al guardar el grafo.");
    }

    @FXML
    public void onLimpiarProblemas() {
        grafoService.limpiarTodosLosProblemas();
        mapaController.limpiarIconosProblemas();
        recalcularRutaOptima();
    }

    @FXML
    public void onSimular() {
        Grafo[] grafos = grafoService.getGrafosPorRuta();
        int indiceMejor = 0;
        double menor = Double.MAX_VALUE;

        for (int i = 0; i < grafos.length; i++) {
            if (grafos[i] == null) continue;
            ResultadoDijkstra r = new Dijkstra(grafos[i])
                    .calcularConTiempo(ORIGEN_FIJO, DESTINO_FIJO, obtenerHora(), obtenerDia());
            if (r != null && r.getCostoTotal() < menor) {
                menor = r.getCostoTotal();
                indiceMejor = i;
            }
        }

        final Grafo grafoMejor = grafos[indiceMejor];
        final ResultadoDijkstra resultado = new Dijkstra(grafoMejor)
                .calcularConTiempo(ORIGEN_FIJO, DESTINO_FIJO, obtenerHora(), obtenerDia());

        if (resultado == null) { mostrarAlerta("No existe ruta disponible."); return; }

        listPasos.getItems().clear();
        for (int i = 0; i < resultado.getTotalPasos(); i++)
            listPasos.getItems().add("Paso " + (i+1) + ": " + resultado.getCamino()[i]);

        String periodo = resultado.getPeriodo() != null ? resultado.getPeriodo().etiqueta() : "";
        lblSimResultado.setText(String.format("%.1f min  |  %d pasos  |  %s",
                resultado.getCostoTotal(), resultado.getTotalPasos(), periodo));

        mapaController.limpiarRuta();
        String[] camino = resultado.getCamino();
        int totalPasos  = resultado.getTotalPasos();

        Timeline timeline = new Timeline();
        for (int i = 0; i < totalPasos; i++) {
            final int paso = i;
            KeyFrame kf = new KeyFrame(Duration.seconds(i * 1.5), e -> {
                listPasos.getSelectionModel().select(paso);
                listPasos.scrollTo(paso);
                String[] sub = new String[paso + 1];
                System.arraycopy(camino, 0, sub, 0, paso + 1);
                mapaController.dibujarRuta(sub, paso + 1, grafoMejor);
            });
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }

    private void configurarTablas() {
        colCompRuta.setCellValueFactory(d -> d.getValue().rutaProperty());
        colCompKm.setCellValueFactory(d -> d.getValue().kmProperty());
        colCompTiempoBase.setCellValueFactory(d -> d.getValue().tiempoBaseProperty());
        colCompTiempoAct.setCellValueFactory(d -> d.getValue().tiempoActualProperty());
        colCompEstado.setCellValueFactory(d -> d.getValue().estadoProperty());
        colCiudadId.setCellValueFactory(d -> d.getValue().idProperty());
        colCiudadNombre.setCellValueFactory(d -> d.getValue().nombreProperty());
        colCiudadLat.setCellValueFactory(d -> d.getValue().latProperty());
        colCiudadLon.setCellValueFactory(d -> d.getValue().lonProperty());
        colCiudadAlt.setCellValueFactory(d -> d.getValue().altProperty());
        colCiudadTipo.setCellValueFactory(d -> d.getValue().tipoProperty());
    }

    private void llenarTablaCiudades() {
        ObservableList<CiudadFila> ciudades = FXCollections.observableArrayList();
        NodoGrafo nodo = grafoService.getGrafo().getHead();
        while (nodo != null) {
            ciudades.add(new CiudadFila(
                    String.valueOf(nodo.getCiudad().getId()),
                    nodo.getCiudad().getNombre(),
                    String.format("%.4f", nodo.getCiudad().getLatitud()),
                    String.format("%.4f", nodo.getCiudad().getLongitud()),
                    String.format("%.1f", nodo.getCiudad().getAltitud()),
                    nodo.getCiudad().getTipo()
            ));
            nodo = nodo.getSiguiente();
        }
        tablaCiudades.setItems(ciudades);
    }

    private double calcularDistanciaTotal(Grafo grafo, String[] camino, int pasos) {
        double total = 0;
        for (int i = 0; i < pasos - 1; i++) {
            edu.uvg.gps.model.NodoAdyacencia ady = grafo.obtenerAdyacencias(camino[i]);
            while (ady != null) {
                if (ady.getCiudadDestino().equalsIgnoreCase(camino[i + 1])) {
                    total += ady.getDistancia();
                    break;
                }
                ady = ady.getSiguiente();
            }
        }
        return total;
    }

    private String construirCaminoTexto(ResultadoDijkstra resultado) {
        StringBuilder sb = new StringBuilder("Recorrido:\n");
        for (int i = 0; i < resultado.getTotalPasos(); i++)
            sb.append(i + 1).append(". ").append(resultado.getCamino()[i]).append("\n");
        return sb.toString();
    }

    private String obtenerCoordsRuta(Grafo grafo, String[] camino, int pasos) {
        StringBuilder sb = new StringBuilder("[");
        boolean primero = true;
        for (int p = 0; p < pasos; p++) {
            edu.uvg.gps.model.Ciudad c = grafo.obtenerCiudad(camino[p]);
            if (c != null) {
                if (!primero) sb.append(",");
                sb.append(String.format(java.util.Locale.US, "[%f,%f]", c.getLatitud(), c.getLongitud()));
                primero = false;
            }
        }
        return sb.append("]").toString();
    }

    private String calcularColorSaturacion(double actual, double base) {
        if (base <= 0) return "#4fc3f7";
        double ratio = actual / base;
        if (ratio <= 1.05) return "#00e676";
        if (ratio <= 1.2)  return "#c6ff00";
        if (ratio <= 1.4)  return "#ffeb3b";
        if (ratio <= 1.7)  return "#ff9800";
        return "#f44336";
    }

    private void mostrarAlerta(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("GPS Navigator"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("GPS Navigator"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}