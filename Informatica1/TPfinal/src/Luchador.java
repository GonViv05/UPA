import java.util.ArrayList;

/**
 * El personaje del jugador.
 * Hereda nombre, vida y atributos de Personaje, y agrega nivel, plata, armas, comida y aguante.
 */
public class Luchador extends Personaje {
    // Constantes (static final): son iguales para todos los luchadores y no cambian nunca
    public static final int AGUANTE_MAX = 100;
    public static final int COSTO_ESQUIVA = 30;
    public static final int VENDAS_POR_PELEA = 2; // vendas gratis que se reponen en cada pelea

    private final TipoLuchador tipo;
    private int nivel = 1;
    private int experiencia = 0;
    private int puntosLibres = 0; // puntos para repartir al subir de nivel
    private int monedas = 30;     // plata inicial
    private int comidas = 0;      // cantidad de onigiris
    private int vendasDePelea = 0;   // las gratis de la pelea actual
    private int vendasCompradas = 0; // las compradas: se guardan entre peleas
    private int aguante = AGUANTE_MAX;
    // ArrayList y no un arreglo, porque la cantidad de armas crece al comprar
    private final ArrayList<Arma> armas = new ArrayList<>();
    private Arma armaEquipada;

    public Luchador(String nombre, TipoLuchador tipo) {
        // super(...) llama al constructor de Personaje. La vida máxima depende de la fuerza:
        // 100 + fuerza * 5 (El Bruto, con fuerza 8, tiene 140)
        super(nombre, 100 + tipo.getFuerza() * 5, tipo.getFuerza(), tipo.getAgilidad(), tipo.getCarisma());
        this.tipo = tipo;
        // Todos tienen los puños, más el arma inicial de su tipo (que queda equipada)
        armas.add(new Punos());
        armaEquipada = tipo.crearArmaInicial();
        armas.add(armaEquipada);
    }

    // ---------- Niveles ----------

    /** Experiencia necesaria para el próximo nivel: 100 en nivel 1, 200 en nivel 2, etc. */
    public int xpParaSubir() {
        return nivel * 100;
    }

    public void ganarExperiencia(int xp) {
        experiencia += xp;
        // while y no if: si gana mucha experiencia de golpe, puede subir varios niveles
        while (experiencia >= xpParaSubir()) {
            experiencia -= xpParaSubir(); // la experiencia que sobra queda para el nivel siguiente
            subirNivel();
        }
    }

    // Privado: solo se sube de nivel ganando experiencia
    private void subirNivel() {
        nivel++;
        vidaMax += 10;
        vida = vidaMax; // al subir de nivel se cura por completo
        puntosLibres += 2;
    }

    /** Gasta un punto libre en el atributo elegido. Devuelve false si no quedaban puntos. */
    public boolean asignarPunto(Atributo atributo) {
        if (puntosLibres == 0) {
            return false;
        }
        switch (atributo) {
            case FUERZA: fuerza++; break;
            case AGILIDAD: agilidad++; break;
            case CARISMA: carisma++; break;
        }
        puntosLibres--;
        return true;
    }

    /** ¿Tiene el atributo en el mínimo pedido o más? Es el chequeo de los requisitos tipo "[Carisma 6]". */
    public boolean cumple(Atributo atributo, int minimo) {
        return getAtributo(atributo) >= minimo;
    }

    // ---------- Plata, armas y comida ----------

    /** 5 % de descuento por punto de carisma, hasta 40 %. */
    public int precioCon(int precio) {
        int descuento = Math.min(40, carisma * 5); // Math.min pone el tope en 40
        // Se multiplica antes de dividir para no perder precisión con la división entera
        return precio * (100 - descuento) / 100;
    }

    /** Cobra la cantidad si le alcanza. Si no, devuelve false y no le saca nada. */
    /** Plata que se gana, por ejemplo la bolsa por ganar una ronda. */
    public void ganarMonedas(int cantidad) {
        monedas += cantidad;
    }

