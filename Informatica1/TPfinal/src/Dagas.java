import java.util.Random;

/** Las dagas: dos cortes y sangrado. Necesitan Agilidad 6. Arma inicial de La Sombra. */
public class Dagas extends Arma {

    public Dagas() {
        super("Dagas", "Dos cortes y sangrado 3 turnos", 25, Atributo.AGILIDAD, 6);
    }

    @Override
    public String golpear(Luchador atacante, Enemigo objetivo, Random azar) {
        // Cada corte escala con la agilidad: con agilidad 8 son 2 + 4 = 6
        int corte = 2 + atacante.getAgilidad() / 2;
        objetivo.recibirDanio(corte); // primer corte
        objetivo.recibirDanio(corte); // segundo corte
        objetivo.sangrar(3);          // pierde vida extra al final de los próximos 3 turnos
        return "Dos cortes rápidos: -" + corte + " y -" + corte + ". Está sangrando.";
    }
}
