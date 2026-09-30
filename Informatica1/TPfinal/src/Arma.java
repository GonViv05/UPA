import java.util.Random;

/**
 * Un arma. Cada subclase tiene su propio efecto al golpear (polimorfismo).
 * Es abstracta: no existe "un arma a secas", solo Puños, Mazo, Dagas o Látigo.
 */
public abstract class Arma {
    // protected: los ven las subclases (Mazo, Dagas...) pero no el resto del programa
    protected String nombre;
    protected String descripcion;
    protected int precio;         // precio base en la tienda, antes del descuento por carisma
    protected Atributo requisito; // null = cualquiera la puede usar
    protected int minimo;         // valor mínimo del requisito (por ejemplo 6 en "[Fuerza 6]")

    public Arma(String nombre, String descripcion, int precio, Atributo requisito, int minimo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.requisito = requisito;
        this.minimo = minimo;
    }

    /**
     * Lo que pasa al atacar, igual para todas las armas:
     * primero el rival puede esquivar, después se tira el crítico y recién ahí golpea.
     * Así esa lógica común está en un solo lugar y cada arma solo escribe su golpe.
     */
    public String usar(Luchador atacante, Enemigo objetivo, Random azar) {
        if (azar.nextDouble() < objetivo.probabilidadEvadir()) {
            return objetivo.textoEvasion(azar);
        }
        boolean critico = azar.nextDouble() < Personaje.PROB_CRITICO;
        String texto = golpear(atacante, objetivo, azar, critico);
        if (critico) {
            texto = Consola.color("¡CRÍTICO! ", Consola.AMARILLO + Consola.NEGRITA) + texto;
        }
        return texto;
    }

    /**
     * Golpea al enemigo y devuelve qué pasó, para mostrarlo.
     * Es abstracto: no tiene cuerpo acá y cada subclase está obligada a escribirlo.
     * Si critico es true, el arma tiene que hacer el doble de daño.
     * Devuelve un String en vez de imprimir, así el arma no depende de cómo se muestra el texto.
     */
    public abstract String golpear(Luchador atacante, Enemigo objetivo, Random azar, boolean critico);

    /** Texto del requisito, por ejemplo "[Fuerza 6]" (vacío si no tiene). */
    public String textoRequisito() {
        // Operador ternario: condición ? valor si es true : valor si es false
        return requisito == null ? "" : "[" + requisito.getNombre() + " " + minimo + "]";
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getPrecio() { return precio; }
    public Atributo getRequisito() { return requisito; }
    public int getMinimo() { return minimo; }
}
