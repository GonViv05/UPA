import java.util.Random;

/** El látigo: poco daño, pero enreda al rival. Necesita Carisma 6. Arma inicial de El Charlatán. */
public class Latigo extends Arma {

    public Latigo() {
        super("Látigo", "Lo enreda: 50 % de que pierda su próximo ataque", 25, Atributo.CARISMA, 6);
    }

    @Override
    public String golpear(Luchador atacante, Enemigo objetivo, Random azar) {
        // Escala con el carisma (el látigo es el arma "de show"): con carisma 8 son 6 + 2 = 8
        int danio = 6 + atacante.getCarisma() / 3;
        objetivo.recibirDanio(danio);
        objetivo.enredar(); // el 50 % se tira después, cuando el enemigo intente atacar
        return "¡Latigazo! -" + danio + ". Le enredás las piernas.";
    }
}
