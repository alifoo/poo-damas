package damas.modelo;

import java.io.Serializable;

public class Tabuleiro implements Serializable {

    private static final long serialVersionUID = 1L;

    private Peca[][] casas;

    public Tabuleiro() {
        casas = new Peca[8][8];
    }

    public void inicializar() {
        for (int linha = 5; linha <= 7; linha++) {
            for (int col = 0; col < 8; col++) {
                if ((linha + col) % 2 == 0) {
                    casas[linha][col] = new PecaPreta();
                }
            }
        }
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

    public int contarPecas(Peca.Cor cor) {
        int count = 0;
        for (int l = 0; l < 8; l++)
            for (int c = 0; c < 8; c++)
                if (casas[l][c] != null && casas[l][c].getCor() == cor) count++;
        return count;
    }

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
