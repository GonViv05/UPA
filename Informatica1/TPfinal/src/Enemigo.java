import java.util.Random;

/**
 * Un rival. Cada tipo de enemigo decide a su manera qué hacer en su turno.
 * Es abstracta: define QUÉ tiene que saber hacer cualquier rival (los métodos abstract),
 * pero cada subclase (Sumo, Mantis) decide CÓMO lo hace.
 */
public abstract class Enemigo extends Personaje {
    public static final int DANIO_SANGRADO = 5; // vida que pierde por turno mientras sangra
    public static final int AGUANTE_POR_ARENGA = 15; // lo que te da la tribuna si al rival no le afecta

    // Estados alterados que le causan las armas del jugador
    protected int xpQueOtorga;     // experiencia que gana el jugador al vencerlo
    protected int turnosAturdido;  // > 0: pierde su próxima acción (Mazo)
    protected int turnosSangrado;  // turnos de sangrado que le quedan (Dagas)
    protected boolean enredado;    // true: 50 % de perder su próximo ataque (Látigo)

    public Enemigo(String nombre, int vidaMax, int fuerza, int xpQueOtorga) {
        // Los enemigos no usan agilidad ni carisma, por eso van en 1
        super(nombre, vidaMax, fuerza, 1, 1);
        this.xpQueOtorga = xpQueOtorga;
    }

    /** Elige qué va a hacer este turno (antes de que el jugador decida). */
    public abstract void elegirIntencion();

    /** Texto que avisa al jugador lo que el enemigo está por hacer. */
    public abstract String describirIntencion();

    /** Ejecuta su turno. Devuelve qué pasó para mostrarlo. */
    public abstract String actuar(Luchador objetivo, boolean jugadorEsquiva, Random azar);

    /** ¿Este enemigo puede aceptar comida para dejar de pelear? */
    public abstract boolean aceptaComida();

    /** Qué hace cuando le ofrecen comida (aceptada es true si se la come y deja de pelear). */
    public abstract String reaccionarComida(boolean aceptada, Random azar);

    /** Nombre con apodo, para los títulos (por ejemplo: Takeshi "La Montaña"). */
    public abstract String getApodo();

    /** Le pagaron para que se deje perder. Por defecto no acepta. */
    public void dejarsePerder() {
        // vacío a propósito: cada enemigo lo redefine (@Override) si quiere reaccionar
    }

    /** Lo intimidaron antes de pelear. Por defecto no le afecta. */
    public void intimidar() {
        // vacío a propósito, igual que dejarsePerder()
    }

    /** Probabilidad de esquivar los ataques del jugador. Por defecto no esquiva nunca. */
    public double probabilidadEvadir() {
        return 0.0;
    }

    /** Texto cuando esquiva un ataque (solo se usa si probabilidadEvadir() es mayor a 0). */
    public String textoEvasion(Random azar) {
        return nombre + " se corre justo a tiempo y tu golpe no toca nada.";
    }

    /**
     * El jugador arenga a la tribuna. Por defecto al rival no le afecta,
     * pero el aliento del público te devuelve un poco de aguante.
     * Un rival puede redefinirlo (como la Mantis) para que le afecte de otra forma.
     */
    public String reaccionarArenga(Luchador luchador, Random azar) {
        luchador.sumarAguante(AGUANTE_POR_ARENGA);
        String[] frases = {
            "Levantás los brazos y la tribuna corea tu nombre",
            "Señalás a la tribuna y el público te devuelve un rugido"
        };
        return Consola.alAzar(frases, azar) + ". A " + nombre + " no le importa, pero a vos te sube la adrenalina: "
                + Consola.color("+" + AGUANTE_POR_ARENGA + " de aguante", Consola.CIAN) + ".";
    }

    /** true si el rival se fue del ring sin que haga falta ganarle a golpes. Por defecto nunca. */
    public boolean abandonoLaPelea() {
        return false;
    }

    /** Texto extra al lado de su barra de vida (por ejemplo "¡FURIOSO!"). Por defecto nada. */
    public String estadoExtra() {
        return "";
    }

    /** Lo usa el Mazo: pierde su próxima acción. */
    public void aturdir() {
        turnosAturdido = 1;
    }

    /** Lo usan las Dagas. Math.max: si ya sangraba, se renueva a 3 turnos pero no se acumula. */
    public void sangrar(int turnos) {
        turnosSangrado = Math.max(turnosSangrado, turnos);
    }

    /** Lo usa el Látigo. El 50 % se tira en actuar(), cuando intenta atacar. */
    public void enredar() {
        enredado = true;
    }

    /** Aplica el daño del sangrado al final del turno. Devuelve cuánto sacó. */
    public int aplicarSangrado() {
        if (turnosSangrado == 0) {
            return 0; // no está sangrando
        }
        turnosSangrado--;
        recibirDanio(DANIO_SANGRADO);
        return DANIO_SANGRADO;
    }

    /** Tira el dado del crítico para los ataques del rival (mismo 20 % que el jugador). */
    protected boolean tiraCritico(Random azar) {
        return azar.nextDouble() < PROB_CRITICO;
    }

    /** Daño recibido por el jugador, en rojo: por ejemplo "-18". */
    protected String textoDanio(int danio) {
        return Consola.color("-" + danio, Consola.ROJO);
    }

    public int getXpQueOtorga() {
        return xpQueOtorga;
    }
}
