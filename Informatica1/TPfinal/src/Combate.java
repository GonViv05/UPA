import java.util.ArrayList;
import java.util.Random;

/**
 * Pelea por turnos. En cada turno el enemigo avisa lo que va a hacer,
 * el jugador elige una acción y después actúa el enemigo.
 * Sirve para cualquier rival (Sumo, Mantis...) porque solo usa los métodos de Enemigo.
 */
public class Combate {
    private final Luchador luchador;
    // Se guarda como Enemigo y no como Sumo o Mantis: así esta clase sirve para cualquier rival
    private final Enemigo enemigo;
    private final Random azar; // el mismo Random de Juego: una sola fuente de azar en la partida

    public Combate(Luchador luchador, Enemigo enemigo, Random azar) {
        this.luchador = luchador;
        this.enemigo = enemigo;
        this.azar = azar;
    }

    /** Repite turnos hasta que alguien pierde. Devuelve cómo terminó la pelea. */
    public Resultado pelear() {
        luchador.prepararParaCombate();
        Consola.titulo("¡PELEA! " + luchador.getNombre() + " vs " + enemigo.getApodo());

        int turno = 1;
        // Bucle infinito: se sale solo con alguno de los return de adentro
        while (true) {
            // 1) El enemigo decide ANTES que el jugador, y se lo avisa
            enemigo.elegirIntencion();
            mostrarEstado(turno);
            System.out.println("\n" + Consola.color(">> " + enemigo.describirIntencion(), Consola.NEGRITA));

            // 2) Turno del jugador
            boolean esquiva = false;     // ¿eligió esquivar este turno?
            boolean turnoUsado = false;  // se pone en true cuando hace algo que gasta el turno
            // Este while interno existe porque cambiar de arma (o intentar algo imposible)
            // no gasta el turno: el menú se repite hasta que haga una acción válida
            while (!turnoUsado) {
                mostrarMenu();
                int opcion = Consola.leerOpcion(1, 6);
                System.out.println();
                if (opcion == 1) {
                    // Polimorfismo: no importa qué arma sea, cada una sabe cómo golpear.
                    // usar() se encarga de la esquiva del rival y del crítico
                    luchador.marcarViolencia(); // aunque erre el golpe, ya no hay final pacifista
                    System.out.println(luchador.getArmaEquipada().usar(luchador, enemigo, azar));
                    turnoUsado = true;
                } else if (opcion == 2) {
                    if (!luchador.puedeEsquivar()) {
                        System.out.println("Estás sin aire: no te da para esquivar.");
                    } else {
                        luchador.gastarAguante();
                        // Todavía no se sabe si esquiva: se decide en el turno del enemigo
                        esquiva = true;
                        turnoUsado = true;
                        System.out.println("Flexionás las rodillas y clavás la vista en " + enemigo.getNombre() + ", listo para esquivar...");
                    }
                } else if (opcion == 3) {
                    turnoUsado = vendarse();
                } else if (opcion == 4) {
                    // Cada rival reacciona distinto a la tribuna (polimorfismo otra vez)
                    System.out.println(enemigo.reaccionarArenga(luchador, azar));
                    turnoUsado = true;
                } else if (opcion == 5) {
                    if (!luchador.usarComida()) {
                        System.out.println("No tenés comida.");
                    } else {
                        boolean acepta = enemigo.aceptaComida() && azar.nextDouble() < luchador.probabilidadConvencer();
                        System.out.println(enemigo.reaccionarComida(acepta, azar));
                        if (acepta) {
                            return Resultado.NEGOCIADO; // aceptó la comida: la pelea termina en el acto
                        }
                        turnoUsado = true;
                    }
                } else {
                    cambiarArma(); // no gasta el turno
                }
            }
            // Si el jugador lo dejó en 0, o lo convenció de irse, gana antes de que el enemigo ataque
            if (!enemigo.estaVivo()) {
                return Resultado.GANADO;
            }
            if (enemigo.abandonoLaPelea()) {
                return Resultado.NEGOCIADO;
            }

            // 3) Turno del enemigo. Se le pasa "esquiva" para que sepa si el jugador intenta esquivar
            System.out.println(enemigo.actuar(luchador, esquiva, azar));
            // 4) El aguante solo se recupera en los turnos en que no esquivó
            if (!esquiva) {
                luchador.recuperarAguante();
            }
            // 5) Sangrado de las dagas, al final del turno
            int sangrado = enemigo.aplicarSangrado();
            if (sangrado > 0) {
                System.out.println("Los cortes de " + enemigo.getNombre() + " siguen sangrando: -" + sangrado);
            }

            // 6) ¿Terminó? Primero el enemigo (pudo morir por el sangrado) y después el jugador
            if (!enemigo.estaVivo()) {
                return Resultado.GANADO;
            }
            if (!luchador.estaVivo()) {
                return Resultado.PERDIDO;
            }
            turno++;
        }
    }

