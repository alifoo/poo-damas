package pacote1;

/**
 * Exceção lançada quando um movimento de peça é inválido.
 * Derivada de Exception (requisito RA2 - classe de exceção customizada).
 */
public class MovimentoInvalidoException extends Exception {

    private int origemLinha;
    private int origemColuna;
    private int destinoLinha;
    private int destinoColuna;

    public MovimentoInvalidoException(String mensagem) {
        super(mensagem);
    }

    public MovimentoInvalidoException(String mensagem, int origemLinha, int origemColuna,
                                       int destinoLinha, int destinoColuna) {
        super(mensagem);
        this.origemLinha = origemLinha;
        this.origemColuna = origemColuna;
        this.destinoLinha = destinoLinha;
        this.destinoColuna = destinoColuna;
    }

    public int getOrigemLinha()   { return origemLinha; }
    public int getOrigemColuna()  { return origemColuna; }
    public int getDestinoLinha()  { return destinoLinha; }
    public int getDestinoColuna() { return destinoColuna; }

    @Override
    public String toString() {
        return "MovimentoInvalidoException: " + getMessage() +
               " [(" + origemLinha + "," + origemColuna + ") -> (" +
               destinoLinha + "," + destinoColuna + ")]";
    }
}
