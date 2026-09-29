import java.util.Scanner;

/**
 * Ayudas para leer y mostrar cosas por la terminal.
 * Todos los métodos son static: se usan directamente como Consola.leerOpcion(1, 4),
 * sin crear un objeto Consola.
 */
public class Consola {
    // Un solo Scanner para todo el programa. Si se crearan varios sobre System.in,
    // podrían "robarse" líneas entre ellos.
    private static final Scanner teclado = new Scanner(System.in);

    /** Pide un número entre min y max hasta que el usuario escriba uno válido. */
    public static int leerOpcion(int min, int max) {
        // Bucle infinito: la única salida es el return con un número válido
        while (true) {
            System.out.print("> ");
            System.out.flush(); // fuerza a que el "> " se vea antes de esperar la respuesta
            if (!teclado.hasNextLine()) {
                System.exit(0); // se cerró la entrada (Ctrl+D)
            }
            // Se lee la línea entera (y no nextInt) para que un texto inválido no quede trabado en el Scanner
            String linea = teclado.nextLine().trim();
            try {
                int opcion = Integer.parseInt(linea); // lanza NumberFormatException si no es un número
                if (opcion >= min && opcion <= max) {
                    return opcion;
                }
            } catch (NumberFormatException e) {
                // no era un número: se vuelve a pedir
            }
            // Si llegó acá, el número estaba fuera de rango o no era un número
            System.out.println("Elegí un número entre " + min + " y " + max + ".");
        }
    }

    /** Lee una línea de texto (se usa para el nombre del luchador). */
    public static String leerTexto() {
        System.out.print("> ");
        System.out.flush();
        if (!teclado.hasNextLine()) {
            System.exit(0);
        }
        return teclado.nextLine().trim(); // trim() saca los espacios de los costados
    }

    /** Frena el programa hasta que el usuario apriete Enter, para que pueda leer tranquilo. */
    public static void pausa() {
        System.out.print("\n(Enter para seguir)");
        System.out.flush();
        if (teclado.hasNextLine()) {
            teclado.nextLine(); // lo que haya escrito no importa, solo esperamos el Enter
        }
    }

    /** Muestra un título entre dos líneas de "=" del mismo largo que el texto. */
    public static void titulo(String texto) {
        // +4 por los dos espacios de sangría a cada lado del texto
        String linea = "=".repeat(texto.length() + 4);
        System.out.println("\n" + linea);
        System.out.println("  " + texto);
        System.out.println(linea);
    }

    /** Barra de texto, por ejemplo [########------] 80/140 */
    public static String barra(int valor, int maximo, int ancho) {
        // Regla de tres: qué parte del ancho corresponde al valor.
        // El (double) evita la división entera (70 / 140 daría 0 en vez de 0.5)
        int llenos = (int) Math.round((double) valor / maximo * ancho);
        return "[" + "#".repeat(llenos) + "-".repeat(ancho - llenos) + "] " + valor + "/" + maximo;
    }
}
