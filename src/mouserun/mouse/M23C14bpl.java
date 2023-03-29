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
     * @brief Metodo que mueve al raton
     * @param currentGrid celda actual
     * @param cheese queso del laberinto
     * @return Retorna la celda donde se mueve el raton
     */
    @Override
    public int move(Grid currentGrid, Cheese cheese) {
        Pair<Integer, Integer> posicion = new Pair(currentGrid.getX(), currentGrid.getY());

        if (!celdasVisitadas.containsKey(posicion)) {
            celdasVisitadas.put(posicion, currentGrid);
            incExploredGrids();
        }

        Pair<Integer, Integer> posQueso = new Pair(cheese.getX(), cheese.getY());
        

        if (celdasVisitadas.containsKey(posQueso)) {
            if (tipo == 0) {
                tipo = 1;
                celdaExploracion = currentGrid;
            }
            Grid celdaQueso = new Grid(cheese.getX(), cheese.getY());
            if (recorreCamino.isEmpty()) {
                caminoDFS(celdaQueso, currentGrid,0);
            }
            pilaMovimientos.add(currentGrid);
            return seguirCamino(currentGrid);
        } else {
            if (tipo == 0) {
                ArrayList<Integer> celdasPosibles = movimientosPosibles(currentGrid); //Vector que nos dice los movimientos de la celda actual
                if (!celdasPosibles.isEmpty()) { //Si hay movimientos
                    pilaMovimientos.add(currentGrid);

                    return exploracion(celdasPosibles);// Recorre aleatoriamente el laberinto 
                } else {
                    return volver(currentGrid);//Nos lleva a la anterior visitada    
                }

            } else {
                if (recorreCamino.isEmpty()) {
                    caminoDFS(celdaExploracion, currentGrid,0);
                }
                if (recorreCamino.size() == 1) {
                    tipo = 0;
                }
                return seguirCamino(currentGrid);
            }
        }

    }


    /**
     * @brief Metodo que calcula la busqueda en profundidad
     * @param destino celda actual (currentGrid)
     * @param quesoIni celda visitada donde esta el queso
     * @param profundidad profundidad a llevar a cabo
     * @return booleano dependiendo si hay camino
     */
    private boolean caminoDFS(Grid quesoIni, Grid destino, int profundidad) {
        if(profundidad>=PROFUNDIDAD_LIMITE)
            return false;
        //Inicializamos todo
        camino = false;
        recorreCamino.clear();
        cerradas.clear();
        //
        Grid queso = celdasVisitadas.get(new Pair<>(quesoIni.getX(), quesoIni.getY()));
        Pair<Integer, Integer> pairQueso = new Pair<>(quesoIni.getX(), quesoIni.getY());
        cerradas.put(pairQueso, pairQueso);
        recorreCamino.add(queso);
        ArrayList<Grid> abiertas = adyacentes(queso);
        for (int i = 0; i < abiertas.size(); i++) {
            caminoDFSPrivado(abiertas.get(i), destino, profundidad+1);
            if (camino) {
                return recorreCamino.isEmpty();
            }
        }
        return recorreCamino.isEmpty();

    }

    /**
     * @brief Metodo que calcula la busqueda en profundidad
     * @param actual celda por donde vamos en la busqueda en profundidad
     * @param destino celda actual (currentGrid)
     */
    private void caminoDFSPrivado(Grid actual, Grid destino, int profundidad) {
        if(PROFUNDIDAD_LIMITE<=profundidad)
            return;
        if (!camino) {
            Pair<Integer, Integer> pairQueso = new Pair<>(actual.getX(), actual.getY());
            if (actual.getX() == destino.getX() && actual.getY() == destino.getY()) {
                camino = true;
            } else {
                cerradas.put(pairQueso, pairQueso);
                recorreCamino.add(actual);
                ArrayList<Grid> abiertas = adyacentes(actual);
                for (int i = 0; i < abiertas.size(); i++) {
                    caminoDFSPrivado(abiertas.get(i), destino, profundidad+1);
                }
                if (!camino) {
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
     * @brief Metodo que nos sirve para saber los movimientos posbiles para
     * seguir el camino de DFS
     * @param currentGrid celda actual
     * @return Vector con los posibles movimientos
     */
    private ArrayList<Grid> adyacentes(Grid currentGrid) {
        ArrayList<Grid> salida = new ArrayList<>();

        if (celdasVisitadas.containsKey(new Pair<>(currentGrid.getX(), currentGrid.getY() + 1)) && currentGrid.canGoUp() && !cerradas.containsKey(new Pair<>(currentGrid.getX(), currentGrid.getY() + 1))) {
            salida.add(celdasVisitadas.get(new Pair<>(currentGrid.getX(), currentGrid.getY() + 1)));
        }
        if (celdasVisitadas.containsKey(new Pair<>(currentGrid.getX(), currentGrid.getY() - 1)) && currentGrid.canGoDown() && !cerradas.containsKey(new Pair<>(currentGrid.getX(), currentGrid.getY() - 1))) {
            salida.add(celdasVisitadas.get(new Pair<>(currentGrid.getX(), currentGrid.getY() - 1)));
        }
        if (celdasVisitadas.containsKey(new Pair<>(currentGrid.getX() - 1, currentGrid.getY())) && currentGrid.canGoLeft() && !cerradas.containsKey(new Pair<>(currentGrid.getX() - 1, currentGrid.getY()))) {
            salida.add(celdasVisitadas.get(new Pair<>(currentGrid.getX() - 1, currentGrid.getY())));
        }
        if (celdasVisitadas.containsKey(new Pair<>(currentGrid.getX() + 1, currentGrid.getY())) && currentGrid.canGoRight() && !cerradas.containsKey(new Pair<>(currentGrid.getX() + 1, currentGrid.getY()))) {
            salida.add(celdasVisitadas.get(new Pair<>(currentGrid.getX() + 1, currentGrid.getY())));
        }
        return salida;
    }

    /**
     * @brief Metodo que devuelve los posibles movimientos para ir
     * @param currentGrid celda actual
     * @return Vector con los posibles movimientos
     */
    private ArrayList<Integer> movimientosPosibles(Grid currentGrid) {
        ArrayList<Integer> salida = new ArrayList<>(); //Vector de salida de movimiento
        int x = currentGrid.getX(); //Casilla del eje X de la celda actual
        int y = currentGrid.getY();//Casilla del eje Y de la celda actual
        if (currentGrid.canGoUp()) { //Si puede ir hacia arriba
            Pair<Integer, Integer> pos = new Pair(x, y + 1); //
            if (!celdasVisitadas.containsKey(pos)) {   //Sino ha sido visitada subimos
                salida.add(Mouse.UP); //Metemos la direccion arriba para ir en el vector de salida
            }
        }
        if (currentGrid.canGoDown()) { //Si se puede ir hacia abajo
            Pair<Integer, Integer> pos = new Pair(x, y - 1);
            if (!celdasVisitadas.containsKey(pos)) { //Sino ha sido visitada bajamos
                salida.add(Mouse.DOWN); //Metemos la direccion abajo para ir en el vector de salida
            }
        }

        if (currentGrid.canGoLeft()) { //Si se puede ir hacia la izquierda
            Pair<Integer, Integer> pos = new Pair(x - 1, y);
            if (!celdasVisitadas.containsKey(pos)) { //Sino ha sido visitada vamos hacia la izquierda
                salida.add(Mouse.LEFT); //Metemos la direccion izquierda para ir en el vector de salida
            }
        }
        if (currentGrid.canGoRight()) { //Si se puede ir hacia la derecha
            Pair<Integer, Integer> pos = new Pair(x + 1, y);
            if (!celdasVisitadas.containsKey(pos)) { //Sino ha sido visitada vamos hacia la derecha
                salida.add(Mouse.RIGHT); //Metemos la direccion derecha para ir en el vector de salida
            }
        }
        return salida; // Retornamos el vector de salida
    }

    /**
     * @brief Metodo que vuelve hacia la casilla anterior
     * @param currentGrid celda actual
     * @return retorna la posicion anterior
     */
    private int volver(Grid currentGrid) {
        int Anterior = -1;//en caso de no meter ninguna mete -1
        Grid girdAnterior = pilaMovimientos.pop();
        if (actualDerecha(currentGrid, girdAnterior)) { // Se comprueba si la anterior es la izquierda
            Anterior = Mouse.LEFT;
        }
        if (actualArriba(currentGrid, girdAnterior)) { // Se comprueba si la anterior es abajo
            Anterior = Mouse.DOWN;
        }
        if (actualIzquierda(currentGrid, girdAnterior)) { // Se comprueba si la anterior es hacia la derecha
            Anterior = Mouse.RIGHT;
        }
        if (actualAbajo(currentGrid, girdAnterior)) {// Se comprueba si la anterior es hacia arriba
            Anterior = Mouse.UP;
        }
        return Anterior;
    }

    /**
     * @brief Metodo que recorre el laberinto
     * @param celdasPosibles Vector de celdas disponibles a seguir
     * @return Retorna la celda donde se puede seguir
     */
    private int exploracion(ArrayList<Integer> celdasPosibles) {
        Random aleatorio = new Random();//Creamos un objeto aleatorio
        return celdasPosibles.get(aleatorio.nextInt(celdasPosibles.size())); //retornamos un aleatorio de las posibles celdas
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

    