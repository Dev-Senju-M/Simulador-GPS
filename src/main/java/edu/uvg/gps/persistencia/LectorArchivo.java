package edu.uvg.gps.persistencia;

import edu.uvg.gps.grafo.Grafo;
import edu.uvg.gps.model.Ciudad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LectorArchivo {

    private Grafo grafo;

    public LectorArchivo(Grafo grafo) {
        this.grafo = grafo;
    }

    public boolean cargarArchivo(String rutaArchivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            String seccion = "";

            System.out.println("Cargando archivo: " + rutaArchivo);

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();

                if (linea.isEmpty()) continue;

                if (linea.equalsIgnoreCase("# NODOS")) {
                    seccion = "NODOS";
                    continue;
                }
                if (linea.equalsIgnoreCase("# ARISTAS")) {
                    seccion = "ARISTAS";
                    continue;
                }

                // Ignorar cabeceras
                if (linea.startsWith("id,") || linea.startsWith("idOrigen,")) continue;

                if (seccion.equals("NODOS")) {
                    procesarNodo(linea);
                } else if (seccion.equals("ARISTAS")) {
                    procesarArista(linea);
                }
            }

            System.out.println("Archivo cargado exitosamente: " + rutaArchivo);
            return true;

        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + rutaArchivo);
            System.out.println("Detalle: " + e.getMessage());
            return false;
        }
    }

    private void procesarNodo(String linea) {
        try {
            String[] partes = linea.split(",");
            if (partes.length < 6) {
                System.out.println("Linea de nodo invalida: " + linea);
                return;
            }
            int id = Integer.parseInt(partes[0].trim());
            String nombre = partes[1].trim();
            double latitud = Double.parseDouble(partes[2].trim());
            double longitud = Double.parseDouble(partes[3].trim());
            double altitud  = Double.parseDouble(partes[4].trim());
            String tipo = partes[5].trim();

            Ciudad ciudad = new Ciudad(id, nombre, latitud, longitud, altitud, tipo);
            grafo.agregarCiudad(ciudad);

        } catch (NumberFormatException e) {
            System.out.println("Error de formato en nodo: " + linea);
        }
    }

    private void procesarArista(String linea) {
        try {
            String[] partes = linea.split(",");
            if (partes.length < 2) {
                System.out.println("Linea de arista invalida: " + linea);
                return;
            }
            int idOrigen  = Integer.parseInt(partes[0].trim());
            int idDestino = Integer.parseInt(partes[1].trim());

            grafo.agregarRuta(idOrigen, idDestino);

        } catch (NumberFormatException e) {
            System.out.println("Error de formato en arista: " + linea);
        }
    }

    public void cargarMultiplesArchivos(String[] rutasArchivos) {
        int exitosos = 0;
        for (String ruta : rutasArchivos) {
            if (cargarArchivo(ruta)) exitosos++;
        }
        System.out.println("\nArchivos cargados: " + exitosos + "/" + rutasArchivos.length);
    }
}