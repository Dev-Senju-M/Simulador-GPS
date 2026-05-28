package edu.uvg.gps.algoritmos;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenRouteService {

    private static final String API_KEY = "TU_API_KEY_AQUI";
    private static final String BASE_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    public static double[] obtenerDistanciaYTiempo(double latOrigen, double lonOrigen,
                                                   double latDestino, double lonDestino) {
        try {
            String urlStr = BASE_URL + "?api_key=" + API_KEY +
                    "&start=" + lonOrigen + "," + latOrigen +
                    "&end=" + lonDestino + "," + latDestino;

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error en API: codigo " + responseCode +
                        " — usando Haversine como respaldo");
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder respuesta = new StringBuilder();
            String linea;
            while ((linea = br.readLine()) != null) {
                respuesta.append(linea);
            }
            br.close();

            return parsearRespuesta(respuesta.toString());

        } catch (Exception e) {
            System.out.println("Error al llamar OpenRouteService: " + e.getMessage() +
                    " — usando Haversine como respaldo");
            return null;
        }
    }

    private static double[] parsearRespuesta(String json) {
        try {
            int indexDist = json.indexOf("\"distance\":");
            int indexDur  = json.indexOf("\"duration\":");

            if (indexDist == -1 || indexDur == -1) {
                System.out.println("No se encontraron datos en la respuesta");
                return null;
            }

            String subDist = json.substring(indexDist + 11);
            double distanciaMetros = Double.parseDouble(subDist.substring(0, subDist.indexOf(',')).trim());

            String subDur = json.substring(indexDur + 11);
            double duracionSegundos = Double.parseDouble(subDur.substring(0, subDur.indexOf(',')).trim());

            double distanciaKm     = distanciaMetros / 1000.0;
            double tiempoMinutos   = duracionSegundos / 60.0;

            System.out.println("API OK → " + String.format("%.2f", distanciaKm) +
                    " km | " + String.format("%.1f", tiempoMinutos) + " min");

            return new double[]{distanciaKm, tiempoMinutos};

        } catch (Exception e) {
            System.out.println("Error al parsear respuesta: " + e.getMessage());
            return null;
        }
    }
}