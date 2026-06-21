package damas.app;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import damas.logica.MotorJogo;
import damas.modelo.Jogador;
import damas.modelo.JogadorInvalidoException;
import damas.modelo.MovimentoInvalidoException;
import damas.modelo.Peca;
import damas.modelo.Tabuleiro;

/**
 * Painel Swing que desenha o tabuleiro 8x8 e trata a interacao com o usuario
 * (requisito RA3 — interface grafica do P2).
 *
 * Fluxo: o jogador clica na peca de origem e depois na casa de destino. O painel
 * delega a regra ao MotorJogo, exibe erros (excecoes) na barra de status, trata
 * captura em cadeia e grava cada lance via RegistradorResultados.
 */
public class TabuleiroPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int CELULA       = 70;
    private static final int LADO         = 8 * CELULA;        // 560
    private static final int MARGEM_ESQ   = 28;                // numeros das linhas
    private static final int MARGEM_BAIXO = 26;                // letras das colunas

    private static final Color COR_CLARA     = new Color(238, 238, 210);
    private static final Color COR_ESCURA    = new Color(118, 150, 86);
    private static final Color COR_SELECAO   = new Color(246, 246, 105);
    private static final Color COR_CAPTURA   = new Color(214, 90, 70);
    private static final Color BRANCA_FILL   = new Color(245, 245, 245);
    private static final Color PRETA_FILL    = new Color(45, 45, 45);
    private static final Color OURO          = new Color(212, 175, 55);

    private final MotorJogo motor;
    private final RegistradorResultados registrador;
    private final JLabel status;
    private final Tabuleiro tabuleiro;

    private int selLinha = -1;
    private int selCol = -1;
    private boolean capturaEmCadeia = false;
    private boolean jogoEncerrado = false;

    public TabuleiroPanel(MotorJogo motor, RegistradorResultados registrador, JLabel status) {
        this.motor = motor;
        this.registrador = registrador;
        this.status = status;
        this.tabuleiro = motor.getTabuleiro();

        setPreferredSize(new Dimension(MARGEM_ESQ + LADO, LADO + MARGEM_BAIXO));
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { tratarClique(e.getX(), e.getY()); }
        });
        atualizarStatus("Selecione uma peca para mover.");
    }

    // ----------------------------------------------------------------- interacao

    private void tratarClique(int mx, int my) {
        if (jogoEncerrado) return;

        // converte pixel -> casa do tabuleiro (linha 0 embaixo, 7 em cima)
        if (mx < MARGEM_ESQ || my >= LADO) return;
        int col = (mx - MARGEM_ESQ) / CELULA;
        int linha = 7 - (my / CELULA);
        if (col < 0 || col > 7 || linha < 0 || linha > 7) return;

        if (selLinha < 0) {
            selecionar(linha, col);
            return;
        }

        // Permite trocar de peca (exceto durante captura em cadeia)
        if (!capturaEmCadeia) {
            Peca p = tabuleiro.getPeca(linha, col);
            if (p != null && p.pertenceAo(motor.getJogadorAtual())
                    && !(linha == selLinha && col == selCol)) {
                selecionar(linha, col);
                return;
            }
        }

        tentarMover(linha, col);
    }

    private void selecionar(int linha, int col) {
        Peca p = tabuleiro.getPeca(linha, col);
        if (p == null || !p.pertenceAo(motor.getJogadorAtual())) {
            atualizarStatus("Selecione uma peca sua (" + motor.getJogadorAtual().getCor() + ").");
            return;
        }
        selLinha = linha;
        selCol = col;
        atualizarStatus("Peca selecionada em " + posicao(linha, col) + ". Clique no destino.");
        repaint();
    }

    private void tentarMover(int destinoL, int destinoC) {
        try {
            boolean continua = motor.realizarMovimento(selLinha, selCol, destinoL, destinoC);
            registrador.registrarParciais(motor.getLog());

            if (continua) {
                // captura em cadeia: a mesma peca deve continuar capturando
                selLinha = destinoL;
                selCol = destinoC;
                capturaEmCadeia = true;
                atualizarStatus("Captura multipla! Continue capturando com a mesma peca.");
            } else {
                selLinha = -1;
                selCol = -1;
                capturaEmCadeia = false;
                verificarFimDeJogo();
                if (!jogoEncerrado) atualizarStatus("Selecione uma peca para mover.");
            }
        } catch (MovimentoInvalidoException | JogadorInvalidoException e) {
            mostrarErro(e.getMessage());
        }
        repaint();
    }

    private void verificarFimDeJogo() {
        Jogador vencedor = motor.verificarVencedor();
        if (vencedor != null) {
            registrador.registrarVitoria(vencedor);
            encerrar("Fim de jogo!\nVitoria de " + vencedor.getNome()
                    + " (" + vencedor.getCor() + ").");
        } else if (motor.isEmpate()) {
            registrador.registrarEmpate(motor.getMotivoEmpate());
            encerrar("Fim de jogo!\nEmpate.\nMotivo: " + motor.getMotivoEmpate());
        }
    }

    private void encerrar(String mensagem) {
        jogoEncerrado = true;
        atualizarStatus(mensagem.replace('\n', ' '));
        repaint();
        JOptionPane.showMessageDialog(this, mensagem, "Fim de jogo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ----------------------------------------------------------------- status

    private void atualizarStatus(String extra) {
        status.setForeground(Color.DARK_GRAY);
        Jogador b = motor.getJogadorBranco();
        Jogador p = motor.getJogadorPreto();
        StringBuilder sb = new StringBuilder();
        if (!jogoEncerrado) {
            sb.append("Vez: ").append(motor.getJogadorAtual().getNome())
              .append(" (").append(motor.getJogadorAtual().getCor()).append(")");
        }
        sb.append("   |   Capturas — ").append(b.getNome()).append(": ")
          .append(b.getPecasCapturadas()).append(", ").append(p.getNome())
          .append(": ").append(p.getPecasCapturadas());
        if (!jogoEncerrado && motor.temCapturaObrigatoria()) {
            sb.append("   |   CAPTURA OBRIGATORIA");
        }
        if (extra != null && !extra.isEmpty()) {
            sb.append("   —   ").append(extra);
        }
        status.setText(sb.toString());
    }

    private void mostrarErro(String msg) {
        status.setForeground(new Color(170, 0, 0));
        status.setText("Movimento invalido: " + msg);
    }

    // ----------------------------------------------------------------- desenho

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Peças que o jogador é obrigado a usar (regra da captura de maior número)
        boolean[][] obrigatoria = new boolean[8][8];
        if (!jogoEncerrado) {
            for (int[] pos : motor.pecasObrigadasACapturar()) {
                obrigatoria[pos[0]][pos[1]] = true;
            }
        }

        for (int linha = 0; linha <= 7; linha++) {
            for (int col = 0; col <= 7; col++) {
                int x = MARGEM_ESQ + col * CELULA;
                int y = (7 - linha) * CELULA;

                boolean escura = (linha + col) % 2 == 0;
                g2.setColor(escura ? COR_ESCURA : COR_CLARA);
                g2.fillRect(x, y, CELULA, CELULA);

                // destaque da peca selecionada
                if (linha == selLinha && col == selCol) {
                    g2.setColor(COR_SELECAO);
                    g2.fillRect(x, y, CELULA, CELULA);
                }

                // realce das pecas obrigadas a capturar
                if (obrigatoria[linha][col]) {
                    g2.setColor(COR_CAPTURA);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRect(x + 2, y + 2, CELULA - 4, CELULA - 4);
                }

                Peca peca = tabuleiro.getPeca(linha, col);
                if (peca != null) {
                    desenharPeca(g2, x, y, peca);
                }
            }
        }
        desenharRotulos(g2);
    }

    private void desenharPeca(Graphics2D g2, int x, int y, Peca peca) {
        int cx = x + CELULA / 2;
        int cy = y + CELULA / 2;
        int r = (int) (CELULA * 0.34);

        g2.setColor(peca.getCor() == Peca.Cor.BRANCA ? BRANCA_FILL : PRETA_FILL);
        g2.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        g2.setColor(peca.getCor() == Peca.Cor.BRANCA ? Color.GRAY : Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(cx - r, cy - r, 2 * r, 2 * r);

        if (peca.isEDama()) {
            g2.setColor(OURO);
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(cx - r / 2, cy - r / 2, r, r);
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
            g2.drawString("D", cx - 6, cy + 6);
        }
    }

    private void desenharRotulos(Graphics2D g2) {
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        // numeros das linhas (1 embaixo .. 8 em cima)
        for (int linha = 0; linha <= 7; linha++) {
            int y = (7 - linha) * CELULA + CELULA / 2 + 5;
            g2.drawString(String.valueOf(linha + 1), 9, y);
        }
        // letras das colunas (A..H)
        for (int col = 0; col <= 7; col++) {
            int x = MARGEM_ESQ + col * CELULA + CELULA / 2 - 4;
            g2.drawString(String.valueOf((char) ('A' + col)), x, LADO + 18);
        }
    }

    private String posicao(int linha, int col) {
        return "" + (char) ('A' + col) + (linha + 1);
    }
}
