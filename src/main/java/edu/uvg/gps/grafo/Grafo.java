package edu.uvg.gps.grafo;

import edu.uvg.gps.model.Ciudad;
import edu.uvg.gps.model.NodoAdyacencia;
import edu.uvg.gps.model.NodoGrafo;

public class Grafo {
    private NodoGrafo head;
    private int totalNodos;

    public Grafo() {
        this.head = null;
        this.totalNodos = 0;
    }

    private NodoGrafo buscarNodo(String nombreCiudad) {
        NodoGrafo r = head;
        while (r != null) {
            if (r.getCiudad().getNombre().equalsIgnoreCase(nombreCiudad)) return r;
            r = r.getSiguiente();
        }
        return null;
    }

    private NodoGrafo buscarNodoPorId(int id) {
        NodoGrafo r = head;
        while (r != null) {
            if (r.getCiudad().getId() == id) return r;
            r = r.getSiguiente();
        }
        return null;
    }

    public boolean agregarCiudad(Ciudad ciudad) {
        if (buscarNodoPorId(ciudad.getId()) != null) {
            System.out.println("Ya existe una ciudad con ID " + ciudad.getId());
            return false;
        }
        if (buscarNodo(ciudad.getNombre()) != null) {
            System.out.println("Ya existe la ciudad: " + ciudad.getNombre());
            return false;
        }
        NodoGrafo nuevo = new NodoGrafo(ciudad);
        if (head == null) {
            head = nuevo;
        } else {
            NodoGrafo r = head;
            while (r.getSiguiente() != null) r = r.getSiguiente();
            r.setSiguiente(nuevo);
        }
        totalNodos++;
        System.out.println("Ciudad agregada: [" + ciudad.getId() + "] " + ciudad.getNombre());
        return true;
    }

    public boolean eliminarCiudad(int id) {
        if (head == null) { System.out.println("El grafo esta vacio."); return false; }
        String nombreEliminado = "";
        if (head.getCiudad().getId() == id) {
            nombreEliminado = head.getCiudad().getNombre();
            head = head.getSiguiente();
            totalNodos--;
        } else {
            NodoGrafo r = head;
            boolean encontrado = false;
            while (r.getSiguiente() != null) {
                if (r.getSiguiente().getCiudad().getId() == id) {
                    nombreEliminado = r.getSiguiente().getCiudad().getNombre();
                    r.setSiguiente(r.getSiguiente().getSiguiente());
                    totalNodos--;
                    encontrado = true;
                    break;
                }
                r = r.getSiguiente();
            }
            if (!encontrado) { System.out.println("Ciudad con ID " + id + " no encontrada."); return false; }
        }
        NodoGrafo r = head;
        while (r != null) { eliminarAdyacencia(r, nombreEliminado); r = r.getSiguiente(); }
        System.out.println("Ciudad eliminada: [" + id + "] " + nombreEliminado);
        return true;
    }

    public boolean agregarRuta(int idOrigen, int idDestino) {
        NodoGrafo nodoOrigen  = buscarNodoPorId(idOrigen);
        NodoGrafo nodoDestino = buscarNodoPorId(idDestino);
        if (nodoOrigen == null) { System.out.println("Ciudad origen " + idOrigen + " no encontrada."); return false; }
        if (nodoDestino == null) { System.out.println("Ciudad destino " + idDestino + " no encontrada."); return false; }

        double distancia;
        double tiempo;
        double[] resultado = edu.uvg.gps.algoritmos.OpenRouteService.obtenerDistanciaYTiempo(
                nodoOrigen.getCiudad().getLatitud(), nodoOrigen.getCiudad().getLongitud(),
                nodoDestino.getCiudad().getLatitud(), nodoDestino.getCiudad().getLongitud());
        if (resultado != null) {
            distancia = resultado[0]; tiempo = resultado[1];
        } else {
            distancia = edu.uvg.gps.algoritmos.Haversine.calcularDistancia(
                    nodoOrigen.getCiudad().getLatitud(), nodoOrigen.getCiudad().getLongitud(),
                    nodoDestino.getCiudad().getLatitud(), nodoDestino.getCiudad().getLongitud());
            tiempo = (distancia / 60.0) * 60;
        }
        agregarAdyacencia(nodoOrigen, nodoDestino.getCiudad().getNombre(), distancia, tiempo);
        agregarAdyacencia(nodoDestino, nodoOrigen.getCiudad().getNombre(), distancia, tiempo);
        System.out.println("Ruta agregada: [" + idOrigen + "] <-> [" + idDestino + "] | " +
                String.format("%.2f", distancia) + " km | " + String.format("%.1f", tiempo) + " min");
        return true;
    }

