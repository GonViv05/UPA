import java.util.Random;

/**
 * Una partida completa: elegir luchador y un torneo de dos rondas
 * (Takeshi y después la Mantis), con pasillo, pelea y subida de nivel en cada una.
 * Main crea un Juego nuevo por cada partida, así el estado de abajo arranca siempre de cero.
 */
public class Juego {
    public static final int PREMIO_POR_RONDA = 25; // la "bolsa" que se cobra al ganar una ronda

    private final Random azar = new Random(); // única fuente de azar; se le pasa a Combate
    private Luchador luchador;
    private Sumo sumo;       // rival de la ronda 1
    private Mantis mantis;   // rival de la ronda 2
    // Banderas: arrancan en false y pasan a true la primera vez, para que no se pueda repetir
    private boolean robo;        // ya le robó un onigiri al comerciante
    private boolean pagoAlSumo;  // ya le pagó para que se deje perder
    private boolean amenazo;     // ya lo amenazó

    /** El "guion" de la partida, en orden. */
    public void jugar() {
        mostrarPortada();
        luchador = elegirLuchador();
        sumo = new Sumo();
        mantis = new Mantis();

        // pasillo() no vuelve hasta que la pelea de esa ronda se resuelve (peleando o hablando)
        Resultado ronda1 = pasillo(1);
        if (!ronda1.esVictoria()) {
            mostrarDerrota(sumo);
            return; // perdió: la partida termina acá
        }
        premiar(sumo);

        Resultado ronda2 = pasillo(2);
        if (!ronda2.esVictoria()) {
            mostrarDerrota(mantis);
            return;
        }
        premiar(mantis);
        // Final especial: las dos rondas ganadas sin pelear y sin haber atacado ni amenazado nunca
        if (ronda1 == Resultado.NEGOCIADO && ronda2 == Resultado.NEGOCIADO && !luchador.usoLaViolencia()) {
            mostrarFinalPacifista();
        } else {
            mostrarVictoria(ronda1, ronda2);
        }
    }

    /** Después de ganar una ronda: la bolsa de plata, la experiencia y el reparto de puntos. */
    private void premiar(Enemigo vencido) {
        luchador.ganarMonedas(PREMIO_POR_RONDA);
        Consola.titulo("¡Le ganaste a " + vencido.getNombre() + "!");
        System.out.println("El organizador te tira la bolsa de la pelea: " + Consola.color("+$" + PREMIO_POR_RONDA, Consola.AMARILLO) + ".");
        luchador.ganarExperiencia(vencido.getXpQueOtorga());
        subirNivel();
    }

    private void mostrarPortada() {
        System.out.println();
        System.out.println("  ╔════════════════════════════════════╗");
        System.out.println("  ║       T O R N E O                  ║");
        System.out.println("  ║            C L A N D E S T I N O   ║");
        System.out.println("  ╚════════════════════════════════════╝");
        System.out.println("   Tres luchadores. Un solo Victorioso.");
    }

    /** Pide el nombre y el tipo de luchador, y lo crea. */
    private Luchador elegirLuchador() {
        System.out.println("\n¿Cuál es tu nombre de pelea?");
        String nombre = Consola.leerTexto();
        if (nombre.isEmpty()) {
            nombre = "Errante"; // nombre por defecto si no escribió nada
        }

        System.out.println("\nElegí tu luchador:");
        // values() devuelve todos los valores del enum en orden: el menú se arma solo
        TipoLuchador[] tipos = TipoLuchador.values();
        for (int i = 0; i < tipos.length; i++) {
            TipoLuchador t = tipos[i];
            System.out.println((i + 1) + ") " + t.getNombre() + " - " + t.getDescripcion());
            System.out.println("     Fuerza " + t.getFuerza() + " | Agilidad " + t.getAgilidad()
                    + " | Carisma " + t.getCarisma() + " | Arma: " + t.crearArmaInicial().getNombre());
        }
        int elegido = Consola.leerOpcion(1, tipos.length);
        // El usuario elige desde 1, pero el arreglo empieza en 0: por eso el -1
        return new Luchador(nombre, tipos[elegido - 1]);
    }

    // ---------------- Pasillo ----------------

