package damas.logica;

import damas.modelo.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Motor do jogo de damas — contém toda a lógica de regras.
 * Usa polimorfismo ao chamar métodos de Peca (requisito RA2).
 */
public class MotorJogo {

    private Tabuleiro tabuleiro;
    private Jogador jogadorBranco;
    private Jogador jogadorPreto;
    private Jogador jogadorAtual;
    private List<String> logMovimentos;

    // --- Estado para as regras de empate ---
    private static final int LIMITE_LANCES_DAMA = 20; // regra do empate por lances de damas
    private static final int LIMITE_LANCES_3X1  = 20; // regra do empate em 3 damas x 1 dama

    private int lancesDamaSemEvento;        // contador da regra de lances sucessivos de damas
    private Map<String, Integer> historicoPosicoes; // contador da regra de tripla repetição
    private boolean emEndgame3Damas1Dama;   // flag da regra 3 damas x 1 dama
    private int lancesEmEndgame3Damas1Dama; // contador de lances
    private String motivoEmpate;            // null enquanto não houver empate

    // --- Estado da tomada em cadeia em andamento (posição da peça; -1 se não há) ---
    private int cadeiaLinha = -1;
    private int cadeiaCol   = -1;

    public MotorJogo(ConfiguracaoJogo config) {
        this.tabuleiro     = config.getTabuleiro();
        this.jogadorBranco = config.getJogadorBranco();
        this.jogadorPreto  = config.getJogadorPreto();
        this.jogadorAtual  = jogadorBranco; // brancas começam
        this.logMovimentos = new ArrayList<>();

        this.lancesDamaSemEvento        = 0;
        this.historicoPosicoes          = new HashMap<>();
        this.emEndgame3Damas1Dama       = false;
        this.lancesEmEndgame3Damas1Dama = 0;
        this.motivoEmpate               = null;
    }

    public Tabuleiro getTabuleiro()    { return tabuleiro; }
    public Jogador getJogadorAtual()   { return jogadorAtual; }
    public Jogador getJogadorBranco()  { return jogadorBranco; }
    public Jogador getJogadorPreto()   { return jogadorPreto; }
    public List<String> getLog()       { return logMovimentos; }

    /** Indica se a partida terminou em empate por alguma das regras de empate. */
    public boolean isEmpate()          { return motivoEmpate != null; }

    /** Descrição da regra de empate que foi acionada (null se não houve empate). */
    public String getMotivoEmpate()    { return motivoEmpate; }

