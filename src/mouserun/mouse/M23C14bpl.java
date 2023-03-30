package mouserun.mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;
import javafx.util.Pair;
import mouserun.game.Mouse;
import mouserun.game.Grid;
import mouserun.game.Cheese;
import static mouserun.game.Mouse.DOWN;
import static mouserun.game.Mouse.LEFT;
import static mouserun.game.Mouse.RIGHT;
import static mouserun.game.Mouse.UP;

/**
 * Clase que contiene el esqueleto del raton base para las prácticas de
 * Inteligencia Artificial del curso 2020-21.
 *
 * @author Cristóbal José Carmona (ccarmona@ujaen.es)
 */
public class M23C14bpl extends Mouse {

    /**
     * Variable para almacenar la ultima celda visitada
     */
    private Grid lastGrid;

    /**
     * Variable para guardar el anterior movimiento realizado
     */
    private int movAnterior;

    /**
     * Tabla hash para almacenar las celdas visitadas por el raton:
     * Clave:Coordenadas Valor: La celda
     */
    private final HashMap<Pair<Integer, Integer>, Grid> celdasVisitadas;
    /**
     * Tabla hash para almacenar las celdas cerradas en el camino DFS:
     * Clave:Coordenadas Valor: La celda
     */
    private final HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> cerradas;
//    private final HashMap<Pair<Integer, Integer>, Grid> contadorCasillas;

    /**
     * Pila para almacenar el camino recorrido.
     */
    private final Stack<Grid> pilaMovimientos;
    /**
     * Valor maximo de la profundidad
     */
    final int PROFUNDIDAD_LIMITE=3;

    /**
     * Pila para almacenar el camino al queso.
     */
    private final Stack<Grid> recorreCamino;
    /**
     * Entero para saber si estamos explorando
     */
    private int tipo = 0;
    /**
     * Booleano para saber si hemos llegado al destino
     */
    private boolean camino = false;
    /**
     * Grid para saber por que casilla nos hemos quedado explorando
     */
    private Grid celdaExploracion;

    /**
     * Constructor (Puedes modificar el nombre a tu gusto).
     */
    public M23C14bpl() {
        super("Ratatouille");
        celdasVisitadas = new HashMap<>();
        pilaMovimientos = new Stack<>();
        recorreCamino = new Stack<>();
        cerradas = new HashMap<>();
//        contadorCasillas = new HashMap<>();
    }

    /**
     * @brief Función que mueve el ratón por el laberinto.
     * @param currentGrid La celda actual en la que se encuentra el ratón.
     * @param cheese El objeto queso.
     * @return La dirección hacia la que se mueve el ratón.
     */
    @Override
    public int move(Grid currentGrid, Cheese cheese) {
        // Se obtiene la posición actual del ratón.
        Pair<Integer, Integer> posicion = new Pair(currentGrid.getX(), currentGrid.getY());
        // Si la posición actual del ratón no está en las celdas visitadas, se agrega a las celdas visitadas.
        if (!celdasVisitadas.containsKey(posicion)) {
            celdasVisitadas.put(posicion, currentGrid);
            incExploredGrids();
        }
        // Se obtiene la posición del queso.
        Pair<Integer, Integer> posQueso = new Pair(cheese.getX(), cheese.getY());
        
        // Si el queso ya ha sido visitado, se busca el camino más corto.
        if (celdasVisitadas.containsKey(posQueso)) {
            if (tipo == 0) {
                tipo = 1;
                celdaExploracion = currentGrid;
            }
            Grid celdaQueso = new Grid(cheese.getX(), cheese.getY());
            if (recorreCamino.isEmpty()) {
                // Se busca el camino más corto.
                funcionBPL(celdaQueso, currentGrid,0);
            }
            pilaMovimientos.add(currentGrid);
            return seguirCamino(currentGrid);
        } else {
            // Si el queso no ha sido visitado, se explora el laberinto.
            if (tipo == 0) {
                // Se obtienen las celdas adyacentes a la actual.
                ArrayList<Integer> celdasPosibles = movimientosPosibles(currentGrid); 
                // Si hay celdas adyacentes a la actual.
                if (!celdasPosibles.isEmpty()) { 
                    pilaMovimientos.add(currentGrid);
                    // Se recorre aleatoriamente el laberinto.
                    return exploracion(celdasPosibles);
                } else {
                    // Nos lleva a la anterior visitada.
                    return volver(currentGrid);    
                }

            } else {
                // Si se está buscando el queso, se sigue el camino más corto.
                if (recorreCamino.isEmpty()) {
                    // Se busca el camino más corto.
                    funcionBPL(celdaExploracion, currentGrid,0);
                }
                if (recorreCamino.size() == 1) {
                    tipo = 0;
                }
                //Sigue el camino mas corto
                return seguirCamino(currentGrid);
            }
        }

    }


