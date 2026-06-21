package damas.modelo;

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
