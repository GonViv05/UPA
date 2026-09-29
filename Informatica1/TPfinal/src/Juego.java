import java.util.Random;

/**
 * Una partida completa: elegir luchador, pasillo, pelea, subir de nivel y final.
 * Main crea un Juego nuevo por cada partida, así el estado de abajo arranca siempre de cero.
 */
public class Juego {
    private final Random azar = new Random(); // única fuente de azar; se le pasa a Combate
    private Luchador luchador;
    private Sumo sumo;
    // Banderas: arrancan en false y pasan a true la primera vez, para que no se pueda repetir
    private boolean robo;        // ya le robó un onigiri al comerciante
    private boolean pagoAlSumo;  // ya le pagó para que se deje perder
    private boolean amenazo;     // ya lo amenazó

    /** El "guion" de la partida, en orden. */
    public void jugar() {
        mostrarPortada();
        luchador = elegirLuchador();
        sumo = new Sumo();

        // pasillo() no vuelve hasta que la pelea se resuelve (peleando o hablando)
        Resultado resultado = pasillo();
        if (resultado.esVictoria()) {
            luchador.ganarExperiencia(sumo.getXpQueOtorga());
            subirNivel();
            mostrarVictoria(resultado);
        } else {
            mostrarDerrota();
        }
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

    /** Menú del pasillo. Termina cuando la pelea se resuelve (peleando o hablando). */
    private Resultado pasillo() {
        Consola.titulo("El pasillo de la Fosa");
        System.out.println("Bajás a un sótano húmedo. Se escucha el rugido de la gente.");
        System.out.println("Hay un guardia, un comerciante con un carrito y, detrás de la reja, Takeshi \"La Montaña\".");

        // Se repite el menú hasta que algún return devuelva un Resultado
        while (true) {
            mostrarFicha();
            System.out.println("\n1) Hablar con el guardia");
            System.out.println("2) Ver el carrito del comerciante");
            System.out.println("3) Hablar con Takeshi a través de la reja");
            System.out.println("4) Entrar al ring y pelear");
            int opcion = Consola.leerOpcion(1, 4);
            if (opcion == 1) {
                guardia();
            } else if (opcion == 2) {
                comerciante();
            } else if (opcion == 3) {
                // null significa "no se resolvió nada, seguimos en el pasillo"
                Resultado charla = charlaConTakeshi();
                if (charla != null) {
                    return charla;
                }
            } else {
                // Opción 4: se crea el combate y se devuelve lo que devuelva la pelea
                return new Combate(luchador, sumo, azar).pelear();
            }
        }
    }

    /** La línea con los datos del jugador: [Nombre | Tipo | Nv | Vida | F A C | $ | Arma | Comida] */
    private void mostrarFicha() {
        System.out.println("\n[" + luchador.getNombre() + " | " + luchador.getTipo().getNombre()
                + " | Nv " + luchador.getNivel() + " | Vida " + luchador.getVidaMax()
                + " | F " + luchador.getFuerza() + " A " + luchador.getAgilidad() + " C " + luchador.getCarisma()
                + " | $" + luchador.getMonedas() + " | Arma: " + luchador.getArmaEquipada().getNombre()
                + " | Comida: " + luchador.getComidas() + "]");
    }

    /** Muestra una opción; si no se cumple el requisito, aparece marcada. */
    private void opcionConRequisito(int numero, String texto, Atributo atributo, int minimo) {
        String marca = luchador.cumple(atributo, minimo) ? "" : " (no te alcanza)";
        System.out.println(numero + ") [" + atributo.getNombre() + " " + minimo + "] " + texto + marca);
    }

    /** El guardia: da pistas sobre Takeshi según tus atributos. */
    private void guardia() {
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

    /** La tienda. Se queda en el menú hasta que el jugador elige "Volver". */
    private void comerciante() {
        // Arreglo fijo con las armas en venta: las opciones 1, 2 y 3 del menú
        Arma[] armas = { new Mazo(), new Dagas(), new Latigo() };
        int precioComida = luchador.precioCon(10);

        while (true) {
            Consola.titulo("El comerciante (tenés $" + luchador.getMonedas() + ")");
            System.out.println("—Todo tiene precio, pibe. A los simpáticos les hago rebaja.");
            for (int i = 0; i < armas.length; i++) {
                Arma a = armas[i];
                System.out.println((i + 1) + ") " + a.getNombre() + " " + a.textoRequisito()
                        + " - $" + luchador.precioCon(a.getPrecio()) + " - " + a.getDescripcion());
            }
            System.out.println("4) Onigiri gigante - $" + precioComida);
            // El robo solo se puede hacer una vez: después cambia el texto de la opción
            if (!robo) {
                opcionConRequisito(5, "Manotear un onigiri cuando no mira", Atributo.AGILIDAD, 6);
            } else {
                System.out.println("5) (ya no te quita los ojos de encima)");
            }
            System.out.println("6) Volver");

            int opcion = Consola.leerOpcion(1, 6);
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
                if (!robo && luchador.cumple(Atributo.AGILIDAD, 6)) {
                    robo = true; // bandera: ya no se puede volver a robar
                    luchador.agregarComida();
                    System.out.println("Con un movimiento rápido te guardás un onigiri. Nadie vio nada.");
                } else {
                    System.out.println("No hay forma de hacerlo sin que te vea.");
                }
            } else {
                return; // opción 6: volver al pasillo
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

    /** Pantalla final de victoria. El texto cambia según cómo ganó. */
    private void mostrarVictoria(Resultado resultado) {
        Consola.titulo("V I C T O R I O S O");
        if (resultado == Resultado.NEGOCIADO) {
            System.out.println(luchador.getNombre() + " salió del Ring de la Fosa como campeón sin tirar una sola piña.");
        } else {
            System.out.println(luchador.getNombre() + " salió del Ring de la Fosa como campeón, a puro golpe.");
        }
        System.out.println("Nivel " + luchador.getNivel() + " | Fuerza " + luchador.getFuerza()
                + " | Agilidad " + luchador.getAgilidad() + " | Carisma " + luchador.getCarisma());
    }

    private void mostrarDerrota() {
        Consola.titulo("D E R R O T A");
        System.out.println("A " + luchador.getNombre() + " lo sacaron en camilla. La Fosa no perdona.");
    }
}