    /**
     * Menú del pasillo antes de cada ronda. Termina cuando la pelea se resuelve (peleando o hablando).
     * La opción 3 cambia según la ronda: con Takeshi se puede hablar, con la Mantis no.
     */
    private Resultado pasillo(int ronda) {
        Consola.titulo("El pasillo de la Fosa - Ronda " + ronda);
        if (ronda == 1) {
            System.out.println("Bajás a un sótano húmedo. Se escucha el rugido de la gente.");
            System.out.println("Hay un guardia, un comerciante con un carrito y, detrás de la reja, Takeshi \"La Montaña\".");
        } else {
            System.out.println("Volvés al pasillo con la bolsa en la mano. La tribuna todavía grita tu nombre.");
            System.out.println("En una jaula al fondo, algo verde de tres metros te mira sin parpadear: la próxima rival.");
        }

        // Se repite el menú hasta que algún return devuelva un Resultado
        while (true) {
            mostrarFicha();
            System.out.println("\n1) Hablar con el guardia");
            System.out.println("2) Ver el carrito del comerciante");
            if (ronda == 1) {
                System.out.println("3) Hablar con Takeshi a través de la reja");
            } else {
                System.out.println("3) Acercarte a la jaula de la mantis");
            }
            System.out.println("4) Entrar al ring y pelear");
            int opcion = Consola.leerOpcion(1, 4);
            if (opcion == 1) {
                guardia(ronda);
            } else if (opcion == 2) {
                comerciante();
            } else if (opcion == 3 && ronda == 1) {
                // null significa "no se resolvió nada, seguimos en el pasillo"
                Resultado charla = charlaConTakeshi();
                if (charla != null) {
                    return charla;
                }
            } else if (opcion == 3) {
                jaulaDeLaMantis();
            } else {
                // Opción 4: se crea el combate contra el rival de la ronda y se devuelve lo que devuelva la pelea.
                // Se guarda en una variable de tipo Enemigo: Combate no necesita saber si es Sumo o Mantis
                Enemigo rival = (ronda == 1) ? sumo : mantis;
                return new Combate(luchador, rival, azar).pelear();
            }
        }
    }

    /** La línea con los datos del jugador: [Nombre | Tipo | Nv | Vida | F A C | $ | Arma | Comida] */
    private void mostrarFicha() {
        System.out.println("\n[" + luchador.getNombre() + " | " + luchador.getTipo().getNombre()
                + " | Nv " + luchador.getNivel() + " | Vida " + luchador.getVidaMax()
                + " | F " + luchador.getFuerza() + " A " + luchador.getAgilidad() + " C " + luchador.getCarisma()
                + " | $" + luchador.getMonedas() + " | Arma: " + luchador.getArmaEquipada().getNombre()
                + " | Comida: " + luchador.getComidas() + " | Vendas: " + luchador.getVendasParaPelear() + "]");
    }

    /** Muestra una opción; si no se cumple el requisito, aparece marcada. */
    private void opcionConRequisito(int numero, String texto, Atributo atributo, int minimo) {
        String marca = luchador.cumple(atributo, minimo) ? "" : " (no te alcanza)";
        System.out.println(numero + ") [" + atributo.getNombre() + " " + minimo + "] " + texto + marca);
    }

    /** El guardia: da pistas sobre el rival de la ronda según tus atributos. */
    private void guardia(int ronda) {
        if (ronda == 2) {
            guardiaRonda2();
            return;
        }
        Consola.titulo("El guardia");
        System.out.println("—¿Otro novato? Tu rival es Takeshi \"La Montaña\". Nadie lo movió del centro del ring.");
        System.out.println("\n1) Preguntar cómo funciona el torneo");
        opcionConRequisito(2, "Invitarle un cigarro y preguntarle por el sumo", Atributo.CARISMA, 6);
        opcionConRequisito(3, "Mirarlo fijo hasta que hable", Atributo.FUERZA, 6);
        System.out.println("4) Volver");
        int opcion = Consola.leerOpcion(1, 4);
        // Las opciones bloqueadas se pueden elegir igual, por eso se vuelve a chequear el requisito
        if (opcion == 1) {
            System.out.println("—Peleás, ganás, subís. El último en pie sale Victorioso.");
        } else if (opcion == 2 && luchador.cumple(Atributo.CARISMA, 6)) {
            System.out.println("—Entre nos: antes de la embestida raspa el piso. Si lo esquivás, se estrella");
            System.out.println(" contra las cuerdas. Y vive con hambre... por un onigiri se olvida de pelear.");
        } else if (opcion == 3 && luchador.cumple(Atributo.FUERZA, 6)) {
            System.out.println("—Está bien, está bien... Pega como un camión. No te quedes quieto cuando carga.");
        } else if (opcion != 4) {
            // Eligió la 2 o la 3 sin cumplir el requisito
            System.out.println("El guardia ni te mira.");
        }
        if (opcion != 4) {
            Consola.pausa();
        }
    }

