package damas.modelo;

/**
 * Peça preta do jogo de damas.
 * Subclasse de Peca — generalização/especialização (requisito RA2).
 * Pretas ficam nas linhas 5-7 (parte superior da tela) e avançam
 * para linhas menores (-1), ou seja, "para baixo" visualmente.
 */
public class PecaPreta extends Peca {

    private static final long serialVersionUID = 1L;

    public PecaPreta() {
        super(Cor.PRETA);
    }

    @Override
    public int getDirecaoLinha() {
        return -1;
    }
}
