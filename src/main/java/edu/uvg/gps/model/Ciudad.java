package edu.uvg.gps.model;

public class Ciudad {
    private int id;
    private String nombre;
    private double latitud;
    private double longitud;
    private double altitud;
    private String tipo;
    private double[] demorasCongestión;
    private TipoProblema problemaActual;

    public Ciudad(int id, String nombre, double latitud, double longitud,
                  double altitud, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.tipo = tipo;
        this.demorasCongestión = new double[]{0, 0, 0, 0, 0};
        this.problemaActual = TipoProblema.NINGUNO;
    }

    public Ciudad(int id, String nombre, double latitud, double longitud,
                  double altitud, double[] demorasCongestión) {
        this.id = id;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
        this.altitud = altitud;
        this.tipo = "waypoint";
        this.demorasCongestión = demorasCongestión;
        this.problemaActual = TipoProblema.NINGUNO;
    }

    public double getDemoraCongestión(int indicePeriodo) {
        double demoraBase = demorasCongestión[indicePeriodo];
        return demoraBase * problemaActual.getMultiplicadorExtra();
    }

    public void reportarProblema(TipoProblema problema) {
        this.problemaActual = problema;
        System.out.println("Problema reportado en " + nombre +
                ": " + problema.getEtiqueta());
    }

    public void limpiarProblema() {
        this.problemaActual = TipoProblema.NINGUNO;
    }

    public void setDemorasCongestión(double[] demoras) { this.demorasCongestión = demoras; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getLongitud() { return longitud; }
    public void setLongitud(double longitud) { this.longitud = longitud; }
    public double getAltitud() { return altitud; }
    public void setAltitud(double altitud) { this.altitud = altitud; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public double[] getDemorasCongestión() { return demorasCongestión; }
    public TipoProblema getProblemaActual() { return problemaActual; }

    @Override
    public String toString() {
        String problema = problemaActual != TipoProblema.NINGUNO ?
                " ⚠ " + problemaActual.getEtiqueta() : "";
        return "[" + id + "] " + nombre +
                " [" + latitud + ", " + longitud + ", alt: " + altitud + "m]" +
                " (" + tipo + ")" + problema;
    }
}