    /** Las pistas del guardia sobre la Mantis. Mismo esquema de requisitos que en la ronda 1. */
    private void guardiaRonda2() {
        Consola.titulo("El guardia");
        System.out.println("—Así que le ganaste a Takeshi... Ahora te toca Sor Tijereta. La trajeron de un convento.");
        System.out.println("\n1) Preguntar qué es esa cosa");
        opcionConRequisito(2, "Convidarle un mate y preguntarle por sus costumbres", Atributo.CARISMA, 6);
        opcionConRequisito(3, "Mirarlo fijo hasta que hable", Atributo.FUERZA, 6);
        System.out.println("4) Volver");
        int opcion = Consola.leerOpcion(1, 4);
        if (opcion == 1) {
            System.out.println("—Una mantis religiosa. Gigante. No habla, no come y es más rápida que vos. Suerte.");
        } else if (opcion == 2 && luchador.cumple(Atributo.CARISMA, 6)) {
            System.out.println("—Es muy devota. Si hacés que la tribuna cante, se arrodilla a rezar y se olvida de pelear.");
            System.out.println(" Dicen que si canta tres veces, se vuelve al convento.");
        } else if (opcion == 3 && luchador.cumple(Atributo.FUERZA, 6)) {
            System.out.println("—Ataca dos veces seguidas, pero es flaquita: si la agarrás de lleno, siente cada golpe.");
            System.out.println(" Y cuando abre las patas, corré: ese abrazo te parte al medio.");
        } else if (opcion != 4) {
            System.out.println("El guardia ni te mira.");
        }
        if (opcion != 4) {
            Consola.pausa();
        }
    }

    /** Con la mantis no se puede hablar: solo se la puede observar. */
    private void jaulaDeLaMantis() {
        Consola.titulo("La jaula de Sor Tijereta");
        System.out.println("Te acercás a los barrotes y le decís algo. Sor Tijereta gira la cabeza 180 grados,");
        System.out.println("te mira con dos ojos enormes y hace clic-clic con las pinzas. No entiende nada.");
        if (luchador.cumple(Atributo.AGILIDAD, 6)) {
            System.out.println("\nTe quedás mirándola un rato y notás algo: antes de atacar fuerte, abre las patas");
            System.out.println("delanteras de par en par. Ese es el momento de esquivar.");
        }
        Consola.pausa();
    }

    /** La tienda. Se queda en el menú hasta que el jugador elige "Volver". */
    private void comerciante() {
        // Arreglo fijo con las armas en venta: las opciones 1, 2 y 3 del menú
        Arma[] armas = { new Mazo(), new Dagas(), new Latigo() };
        int precioComida = luchador.precioCon(10);
        int precioVenda = luchador.precioCon(10);

        while (true) {
            Consola.titulo("El comerciante (tenés $" + luchador.getMonedas() + ")");
            System.out.println("—Todo tiene precio, pibe. A los simpáticos les hago rebaja.");
            for (int i = 0; i < armas.length; i++) {
                Arma a = armas[i];
                System.out.println((i + 1) + ") " + a.getNombre() + " " + a.textoRequisito()
                        + " - $" + luchador.precioCon(a.getPrecio()) + " - " + a.getDescripcion());
            }
            System.out.println("4) Onigiri gigante - $" + precioComida);
            System.out.println("5) Venda (cura el 30 % de tu vida en combate) - $" + precioVenda);
            // El robo solo se puede hacer una vez: después cambia el texto de la opción
            if (!robo) {
                opcionConRequisito(6, "Manotear un onigiri cuando no mira", Atributo.AGILIDAD, 6);
            } else {
                System.out.println("6) (ya no te quita los ojos de encima)");
            }
            System.out.println("7) Volver");

            int opcion = Consola.leerOpcion(1, 7);
            if (opcion <= 3) {
                comprarArma(armas[opcion - 1]); // opción 1 -> armas[0], etc.
            } else if (opcion == 4) {
                if (luchador.pagar(precioComida)) {
                    luchador.agregarComida();
                    System.out.println("Compraste un onigiri gigante.");
                } else {
                    System.out.println("No te alcanza la plata.");
                }
            } else if (opcion == 5) {
                if (luchador.pagar(precioVenda)) {
                    luchador.agregarVenda();
                    System.out.println("Compraste una venda. Se suma a las " + Luchador.VENDAS_POR_PELEA + " que te dan en cada pelea.");
                } else {
                    System.out.println("No te alcanza la plata.");
                }
            } else if (opcion == 6) {
                if (!robo && luchador.cumple(Atributo.AGILIDAD, 6)) {
                    robo = true; // bandera: ya no se puede volver a robar
                    luchador.agregarComida();
                    System.out.println("Con un movimiento rápido te guardás un onigiri. Nadie vio nada.");
                } else {
                    System.out.println("No hay forma de hacerlo sin que te vea.");
                }
            } else {
                return; // opción 7: volver al pasillo
            }
        }
    }

