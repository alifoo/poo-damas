package damas.modelo;

import java.io.Serializable;

public class ConfiguracaoJogo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Jogador jogadorBranco;
    private Jogador jogadorPreto;
    private Tabuleiro tabuleiro;

    public ConfiguracaoJogo(Jogador jogadorBranco, Jogador jogadorPreto) {
        this.jogadorBranco = jogadorBranco;
        this.jogadorPreto  = jogadorPreto;
        this.tabuleiro = new Tabuleiro();
        this.tabuleiro.inicializar();
    }

    public Jogador getJogadorBranco() { return jogadorBranco; }
    public Jogador getJogadorPreto()  { return jogadorPreto; }
    public Tabuleiro getTabuleiro()   { return tabuleiro; }

    @Override
    public String toString() {
        return "ConfiguracaoJogo{\n  " + jogadorBranco + "\n  " + jogadorPreto + "\n}";
    }
}
