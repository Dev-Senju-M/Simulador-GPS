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

            // ── Seccion CIUDADES ──
            NodoGrafo recorrer = grafo.getHead();
            while (recorrer != null) {
                // CIUDAD|id|nombre|lat|lon|alt|tipo|d0,d1,d2,d3,d4
                double[] demoras = recorrer.getCiudad().getDemorasCongestión();
                bw.write("CIUDAD|" +
                        recorrer.getCiudad().getId()       + "|" +
                        recorrer.getCiudad().getNombre()   + "|" +
                        recorrer.getCiudad().getLatitud()  + "|" +
                        recorrer.getCiudad().getLongitud() + "|" +
                        recorrer.getCiudad().getAltitud()  + "|" +
                        recorrer.getCiudad().getTipo()     + "|" +
                        demoras[0] + "," + demoras[1] + "," +
                        demoras[2] + "," + demoras[3] + "," + demoras[4]
                );
                bw.newLine();
                recorrer = recorrer.getSiguiente();
            }

            // ── Seccion RUTAS ──
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
                        // RUTA|idOrigen|idDestino|distancia|tiempoBase|m0,m1,m2,m3,m4
                        double[] mult = ady.getMultiplicadores();
                        bw.write("RUTA|" +
                                idOrigen + "|" +
                                idDestino + "|" +
                                String.format("%.4f", ady.getDistancia()) + "|" +
                                String.format("%.4f", ady.getTiempoBase()) + "|" +
                                ady.getVelocidadMaxima() + "|" +
                                mult[0] + "," + mult[1] + "," +
                                mult[2] + "," + mult[3] + "," + mult[4]
                        );
                        bw.newLine();
                        escritos[totalEscritos][0] = idOrigen;
                        escritos[totalEscritos][1] = idDestino;
                        totalEscritos++;
                    }
                    ady = ady.getSiguiente();
                }
                recorrer = recorrer.getSiguiente();
            }

            System.out.println("Grafo guardado en: " + rutaArchivo);
            return true;

        } catch (IOException e) {
            System.out.println("Error al guardar: " + e.getMessage());
            return false;
        }
    }
}