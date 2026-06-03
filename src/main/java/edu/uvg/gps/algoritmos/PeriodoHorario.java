package edu.uvg.gps.algoritmos;

import java.time.DayOfWeek;
import java.time.LocalTime;

public enum PeriodoHorario {
    MADRUGADA,
    PICO_MANANA,
    DIA,
    PICO_TARDE,
    NOCHE;

    public static PeriodoHorario desdeHora(LocalTime hora) {
        int h = hora.getHour();
        if (h >= 0  && h < 6)  return MADRUGADA;
        if (h >= 6  && h < 9)  return PICO_MANANA;
        if (h >= 9  && h < 15) return DIA;
        if (h >= 15 && h < 19) return PICO_TARDE;
        return NOCHE;
    }

    public static double factorPorDia(DayOfWeek dia) {
        switch (dia) {
            case SATURDAY: return 0.80;  // 20% menos trafico
            case SUNDAY:   return 0.65;  // 35% menos trafico
            default:       return 1.0;
        }
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