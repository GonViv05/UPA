import java.util.Random;

/**
 * Sor Tijereta, la Mantis Religiosa Gigante: la rival de la segunda ronda.
 * Es muy rápida: ataca dos veces por turno y esquiva el 30 % de tus golpes.
 * Cada 3 turnos avisa un ABRAZO MORTAL que saca el 30 % de tu vida.
 * No se puede hablar con ella, pero es muy devota: si la tribuna canta,
 * se arrodilla a rezar. Tres himnos y se va del ring a hacerse monja.
 */
public class Mantis extends Enemigo {
    private enum Intencion { TIJERETAZOS, ABRAZO }

    public static final int DANIO_TIJERETAZO = 10;
    public static final int FERVOR_PARA_CONVERTIRSE = 3;

    private Intencion intencion;
    private int turnosHastaAbrazo = 3; // cuenta regresiva: el abrazo cae en los turnos 3, 6, 9...
    private int fervor = 0;            // himnos exitosos de la tribuna
    private boolean rezando;           // true: se arrodilló y pierde su próxima acción

    public Mantis() {
        // nombre, vida 170, fuerza 8, da 200 de experiencia (lo necesario para pasar de nivel 2 a 3)
        super("Sor Tijereta", 170, 8, 200);
    }

    /** Muestra el fervor al lado de su barra de vida, para que se vea cuánto falta. */
    @Override
    public String estadoExtra() {
        if (fervor == 0) {
            return "";
        }
        return Consola.color("  Fervor " + fervor + "/" + FERVOR_PARA_CONVERTIRSE, Consola.CIAN);
    }

    @Override
    public void elegirIntencion() {
        turnosHastaAbrazo--;
        if (turnosHastaAbrazo <= 0) {
            intencion = Intencion.ABRAZO;
            turnosHastaAbrazo = 3;
        } else {
            intencion = Intencion.TIJERETAZOS;
        }
    }

    @Override
    public String describirIntencion() {
        // El orden importa: rezar y estar aturdida le hacen perder el turno, así que se avisan primero
        if (rezando) {
            return "Sor Tijereta está de rodillas con las patas juntas, rezando. Este turno no ataca.";
        }
        if (turnosAturdido > 0) {
            return "Sor Tijereta todavía está desenredando sus patas: este turno no puede atacar.";
        }
        if (intencion == Intencion.ABRAZO) {
            return "¡Sor Tijereta abre las patas delanteras de par en par! Prepara un ABRAZO MORTAL (30 % de tu vida).";
        }
        return "Sor Tijereta afila las pinzas una contra otra: va a lanzar DOS TIJERETAZOS.";
    }

    @Override
    public String actuar(Luchador objetivo, boolean jugadorEsquiva, Random azar) {
        if (rezando) {
            rezando = false;
            String[] rezos = {
                "Sor Tijereta murmura un rosario entero con los ojos cerrados. Ni te mira.",
                "Sor Tijereta se persigna con una pinza (casi se corta sola) y sigue rezando."
            };
            return Consola.alAzar(rezos, azar);
        }
        if (turnosAturdido > 0) {
            turnosAturdido--;
            return "Sor Tijereta se tambalea sobre sus patas finitas, desorientada.";
        }
        if (enredado) {
            enredado = false;
            if (azar.nextDouble() < 0.5) {
                return "Sor Tijereta tiene seis patas y el látigo se las enredó todas. Pierde el ataque.";
            }
        }
        if (intencion == Intencion.ABRAZO) {
            return abrazar(objetivo, jugadorEsquiva, azar);
        }
        // Es rápida: dos ataques en el mismo turno, cada uno se esquiva por separado
        return tijeretazo(objetivo, jugadorEsquiva, azar) + "\n" + tijeretazo(objetivo, jugadorEsquiva, azar);
    }