    /**
     * Compra un arma. Primero valida todo y recién al final cobra,
     * así nunca se pierde plata por una compra que no se podía hacer.
     */
    private void comprarArma(Arma arma) {
        if (luchador.tieneArma(arma.getNombre())) {
            System.out.println("Ya tenés " + arma.getNombre() + ".");
        } else if (!luchador.puedeUsar(arma)) {
            System.out.println("No te da la " + arma.getRequisito().getNombre().toLowerCase() + " para usarla.");
        } else if (!luchador.pagar(luchador.precioCon(arma.getPrecio()))) {
            // pagar() devuelve false y no cobra nada si no alcanza
            System.out.println("No te alcanza la plata.");
        } else {
            luchador.agregarArma(arma);
            luchador.equipar(arma);
            System.out.println("Compraste y equipaste: " + arma.getNombre() + ".");
        }
    }

    /** Devuelve un resultado si la pelea se resolvió hablando, o null si hay que seguir. */
    private Resultado charlaConTakeshi() {
        Consola.titulo("Takeshi \"La Montaña\"");
        System.out.println("Takeshi se acerca a la reja masticando algo.");
        System.out.println("—Pequeño... ¿viniste a pelear o a que te lleven en camilla?");
        System.out.println();
        opcionConRequisito(1, "Convencerlo de que pelear con vos no le suma nada", Atributo.CARISMA, 7);
        System.out.println("2) Pasarle un onigiri por la reja (tenés " + luchador.getComidas() + ")");
        System.out.println("3) Ofrecerle $" + luchador.precioCon(20) + " para que se deje ganar"
                + (pagoAlSumo ? " (ya le pagaste)" : ""));
        opcionConRequisito(4, "Amenazarlo en voz baja" + (amenazo ? " (ya lo hiciste)" : ""), Atributo.FUERZA, 7);
        System.out.println("5) Volver");

        int opcion = Consola.leerOpcion(1, 5);
        if (opcion == 1) {
            // Convencerlo: con Carisma 7 se gana sin pelear
            if (luchador.cumple(Atributo.CARISMA, 7)) {
                System.out.println("Le hablás de su leyenda, de su dignidad, de lo poco que ganaría aplastando a alguien como vos.");
                System.out.println("Takeshi se ríe y se sienta. —Tenés razón. Pasá, campeón.");
                Consola.pausa();
                return Resultado.NEGOCIADO;
            }
            System.out.println("Takeshi bosteza. No le interesa nada de lo que decís.");
        } else if (opcion == 2) {
            // Onigiri: se gasta siempre, y acepta según la probabilidad que da el carisma
            if (!luchador.usarComida()) {
                System.out.println("No tenés comida para darle.");
            } else if (azar.nextDouble() < luchador.probabilidadConvencer()) {
                System.out.println("Takeshi agarra el onigiri, lo huele y se olvida de que había una pelea.");
                Consola.pausa();
                return Resultado.NEGOCIADO;
            } else {
                System.out.println("Takeshi tira el onigiri al piso. —¿Me querés comprar con eso?");
            }
        } else if (opcion == 3) {
            // Soborno: no gana directo, pero Takeshi pelea mucho más débil (ver Sumo.dejarsePerder)
            if (pagoAlSumo) {
                System.out.println("Ya le pagaste.");
            } else if (luchador.pagar(luchador.precioCon(20))) {
                pagoAlSumo = true;
                sumo.dejarsePerder();
                System.out.println("Takeshi guarda las monedas y te guiña un ojo. Va a pelear a media máquina.");
            } else {
                System.out.println("No te alcanza la plata.");
            }
        } else if (opcion == 4) {
            // Amenaza: con Fuerza 7, Takeshi arranca con 20 % menos de vida (una sola vez)
            luchador.marcarViolencia(); // amenazar cuenta como violencia, le salga o no
            if (!amenazo && luchador.cumple(Atributo.FUERZA, 7)) {
                amenazo = true;
                sumo.intimidar();
                System.out.println("A Takeshi le tiembla el labio. Va a arrancar la pelea nervioso (-20 % de vida).");
            } else if (amenazo) {
                System.out.println("Ya lo amenazaste. Ahora solo te mira con odio.");
            } else {
                System.out.println("Takeshi se ríe en tu cara.");
            }
        }
        if (opcion != 5) {
            Consola.pausa();
        }
        return null; // no se resolvió la pelea: se vuelve al menú del pasillo
    }

