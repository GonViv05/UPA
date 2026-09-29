/**
 * Base de todos los que pelean. Es abstracta: no existe un "personaje a secas".
 * Luchador (el jugador) y Enemigo (los rivales) heredan de acá lo que tienen en común,
 * así no se repite el mismo código en las dos clases.
 */
public abstract class Personaje {
    // protected: accesibles desde las subclases (Luchador, Enemigo, Sumo), no desde afuera
    protected String nombre;
    protected int vida;     // vida actual
    protected int vidaMax;  // vida máxima (la barra "llena")
    protected int fuerza;
    protected int agilidad;
    protected int carisma;

    public Personaje(String nombre, int vidaMax, int fuerza, int agilidad, int carisma) {
        this.nombre = nombre;
        this.vidaMax = vidaMax;
        this.vida = vidaMax; // todos empiezan con la vida al máximo
        this.fuerza = fuerza;
        this.agilidad = agilidad;
        this.carisma = carisma;
    }

    /** Resta vida sin dejarla negativa: si tiene 10 y recibe 18, queda en 0 (no en -8). */
    public void recibirDanio(int danio) {
        vida = Math.max(0, vida - danio);
    }

    public boolean estaVivo() {
        return vida > 0;
    }

    /**
     * Devuelve el valor del atributo pedido. Sirve para chequear requisitos
     * sin tener que preguntar "¿es fuerza?, ¿es agilidad?..." en cada lugar.
     */
    public int getAtributo(Atributo atributo) {
        switch (atributo) {
            case FUERZA: return fuerza;
            case AGILIDAD: return agilidad;
            default: return carisma; // el único que queda: CARISMA
        }
    }

    public String getNombre() { return nombre; }
    public int getVida() { return vida; }
    public int getVidaMax() { return vidaMax; }
    public int getFuerza() { return fuerza; }
    public int getAgilidad() { return agilidad; }
    public int getCarisma() { return carisma; }
}
