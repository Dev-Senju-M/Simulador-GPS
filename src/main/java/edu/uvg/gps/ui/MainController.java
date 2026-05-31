package edu.uvg.gps.ui;

import edu.uvg.gps.algoritmos.Dijkstra;
import edu.uvg.gps.algoritmos.FloydWarshall;
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

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class MainController {

    private static final String ORIGEN_FIJO = "Plaza Tigo Torre 3";
    private static final String DESTINO_FIJO = "UMG Antigua Jocotenango";
    private static final String[] NOMBRES_RUTAS = {
            "Ruta 1 — Roosevelt",
            "Ruta 2 — Periférico",
            "Ruta 3 — Petapa",
            "Ruta 4 — Aguilar Batres",
            "Ruta 5 — Centro Histórico"
    };
    private static final String[] RUTAS_ARCHIVOS = {
            "/edu/uvg/gps/data/ruta1.txt",
            "/edu/uvg/gps/data/ruta2.txt",
            "/edu/uvg/gps/data/ruta3.txt",
            "/edu/uvg/gps/data/ruta4.txt",
            "/edu/uvg/gps/data/ruta5.txt"
    };

    @FXML private TextField txtHora;
    @FXML private ComboBox<String> cbAlgoritmo;
    @FXML private Label lblResultado;
    @FXML private WebView webView;
    @FXML private ListView<String> listPasos;
    @FXML private Label lblSimResultado;

    @FXML private TableView<ComparacionFila> tablaComparacion;
    @FXML private TableColumn<ComparacionFila, String> colCompRuta;
    @FXML private TableColumn<ComparacionFila, String> colCompKm;
    @FXML private TableColumn<ComparacionFila, String> colCompTiempoBase;
    @FXML private TableColumn<ComparacionFila, String> colCompTiempoAct;
    @FXML private TableColumn<ComparacionFila, String> colCompEstado;

    @FXML private TableView<CiudadFila> tablaCiudades;
    @FXML private TableColumn<CiudadFila, String> colCiudadId;
    @FXML private TableColumn<CiudadFila, String> colCiudadNombre;
    @FXML private TableColumn<CiudadFila, String> colCiudadLat;
    @FXML private TableColumn<CiudadFila, String> colCiudadLon;
    @FXML private TableColumn<CiudadFila, String> colCiudadAlt;
    @FXML private TableColumn<CiudadFila, String> colCiudadTipo;

    private GrafoService grafoService;
    private MapaController mapaController;
    private ResultadoDijkstra ultimoResultado;

    @FXML
    public void initialize() {
        grafoService = new GrafoService();
        mapaController = new MapaController(webView);

        mapaController.setGrafoService(grafoService);
        mapaController.setOnProblemaReportado(this::recalcularRutaOptima);
        mapaController.setOnMapaCargado(this::recalcularRutaOptima);
        mapaController.inicializarMapa("/edu/uvg/gps/leaflet.html");

        grafoService.cargarMultiplesDatasets(RUTAS_ARCHIVOS);

        txtHora.setText(LocalTime.now().withSecond(0).withNano(0).toString());
        cbAlgoritmo.getItems().addAll("Dijkstra", "Floyd-Warshall");
        cbAlgoritmo.setValue("Dijkstra");

        configurarTablas();
        mapaController.mostrarNodos(grafoService.getGrafo());
        llenarTablaCiudades();
    }

    private void recalcularRutaOptima() {
        LocalTime hora = parsearHora(txtHora.getText());
        if (hora == null) hora = LocalTime.now();

        String algoritmo = cbAlgoritmo.getValue();
        ResultadoDijkstra resultado = null;

        if ("Floyd-Warshall".equals(algoritmo)) {
            FloydWarshall fw = new FloydWarshall(grafoService.getGrafo());
            fw.calcularConTiempo(hora);
            resultado = fw.obtenerResultado(ORIGEN_FIJO, DESTINO_FIJO);
        } else {
            resultado = grafoService.calcularRuta(ORIGEN_FIJO, DESTINO_FIJO, hora);
        }

        if (resultado == null) {
            lblResultado.setText("Sin ruta disponible.");
            return;
        }

        ultimoResultado = resultado;
        mapaController.dibujarRutaOptima(resultado.getCamino(), resultado.getTotalPasos(), grafoService.getGrafo());

        String periodo = resultado.getPeriodo() != null ? resultado.getPeriodo().etiqueta() : "";
        lblResultado.setText(String.format(
                "Ruta óptima ✅\nAlgoritmo: %s\nPasos: %d\nTiempo: %.1f min\n%s\n\n%s",
                algoritmo, resultado.getTotalPasos(), resultado.getCostoTotal(), periodo,
                construirCaminoTexto(resultado)));

        actualizarTablaComparacion(hora);
    }

    private void actualizarTablaComparacion(LocalTime hora) {
        Grafo[] grafosPorRuta = grafoService.getGrafosPorRuta();
        if (grafosPorRuta == null) return;

        double menorTiempo = Double.MAX_VALUE;
        double[] tiemposActuales = new double[grafosPorRuta.length];
        double[] distancias = new double[grafosPorRuta.length];
        double[] tiemposBase = new double[grafosPorRuta.length];

        for (int i = 0; i < grafosPorRuta.length; i++) {
            if (grafosPorRuta[i] == null) { tiemposActuales[i] = -1; continue; }
            Dijkstra d = new Dijkstra(grafosPorRuta[i]);
            ResultadoDijkstra r = d.calcularConTiempo(ORIGEN_FIJO, DESTINO_FIJO, hora);
            ResultadoDijkstra rBase = d.calcularConTiempo(ORIGEN_FIJO, DESTINO_FIJO, LocalTime.of(10, 0));
            if (r != null) {
                tiemposActuales[i] = r.getCostoTotal();
                distancias[i] = calcularDistanciaTotal(grafosPorRuta[i], r.getCamino(), r.getTotalPasos());
                if (tiemposActuales[i] < menorTiempo) menorTiempo = tiemposActuales[i];
            } else {
                tiemposActuales[i] = -1;
            }
            tiemposBase[i] = rBase != null ? rBase.getCostoTotal() : -1;
        }

        ObservableList<ComparacionFila> filas = FXCollections.observableArrayList();
        for (int i = 0; i < grafosPorRuta.length; i++) {
            String tiempoActStr = tiemposActuales[i] < 0 ? "Sin ruta" : String.format("%.1f min", tiemposActuales[i]);
            String tiempoBaseStr = tiemposBase[i] < 0 ? "Sin ruta" : String.format("%.1f min", tiemposBase[i]);
            String distStr = distancias[i] <= 0 ? "-" : String.format("%.1f km", distancias[i]);
            boolean esMejor = tiemposActuales[i] > 0 && tiemposActuales[i] == menorTiempo;
            String estado = esMejor ? "✅ MEJOR RUTA" : (tiemposActuales[i] < 0 ? "Sin conexión" : "");
            filas.add(new ComparacionFila(NOMBRES_RUTAS[i], distStr, tiempoBaseStr, tiempoActStr, estado));
        }
        tablaComparacion.setItems(filas);

        tablaComparacion.setRowFactory(tv -> new TableRow<ComparacionFila>() {
            @Override
            protected void updateItem(ComparacionFila item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.estadoProperty().get().contains("MEJOR")) {
                    setStyle("-fx-background-color: #1b5e20;");
                } else if (item != null && !empty) {
                    setStyle("-fx-background-color: #0d2137;");
                } else {
                    setStyle("");
                }
            }
        });
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
        for (int i = 0; i < resultado.getTotalPasos(); i++) {
            sb.append(i + 1).append(". ").append(resultado.getCamino()[i]).append("\n");
        }
        return sb.toString();
    }

    @FXML public void onBuscarRuta() { recalcularRutaOptima(); }

    @FXML
    public void onActualizarTiempos() {
        LocalTime hora = parsearHora(txtHora.getText());
        if (hora == null) hora = LocalTime.now();
        actualizarTablaComparacion(hora);
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
        LocalTime hora = parsearHora(txtHora.getText());
        if (hora == null) return;

        ResultadoDijkstra resultado = grafoService.calcularRuta(ORIGEN_FIJO, DESTINO_FIJO, hora);
        if (resultado == null) {
            mostrarAlerta("No existe ruta disponible.");
            return;
        }

        listPasos.getItems().clear();
        for (int i = 0; i < resultado.getTotalPasos(); i++) {
            listPasos.getItems().add("Paso " + (i + 1) + ": " + resultado.getCamino()[i]);
        }

        String periodo = resultado.getPeriodo() != null ? resultado.getPeriodo().etiqueta() : "";
        lblSimResultado.setText(String.format("%.1f min  |  %d pasos  |  %s",
                resultado.getCostoTotal(), resultado.getTotalPasos(), periodo));

        mapaController.limpiarRuta();
        String[] camino = resultado.getCamino();
        int totalPasos = resultado.getTotalPasos();

        Timeline timeline = new Timeline();
        for (int i = 0; i < totalPasos; i++) {
            final int paso = i;
            KeyFrame kf = new KeyFrame(Duration.seconds(i * 1.5), e -> {
                listPasos.getSelectionModel().select(paso);
                listPasos.scrollTo(paso);
                String[] subCamino = new String[paso + 1];
                System.arraycopy(camino, 0, subCamino, 0, paso + 1);
                mapaController.dibujarRuta(subCamino, paso + 1, grafoService.getGrafo());
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

    private LocalTime parsearHora(String texto) {
        try {
            return LocalTime.parse(texto.trim());
        } catch (DateTimeParseException e) {
            mostrarAlerta("Formato invalido. Usa HH:mm");
            return null;
        }
    }

    private TipoProblema buscarTipoProblema(String etiqueta) {
        for (TipoProblema tp : TipoProblema.values()) {
            if (tp.getEtiqueta().equals(etiqueta)) return tp;
        }
        return TipoProblema.NINGUNO;
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("GPS Navigator");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("GPS Navigator");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}