    private String tijeretazo(Luchador objetivo, boolean jugadorEsquiva, Random azar) {
        if (jugadorEsquiva && azar.nextDouble() < objetivo.probabilidadEsquivar(0.45)) {
            String[] esquivas = {
                "Te agachás y la pinza te pasa rozando el pelo.",
                "Das un paso atrás y el tijeretazo corta el aire con un silbido."
            };
            return Consola.alAzar(esquivas, azar);
        }
        int danio = DANIO_TIJERETAZO;
        boolean critico = tiraCritico(azar);
        if (critico) {
            danio *= 2;
        }
        objetivo.recibirDanio(danio);
        String[] golpes = {
            "Un tijeretazo tan rápido que ni lo ves venir te abre el brazo",
            "La pinza te engancha el hombro y tira",
            "Sor Tijereta te da un tijeretazo en las costillas mientras reza un Avemaría"
        };
        String texto = Consola.alAzar(golpes, azar) + ": " + textoDanio(danio);
        if (critico) {
            texto = Consola.color("¡CRÍTICO! ", Consola.AMARILLO + Consola.NEGRITA) + texto;
        }
        return texto;
    }

    private String abrazar(Luchador objetivo, boolean jugadorEsquiva, Random azar) {
        if (jugadorEsquiva && azar.nextDouble() < objetivo.probabilidadEsquivar(0.35)) {
            turnosAturdido = 1;
            return "¡Te escapás por debajo! Sor Tijereta se abraza a sí misma y queda hecha un nudo de patas.";
        }
        // Igual que la embestida de Takeshi: un porcentaje de TU vida máxima, sin crítico
        int danio = (int) Math.round(objetivo.getVidaMax() * 0.3);
        objetivo.recibirDanio(danio);
        return "¡ABRAZO MORTAL! Sor Tijereta te atrapa entre sus patas y te aprieta como en misa de domingo: " + textoDanio(danio);
    }

    /** Es tan rápida que esquiva el 30 % de tus golpes. */
    @Override
    public double probabilidadEvadir() {
        return 0.30;
    }

    @Override
    public String textoEvasion(Random azar) {
        String[] frases = {
            "Sor Tijereta se hace a un lado a una velocidad ridícula. Tu golpe corta el aire.",
            "Sor Tijereta salta hacia atrás sobre sus patas finitas y te deja pegándole al vacío.",
            "Donde estaba Sor Tijereta ahora no hay nada. Aparece a tu espalda, con cara de santa."
        };
        return Consola.alAzar(frases, azar);
    }

    /**
     * Acá está la forma de ganarle con carisma: la tribuna canta un himno y ella,
     * muy devota, se arrodilla a rezar. La chance es 10 % por punto de carisma (máximo 90 %).
     */
    @Override
    public String reaccionarArenga(Luchador luchador, Random azar) {
        double chance = Math.min(0.9, luchador.getCarisma() * 0.10);
        if (azar.nextDouble() >= chance) {
            String[] fallos = {
                "Arrancás un canto, pero la tribuna te chifla y te tira vasos. Sor Tijereta ni se inmuta.",
                "Nadie te sigue el canto. Se escucha un grillo (Sor Tijereta lo mira con hambre)."
            };
            return Consola.alAzar(fallos, azar);
        }
        fervor++;
        if (fervor >= FERVOR_PARA_CONVERTIRSE) {
            return "La tribuna entera canta un Aleluya a coro. Sor Tijereta suelta las pinzas, levanta la vista\n"
                    + "al techo, llora y se va del ring caminando despacio: dice que la llamó el convento.";
        }
        rezando = true;
        String[] himnos = {
            "Arrancás un himno y la tribuna lo sigue. Sor Tijereta se arrodilla y junta las patas a rezar.",
            "¡La tribuna canta \"Aleluya\"! Sor Tijereta se queda dura, se persigna y se pone a rezar."
        };
        return Consola.alAzar(himnos, azar) + " " + Consola.color("(Fervor " + fervor + "/" + FERVOR_PARA_CONVERTIRSE + ")", Consola.CIAN);
    }

    /** Con 3 himnos se convierte y se va: la pelea termina sin que haga falta ganarle a golpes. */
    @Override
    public boolean abandonoLaPelea() {
        return fervor >= FERVOR_PARA_CONVERTIRSE;
    }

    /** No se puede negociar con ella: nunca acepta comida. */
    @Override
    public boolean aceptaComida() {
        return false;
    }

    @Override
    public String reaccionarComida(boolean aceptada, Random azar) {
        return "Sor Tijereta atrapa el onigiri al vuelo, lo corta prolijamente en dos con una pinza y lo tira. Está de ayuno.";
    }

    @Override
    public String getApodo() {
        return "Sor Tijereta, la Mantis Religiosa Gigante";
    }
}
