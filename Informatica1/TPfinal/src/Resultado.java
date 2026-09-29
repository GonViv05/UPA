/** Cómo terminó una pelea. Lo devuelven Combate.pelear() y la charla con Takeshi. */
public enum Resultado {
    /** Takeshi quedó en 0 de vida. */
    GANADO,
    /** El jugador quedó en 0 de vida. */
    PERDIDO,
    /** Se resolvió hablando o con comida: cuenta como victoria. */
    NEGOCIADO;

    /** true para GANADO y NEGOCIADO: las dos formas de ganar. */
    public boolean esVictoria() {
        return this != PERDIDO;
    }
}
