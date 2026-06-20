package pacote1;

/**
 * Exceção lançada quando um jogador inválido tenta realizar uma ação.
 * Derivada de Exception (requisito RA2 - segunda classe de exceção customizada).
 */
public class JogadorInvalidoException extends Exception {

    private String nomeJogador;

    public JogadorInvalidoException(String mensagem) {
        super(mensagem);
    }

    public JogadorInvalidoException(String mensagem, String nomeJogador) {
        super(mensagem);
        this.nomeJogador = nomeJogador;
    }

    public String getNomeJogador() { return nomeJogador; }

    @Override
    public String toString() {
        return "JogadorInvalidoException: " + getMessage() +
               (nomeJogador != null ? " [Jogador: " + nomeJogador + "]" : "");
    }
}
