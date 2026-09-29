import java.util.Random;

/**
 * Takeshi "La Montaña". Cada turno avisa lo que va a hacer:
 * APLASTAR (daño medio) o EMBESTIDA (saca el 40 % de tu vida, cada 3 turnos).
 * A veces se tropieza. Con menos de la mitad de vida se enfurece.
 */
public class Sumo extends Enemigo {
    // Enum privado: los dos ataques posibles. Solo lo usa esta clase
    private enum Intencion { APLASTAR, EMBESTIDA }

    private Intencion intencion;          // lo que eligió hacer este turno
    private int turnosHastaEmbestida = 2; // cuenta regresiva: la primera embestida cae en el turno 2
    private boolean arreglado;            // true si el jugador le pagó para que se deje perder

    public Sumo() {
        // nombre, vida 200, fuerza 10, da 100 de experiencia (justo lo necesario para subir a nivel 2)
        super("Takeshi", 200, 10, 100);
    }

    /** Furioso con menos de la mitad de la vida, salvo que esté arreglado. */
    public boolean estaEnfurecido() {
        return !arreglado && vida < vidaMax / 2;
    }

    /**
     * Se llama al principio de cada turno. Resta 1 a la cuenta regresiva y, cuando llega a 0,
     * embiste. Con el contador arrancando en 2, las embestidas caen en los turnos 2, 5, 8...
     */
    @Override
    public void elegirIntencion() {
        turnosHastaEmbestida--;
        if (!arreglado && turnosHastaEmbestida <= 0) {
            intencion = Intencion.EMBESTIDA;
            // Reinicia la cuenta: furioso embiste más seguido
            turnosHastaEmbestida = estaEnfurecido() ? 2 : 3;
        } else {
            intencion = Intencion.APLASTAR;
        }
    }

    /** El aviso que ve el jugador antes de elegir su acción. */
    @Override
    public String describirIntencion() {
        if (turnosAturdido > 0) {
            return "Takeshi está aturdido: este turno no puede atacar.";
        }
        if (intencion == Intencion.EMBESTIDA) {
            return "¡Takeshi baja la cabeza y raspa el piso! Prepara una EMBESTIDA (40 % de tu vida).";
        }
        return "Takeshi levanta los brazos: va a APLASTARTE.";
    }

    private int danioAplastar() {
        return estaEnfurecido() ? 24 : 18;
    }

    /** Su turno. Los chequeos van en orden: aturdido, enredado y recién ahí ataca. */
    @Override
    public String actuar(Luchador objetivo, boolean jugadorEsquiva, Random azar) {
        // 1) Aturdido: pierde la acción y se le pasa el aturdimiento
        if (turnosAturdido > 0) {
            turnosAturdido--;
            return "Takeshi sacude la cabeza, todavía aturdido.";
        }
        // 2) Enredado: el enredo se consume siempre, pero solo la mitad de las veces pierde el ataque
        if (enredado) {
            enredado = false;
            if (azar.nextDouble() < 0.5) {
                return "Takeshi se traba con el látigo y pierde el ataque.";
            }
        }
        // 3) Ataca con lo que eligió en elegirIntencion()
        if (intencion == Intencion.EMBESTIDA) {
            return embestir(objetivo, jugadorEsquiva, azar);
        }
        return aplastar(objetivo, jugadorEsquiva, azar);
    }

    private String aplastar(Luchador objetivo, boolean jugadorEsquiva, Random azar) {
        // Primero se fija si se tropieza (le pasa aunque el jugador no esquive).
        // Si le pagaron, se tropieza mucho más a propósito
        double fallo = arreglado ? 0.6 : 0.25;
        if (azar.nextDouble() < fallo) {
            turnosAturdido = 1;
            return "¡Takeshi se tropieza y cae! Pierde su próximo turno.";
        }
        // Si el jugador eligió esquivar, se tira su chance (base 55 % + agilidad)
        if (jugadorEsquiva && azar.nextDouble() < objetivo.probabilidadEsquivar(0.55)) {
            return "Te corrés justo a tiempo. El piso tiembla donde estabas.";
        }
        int danio = danioAplastar();
        objetivo.recibirDanio(danio);
        return "Takeshi te aplasta con todo su peso: -" + danio;
    }

    private String embestir(Luchador objetivo, boolean jugadorEsquiva, Random azar) {
        // La embestida es más difícil de esquivar (base 40 %), pero si sale, Takeshi queda aturdido
        if (jugadorEsquiva && azar.nextDouble() < objetivo.probabilidadEsquivar(0.40)) {
            turnosAturdido = 1;
            return "¡Esquivás la embestida! Takeshi se estrella contra las cuerdas y queda mareado.";
        }
        // El daño es un porcentaje de la vida máxima del JUGADOR, así es peligrosa para cualquier luchador
        int danio = (int) Math.round(objetivo.getVidaMax() * 0.4);
        objetivo.recibirDanio(danio);
        return "¡EMBESTIDA! Salís volando: -" + danio;
    }

    @Override
    public boolean aceptaComida() {
        return true; // Takeshi siempre tiene hambre (igual depende del carisma del jugador)
    }

    @Override
    public String getApodo() {
        return "Takeshi \"La Montaña\""; // \" permite poner comillas dentro de un String
    }

    /** Arranca con el 30 % de la vida, no embiste y se tropieza más. */
    @Override
    public void dejarsePerder() {
        arreglado = true;
        // Math.min: si ya tenía menos vida (por ejemplo, por una amenaza), no se la sube
        vida = Math.min(vida, (int) Math.round(vidaMax * 0.3));
    }

    /** Pierde el 20 % de la vida por los nervios. */
    @Override
    public void intimidar() {
        recibirDanio((int) Math.round(vidaMax * 0.2));
    }
}
