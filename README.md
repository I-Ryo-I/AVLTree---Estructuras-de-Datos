# 🌳 Árbol AVL Interactivo

**Estudiante:** MORENO MOLINA JUAN SEBASTIAN  
**Carné:** 20252578004  
**Examen Final – Estructuras de Datos**  
**Secuencia Asignada (Estudiante #9):** `48, 28, 75, 18, 38, 68, 88, 36, 37, 35, 34, 39`

---

## 📋 Requisitos

- **Apache NetBeans 12 o superior** ([descargar aquí](https://netbeans.apache.org/front/main/download/))
- **Java JDK 11 o superior** ([descargar aquí](https://www.oracle.com/java/technologies/downloads/))
- No requiere librerías externas — todo usa la API estándar de Java (Swing, AWT, Java2D)

---

## 🚀 Cómo abrir y ejecutar en NetBeans

1. Abre NetBeans → `File → New Project`
2. Selecciona **Java with Ant → Java Application** → Next
3. Nombre: `AVLTree` — **desmarca** "Create Main Class" → Finish
4. En el panel izquierdo, clic derecho en `Source Packages → New → Java Package` → nombre: `avl`
5. Copia los 5 archivos `.java` de la carpeta `src/avl/` dentro del paquete `avl` recién creado
6. Clic derecho sobre `Main.java` → **Run File** (o `Shift + F6`)

> ⚠️ Si NetBeans muestra error *"Main class not found"*: clic derecho en el proyecto → `Properties → Run` → en "Main Class" escribe `avl.Main` → OK

---

## 🎮 Funcionalidades implementadas

| Funcionalidad | Ubicación |
|---|---|
| Inserción con rotaciones AVL | `AVLTree.java` → `insertRec()` + `balance()` |
| Eliminación con rebalanceo | `AVLTree.java` → `deleteRec()` |
| Búsqueda animada (paso a paso visual) | `AVLVisualizer.java` → `doSearch()` |
| Dibujado del árbol (nodos h y FE) | `TreePanel.java` → `paintComponent()` |
| Modo automático (toggle) | Botón "Automático" en panel derecho |
| Balanceo paso a paso | Botón "Insertar paso a paso" |
| Pausa/reanuda con slider de velocidad | `speedSlider` (100ms–2000ms) |
| Panel de log con timestamps | `logModel` / `logList` |
| Cargar secuencia asignada | Botón "📋 Cargar secuencia" |
| Generar aleatorio (5–20 nodos) | Botón "🎲 Aleatorio" |
| Reiniciar árbol | Botón "🔄 Reiniciar árbol" |
| Exportar PNG | Botón "🖼 PNG" |
| Exportar PDF (con estadísticas) | Botón "📄 PDF" — sin librerías externas |
| Imprimir | Botón "🖨 Imprimir" |

---

## 📁 Estructura del proyecto

```
AVLTree/
├── src/
│   └── avl/
│       ├── Main.java           # Punto de entrada
│       ├── AVLNode.java        # Nodo del árbol (valor, altura, FE)
│       ├── AVLTree.java        # Lógica AVL: insert, delete, search, rotaciones
│       ├── TreePanel.java      # Canvas de dibujado (Java2D)
│       └── AVLVisualizer.java  # GUI completa (Swing)
└── README.md
```

---

## 🔄 Rotaciones implementadas

- **LL (Izquierda-Izquierda):** `rotateRight()`
- **RR (Derecha-Derecha):** `rotateLeft()`
- **LR (Izquierda-Derecha):** `rotateLeft(left)` → `rotateRight(node)`
- **RL (Derecha-Izquierda):** `rotateRight(right)` → `rotateLeft(node)`

Cada rotación queda registrada en el panel de log con su tipo.

---

## 📊 Verificación de la secuencia asignada

Insertar `48, 28, 75, 18, 38, 68, 88, 36, 37, 35, 34, 39` produce:

- **Raíz:** 36  
- **Altura:** 4  
- **Factor de Equilibrio raíz:** 0  
- **Inorden:** `[18, 28, 34, 35, 36, 37, 38, 39, 48, 68, 75, 88]` ✅

---

## ⚙️ Notas técnicas

- El PDF se genera en **Java puro** (sin iText, PDFBox ni ninguna dependencia externa).
- La animación de búsqueda resalta el camino recorrido nodo por nodo con color naranja, y el nodo encontrado en verde.
- La velocidad del slider afecta tanto la animación de búsqueda como la inserción automática de la secuencia.
