package damas.modelo;

import java.io.Serializable;

/**
 * Classe abstrata que representa uma peça do jogo de damas.
 * Requisito RA2 - definição de classe abstrata com método abstrato.
 * Implementa Serializable para persistência binária (requisito RA3).
 */
public abstract class Peca implements Movimentavel, Serializable {

    private static final long serialVersionUID = 1L;

    public enum Cor { BRANCA, PRETA }

    private Cor cor;
    private boolean eDama;

    public Peca(Cor cor) {
        this.cor = cor;
        this.eDama = false;
    }

    public Cor getCor() { return cor; }

    public boolean isEDama() { return eDama; }

    public void promoverDama() { this.eDama = true; }

    /**
     * Método abstrato — cada subclasse define sua direção de movimento preferencial.
     * Requisito RA2 - método abstrato.
     */
    public abstract int getDirecaoLinha();

    /**
     * Verifica se esta peça pertence ao jogador informado.
     * Método polimórfico — pode ser sobrescrito em subclasses.
     */
    public boolean pertenceAo(Jogador jogador) {
        return this.cor == jogador.getCor();
    }

    @Override
    public boolean podeMovimentar(int origemL, int origemC, int destinoL, int destinoC, Tabuleiro tab) {
        if (destinoL < 0 || destinoL > 7 || destinoC < 0 || destinoC > 7) return false;
        if (tab.getPeca(destinoL, destinoC) != null) return false;

        int dl = destinoL - origemL;
        int dc = Math.abs(destinoC - origemC);

        if (eDama) {
            // Dama move em qualquer diagonal   dc == 0 pois a dama se movimenta mais do que uma casa, ass: bruna
            if (Math.abs(dl) != dc || dc == 0) return false;
            // verifica se o caminho esta livre ate o destino
            int stepL = (destinoL - origemL) > 0 ? 1 : -1;
            int stepC = (destinoC - origemC) > 0 ? 1 : -1;
            int linhaAtual = origemL + stepL, colunaAtual = origemC + stepC;
            while (linhaAtual != destinoL){
                if (tab.getPeca(linhaAtual, colunaAtual) != null) return false; // caminho bloqueado
                linhaAtual += stepL;
                colunaAtual += stepC; 
            }
            return true;
        } else {
            // Peão move apenas para frente
            return dl == getDirecaoLinha() && dc == 1;
        }
    }

    @Override
    public boolean temCapturaDisponivel(int linhaAtual, int colunaAtual, Tabuleiro tab) {
        int[] direcoes = new int[]{-1, 1};
        for (int dLinha : direcoes) {
            for (int dCol : new int[]{-1, 1}) {
                if (eDama){ // movimento apenas para a dama
                    boolean encontrouAdversaria = false;
                    int l = linhaAtual + dLinha, c = colunaAtual + dCol;
                    while (l >= 00 && l <= 7 && c >= 0 && c <= 7) {
                        Peca alvo = tab.getPeca(l, c);
                        if (alvo != null) {
                            if (alvo.getCor() == this.cor) break; // peça propria bloqueia 
                            if (encontrouAdversaria) break; // segunda adversaria bloqueia
                            encontrouAdversaria = true;
                        } else if (encontrouAdversaria) {
                            return true;
                        }
                    l += dLinha;
                    c += dCol;
                    }
                } else{
                int meiLinha  = linhaAtual + dLinha;
                int meiCol    = colunaAtual + dCol;
                int destLinha = linhaAtual + 2 * dLinha;
                int destCol   = colunaAtual + 2 * dCol;
                if (destLinha < 0 || destLinha > 7 || destCol < 0 || destCol > 7) continue;
                Peca alvo = tab.getPeca(meiLinha, meiCol);
                if (alvo != null && alvo.getCor() != this.cor && tab.getPeca(destLinha, destCol) == null) {
                    return true;
                }
            }
        }
    }
        return false;
}

    @Override
    public String toString() {
        return (eDama ? "Dama" : "Peão") + " " + cor;
    }
}
