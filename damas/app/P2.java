package damas.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import damas.logica.MotorJogo;
import damas.modelo.ConfiguracaoJogo;

/**
 * Programa P2 (requisito RA3).
 *
 * Restaura em memoria a ConfiguracaoJogo salva em binario pelo P1, exibe o
 * tabuleiro em interface grafica (Swing) para a interacao com os jogadores e
 * grava os resultados parciais e finais em arquivo texto.
 *
 * Uso:
 *   java damas.app.P2 [arquivoBinario.dat] [arquivoResultados.txt]
 * Padroes:
 *   binario     = dados/configuracao.dat
 *   resultados  = dados/resultados.txt
 */
public class P2 {

    private static final String BIN_PADRAO = "dados/configuracao.dat";
    private static final String RES_PADRAO = "dados/resultados.txt";

    public static void main(String[] args) {
        String binario    = (args.length > 0) ? args[0] : BIN_PADRAO;
        String resultados = (args.length > 1) ? args[1] : RES_PADRAO;

        // 1) Restaura os objetos persistentes do binario
        ConfiguracaoJogo config;
        try {
            config = Persistencia.carregar(binario);
        } catch (IOException e) {
            erroFatal("Nao foi possivel ler o binario '" + binario
                    + "'.\nRode o P1 primeiro.\n\nDetalhe: " + e.getMessage());
            return;
        } catch (ClassNotFoundException e) {
            erroFatal("Arquivo binario incompativel: " + e.getMessage());
            return;
        }

        // 2) Abre o arquivo de resultados
        RegistradorResultados registrador;
        try {
            registrador = new RegistradorResultados(resultados);
        } catch (IOException e) {
            erroFatal("Nao foi possivel criar o arquivo de resultados '"
                    + resultados + "'.\n\nDetalhe: " + e.getMessage());
            return;
        }

        // 3) Monta a interface grafica na thread do Swing
        final ConfiguracaoJogo cfg = config;
        final RegistradorResultados reg = registrador;
        SwingUtilities.invokeLater(() -> construirGUI(cfg, reg));
    }

    private static void construirGUI(ConfiguracaoJogo config, RegistradorResultados registrador) {
        MotorJogo motor = new MotorJogo(config);

        JFrame janela = new JFrame("Damas — "
                + config.getJogadorBranco().getNome() + " (Brancas)  x  "
                + config.getJogadorPreto().getNome() + " (Pretas)");
        janela.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JLabel status = new JLabel();
        status.setFont(new Font("SansSerif", Font.PLAIN, 14));
        status.setForeground(Color.DARK_GRAY);
        status.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        TabuleiroPanel painel = new TabuleiroPanel(motor, registrador, status);

        janela.setLayout(new BorderLayout());
        janela.add(painel, BorderLayout.CENTER);
        janela.add(status, BorderLayout.SOUTH);

        // Fecha o arquivo de resultados ao sair (garante o flush final)
        janela.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                registrador.close();
                janela.dispose();
                System.exit(0);
            }
        });

        janela.pack();
        janela.setResizable(false);
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }

    private static void erroFatal(String mensagem) {
        JOptionPane.showMessageDialog(null, mensagem, "P2 — erro",
                JOptionPane.ERROR_MESSAGE);
    }
}
