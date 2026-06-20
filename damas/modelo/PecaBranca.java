package damas.modelo;

/**
 * Peça branca do jogo de damas.
 * Subclasse de Peca — generalização/especialização (requisito RA2).
 * Brancas ficam nas linhas 0-2 (parte inferior da tela) e avançam
 * para linhas maiores (+1), ou seja, "para cima" visualmente.
 */
public class PecaBranca extends Peca {

    private static final long serialVersionUID = 1L;

    public PecaBranca() {
        super(Cor.BRANCA);
    }

    @Override
    public int getDirecaoLinha() {
        return 1;
    }
}
