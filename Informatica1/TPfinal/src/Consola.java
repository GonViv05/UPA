import java.util.Random;
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

    // Códigos ANSI: secuencias especiales que la terminal interpreta como "cambiá el color".
    // \u001B es el carácter ESC; RESET vuelve al color normal.
    public static final String RESET = "\u001B[0m";
    public static final String NEGRITA = "\u001B[1m";
    public static final String ROJO = "\u001B[31m";
    public static final String VERDE = "\u001B[32m";
    public static final String AMARILLO = "\u001B[33m";
    public static final String CIAN = "\u001B[36m";

    // Se apaga con "--sin-color" para terminales que no entienden los códigos ANSI
    private static boolean colores = true;

    public static void desactivarColores() {
        colores = false;
    }

    /** Pinta el texto con el color pedido (o lo deja igual si los colores están apagados). */
    public static String color(String texto, String codigo) {
        if (!colores) {
            return texto;
        }
        return codigo + texto + RESET;
    }

    /** Elige una frase al azar de la lista, para que los textos no se repitan siempre iguales. */
    public static String alAzar(String[] opciones, Random azar) {
        // nextInt(n) da un número entre 0 y n-1: justo un índice válido del arreglo
        return opciones[azar.nextInt(opciones.length)];
    }

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

    /** Barra de vida con color: verde, amarilla con 60 % o menos y roja con 30 % o menos. */
    public static String barraVida(int valor, int maximo, int ancho) {
        // Se multiplica en vez de dividir para comparar porcentajes sin decimales:
        // "valor / maximo <= 30 %" es lo mismo que "valor * 100 <= maximo * 30"
        String codigo;
        if (valor * 100 <= maximo * 30) {
            codigo = ROJO;
        } else if (valor * 100 <= maximo * 60) {
            codigo = AMARILLO;
        } else {
            codigo = VERDE;
        }
        return color(barra(valor, maximo, ancho), codigo);
    }
}