    /** Intenta vendarse. Devuelve true si gastó el turno. */
    private boolean vendarse() {
        if (luchador.getVendas() == 0) {
            System.out.println("No te quedan vendas.");
            return false;
        }
        if (luchador.getVida() == luchador.getVidaMax()) {
            System.out.println("Estás entero: no hace falta vendarte.");
            return false;
        }
        int cura = luchador.curar();
        System.out.println("Retrocedés un paso, te ajustás una venda sobre la herida y respirás hondo: "
                + Consola.color("+" + cura + " de vida", Consola.VERDE) + ".");
        return true;
    }

    /** Número de turno y las barras de vida y aguante. */
    private void mostrarEstado(int turno) {
        System.out.println("\n----------------- Turno " + turno + " -----------------");
        // printf con %-12s: el texto ocupa siempre 12 lugares (alineado a la izquierda),
        // así las barras quedan una debajo de la otra. %n es el salto de línea
        System.out.printf("%-12s Vida    %s%n", luchador.getNombre(), Consola.barraVida(luchador.getVida(), luchador.getVidaMax(), 20));
        System.out.printf("%-12s Aguante %s%n", "", Consola.color(Consola.barra(luchador.getAguante(), Luchador.AGUANTE_MAX, 20), Consola.CIAN));
        // estadoExtra() lo decide cada rival: "¡FURIOSO!" en Takeshi, el fervor en la Mantis
        System.out.printf("%-12s Vida    %s%s%n", enemigo.getNombre(), Consola.barraVida(enemigo.getVida(), enemigo.getVidaMax(), 20), enemigo.estadoExtra());
    }

    /** Las 6 acciones del jugador. */
    private void mostrarMenu() {
        System.out.println();
        System.out.println("1) Atacar con " + luchador.getArmaEquipada().getNombre()
                + " (" + luchador.getArmaEquipada().getDescripcion() + ")");
        System.out.println("2) Esquivar (gasta " + Luchador.COSTO_ESQUIVA + " de aguante)"
                + (luchador.puedeEsquivar() ? "" : "  -- sin aire"));
        System.out.println("3) Vendarse (cura el 30 % de tu vida, te quedan " + luchador.getVendas() + ")");
        System.out.println("4) Arengar a la tribuna");
        System.out.println("5) Ofrecer comida (tenés " + luchador.getComidas() + ")");
        System.out.println("6) Cambiar de arma (no gasta el turno)");
    }

    /** Muestra solo las armas que el luchador puede usar y equipa la elegida. */
    private void cambiarArma() {
        // Lista auxiliar con las armas usables, para que el menú no muestre armas bloqueadas
        ArrayList<Arma> usables = new ArrayList<>();
        for (Arma arma : luchador.getArmas()) {
            if (luchador.puedeUsar(arma)) {
                usables.add(arma);
            }
        }
        // Se muestran desde 1 (i + 1) porque el usuario cuenta desde 1 y la lista desde 0
        for (int i = 0; i < usables.size(); i++) {
            System.out.println("  " + (i + 1) + ") " + usables.get(i).getNombre() + " - " + usables.get(i).getDescripcion());
        }
        int elegida = Consola.leerOpcion(1, usables.size());
        luchador.equipar(usables.get(elegida - 1)); // -1 para volver a contar desde 0
        System.out.println("Equipaste: " + luchador.getArmaEquipada().getNombre());
    }
}