    /**
* @brief Método que realiza la búsqueda en profundidad limitada para encontrar un camino hasta el queso.
*@param quesoIni Celda visitada donde está el queso.
*@param destino Celda actual (currentGrid).
*@param profundidad La profundidad máxima permitida para la búsqueda en profundidad.
*@return Booleano que indica si se encontró un camino hasta el queso dentro del límite de profundidad.
     */
    private boolean funcionBPL(Grid quesoIni, Grid destino, int profundidad) {
        // Se verifica si se ha alcanzado el límite de profundidad.
        if(profundidad>=PROFUNDIDAD_LIMITE)
            return false;
        // Se inicializan los atributos para la búsqueda en profundidad.
        camino = false;
        recorreCamino.clear();
        cerradas.clear();
        // Se obtiene la celda donde se encuentra el queso y se marca como cerrada.
        Grid queso = celdasVisitadas.get(new Pair<>(quesoIni.getX(), quesoIni.getY()));
        Pair<Integer, Integer> pairQueso = new Pair<>(quesoIni.getX(), quesoIni.getY());
        cerradas.put(pairQueso, pairQueso);
        // Se agrega la celda del queso a la pila de celdas recorridas.
        recorreCamino.add(queso);
        // Se obtienen las celdas adyacentes a la celda del queso.
        ArrayList<Grid> abiertas = adyacentes(queso);
        // Se realiza la búsqueda en profundidad para cada celda adyacente.
        for (int i = 0; i < abiertas.size(); i++) {
            funcionBPLPrivada(abiertas.get(i), destino, profundidad+1);
            if (camino) {
                return recorreCamino.isEmpty();
            }
        }
        // Si no se encontró un camino hasta el queso dentro del límite de profundidad, se devuelve false.
        return recorreCamino.isEmpty();

    }

    /**
     * @brief Función recursiva que implementa una búsqueda en profundidad limitada para encontrar un camino desde una posición inicial hasta una posición destino.
     * @param actual actual posición actual a partir de la cual se va a realizar la búsqueda
     * @param destino destino posición destino a la que se quiere llegar
     * @param profundidad profundidad actual en la que se encuentra la búsqueda
     * @return void
     */
    private void funcionBPLPrivada(Grid actual, Grid destino, int profundidad) {
        // Si se ha alcanzado la profundidad límite, se sale de la funcion
        if(PROFUNDIDAD_LIMITE<=profundidad)
            return;
        //Si ya se ha encontrado un camino, se sale de la función
        if (!camino) {
            Pair<Integer, Integer> pairQueso = new Pair<>(actual.getX(), actual.getY());
            // Si la posición actual es igual a la posición destino, se ha encontrado un camino
            if (actual.getX() == destino.getX() && actual.getY() == destino.getY()) {
                camino = true;
            } else {
                cerradas.put(pairQueso, pairQueso);
                recorreCamino.add(actual);
                ArrayList<Grid> abiertas = adyacentes(actual);
                for (int i = 0; i < abiertas.size(); i++) {
                    // Se llama a la función de forma recursiva para cada nodo adyacente
                    funcionBPLPrivada(abiertas.get(i), destino, profundidad+1);
                }
                if (!camino) {
                    // Si no se ha encontrado un camino en la rama actual, se elimina la posición actual del camino
                    recorreCamino.pop();
                }
            }
        }
    }

    /**
     * @brief Metodo que sigue vuelve hacia atras DFS
     * @param currentGrid celda actual
     * @return Entero con el movimiento a realizar
     */
    private int seguirCamino(Grid currentGrid) {
        int atras = -1;
        Grid anterior = recorreCamino.pop();
        if (actualDerecha(currentGrid, anterior)) {
            atras = Mouse.LEFT;
        }
        if (actualIzquierda(currentGrid, anterior)) {
            atras = Mouse.RIGHT;
        }
        if (actualAbajo(currentGrid, anterior)) {
            atras = Mouse.UP;
        }
        if (actualArriba(currentGrid, anterior)) {
            atras = Mouse.DOWN;
        }
        return atras;
    }
    