    public boolean agregarRuta(String origen, String destino) {
        NodoGrafo nodoOrigen  = buscarNodo(origen);
        NodoGrafo nodoDestino = buscarNodo(destino);
        if (nodoOrigen == null || nodoDestino == null) {
            System.out.println("Una o ambas ciudades no encontradas."); return false;
        }
        double distancia;
        double tiempo;
        double[] resultado = edu.uvg.gps.algoritmos.OpenRouteService.obtenerDistanciaYTiempo(
                nodoOrigen.getCiudad().getLatitud(), nodoOrigen.getCiudad().getLongitud(),
                nodoDestino.getCiudad().getLatitud(), nodoDestino.getCiudad().getLongitud());
        if (resultado != null) {
            distancia = resultado[0]; tiempo = resultado[1];
        } else {
            distancia = edu.uvg.gps.algoritmos.Haversine.calcularDistancia(
                    nodoOrigen.getCiudad().getLatitud(), nodoOrigen.getCiudad().getLongitud(),
                    nodoDestino.getCiudad().getLatitud(), nodoDestino.getCiudad().getLongitud());
            tiempo = (distancia / 60.0) * 60;
        }
        agregarAdyacencia(nodoOrigen, destino, distancia, tiempo);
        agregarAdyacencia(nodoDestino, origen, distancia, tiempo);
        System.out.println("Ruta agregada: " + origen + " <-> " + destino + " | " +
                String.format("%.2f", distancia) + " km | " + String.format("%.1f", tiempo) + " min");
        return true;
    }

    public boolean agregarRutaDirecta(int idOrigen, int idDestino, double distancia, double tiempoBase, double[] multiplicadores) {
        NodoGrafo nodoOrigen  = buscarNodoPorId(idOrigen);
        NodoGrafo nodoDestino = buscarNodoPorId(idDestino);
        if (nodoOrigen == null || nodoDestino == null) {
            System.out.println("Una o ambas ciudades no encontradas para ruta directa."); return false;
        }
        agregarAdyacenciaConMultiplicadores(nodoOrigen, nodoDestino.getCiudad().getNombre(), distancia, tiempoBase, multiplicadores);
        agregarAdyacenciaConMultiplicadores(nodoDestino, nodoOrigen.getCiudad().getNombre(), distancia, tiempoBase, multiplicadores);
        System.out.println("Ruta directa: [" + idOrigen + "] <-> [" + idDestino + "] | " +
                String.format("%.2f", distancia) + " km | base " + String.format("%.1f", tiempoBase) + " min");
        return true;
    }

    public boolean agregarRutaDirecta(int idOrigen, int idDestino, double distancia,
                                      double tiempoBase, int velocidadMaxima,
                                      double[] multiplicadores) {
        NodoGrafo nodoOrigen  = buscarNodoPorId(idOrigen);
        NodoGrafo nodoDestino = buscarNodoPorId(idDestino);
        if (nodoOrigen == null || nodoDestino == null) {
            System.out.println("Una o ambas ciudades no encontradas."); return false;
        }
        agregarAdyacenciaCompleta(nodoOrigen, nodoDestino.getCiudad().getNombre(), distancia, tiempoBase, velocidadMaxima, multiplicadores);
        agregarAdyacenciaCompleta(nodoDestino, nodoOrigen.getCiudad().getNombre(), distancia, tiempoBase, velocidadMaxima, multiplicadores);
        System.out.println("Ruta directa: [" + idOrigen + "] <-> [" + idDestino + "] | " +
                String.format("%.2f", distancia) + " km | " +
                velocidadMaxima + " km/h max");
        return true;
    }

    private void agregarAdyacenciaCompleta(NodoGrafo nodo, String destino, double distancia,
                                           double tiempoBase, int velocidadMaxima,
                                           double[] multiplicadores) {
        NodoAdyacencia nueva = new NodoAdyacencia(destino, distancia, tiempoBase,
                velocidadMaxima, multiplicadores);
        insertarAdyacencia(nodo, nueva);
    }

    private void agregarAdyacencia(NodoGrafo nodo, String destino, double distancia, double tiempo) {
        NodoAdyacencia nueva = new NodoAdyacencia(destino, distancia, tiempo);
        insertarAdyacencia(nodo, nueva);
    }

