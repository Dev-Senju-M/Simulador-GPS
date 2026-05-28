package edu.uvg.gps.model;

public class NodoGrafo {
    private Ciudad ciudad;
    private NodoAdyacencia listaAdyacencia;
    private NodoGrafo siguiente;

    public NodoGrafo(Ciudad ciudad) {
        this.ciudad = ciudad;
        this.listaAdyacencia = null;
        this.siguiente = null;
    }

    public Ciudad getCiudad() { return ciudad; }
    public void setCiudad(Ciudad ciudad) { this.ciudad = ciudad; }

    public NodoAdyacencia getListaAdyacencia() { return listaAdyacencia; }
    public void setListaAdyacencia(NodoAdyacencia listaAdyacencia) { this.listaAdyacencia = listaAdyacencia; }

    public NodoGrafo getSiguiente() { return siguiente; }
    public void setSiguiente(NodoGrafo siguiente) { this.siguiente = siguiente; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(ciudad.getId()).append("] ");
        sb.append("[").append(ciudad.getTipo().toUpperCase()).append("] ");
        sb.append(ciudad.getNombre()).append(": ");
        NodoAdyacencia ady = listaAdyacencia;
        while (ady != null) {
            sb.append(ady.toString());
            if (ady.getSiguiente() != null) sb.append(", ");
            ady = ady.getSiguiente();
        }
        return sb.toString();
    }
}