    /**
     * @brief Esta función devuelve una lista de los nodos adyacentes a una celda dada.
     * @param currentGrid Es la celda actual de la que se buscan los adyacentes.
     * @return ArrayList<Grid> Es una lista de celdas adyacentes que se pueden visitar.
     */
    private ArrayList<Grid> adyacentes(Grid currentGrid) {
        //Inicializamos el ArrayList de salida de la funcion
        ArrayList<Grid> salida = new ArrayList<>();
        // Comprobar si existe la celda superior, se puede llegar a ella desde la celda actual y no está cerrada
        if (celdasVisitadas.containsKey(new Pair<>(currentGrid.getX(), currentGrid.getY() + 1)) && currentGrid.canGoUp() && !cerradas.containsKey(new Pair<>(currentGrid.getX(), currentGrid.getY() + 1))) {
            salida.add(celdasVisitadas.get(new Pair<>(currentGrid.getX(), currentGrid.getY() + 1)));
        }
        // Comprobar si existe la celda inferior, se puede llegar a ella desde la celda actual y no está cerrada
        if (celdasVisitadas.containsKey(new Pair<>(currentGrid.getX(), currentGrid.getY() - 1)) && currentGrid.canGoDown() && !cerradas.containsKey(new Pair<>(currentGrid.getX(), currentGrid.getY() - 1))) {
            salida.add(celdasVisitadas.get(new Pair<>(currentGrid.getX(), currentGrid.getY() - 1)));
        }
        // Comprobar si existe la celda izquierda, se puede llegar a ella desde la celda actual y no está cerrada
        if (celdasVisitadas.containsKey(new Pair<>(currentGrid.getX() - 1, currentGrid.getY())) && currentGrid.canGoLeft() && !cerradas.containsKey(new Pair<>(currentGrid.getX() - 1, currentGrid.getY()))) {
            salida.add(celdasVisitadas.get(new Pair<>(currentGrid.getX() - 1, currentGrid.getY())));
        }
        // Comprobar si existe la celda derecha, se puede llegar a ella desde la celda actual y no está cerrada
        if (celdasVisitadas.containsKey(new Pair<>(currentGrid.getX() + 1, currentGrid.getY())) && currentGrid.canGoRight() && !cerradas.containsKey(new Pair<>(currentGrid.getX() + 1, currentGrid.getY()))) {
            salida.add(celdasVisitadas.get(new Pair<>(currentGrid.getX() + 1, currentGrid.getY())));
        }
        return salida;
    }

    /**
     * @brief Esta función devuelve una lista de enteros que representan los movimientos posibles a realizar a partir de una celda del laberinto.
     * @param currentGrid currentGrid la celda actual del laberinto
     * @return una lista de enteros que representan los movimientos posibles a realizar desde la celda actual del laberinto
     */
    private ArrayList<Integer> movimientosPosibles(Grid currentGrid) {
        // Se inicializa una lista vacía para almacenar los movimientos posibles
        ArrayList<Integer> salida = new ArrayList<>();
        // Se obtienen las coordenadas de la celda actual
        int x = currentGrid.getX(); 
        int y = currentGrid.getY();
        // Si es posible moverse hacia arriba, se añade el movimiento a la lista de movimientos posibles
        if (currentGrid.canGoUp()) {
            Pair<Integer, Integer> pos = new Pair(x, y + 1);
            // Si la celda adyacente a la que se movería no ha sido visitada, se añade el movimiento a la lista de movimientos posibles
            if (!celdasVisitadas.containsKey(pos)) {   
                salida.add(Mouse.UP);
            }
        }
        // Si es posible moverse hacia abajo, se añade el movimiento a la lista de movimientos posibles
        if (currentGrid.canGoDown()) { 
            Pair<Integer, Integer> pos = new Pair(x, y - 1);
            // Si la celda adyacente a la que se movería no ha sido visitada, se añade el movimiento a la lista de movimientos posibles
            if (!celdasVisitadas.containsKey(pos)) { 
                salida.add(Mouse.DOWN); 
            }
        }
        // Si es posible moverse hacia la izquierda, se añade el movimiento a la lista de movimientos posibles
        if (currentGrid.canGoLeft()) { 
            Pair<Integer, Integer> pos = new Pair(x - 1, y);
            // Si la celda adyacente a la que se movería no ha sido visitada, se añade el movimiento a la lista de movimientos posibles
            if (!celdasVisitadas.containsKey(pos)) { 
                salida.add(Mouse.LEFT); 
            }
        }
        // Si es posible moverse hacia la derecha, se añade el movimiento a la lista de movimientos posibles
        if (currentGrid.canGoRight()) { 
            Pair<Integer, Integer> pos = new Pair(x + 1, y);
             // Si la celda adyacente a la que se movería no ha sido visitada, se añade el movimiento a la lista de movimientos posibles
            if (!celdasVisitadas.containsKey(pos)) { 
                salida.add(Mouse.RIGHT); 
            }
        }
        //devuelve la lista de movimientos posibles
        return salida;
    }