    private void agregarAdyacenciaConMultiplicadores(NodoGrafo nodo, String destino, double distancia, double tiempoBase, double[] multiplicadores) {
        NodoAdyacencia nueva = new NodoAdyacencia(destino, distancia, tiempoBase, multiplicadores);
        insertarAdyacencia(nodo, nueva);
    }

    private void insertarAdyacencia(NodoGrafo nodo, NodoAdyacencia nueva) {
        if (nodo.getListaAdyacencia() == null) {
            nodo.setListaAdyacencia(nueva);
        } else {
            NodoAdyacencia r = nodo.getListaAdyacencia();
            while (r.getSiguiente() != null) r = r.getSiguiente();
            r.setSiguiente(nueva);
        }
    }

    public boolean eliminarRuta(int idOrigen, int idDestino) {
        NodoGrafo nodoOrigen  = buscarNodoPorId(idOrigen);
        NodoGrafo nodoDestino = buscarNodoPorId(idDestino);
        if (nodoOrigen == null || nodoDestino == null) {
            System.out.println("Una o ambas ciudades no existen."); return false;
        }
        eliminarAdyacencia(nodoOrigen, nodoDestino.getCiudad().getNombre());
        eliminarAdyacencia(nodoDestino, nodoOrigen.getCiudad().getNombre());
        System.out.println("Ruta eliminada: [" + idOrigen + "] <-> [" + idDestino + "]");
        return true;
    }

    private void eliminarAdyacencia(NodoGrafo nodo, String destino) {
        NodoAdyacencia ady = nodo.getListaAdyacencia();
        if (ady == null) return;
        if (ady.getCiudadDestino().equalsIgnoreCase(destino)) {
            nodo.setListaAdyacencia(ady.getSiguiente()); return;
        }
        while (ady.getSiguiente() != null) {
            if (ady.getSiguiente().getCiudadDestino().equalsIgnoreCase(destino)) {
                ady.setSiguiente(ady.getSiguiente().getSiguiente()); return;
            }
            ady = ady.getSiguiente();
        }
    }

    public boolean existeCamino(String origen, String destino) {
        if (buscarNodo(origen) == null || buscarNodo(destino) == null) {
            System.out.println("Una o ambas ciudades no existen."); return false;
        }
        String[] visitados = new String[totalNodos];
        int totalVisitados = 0;
        edu.uvg.gps.estructuras.Cola cola = new edu.uvg.gps.estructuras.Cola();
        cola.insertarNodo(origen);
        while (!cola.estaVacia()) {
            String actual = cola.desencolar();
            if (actual.equalsIgnoreCase(destino)) return true;
            boolean yaVisitado = false;
            for (int i = 0; i < totalVisitados; i++) {
                if (visitados[i].equalsIgnoreCase(actual)) { yaVisitado = true; break; }
            }
            if (yaVisitado) continue;
            visitados[totalVisitados++] = actual;
            NodoGrafo nodo = buscarNodo(actual);
            if (nodo != null) {
                NodoAdyacencia ady = nodo.getListaAdyacencia();
                while (ady != null) { cola.insertarNodo(ady.getCiudadDestino()); ady = ady.getSiguiente(); }
            }
        }
        return false;
    }

    public void mostrarGrafo() {
        if (head == null) { System.out.println("El grafo esta vacio."); return; }
        System.out.println("\n===== MAPA DE CONEXIONES =====");
        NodoGrafo r = head;
        while (r != null) { System.out.println(r.toString()); r = r.getSiguiente(); }
        System.out.println("==============================\n");
    }

    public NodoGrafo getHead() { return head; }
    public int getTotalNodos() { return totalNodos; }

    public String[] obtenerNombresNodos() {
        String[] nombres = new String[totalNodos];
        NodoGrafo r = head;
        int i = 0;
        while (r != null) { nombres[i++] = r.getCiudad().getNombre(); r = r.getSiguiente(); }
        return nombres;
    }

    public NodoAdyacencia obtenerAdyacencias(String nombreCiudad) {
        NodoGrafo nodo = buscarNodo(nombreCiudad);
        return nodo == null ? null : nodo.getListaAdyacencia();
    }

    public Ciudad obtenerCiudad(String nombreCiudad) {
        NodoGrafo nodo = buscarNodo(nombreCiudad);
        return nodo == null ? null : nodo.getCiudad();
    }

    public Ciudad obtenerCiudadPorId(int id) {
        NodoGrafo nodo = buscarNodoPorId(id);
        return nodo == null ? null : nodo.getCiudad();
    }
}