    // ---------------- Después de la pelea ----------------

    /** Reparte los puntos libres que dio la subida de nivel. */
    private void subirNivel() {
        Consola.titulo("¡Subiste a nivel " + luchador.getNivel() + "!");
        System.out.println("Vida máxima: " + luchador.getVidaMax() + " (+10)");
        // Pide un atributo por cada punto libre, hasta que no quede ninguno
        while (luchador.getPuntosLibres() > 0) {
            System.out.println("\nPuntos para repartir: " + luchador.getPuntosLibres());
            System.out.println("1) Fuerza   (" + luchador.getFuerza() + ")");
            System.out.println("2) Agilidad (" + luchador.getAgilidad() + ")");
            System.out.println("3) Carisma  (" + luchador.getCarisma() + ")");
            int opcion = Consola.leerOpcion(1, 3);
            // Atributo.values() es {FUERZA, AGILIDAD, CARISMA}: la opción 1 es la posición 0
            luchador.asignarPunto(Atributo.values()[opcion - 1]);
        }
    }

    /** Pantalla final de victoria. Cuenta cómo ganó cada ronda. */
    private void mostrarVictoria(Resultado ronda1, Resultado ronda2) {
        Consola.titulo("V I C T O R I O S O");
        System.out.println(luchador.getNombre() + " salió del Ring de la Fosa como campeón del torneo.");
        System.out.println("  Ronda 1, Takeshi:      " + (ronda1 == Resultado.NEGOCIADO ? "sin tirar una sola piña" : "a puro golpe"));
        System.out.println("  Ronda 2, Sor Tijereta: " + (ronda2 == Resultado.NEGOCIADO ? "la mandaste de vuelta al convento" : "a puro golpe"));
        mostrarStats();
        // Ganó las dos sin pelear, pero en algún momento atacó o amenazó: se le da una pista
        if (ronda1 == Resultado.NEGOCIADO && ronda2 == Resultado.NEGOCIADO) {
            System.out.println("\nCasi... La próxima, probá sin levantar la mano ni una sola vez.");
        }
    }

    /** El final secreto: ganó todo con palabras, comida y canciones, sin atacar ni amenazar. */
    private void mostrarFinalPacifista() {
        // Sin color en el título: titulo() mide el largo del texto y los códigos de color lo agrandarían
        Consola.titulo("F I N A L   P A C I F I S T A");
        System.out.println(luchador.getNombre() + " ganó el Torneo Clandestino sin tirar una sola piña.");
        System.out.println();
        System.out.println("En la tribuna, Takeshi comparte onigiris con desconocidos y te aplaude con la boca llena.");
        System.out.println("A su lado, Sor Tijereta reza un rosario entero por tu alma.");
        System.out.println("El organizador revolea la bolsa de apuestas: —¿¡Un torneo entero sin sangre!? ¡Me fundiste!");
        System.out.println("La gente baja al ring y te saca en andas cantando tu nombre.");
        System.out.println();
        System.out.println(Consola.color("La Fosa nunca había visto algo así: un campeón que no lastimó a nadie.", Consola.VERDE));
        mostrarStats();
    }

    /** La línea de atributos finales, compartida por los dos finales de victoria. */
    private void mostrarStats() {
        System.out.println("Nivel " + luchador.getNivel() + " | Fuerza " + luchador.getFuerza()
                + " | Agilidad " + luchador.getAgilidad() + " | Carisma " + luchador.getCarisma());
    }

    /** Pantalla de derrota: dice contra quién perdió. */
    private void mostrarDerrota(Enemigo ganador) {
        Consola.titulo("D E R R O T A");
        System.out.println("A " + luchador.getNombre() + " lo sacaron en camilla después de enfrentar a " + ganador.getApodo() + ".");
        System.out.println("La Fosa no perdona.");
    }
}
