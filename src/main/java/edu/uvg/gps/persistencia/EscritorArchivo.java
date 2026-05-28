package edu.uvg.gps.persistencia;

import edu.uvg.gps.grafo.Grafo;
import edu.uvg.gps.model.NodoAdyacencia;
import edu.uvg.gps.model.NodoGrafo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class EscritorArchivo {

    private Grafo grafo;

    public EscritorArchivo(Grafo grafo) {
        this.grafo = grafo;
    }

    public boolean guardarArchivo(String rutaArchivo) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {

            // ── Seccion NODOS ──
            bw.write("# NODOS");
            bw.newLine();
            bw.write("id,nombre,latitud,longitud,altitud,tipo");
            bw.newLine();

            NodoGrafo recorrer = grafo.getHead();
            while (recorrer != null) {
                bw.write(
                        recorrer.getCiudad().getId()       + "," +
                                recorrer.getCiudad().getNombre()   + "," +
                                recorrer.getCiudad().getLatitud()  + "," +
                                recorrer.getCiudad().getLongitud() + "," +
                                recorrer.getCiudad().getAltitud()  + "," +
                                recorrer.getCiudad().getTipo()
                );
                bw.newLine();
                recorrer = recorrer.getSiguiente();
            }

            // ── Seccion ARISTAS ──
            bw.newLine();
            bw.write("# ARISTAS");
            bw.newLine();
            bw.write("idOrigen,idDestino");
            bw.newLine();

            // Para no duplicar aristas (grafo no dirigido)
            int[][] escritos = new int[grafo.getTotalNodos() * grafo.getTotalNodos()][2];
            int totalEscritos = 0;

            recorrer = grafo.getHead();
            while (recorrer != null) {
                int idOrigen = recorrer.getCiudad().getId();
                NodoAdyacencia ady = recorrer.getListaAdyacencia();

                while (ady != null) {
                    int idDestino = grafo.obtenerCiudad(ady.getCiudadDestino()).getId();

                    // Verificar si ya escribimos este par
                    boolean yaEscrito = false;
                    for (int i = 0; i < totalEscritos; i++) {
                        if ((escritos[i][0] == idOrigen  && escritos[i][1] == idDestino) ||
                                (escritos[i][0] == idDestino && escritos[i][1] == idOrigen)) {
                            yaEscrito = true;
                            break;
                        }
                    }

                    if (!yaEscrito) {
                        bw.write(idOrigen + "," + idDestino);
                        bw.newLine();
                        escritos[totalEscritos][0] = idOrigen;
                        escritos[totalEscritos][1] = idDestino;
                        totalEscritos++;
                    }

                    ady = ady.getSiguiente();
                }
                recorrer = recorrer.getSiguiente();
            }

            System.out.println("Grafo guardado exitosamente en: " + rutaArchivo);
            return true;

        } catch (IOException e) {
            System.out.println("Error al guardar el archivo: " + rutaArchivo);
            System.out.println("Detalle: " + e.getMessage());
            return false;
        }
    }
}