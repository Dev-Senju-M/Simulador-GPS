package edu.uvg.gps.persistencia;

import edu.uvg.gps.grafo.Grafo;
import edu.uvg.gps.model.Ciudad;
import java.util.List;

public class GrafoIO {

    // Construye un grafo nuevo desde cero
    public static Grafo construirGrafo(List<String> lineas) {
        Grafo grafo = new Grafo();
        agregarAlGrafo(lineas, grafo);
        return grafo;
    }

    // Agrega nodos y rutas a un grafo existente (para cargar multiples archivos)
    public static void agregarAlGrafo(List<String> lineas, Grafo grafo) {
        for (String linea : lineas) {
            if (linea.startsWith("CIUDAD|")) {
                parsearCiudad(linea, grafo);
            } else if (linea.startsWith("RUTA|")) {
                parsearRuta(linea, grafo);
            }
        }
    }

    private static void parsearCiudad(String linea, Grafo grafo) {
        try {
            String[] p = linea.split("\\|");
            // CIUDAD|id|nombre|lat|lon|alt|tipo|d0,d1,d2,d3,d4
            int id           = Integer.parseInt(p[1].trim());
            String nombre    = p[2].trim();
            double lat       = Double.parseDouble(p[3].trim());
            double lon       = Double.parseDouble(p[4].trim());
            double alt       = Double.parseDouble(p[5].trim());
            String tipo      = p[6].trim();
            double[] demoras = parsearArrayDouble(p[7].trim());

            Ciudad ciudad = new Ciudad(id, nombre, lat, lon, alt, tipo);
            ciudad.setDemorasCongestión(demoras);
            grafo.agregarCiudad(ciudad);

        } catch (Exception e) {
            System.out.println("Linea de ciudad invalida: " + linea);
        }
    }

    private static void parsearRuta(String linea, Grafo grafo) {
        try {
            String[] p = linea.split("\\|");
            // RUTA|idOrigen|idDestino|distancia|tiempoBase|velocidadMaxima|m0,m1,m2,m3,m4
            int idOrigen            = Integer.parseInt(p[1].trim());
            int idDestino           = Integer.parseInt(p[2].trim());
            double distancia        = Double.parseDouble(p[3].trim());
            double tiempoBase       = Double.parseDouble(p[4].trim());
            int velocidadMaxima     = Integer.parseInt(p[5].trim());
            double[] multiplicadores = parsearArrayDouble(p[6].trim());

            grafo.agregarRutaDirecta(idOrigen, idDestino, distancia,
                    tiempoBase, velocidadMaxima, multiplicadores);

        } catch (Exception e) {
            System.out.println("Linea de ruta invalida: " + linea);
        }
    }

    private static double[] parsearArrayDouble(String csv) {
        String[] partes = csv.split(",");
        double[] valores = new double[partes.length];
        for (int i = 0; i < partes.length; i++) {
            valores[i] = Double.parseDouble(partes[i].trim());
        }
        return valores;
    }
}