/**

 * Los tres atributos de un luchador.
 * Es un enum: una lista cerrada de valores. Así no se puede escribir un atributo
 * que no existe (por ejemplo "Fuersa"), porque el compilador lo rechaza.
 */
public enum Atributo {
    FUERZA("Fuerza"),
    AGILIDAD("Agilidad"),
    CARISMA("Carisma");

    // Nombre "lindo" para mostrar en pantalla (FUERZA -> "Fuerza")
    private final String nombre;

    // El constructor de un enum es privado: solo lo usan los tres valores de arriba
    Atributo(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
