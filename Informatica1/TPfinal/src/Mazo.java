import java.util.Random;

/** El mazo: mucho daño y chance de aturdir. Necesita Fuerza 6. Arma inicial de El Bruto. */
public class Mazo extends Arma {

    public Mazo() {
        super("Mazo", "Mucho daño, 40 % de aturdir (pierde el turno)", 25, Atributo.FUERZA, 6);
    }

    @Override
    public String golpear(Luchador atacante, Enemigo objetivo, Random azar, boolean critico) {
        // Escala mucho con la fuerza: con fuerza 8 hace 12 + 16 = 28
        int danio = 12 + atacante.getFuerza() * 2;
        if (critico) {
            danio *= 2;
        }
        objetivo.recibirDanio(danio);
        String rival = objetivo.getNombre();
        String[] frases = {
            "Levantás el mazo por encima de la cabeza y lo bajás de lleno sobre el hombro de " + rival,
            "Girás con todo el peso del cuerpo y el mazo se hunde en las costillas de " + rival,
            "Un mazazo seco a la rodilla de " + rival + ". Se escucha el crujido desde la tribuna"
        };
        String texto = Consola.alAzar(frases, azar) + ": -" + danio + ".";
        // nextDouble() da un número entre 0 y 1: que sea menor a 0.4 pasa el 40 % de las veces
        if (azar.nextDouble() < 0.4) {
            objetivo.aturdir();
            texto += " " + rival + " se tambalea viendo estrellas: pierde su próximo turno.";
        }
        return texto;
    }
}
