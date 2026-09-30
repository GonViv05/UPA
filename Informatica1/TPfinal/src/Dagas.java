import java.util.Random;

/** Las dagas: dos cortes y sangrado. Necesitan Agilidad 6. Arma inicial de La Sombra. */
public class Dagas extends Arma {

    public Dagas() {
        super("Dagas", "Dos cortes y sangrado 3 turnos", 25, Atributo.AGILIDAD, 6);
    }

    @Override
    public String golpear(Luchador atacante, Enemigo objetivo, Random azar, boolean critico) {
        // Cada corte escala con la agilidad: con agilidad 8 son 2 + 4 = 6
        int corte = 2 + atacante.getAgilidad() / 2;
        if (critico) {
            corte *= 2; // el crítico duplica los dos cortes
        }
        objetivo.recibirDanio(corte); // primer corte
        objetivo.recibirDanio(corte); // segundo corte
        objetivo.sangrar(3);          // pierde vida extra al final de los próximos 3 turnos
        String rival = objetivo.getNombre();
        String[] frases = {
            "Te deslizás por debajo de la guardia de " + rival + " y le abrís dos tajos en el costado",
            "Dos destellos: una daga al brazo y otra al muslo de " + rival,
            "Girás alrededor de " + rival + " y le cruzás la espalda con las dos dagas"
        };
        return Consola.alAzar(frases, azar) + ": -" + corte + " y -" + corte + ". Empieza a sangrar.";
    }
}