    /**
     * Verifica se existe captura obrigatória para o jogador atual.
     * Chamada polimórfica a temCapturaDisponivel (requisito RA2).
     */
    public boolean temCapturaObrigatoria() {
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                Peca p = tabuleiro.getPeca(l, c);
                if (p != null && p.pertenceAo(jogadorAtual)) {
                    if (p.temCapturaDisponivel(l, c, tabuleiro)) return true; // chamada polimórfica
                }
            }
        }
        return false;
    }

    /**
     * Verifica se uma peça específica pode capturar a partir de sua posição.
     */
    public boolean pecaPodeCapturar(int linha, int col) {
        Peca p = tabuleiro.getPeca(linha, col);
        if (p == null || !p.pertenceAo(jogadorAtual)) return false;
        return p.temCapturaDisponivel(linha, col, tabuleiro);
    }

    /**
     * Executa um movimento. Lança exceções se inválido.
     * Método que usa throws (requisito RA2).
     */
    public boolean realizarMovimento(int origemL, int origemC, int destinoL, int destinoC)
            throws MovimentoInvalidoException, JogadorInvalidoException {

        Peca peca = tabuleiro.getPeca(origemL, origemC);

        // Validar se há peça
        if (peca == null) {
            throw new MovimentoInvalidoException(
                "Não há peça na posição de origem.", origemL, origemC, destinoL, destinoC);
        }

        // Validar se a peça pertence ao jogador atual
        if (!peca.pertenceAo(jogadorAtual)) {
            throw new JogadorInvalidoException(
                "A peça selecionada não pertence ao jogador atual.", jogadorAtual.getNome());
        }

        // Durante uma tomada em cadeia, o lance deve continuar com a MESMA peça.
        if (cadeiaLinha != -1 && (origemL != cadeiaLinha || origemC != cadeiaCol)) {
            throw new MovimentoInvalidoException(
                "Tomada em cadeia em andamento: continue capturando com a mesma peça.",
                origemL, origemC, destinoL, destinoC);
        }

        // Captura obrigatória precisa ser checada antes de um movimento simples
        boolean haCapturaObrigatoria = temCapturaObrigatoria();
        int dl = Math.abs(destinoL - origemL);
        int dc = Math.abs(destinoC - origemC);

        if (!(dl == dc && dl > 0)) {
            throw new MovimentoInvalidoException(
                "O movimento deve ser na diagonal.", origemL, origemC, destinoL, destinoC);
        }

        // O tipo de lance é definido pela presença de peça no caminho — e não
        // pela distância. Isso é essencial para a dama, que pode andar várias
        // casas num movimento simples (sem captura), com o caminho livre.
        if (existePecaNoCaminho(origemL, origemC, destinoL, destinoC)) {
            return executarCaptura(peca, origemL, origemC, destinoL, destinoC);
        } else {
            if (haCapturaObrigatoria) {
                throw new MovimentoInvalidoException(
                    "Captura obrigatória disponível! Você deve capturar.", origemL, origemC, destinoL, destinoC);
            }
            return executarMovimentoSimples(peca, origemL, origemC, destinoL, destinoC);
        }
    }

    /**
     * Indica se há alguma peça nas casas entre origem e destino (exclusivo),
     * percorrendo a diagonal. Usado para distinguir captura de movimento simples.
     */
    private boolean existePecaNoCaminho(int origemL, int origemC, int destinoL, int destinoC) {
        int stepL = (destinoL - origemL) > 0 ? 1 : -1;
        int stepC = (destinoC - origemC) > 0 ? 1 : -1;
        int l = origemL + stepL, c = origemC + stepC;
        while (l != destinoL) {
            if (tabuleiro.getPeca(l, c) != null) return true;
            l += stepL;
            c += stepC;
        }
        return false;
    }

    /**
     * Executa movimento simples (sem captura).
     * Repassa exceções — não usa try-catch (requisito RA2).
     */
    private boolean executarMovimentoSimples(Peca peca, int origemL, int origemC, int destinoL, int destinoC)
            throws MovimentoInvalidoException {

        if (!peca.podeMovimentar(origemL, origemC, destinoL, destinoC, tabuleiro)) {
            throw new MovimentoInvalidoException(
                "Movimento diagonal inválido para esta peça.", origemL, origemC, destinoL, destinoC);
        }

        tabuleiro.setPeca(destinoL, destinoC, peca);
        tabuleiro.removerPeca(origemL, origemC);

        String mov = jogadorAtual.getNome() + " moveu " + peca + " de " +
                     posicaoStr(origemL, origemC) + " para " + posicaoStr(destinoL, destinoC);
        logMovimentos.add(mov);

        verificarPromocao(peca, destinoL);
        registrarLanceParaEmpate(peca, false);
        alternarJogador();
        return false; // sem captura em cadeia
    }

    /**
     * Executa uma captura.
     * Repassa exceções — não usa try-catch (requisito RA2).
     */
    private boolean executarCaptura(Peca peca, int origemL, int origemC, int destinoL, int destinoC)
            throws MovimentoInvalidoException {

        if (destinoL < 0 || destinoL > 7 || destinoC < 0 || destinoC > 7) {
            throw new MovimentoInvalidoException("Destino fora do tabuleiro.", origemL, origemC, destinoL, destinoC);
        }
        if (tabuleiro.getPeca(destinoL, destinoC) != null) {
            throw new MovimentoInvalidoException("Casa de destino ocupada.", origemL, origemC, destinoL, destinoC);
        }

        int stepL = (destinoL - origemL) > 0 ? 1 : -1;
        int stepC = (destinoC - origemC) > 0 ? 1 : -1;

        // Percorre TODO o caminho: deve haver exatamente uma peça (a capturada),
        // e o restante das casas — antes e depois dela — precisa estar livre.
        int meiLinha = -1, meiCol = -1;
        int pecasNoCaminho = 0;
        int l = origemL + stepL, c = origemC + stepC;
        while (l != destinoL) {
            if (tabuleiro.getPeca(l, c) != null) {
                pecasNoCaminho++;
                meiLinha = l;
                meiCol = c;
            }
            l += stepL;
            c += stepC;
        }

        if (pecasNoCaminho != 1) {
            throw new MovimentoInvalidoException(
                "Captura inválida: o caminho deve conter exatamente uma peça adversária e estar livre antes e depois dela.",
                origemL, origemC, destinoL, destinoC);
        }

        Peca capturada = tabuleiro.getPeca(meiLinha, meiCol);
        if (capturada.getCor() == peca.getCor()) {
            throw new MovimentoInvalidoException(
                "Não há peça adversária para capturar.", origemL, origemC, destinoL, destinoC);
        }

        // Peça comum captura saltando exatamente uma casa (origem e destino a 2 de distância).
        if (!peca.isEDama()) {
            int dist = Math.abs(destinoL - origemL);
            if (dist != 2) {
                throw new MovimentoInvalidoException(
                    "Peça comum deve capturar pulando exatamente uma casa.", origemL, origemC, destinoL, destinoC);
            }
        }

        // --- Regra da captura de maior número ---
        // O lance escolhido deve fazer parte de uma sequência que captura o
        // máximo possível. No primeiro salto, a peça escolhida tem de alcançar
        // o máximo do jogador; em qualquer salto, o destino escolhido tem de
        // manter o máximo alcançável por aquela peça.
        boolean meioDeCadeia = (cadeiaLinha == origemL && cadeiaCol == origemC);
        if (!meioDeCadeia) {
            int maxJogador = maxCapturasJogador(jogadorAtual);
            if (maxCapturasDaPeca(origemL, origemC, peca) < maxJogador) {
                throw new MovimentoInvalidoException(
                    "Há uma captura maior disponível com outra peça — a captura de maior número é obrigatória.",
                    origemL, origemC, destinoL, destinoC);
            }
        }
        int melhorDaPeca = maxCapturasDaPeca(origemL, origemC, peca);
        if (1 + capturasAposSalto(peca, origemL, origemC, destinoL, destinoC, meiLinha, meiCol) < melhorDaPeca) {
            throw new MovimentoInvalidoException(
                "Existe uma captura que come mais peças a partir desta peça — a captura de maior número é obrigatória.",
                origemL, origemC, destinoL, destinoC);
        }

        tabuleiro.removerPeca(meiLinha, meiCol);
        tabuleiro.setPeca(destinoL, destinoC, peca);
        tabuleiro.removerPeca(origemL, origemC);

        // Atualizar contadores
        jogadorAtual.incrementarCaptura();
        Jogador adversario = (jogadorAtual == jogadorBranco) ? jogadorPreto : jogadorBranco;
        adversario.decrementarPecas();

        String mov = jogadorAtual.getNome() + " capturou com " + peca + " de " +
                     posicaoStr(origemL, origemC) + " para " + posicaoStr(destinoL, destinoC) +
                     " (capturou " + capturada + " em " + posicaoStr(meiLinha, meiCol) + ")";
        logMovimentos.add(mov);

        // Captura em cadeia: a mesma peça deve continuar. A promoção só é
        // verificada quando a peça PARA (fim da cadeia) — uma peça comum não
        // coroa se apenas passa pela última linha no meio de uma tomada em cadeia.
        if (peca.temCapturaDisponivel(destinoL, destinoC, tabuleiro)) {
            cadeiaLinha = destinoL;
            cadeiaCol   = destinoC;
            return true; // permite novo salto da mesma peça
        }

        cadeiaLinha = -1;
        cadeiaCol   = -1;
        verificarPromocao(peca, destinoL);
        registrarLanceParaEmpate(peca, true);
        alternarJogador();
        return false;
    }

    // ------------------------------------------------------------------
    // Apoio à regra da captura de maior número (busca recursiva de saltos)
    // ------------------------------------------------------------------

    /** Um salto de captura: para onde a peça vai e qual peça é capturada. */
    private static final class Salto {
        final int destL, destC, midL, midC;
        Salto(int destL, int destC, int midL, int midC) {
            this.destL = destL; this.destC = destC; this.midL = midL; this.midC = midC;
        }
    }

    private boolean dentro(int linha, int col) {
        return linha >= 0 && linha <= 7 && col >= 0 && col <= 7;
    }

    /** Lista os saltos de captura imediatos de uma peça na posição dada. */
    private List<Salto> saltosImediatos(int linha, int col, Peca peca) {
        List<Salto> saltos = new ArrayList<>();
        int[] dirs = {-1, 1};
        for (int dL : dirs) {
            for (int dC : dirs) {
                if (peca.isEDama()) {
                    // anda até a primeira peça do caminho
                    int l = linha + dL, c = col + dC;
                    while (dentro(l, c) && tabuleiro.getPeca(l, c) == null) { l += dL; c += dC; }
                    if (!dentro(l, c)) continue;
                    if (tabuleiro.getPeca(l, c).getCor() == peca.getCor()) continue; // própria bloqueia
                    // cada casa livre após a adversária é um destino possível
                    int dl = l + dL, dc = c + dC;
                    while (dentro(dl, dc) && tabuleiro.getPeca(dl, dc) == null) {
                        saltos.add(new Salto(dl, dc, l, c));
                        dl += dL; dc += dC;
                    }
                } else {
                    int ml = linha + dL, mc = col + dC;
                    int destL = linha + 2 * dL, destC = col + 2 * dC;
                    if (!dentro(destL, destC)) continue;
                    Peca alvo = tabuleiro.getPeca(ml, mc);
                    if (alvo != null && alvo.getCor() != peca.getCor()
                            && tabuleiro.getPeca(destL, destC) == null) {
                        saltos.add(new Salto(destL, destC, ml, mc));
                    }
                }
            }
        }
        return saltos;
    }

    /** Máximo de capturas alcançável a partir de uma peça, simulando recursivamente. */
    private int maxCapturasDaPeca(int linha, int col, Peca peca) {
        List<Salto> saltos = saltosImediatos(linha, col, peca);
        int melhor = 0;
        for (Salto s : saltos) {
            int total = 1 + capturasAposSalto(peca, linha, col, s.destL, s.destC, s.midL, s.midC);
            if (total > melhor) melhor = total;
        }
        return melhor;
    }

    /** Simula um salto (sem coroar) e devolve o máximo de capturas seguintes daquela peça. */
    private int capturasAposSalto(Peca peca, int origemL, int origemC,
                                  int destinoL, int destinoC, int midL, int midC) {
        Peca capturada = tabuleiro.getPeca(midL, midC);
        tabuleiro.removerPeca(midL, midC);
        tabuleiro.removerPeca(origemL, origemC);
        tabuleiro.setPeca(destinoL, destinoC, peca);

        int resultado = maxCapturasDaPeca(destinoL, destinoC, peca);

        // desfaz a simulação
        tabuleiro.removerPeca(destinoL, destinoC);
        tabuleiro.setPeca(origemL, origemC, peca);
        tabuleiro.setPeca(midL, midC, capturada);
        return resultado;
    }

    /** Máximo de capturas que o jogador consegue neste lance, somando todas as suas peças. */
    private int maxCapturasJogador(Jogador jogador) {
        int melhor = 0;
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                Peca p = tabuleiro.getPeca(l, c);
                if (p != null && p.pertenceAo(jogador)) {
                    int m = maxCapturasDaPeca(l, c, p);
                    if (m > melhor) melhor = m;
                }
            }
        }
        return melhor;
    }

    /**
     * Posições das peças que o jogador atual é OBRIGADO a usar neste lance
     * (as que alcançam a captura de maior número). Vazio se não há captura.
     * Usado pela interface gráfica para destacar as peças corretas.
     */
    public List<int[]> pecasObrigadasACapturar() {
        List<int[]> obrig = new ArrayList<>();
        if (cadeiaLinha != -1) {                 // em cadeia: só a peça em curso
            obrig.add(new int[]{cadeiaLinha, cadeiaCol});
            return obrig;
        }
        int maxJogador = maxCapturasJogador(jogadorAtual);
        if (maxJogador == 0) return obrig;
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                Peca p = tabuleiro.getPeca(l, c);
                if (p != null && p.pertenceAo(jogadorAtual)
                        && maxCapturasDaPeca(l, c, p) == maxJogador) {
                    obrig.add(new int[]{l, c});
                }
            }
        }
        return obrig;
    }

    private void verificarPromocao(Peca peca, int linha) {
        if (!peca.isEDama()) {
            if ((peca.getCor() == Peca.Cor.BRANCA && linha == 7) ||
                (peca.getCor() == Peca.Cor.PRETA  && linha == 0)) {
                peca.promoverDama();
                logMovimentos.add(">>> " + peca.getCor() + " promovida a Dama!");
            }
        }
    }

    private void alternarJogador() {
        jogadorAtual = (jogadorAtual == jogadorBranco) ? jogadorPreto : jogadorBranco;
    }

    /**
     * Chamado uma única vez ao final de cada lance completo (incluindo o fim
     * de uma tomada em cadeia) para atualizar as três regras de empate.
     */
    private void registrarLanceParaEmpate(Peca peca, boolean houveCaptura) {
        atualizarRegraLancesDeDamas(peca, houveCaptura);
        atualizarRegraTriplaRepeticao();
        atualizarRegraTresDamasContraUma();
    }

    /**
     * Regra 1: 20 lances sucessivos de damas, sem captura ou movimento de
     * peças comuns, resultam em empate. Qualquer captura ou movimento de
     * peça comum zera o contador.
     */
    private void atualizarRegraLancesDeDamas(Peca peca, boolean houveCaptura) {
        if (houveCaptura || !peca.isEDama()) {
            lancesDamaSemEvento = 0;
        } else {
            lancesDamaSemEvento++;
            if (lancesDamaSemEvento >= LIMITE_LANCES_DAMA && motivoEmpate == null) {
                motivoEmpate = LIMITE_LANCES_DAMA + " lances sucessivos de damas sem captura ou movimento de peças comuns.";
            }
        }
    }

    /**
     * Regra 2: se a mesma posição (tabuleiro + jogador da vez) se repetir
     * pela terceira vez, a partida é considerada empatada.
     */
    private void atualizarRegraTriplaRepeticao() {
        Jogador proximoJogador = (jogadorAtual == jogadorBranco) ? jogadorPreto : jogadorBranco;
        String assinatura = gerarAssinaturaPosicao(proximoJogador);

        int ocorrencias = historicoPosicoes.merge(assinatura, 1, Integer::sum);
        if (ocorrencias >= 3 && motivoEmpate == null) {
            motivoEmpate = "A mesma posição se repetiu pela terceira vez.";
        }
    }

    /**
     * Regra 3: se um jogador tiver três damas contra uma dama do adversário
     * (sem nenhuma peça comum em jogo) e não conseguir vencer em 20 lances
     * a partir do início desse cenário, a partida é empatada.
     */
    private void atualizarRegraTresDamasContraUma() {
        int qtdBranco = tabuleiro.contarPecas(Peca.Cor.BRANCA);
        int qtdPreto  = tabuleiro.contarPecas(Peca.Cor.PRETA);

        boolean cenario3x1 =
            (qtdBranco == 3 && qtdPreto == 1 && todasDamas(Peca.Cor.BRANCA) && todasDamas(Peca.Cor.PRETA)) ||
            (qtdPreto == 3 && qtdBranco == 1 && todasDamas(Peca.Cor.PRETA) && todasDamas(Peca.Cor.BRANCA));

        if (!cenario3x1) {
            emEndgame3Damas1Dama = false;
            lancesEmEndgame3Damas1Dama = 0;
            return;
        }

        if (!emEndgame3Damas1Dama) {
            // Primeiro lance em que o cenário 3 damas x 1 dama aparece.
            emEndgame3Damas1Dama = true;
            lancesEmEndgame3Damas1Dama = 0;
        } else {
            lancesEmEndgame3Damas1Dama++;
            if (lancesEmEndgame3Damas1Dama >= LIMITE_LANCES_3X1 && motivoEmpate == null) {
                motivoEmpate = "Jogador com 3 damas não venceu o adversário com 1 dama em "
                             + LIMITE_LANCES_3X1 + " lances.";
            }
        }
    }

    /** Verifica se todas as peças de uma cor no tabuleiro já são damas. */
    private boolean todasDamas(Peca.Cor cor) {
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                Peca p = tabuleiro.getPeca(l, c);
                if (p != null && p.getCor() == cor && !p.isEDama()) return false;
            }
        }
        return true;
    }

    /**
     * Gera uma assinatura textual da posição atual do tabuleiro, incluindo
     * de quem é a vez de jogar (necessário para a regra da tripla repetição,
     * já que a mesma disposição de peças com jogadores diferentes na vez
     * não é considerada a mesma posição).
     */
    private String gerarAssinaturaPosicao(Jogador proximoJogador) {
        StringBuilder sb = new StringBuilder();
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                Peca p = tabuleiro.getPeca(l, c);
                if (p == null) {
                    sb.append('-');
                } else {
                    char ch = (p.getCor() == Peca.Cor.BRANCA) ? 'b' : 'p';
                    if (p.isEDama()) ch = Character.toUpperCase(ch);
                    sb.append(ch);
                }
            }
        }
        sb.append('|').append(proximoJogador.getCor());
        return sb.toString();
    }

    public Jogador verificarVencedor() {
        if (jogadorPreto.perdeu()  || !temMovimentoValido(jogadorPreto))  return jogadorBranco;
        if (jogadorBranco.perdeu() || !temMovimentoValido(jogadorBranco)) return jogadorPreto;
        return null;
    }

    private boolean temMovimentoValido(Jogador jogador) {
        for (int l = 0; l < 8; l++) {
            for (int c = 0; c < 8; c++) {
                Peca p = tabuleiro.getPeca(l, c);
                if (p != null && p.pertenceAo(jogador)) {
                    // Verificar movimentos simples
                    for (int dl : new int[]{-1, 1}) {
                        for (int dc : new int[]{-1, 1}) {
                            if (p.podeMovimentar(l, c, l + dl, c + dc, tabuleiro)) return true;
                        }
                    }
                    // Verificar capturas
                    if (p.temCapturaDisponivel(l, c, tabuleiro)) return true;
                }
            }
        }
        return false;
    }

    private String posicaoStr(int linha, int col) {
        return "" + (char)('A' + col) + (linha + 1);
    }
}