    /**
     * @brief Función que se encarga de calcular la dirección a la que se debe mover el ratón para volver
       a la celda anterior en la que estuvo.
     * @param currentGrid La posición actual del ratón en el laberinto.
     * @return El movimiento necesario para volver a la posición anterior en la pila de movimientos, o -1 en caso de 
     * no tener una posición anterior almacenada.
     */
    private int volver(Grid currentGrid) {
        int Anterior = -1; // Dirección por defecto en caso de no encontrar ninguna dirección
        Grid girdAnterior = pilaMovimientos.pop(); // Se obtiene la ultima celda de la pila
        if (actualDerecha(currentGrid, girdAnterior)) { 
            Anterior = Mouse.LEFT; // Si la anterior está a la derecha, debe ir a la izquierda
        }
        if (actualArriba(currentGrid, girdAnterior)) {
            Anterior = Mouse.DOWN; // Si la anterior está abajo, debe ir hacia abajo
        }
        if (actualIzquierda(currentGrid, girdAnterior)) { 
            Anterior = Mouse.RIGHT; // Si la anterior está a la izquierda, debe ir a la derecha
        }
        if (actualAbajo(currentGrid, girdAnterior)) {
            Anterior = Mouse.UP; // Si la anterior está arriba, debe ir hacia arriba
        }
        return Anterior; //Devuelve la direccion a la quedebe ir el raton para volver
    }

    /**
     * @brief Esta función devuelve un número aleatorio dentro del rango de celdas posibles para la exploración.
     * @param celdasPosibles Lista de celdas disponibles para explorar.
     * @return devuelve la celda que debe seguir
     */
    private int exploracion(ArrayList<Integer> celdasPosibles) {
        Random aleatorio = new Random();// Se crea un objeto Random para generar un número aleatorio.
        return celdasPosibles.get(aleatorio.nextInt(celdasPosibles.size())); // Se devuelve un número aleatorio dentro del rango de celdas posibles.
    }

    /**
     * @brief Método que se llama cuando aparece un nuevo queso
     */
    @Override
    public void newCheese() {
    }

    /**
     * @brief Método que se llama cuando el raton pisa una bomba
     */
    @Override
    public void respawned() {

    }

    /**
     * @brief Método para evaluar que no nos movamos a la misma celda anterior
     * @param direction Direccion del raton
     * @param currentGrid Celda actual
     * @return True Si las casillas X e Y anterior son distintas a las actuales
     */
    public boolean testGrid(int direction, Grid currentGrid) {
        if (lastGrid == null) {
            return true;
        }

        int x = currentGrid.getX();
        int y = currentGrid.getY();

        switch (direction) {
            case UP:
                y += 1;
                break;

            case DOWN:
                y -= 1;
                break;

            case LEFT:
                x -= 1;
                break;

            case RIGHT:
                x += 1;
                break;
        }

        return !(lastGrid.getX() == x && lastGrid.getY() == y);

    }

    /**
     *
     * @brief Método que devuelve si de una casilla dada, está contenida en el
     * mapa de celdasVisitadas
     * @param casilla Casilla que se pasa para saber si ha sido visitada
     * @param direccion Dirección de la casilla visitada
     * @return True Si la casilla vecina que indica la dirección había sido
     * visitada
     */
    public boolean visitada(Grid casilla, int direccion) {
        int x = casilla.getX();
        int y = casilla.getY();

        switch (direccion) {
            case UP:
                y += 1;
                break;

            case DOWN:
                y -= 1;
                break;

            case LEFT:
                x -= 1;
                break;

            case RIGHT:
                x += 1;
                break;
        }
        Pair par = new Pair(x, y);
        return celdasVisitadas.containsKey(par);
    }

    /**
     * @brief Método para calcular si una casilla está en una posición relativa
     * respecto a otra
     * @param actual Celda actual
     * @param anterior Celda anterior
     * @return True Si la posición Y de la actual es mayor que la de la anterior
     */
    public boolean actualArriba(Grid actual, Grid anterior) {
        return actual.getY() > anterior.getY();
    }

    /**
     * @brief Método para calcular si una casilla está en una posición relativa
     * respecto a otra
     * @param actual Celda actual
     * @param anterior Celda anterior
     * @return True Si la posición Y de la actual es menor que la de la anterior
     */
    public boolean actualAbajo(Grid actual, Grid anterior) {
        return actual.getY() < anterior.getY();
    }

    /**
     * @brief Método para calcular si una casilla está en una posición relativa
     * respecto a otra
     * @param actual Celda actual
     * @param anterior Celda anterior
     * @return True Si la posición X de la actual es mayor que la de la anterior
     */
    public boolean actualDerecha(Grid actual, Grid anterior) {
        return actual.getX() > anterior.getX();
    }

    /**
     * @brief Método para calcular si una casilla está en una posición relativa
     * respecto a otra
     * @param actual Celda actual
     * @param anterior Celda anterior
     * @return True Si la posición X de la actual es menor que la de la anterior
     */
    public boolean actualIzquierda(Grid actual, Grid anterior) {
        return actual.getX() < anterior.getX();
    }
}

    