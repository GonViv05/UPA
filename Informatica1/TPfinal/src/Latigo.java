import java.util.Random;

/** El látigo: poco daño, pero enreda al rival. Necesita Carisma 6. Arma inicial de El Charlatán. */
public class Latigo extends Arma {

    public Latigo() {
        super("Látigo", "Lo enreda: 50 % de que pierda su próximo ataque", 25, Atributo.CARISMA, 6);
    }

    @Override
    public String golpear(Luchador atacante, Enemigo objetivo, Random azar, boolean critico) {
        // Escala con el carisma (el látigo es el arma "de show"): con carisma 8 son 6 + 2 = 8
        int danio = 6 + atacante.getCarisma() / 3;
        if (critico) {
            danio *= 2;
        }
        objetivo.recibirDanio(danio);
        objetivo.enredar(); // el 50 % se tira después, cuando el enemigo intente atacar
        String rival = objetivo.getNombre();
        String[] frases = {
            "El látigo restalla y se enrosca en los tobillos de " + rival,
            "Hacés un giro para la tribuna y el látigo le azota la espalda a " + rival,
            "Un chasquido que hace gritar al público: el látigo atrapa las piernas de " + rival
        };
        return Consola.alAzar(frases, azar) + ": -" + danio + ".";
    }
}
