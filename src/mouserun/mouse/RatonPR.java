package mouserun.mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;
import mouserun.game.Mouse;
import mouserun.game.Grid;
import mouserun.game.Cheese;

/**
 *
 * @author Domin
 */
public class RatonPR extends Mouse {

    /**
     * Variable para almacenar la ultima celda visitada
     */
    private Grid lastGrid;
    /**
     * Contador del numero de casillas
     */
    private int num_celdasVisitadas;

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
     * Pila para almacenar el camino recorrido.
     */
    private final Stack<Integer> pilaMovimientos;

    /**
     * Constructor (Puedes modificar el nombre a tu gusto).
     */
    public RatonPR() {
        super("F_Alonso");
        celdasVisitadas = new HashMap<>();
        pilaMovimientos = new Stack<>();
    }

    /**
     */
    @Override
    public int move(Grid currentGrid, Cheese cheese) {
        //Metemos el Grid Actual en el terreno recorrido
        Pair actual = GridToPair(currentGrid,0);
        celdasVisitadas.put(actual, currentGrid);
        //Ahora vemos hacia donde nos podemos mover
        if(currentGrid.canGoUp() && celdasVisitadas.get(GridToPair(currentGrid,1)) == null){
            incExploredGrids();
            pilaMovimientos.push(1);
            return Mouse.UP;
        }else if((currentGrid.canGoRight() && celdasVisitadas.get(GridToPair(currentGrid,2)) == null)){
            incExploredGrids();
            pilaMovimientos.push(2);
            return Mouse.RIGHT;
        }else if(currentGrid.canGoDown() && celdasVisitadas.get(GridToPair(currentGrid,3)) == null){
            incExploredGrids();
            pilaMovimientos.push(3);
            return Mouse.DOWN;
        }else if(currentGrid.canGoLeft() && celdasVisitadas.get(GridToPair(currentGrid,4)) == null){
            incExploredGrids();
            pilaMovimientos.push(4);
            return Mouse.LEFT;
        }else if(!pilaMovimientos.isEmpty()){ //Si no se cumple ninguna condicion y la pila no est?? vac??a, hay que ir hacia atras
            switch(pilaMovimientos.pop()){
            case 1:
                System.out.println("abajo");
                return Mouse.DOWN;
            case 2:
                System.out.println("izq");
                return Mouse.LEFT;
            case 3:
                System.out.println("arri");
                return Mouse.UP;
            case 4:
                System.out.println("dere");
                return Mouse.RIGHT;    
            }
        }
        return Mouse.BOMB;
    }
    
    public int moveStack(int movimiento){
        switch (movimiento) {
            case 1:
                return Mouse.DOWN;
            case 2:
                return Mouse.LEFT;
            case 3:
                return Mouse.UP;
            case 4:
                return Mouse.RIGHT;
        }
        return Mouse.BOMB;
    }
    /**
     * 0.- Par actual
     * 1.- Arriba
     * 2.- Derecha
     * 3.- Abajo
     * 4.- Izquierda
    */
    private Pair<Integer,Integer> GridToPair(Grid grid, int movimiento){
        int x = grid.getX();
        int y = grid.getY();
        Pair p = new Pair(x,y);
        switch (movimiento){
            case 0:
                p = new Pair(x,y);
                return p;         
            case 1:
                p = new Pair(x,y+1);
                return p;
            case 2:
                p = new Pair(x+1,y);
                return p;
            case 3:
                p = new Pair(x,y-1);
                return p;
            case 4:
                p = new Pair(x-1,y);
                return p;        
        }
        return p;
    }
    
    /**
     * @brief M??todo que se llama cuando aparece un nuevo queso
     */
    @Override
    public void newCheese() {
        
    }

    /**
     * @brief M??todo que se llama cuando el raton pisa una bomba
     */
    @Override
    public void respawned() {
        
    }

    /**
     * @brief M??todo para evaluar que no nos movamos a la misma celda anterior
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
     * @brief M??todo que devuelve si de una casilla dada, est?? contenida en el
     * mapa de celdasVisitadas
     * @param casilla Casilla que se pasa para saber si ha sido visitada
     * @param direccion Direcci??n de la casilla visitada
     * @return True Si la casilla vecina que indica la direcci??n hab??a sido
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
     * @brief M??todo para calcular si una casilla est?? en una posici??n relativa
     * respecto a otra
     * @param actual Celda actual
     * @param anterior Celda anterior
     * @return True Si la posici??n Y de la actual es mayor que la de la anterior
     */
    public boolean actualArriba(Grid actual, Grid anterior) {
        return actual.getY() > anterior.getY();
    }

    /**
     * @brief M??todo para calcular si una casilla est?? en una posici??n relativa
     * respecto a otra
     * @param actual Celda actual
     * @param anterior Celda anterior
     * @return True Si la posici??n Y de la actual es menor que la de la anterior
     */
    public boolean actualAbajo(Grid actual, Grid anterior) {
        return actual.getY() < anterior.getY();
    }

    /**
     * @brief M??todo para calcular si una casilla est?? en una posici??n relativa
     * respecto a otra
     * @param actual Celda actual
     * @param anterior Celda anterior
     * @return True Si la posici??n X de la actual es mayor que la de la anterior
     */
    public boolean actualDerecha(Grid actual, Grid anterior) {
        return actual.getX() > anterior.getX();
    }

    /**
     * @brief M??todo para calcular si una casilla est?? en una posici??n relativa
     * respecto a otra
     * @param actual Celda actual
     * @param anterior Celda anterior
     * @return True Si la posici??n X de la actual es menor que la de la anterior
     */
    public boolean actualIzquierda(Grid actual, Grid anterior) {
        return actual.getX() < anterior.getX();
    }

    /**
     *
     * @author josema
     * @param <U> First field (key) in a Pair
     * @param <V> Second field (value) in a Pair
     */
// Pair class
    class Pair<U, V> {

        public final U first;       // el primer campo de un par
        public final V second;      // el segundo campo de un par

        // Construye un nuevo par con valores especificados
        private Pair(U first, V second) {
            this.first = first;
            this.second = second;
        }
        
        @Override
        // Verifica que el objeto especificado sea "igual a" el objeto actual o no
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            
            Pair<?, ?> pair = (Pair<?, ?>) o;

            // llamar al m??todo `equals()` de los objetos subyacentes
            if (!first.equals(pair.first)) {
                return false;
            }
            return second.equals(pair.second);
        }
        
        @Override
        // Calcula el c??digo hash de un objeto para admitir tablas hash
        public int hashCode() {
            // usa c??digos hash de los objetos subyacentes
            return 31 * first.hashCode() + second.hashCode();
        }
        
        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }
        
    }
    
}