    public boolean pagar(int cantidad) {
        if (monedas < cantidad) {
            return false;
        }
        monedas -= cantidad;
        return true;
    }

    /** Busca un arma por nombre en la lista (los String se comparan con equals, no con ==). */
    public boolean tieneArma(String nombreArma) {
        for (Arma a : armas) {
            if (a.getNombre().equals(nombreArma)) {
                return true;
            }
        }
        return false;
    }

    public void agregarArma(Arma arma) {
        armas.add(arma);
    }

    /** Se puede usar si el arma no pide nada (requisito null) o si cumple el requisito. */
    public boolean puedeUsar(Arma arma) {
        return arma.getRequisito() == null || cumple(arma.getRequisito(), arma.getMinimo());
    }

    /** Equipa el arma solo si la puede usar; si no, deja la que tenía. */
    public void equipar(Arma arma) {
        if (puedeUsar(arma)) {
            armaEquipada = arma;
        }
    }

    public void agregarComida() {
        comidas++;
    }

    /** Gasta un onigiri. Devuelve false si no tenía ninguno. */
    public boolean usarComida() {
        if (comidas == 0) {
            return false;
        }
        comidas--;
        return true;
    }

    public void agregarVenda() {
        vendasCompradas++;
    }

    /** Vendas con las que va a entrar a la próxima pelea: las gratis más las compradas. */
    public int getVendasParaPelear() {
        return VENDAS_POR_PELEA + vendasCompradas;
    }

    /** Todas las vendas que puede usar ahora, en medio de la pelea: las gratis que quedan más las compradas. */
    public int getVendas() {
        return vendasDePelea + vendasCompradas;
    }

    /**
     * Se venda la herida: recupera el 30 % de la vida máxima, sin pasarse del máximo.
     * Gasta primero las vendas gratis (que igual se pierden al terminar la pelea)
     * y después las compradas. Devuelve cuánta vida recuperó.
     */
    public int curar() {
        if (vendasDePelea > 0) {
            vendasDePelea--;
        } else {
            vendasCompradas--;
        }
        int cura = Math.min((int) Math.round(vidaMax * 0.3), vidaMax - vida);
        vida += cura;
        return cura;
    }

    /** 12 % por punto de carisma, 100 % desde carisma 8. */
    public double probabilidadConvencer() {
        return carisma >= 8 ? 1.0 : carisma * 0.12;
    }

    // ---------- Combate ----------

    /** Antes de cada pelea: vida y aguante al máximo, y se reponen las vendas gratis. */
    public void prepararParaCombate() {
        vida = vidaMax;
        aguante = AGUANTE_MAX;
        vendasDePelea = VENDAS_POR_PELEA;
    }

    public boolean puedeEsquivar() {
        return aguante >= COSTO_ESQUIVA;
    }

    public void gastarAguante() {
        aguante = Math.max(0, aguante - COSTO_ESQUIVA); // Math.max evita que quede negativo
    }

    /** Si no esquivó en este turno, recupera aliento (más rápido con agilidad). */
    public void recuperarAguante() {
        sumarAguante(10 + agilidad);
    }

    /** Suma aguante sin pasarse del máximo. */
    public void sumarAguante(int cantidad) {
        aguante = Math.min(AGUANTE_MAX, aguante + cantidad); // Math.min evita pasarse de 100
    }

    /** Probabilidad de esquivar un ataque: base del ataque + 5 % por punto de agilidad (máx. 95 %). */
    public double probabilidadEsquivar(double base) {
        // El tope de 95 % hace que esquivar nunca sea 100 % seguro
        return Math.min(0.95, base + agilidad * 0.05);
    }

    public TipoLuchador getTipo() { return tipo; }
    public int getNivel() { return nivel; }
    public int getExperiencia() { return experiencia; }
    public int getPuntosLibres() { return puntosLibres; }
    public int getMonedas() { return monedas; }
    public int getComidas() { return comidas; }
    public int getAguante() { return aguante; }
    public ArrayList<Arma> getArmas() { return armas; }
    public Arma getArmaEquipada() { return armaEquipada; }
}
