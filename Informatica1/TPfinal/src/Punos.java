import java.util.Random;

/** Los puños: todos los luchadores los tienen y no piden ningún requisito. */
public class Punos extends Arma {

    public Punos() {
        // nombre, descripción, precio 0, sin requisito (null) y mínimo 0
        super("Puños", "Daño bajo, sin efectos", 0, null, 0);
    }

    @Override
    public String golpear(Luchador atacante, Enemigo objetivo, Random azar, boolean critico) {
        // División entera: con fuerza 3, 3 / 2 da 1 (se descartan los decimales)
        int danio = 4 + atacante.getFuerza() / 2;
        if (critico) {
            danio *= 2;
        }
        objetivo.recibirDanio(danio);
        String rival = objetivo.getNombre();
        String[] frases = {
            "Le metés un gancho a la mandíbula a " + rival,
            "Un directo seco al estómago de " + rival + ", con todo el hombro atrás",
            "Le das un codazo a " + rival + " que el árbitro prefiere no ver"
        };
        return Consola.alAzar(frases, azar) + ": -" + danio;
    }
}
