package damas.modelo;

import java.io.Serializable;

/**
 * Representa o tabuleiro 8x8 do jogo de damas.
 * Implementa Serializable para persistência (requisito RA3).
 */
public class Tabuleiro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Peca[][] casas;

    public Tabuleiro() {
        casas = new Peca[8][8];
    }

    /**
     * Inicializa o tabuleiro com a configuração padrão de damas. As peças ficam
     * nas casas escuras — convenção (linha+col) par —, de modo que a grande
     * diagonal A1–H8 seja escura e o canto à esquerda de cada jogador seja preto.
     */
    public void inicializar() {
        // Peças pretas nas linhas 5, 6, 7 (parte superior)
        for (int linha = 5; linha <= 7; linha++) {
            for (int col = 0; col < 8; col++) {
                if ((linha + col) % 2 == 0) {
                    casas[linha][col] = new PecaPreta();
                }
            }
        }
        // Peças brancas nas linhas 0, 1, 2 (parte inferior)
        for (int linha = 0; linha <= 2; linha++) {
            for (int col = 0; col < 8; col++) {
                if ((linha + col) % 2 == 0) {
                    casas[linha][col] = new PecaBranca();
                }
            }
        }
    }

    public Peca getPeca(int linha, int col) {
        if (linha < 0 || linha > 7 || col < 0 || col > 7) return null;
        return casas[linha][col];
    }

    public void setPeca(int linha, int col, Peca peca) {
        casas[linha][col] = peca;
    }

    public void removerPeca(int linha, int col) {
        casas[linha][col] = null;
    }

    /** Conta peças de uma cor no tabuleiro. */
    public int contarPecas(Peca.Cor cor) {
        int count = 0;
        for (int l = 0; l < 8; l++)
            for (int c = 0; c < 8; c++)
                if (casas[l][c] != null && casas[l][c].getCor() == cor) count++;
        return count;
    }

    /** Representação textual do tabuleiro para logs. */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  A B C D E F G H\n");
        for (int l = 7; l >= 0; l--) {
            sb.append((l + 1)).append(" ");
            for (int c = 0; c < 8; c++) {
                Peca p = casas[l][c];
                if (p == null) sb.append(". ");
                else if (p.getCor() == Peca.Cor.BRANCA) sb.append(p.isEDama() ? "D " : "b ");
                else sb.append(p.isEDama() ? "R " : "p ");
            }
            sb.append((l + 1)).append("\n");
        }
        sb.append("  A B C D E F G H\n");
        return sb.toString();
    }

    
}
