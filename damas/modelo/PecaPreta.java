package damas.modelo;

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
