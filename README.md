# GPS Navigator — Simulador de Rutas Guatemala

> Proyecto Final · Curso de Estructura de Datos  
> **Java 21 · JavaFX · OpenStreetMap + Leaflet.js**

Simulador de navegación GPS para vehículos que registra ciudades (nodos) y rutas (aristas) con distancias en kilómetros, y proporciona la mejor ruta entre dos ubicaciones usando algoritmos de grafos implementados desde cero.

---

## Características principales

- Registrar y eliminar ciudades con coordenadas WGS84 (latitud, longitud, altitud)
- Conectar ciudades con distancia calculada mediante la fórmula Haversine
- Verificar si existe un camino entre dos ciudades (BFS)
- Ruta más corta entre dos ciudades (Dijkstra con min-heap propio)
- Visualización del recorrido paso a paso
- Guardar y cargar grafos en archivos `.txt` con formato propio
- Mapa interactivo con OpenStreetMap + Leaflet.js embebido en JavaFX WebView

### Funcionalidades extra
- Comparación de rutas con Floyd-Warshall
- Tabla de adyacencia en texto plano
- Simulación animada del viaje en el mapa
- Estadísticas del grafo (nodos, aristas, grado por nodo)
- Dataset precargado de 15 ciudades cabeceras de Guatemala con coordenadas reales

---

## Stack tecnológico

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 21 LTS |
| Interfaz gráfica | JavaFX 21 |
| Gestión de dependencias | Maven |
| Mapa visual | OpenStreetMap + Leaflet.js |
| IDE recomendado | IntelliJ IDEA Community |
| Persistencia | Archivos `.txt` propios |

---

## Estructuras de datos implementadas desde cero

Ninguna estructura usa `java.util.LinkedList`, `java.util.Stack`, `java.util.PriorityQueue` ni similares.

| Clase | Descripción |
|---|---|
| `ListaEnlazada<T>` | Lista enlazada genérica |
| `Cola<T>` | Cola FIFO con nodos enlazados — usada por BFS |
| `Pila<T>` | Pila LIFO con array dinámico — usada para reconstruir rutas |
| `ColaPrioridad` | Min-heap manual — usada por Dijkstra |
| `ListaAdyacencia` | Grafo como mapa de listas enlazadas propias |

---

## Algoritmos

| Algoritmo | Clase | Complejidad | Uso |
|---|---|---|---|
| Haversine | `Haversine.java` | O(1) | Distancia real entre coordenadas WGS84 |
| BFS | `BFS.java` | O(V + E) | Verificar si existe camino |
| Dijkstra | `Dijkstra.java` | O((V+E) log V) | Ruta más corta |
| Floyd-Warshall | `FloydWarshall.java` | O(V³) | Todas las rutas entre todos los pares |

---

## Estructura del proyecto

```
gps-simulator/
├── src/main/java/com/gpssimulator/
│   ├── model/          # Ciudad.java, Ruta.java, ResultadoRuta.java
│   ├── estructuras/    # ListaEnlazada, Cola, Pila, ColaPrioridad, ListaAdyacencia
│   ├── algoritmos/     # BFS, Dijkstra, FloydWarshall, Haversine
│   ├── persistencia/   # GrafoIO.java
│   ├── ui/             # MainController.java, MapaController.java
│   └── Main.java
├── src/main/resources/
│   ├── fxml/           # main.fxml
│   └── html/           # leaflet.html
├── data/
│   └── guatemala.txt   # Dataset de 15 ciudades
├── pom.xml
└── README.md
```

---

## Requisitos

- Java 21
- Maven 3.8+

---

## Compilar y ejecutar

```bash
mvn clean javafx:run
```

---

## Formato de persistencia

Los grafos se guardan en archivos `.txt` con el siguiente formato:

```
# GPS-SIMULATOR v1.0
# CIUDADES: id|nombre|latitud|longitud|altitud
CIUDAD|GT-GUA|Guatemala City|14.634900|-90.506900|1500.0
CIUDAD|GT-ANT|Antigua Guatemala|14.558600|-90.729500|1530.0

# RUTAS: origen_id|destino_id|distancia_km|bidireccional
RUTA|GT-GUA|GT-ANT|45.2|true
RUTA|GT-GUA|GT-QUE|196.8|true
```

---

## Dataset de Guatemala (15 ciudades)

| ID | Ciudad | Latitud | Longitud | Altitud (m) |
|---|---|---|---|---|
| GT-GUA | Guatemala City | 14.6349 | -90.5069 | 1500 |
| GT-ANT | Antigua Guatemala | 14.5586 | -90.7295 | 1530 |
| GT-QUE | Quetzaltenango | 14.8444 | -91.5167 | 2333 |
| GT-COB | Cobán | 15.4684 | -90.3704 | 1316 |
| GT-FLO | Flores | 16.9329 | -89.8932 | 113 |
| GT-ESC | Escuintla | 14.3058 | -90.7858 | 347 |
| GT-CHI | Chiquimula | 14.7985 | -89.5424 | 423 |
| GT-HUE | Huehuetenango | 15.3194 | -91.4725 | 1902 |
| GT-PBA | Puerto Barrios | 15.7281 | -88.5958 | 2 |
| GT-MAZ | Mazatenango | 14.5319 | -91.5024 | 371 |
| GT-JAL | Jalapa | 14.6326 | -89.9871 | 1360 |
| GT-ZAC | Zacapa | 14.9717 | -89.5237 | 180 |
| GT-RET | Retalhuleu | 14.5313 | -91.6829 | 239 |
| GT-SOL | Sololá | 14.7764 | -91.1826 | 2113 |
| GT-SAL | Salamá | 15.1027 | -90.3157 | 940 |

---

## Criterios de evaluación

| Criterio | Ponderación |
|---|---|
| Correcta implementación de estructuras | 30% |
| Algoritmos de grafos | 25% |
| Funcionalidades mínimas | 25% |
| Limpieza y claridad del código | 10% |
| Extras y presentación final | 10% |

---

## Referencias

- [JavaFX 21 API Docs](https://openjfx.io/javadoc/21/)
- [Oracle Java 21 SE Docs](https://docs.oracle.com/en/java/javase/21/)
- [Leaflet.js Reference](https://leafletjs.com/reference.html)
- [OpenStreetMap Wiki](https://wiki.openstreetmap.org)
- [Visualización de Dijkstra](https://www.cs.usfca.edu/~galles/visualization/)
- Introduction to Algorithms (CLRS) — Cormen et al., caps. 22–25
- Algorithms, 4th Edition — Sedgewick & Wayne, Part 4

---

*GPS Navigator · Proyecto Final — Estructura de Datos*
