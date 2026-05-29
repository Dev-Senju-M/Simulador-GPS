package edu.uvg.gps.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LectorArchivo {

    public static List<String> leerLineas(String recurso) {
        List<String> lineas = new ArrayList<>();
        try (InputStream is = LectorArchivo.class.getResourceAsStream(recurso);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty() && !linea.startsWith("#")) {
                    lineas.add(linea);
                }
            }
        } catch (IOException | NullPointerException e) {
            System.out.println("Error al leer recurso: " + recurso + " — " + e.getMessage());
        }
        return lineas;
    }
}
