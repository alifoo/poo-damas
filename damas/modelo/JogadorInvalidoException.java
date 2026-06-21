package damas.modelo;

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
