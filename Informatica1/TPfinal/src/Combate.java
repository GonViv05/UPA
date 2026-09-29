import java.util.ArrayList;
import java.util.Random;

/**
 * Pelea por turnos. En cada turno el enemigo avisa lo que va a hacer,
 * el jugador elige una acción y después actúa el enemigo.
 */
public class Combate {
    private final Luchador luchador;
    // Se guarda como Enemigo y no como Sumo: así esta clase sirve para cualquier rival
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
            System.out.println("\n>> " + enemigo.describirIntencion());

            // 2) Turno del jugador
            boolean esquiva = false;     // ¿eligió esquivar este turno?
            boolean turnoUsado = false;  // se pone en true cuando hace algo que gasta el turno
            // Este while interno existe porque cambiar de arma (o intentar algo imposible)
            // no gasta el turno: el menú se repite hasta que haga una acción válida
            while (!turnoUsado) {
                mostrarMenu();
                int opcion = Consola.leerOpcion(1, 4);
                if (opcion == 1) {
                    // Polimorfismo: no importa qué arma sea, cada una sabe cómo golpear
                    System.out.println(luchador.getArmaEquipada().golpear(luchador, enemigo, azar));
                    turnoUsado = true;
                } else if (opcion == 2) {
                    if (!luchador.puedeEsquivar()) {
                        System.out.println("Estás sin aire: no te da para esquivar.");
                    } else {
                        luchador.gastarAguante();
                        // Todavía no se sabe si esquiva: se decide en el turno del enemigo
                        esquiva = true;
                        turnoUsado = true;
                        System.out.println("Te preparás para esquivar...");
                    }
                } else if (opcion == 3) {
                    if (!luchador.usarComida()) {
                        System.out.println("No tenés comida.");
                    } else if (enemigo.aceptaComida() && azar.nextDouble() < luchador.probabilidadConvencer()) {
                        // Aceptó la comida: la pelea termina en el acto
                        System.out.println("Takeshi agarra el onigiri, lo mira... y se sienta a comer. ¡Trato hecho!");
                        return Resultado.NEGOCIADO;
                    } else {
                        System.out.println("Takeshi tira el onigiri al piso. No te cree nada.");
                        turnoUsado = true;
                    }
                } else {
                    cambiarArma(); // no gasta el turno
                }
            }
            // Si el golpe del jugador lo dejó en 0, gana antes de que el enemigo llegue a atacar
            if (!enemigo.estaVivo()) {
                return Resultado.GANADO;
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
                System.out.println("Takeshi sangra: -" + sangrado);
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

    /** Número de turno y las barras de vida y aguante. */
    private void mostrarEstado(int turno) {
        System.out.println("\n----------------- Turno " + turno + " -----------------");
        // printf con %-12s: el texto ocupa siempre 12 lugares (alineado a la izquierda),
        // así las barras quedan una debajo de la otra. %n es el salto de línea
        System.out.printf("%-12s Vida    %s%n", luchador.getNombre(), Consola.barra(luchador.getVida(), luchador.getVidaMax(), 20));
        System.out.printf("%-12s Aguante %s%n", "", Consola.barra(luchador.getAguante(), Luchador.AGUANTE_MAX, 20));
        // instanceof pregunta si el enemigo es un Sumo; recién ahí se puede castear (Sumo) y
        // usar estaEnfurecido(), que solo existe en Sumo
        String furia = (enemigo instanceof Sumo && ((Sumo) enemigo).estaEnfurecido()) ? "  ¡FURIOSO!" : "";
        System.out.printf("%-12s Vida    %s%s%n", enemigo.getNombre(), Consola.barra(enemigo.getVida(), enemigo.getVidaMax(), 20), furia);
    }

    /** Las 4 acciones del jugador. */
    private void mostrarMenu() {
        System.out.println();
        System.out.println("1) Atacar con " + luchador.getArmaEquipada().getNombre()
                + " (" + luchador.getArmaEquipada().getDescripcion() + ")");
        System.out.println("2) Esquivar (gasta " + Luchador.COSTO_ESQUIVA + " de aguante)"
                + (luchador.puedeEsquivar() ? "" : "  -- sin aire"));
        System.out.println("3) Ofrecer comida (tenés " + luchador.getComidas() + ")");
        System.out.println("4) Cambiar de arma (no gasta el turno)");
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
