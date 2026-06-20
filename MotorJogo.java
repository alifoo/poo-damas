package pacote1;

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

        // Verificar captura obrigatória
        boolean haCapturaObrigatoria = temCapturaObrigatoria();
        int dl = Math.abs(destinoL - origemL);
        int dc = Math.abs(destinoC - origemC);

        if (dl == dc && dl > 0) {
            if (dl == 1) {
                // movimento simples
                if (haCapturaObrigatoria) {
                    throw new MovimentoInvalidoException(
                        "Captura obrigatória disponível! Você deve capturar.", origemL, origemC, destinoL, destinoC);
                }
                return executarMovimentoSimples(peca, origemL, origemC, destinoL, destinoC);
            } else {
                return executarCaptura(peca, origemL, origemC, destinoL, destinoC);
            }
        } else {
            throw new MovimentoInvalidoException(
                "Distância de movimento inválida.", origemL, origemC, destinoL, destinoC);
        }
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
        int meiLinha = -1, meiCol = -1;
        int l = origemL + stepL, c = origemC + stepC;
        while (l != destinoL) {
            if (tabuleiro.getPeca(l, c) != null) {
                meiLinha = l;
                meiCol = c;
                break;
            }
            l += stepL;
            c += stepC;
        }
        Peca capturada = (meiLinha != -1) ? tabuleiro.getPeca(meiLinha, meiCol) : null;

        if (capturada == null || capturada.getCor() == peca.getCor()) {
            throw new MovimentoInvalidoException(
                "Não há peça adversária para capturar.", origemL, origemC, destinoL, destinoC);
        }

       
        // Verificar se a distância do pulo é válida para o peão (deve pular exatamente 1 casa / andar 2 posições)
        if (!peca.isEDama()) {
            int dl = Math.abs(destinoL - origemL);
            if (dl != 2) {
                throw new MovimentoInvalidoException(
                    "Movimento de captura do peão deve pular exatamente uma casa.", origemL, origemC, destinoL, destinoC);
            }
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

        verificarPromocao(peca, destinoL);

        // Verificar captura em cadeia
        if (peca.temCapturaDisponivel(destinoL, destinoC, tabuleiro)) {
            return true; // permite novo movimento da mesma peça
        }

        // A tomada em cadeia (se houve) terminou aqui — agora sim o lance se encerra.
        registrarLanceParaEmpate(peca, true);
        alternarJogador();
        return false;
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