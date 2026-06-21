package damas.modelo;

public interface Movimentavel {

    boolean podeMovimentar(int origemL, int origemC, int destinoL, int destinoC, Tabuleiro tabuleiro);

    boolean temCapturaDisponivel(int linhaAtual, int colunaAtual, Tabuleiro tabuleiro);
}
