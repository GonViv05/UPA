/**
 * Los tres luchadores que se pueden elegir, con sus atributos iniciales.
 * Cada valor del enum guarda sus propios datos (nombre, descripción, fuerza, agilidad, carisma).
 */
public enum TipoLuchador {
    // Orden de los datos: nombre, descripción, fuerza, agilidad, carisma
    BRUTO("El Bruto", "Fuerte como una pared, pero lento.", 8, 3, 3),
    VELOZ("La Sombra", "Rápida y escurridiza, pero pega poco.", 3, 8, 3),
    CHARLATAN("El Charlatán", "No destaca peleando, pero convence a cualquiera.", 3, 3, 8);

    private final String nombre;
    private final String descripcion;
    private final int fuerza;
    private final int agilidad;
    private final int carisma;

    TipoLuchador(String nombre, String descripcion, int fuerza, int agilidad, int carisma) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fuerza = fuerza;
        this.agilidad = agilidad;
        this.carisma = carisma;
    }

    /**
     * Crea el arma con la que empieza cada tipo.
     * Devuelve un objeto NUEVO cada vez, para que dos partidas no compartan la misma arma.
     */
    public Arma crearArmaInicial() {
        // "this" es el valor del enum sobre el que se llamó (BRUTO, VELOZ o CHARLATAN)
        switch (this) {
            case BRUTO: return new Mazo();
            case VELOZ: return new Dagas();
            default: return new Latigo(); // CHARLATAN
        }
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getFuerza() { return fuerza; }
    public int getAgilidad() { return agilidad; }
    public int getCarisma() { return carisma; }
}
