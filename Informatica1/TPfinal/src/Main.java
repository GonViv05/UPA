/**
 * Punto de entrada de la versión de consola.
 * Es la clase que se ejecuta con "java -cp out Main". Su única tarea es
 * arrancar partidas y preguntar si se quiere jugar otra.
 */
public class Main {
    public static void main(String[] args) {
        // "java -cp out Main --sin-color" apaga los colores (para terminales que no los muestran bien)
        for (String arg : args) {
            if (arg.equals("--sin-color")) {
                Consola.desactivarColores();
            }
        }

        boolean seguir = true;
        while (seguir) {
            // Cada vuelta crea un Juego nuevo: así todo (luchador, plata, sumo) arranca de cero
            new Juego().jugar();
            System.out.println("\n¿Jugar de nuevo?  1) Sí   2) No");
            // Si elige 1, seguir queda en true y el while da otra vuelta; si elige 2, termina
            seguir = Consola.leerOpcion(1, 2) == 1;
        }
        System.out.println("¡Hasta la próxima!");
    }
}
