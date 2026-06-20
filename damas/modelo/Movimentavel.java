package damas.modelo;

/**
 * Interface que define o contrato para objetos que podem ser movimentados no tabuleiro.
 * Requisito RA2 - definição de interface implementada por alguma classe.
 */
public interface Movimentavel {

    /**
     * Verifica se um movimento de (origemL, origemC) para (destinoL, destinoC) é válido.
     */
    boolean podeMovimentar(int origemL, int origemC, int destinoL, int destinoC, Tabuleiro tabuleiro);

    /**
     * Verifica se existe alguma captura obrigatória disponível para esta peça.
     */
    boolean temCapturaDisponivel(int linhaAtual, int colunaAtual, Tabuleiro tabuleiro);
}
