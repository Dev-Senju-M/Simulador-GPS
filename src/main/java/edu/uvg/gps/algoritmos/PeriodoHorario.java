package edu.uvg.gps.algoritmos;

import java.time.LocalTime;

public enum PeriodoHorario {
    MADRUGADA,   // 0-6h
    PICO_MANANA, // 6-9h
    DIA,         // 9-16h
    PICO_TARDE,  // 16-20h
    NOCHE;       // 20-24h

    public static PeriodoHorario desdeHora(LocalTime hora) {
        int h = hora.getHour();
        if (h >= 0  && h < 6)  return MADRUGADA;
        if (h >= 6  && h < 9)  return PICO_MANANA;
        if (h >= 9  && h < 16) return DIA;
        if (h >= 16 && h < 20) return PICO_TARDE;
        return NOCHE;
    }

    public String etiqueta() {
        switch (this) {
            case MADRUGADA:   return "Madrugada (0-6h) — tráfico ligero";
            case PICO_MANANA: return "Hora pico mañana (6-9h) — tráfico alto";
            case DIA:         return "Día normal (9-16h) — tráfico moderado";
            case PICO_TARDE:  return "Hora pico tarde (16-20h) — tráfico muy alto";
            case NOCHE:       return "Noche (20-24h) — tráfico ligero";
            default:          return "";
        }
    }
}