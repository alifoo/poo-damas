package damas.app;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import damas.modelo.Jogador;

/**
 * Grava os resultados parciais (cada lance) e o resultado final da partida
 * em um arquivo texto (requisito RA3 — P2 escreve resultados em csv/txt).
 *
 * E reutilizado pelo P2: a cada movimento o chamador passa o log atual do
 * MotorJogo e somente os eventos ainda nao escritos sao gravados.
 */
public class RegistradorResultados implements Closeable {

    private final PrintWriter escritor;
    private int eventosEscritos;

    public RegistradorResultados(String caminho) throws IOException {
        this.escritor = new PrintWriter(new FileWriter(caminho));
        this.eventosEscritos = 0;
        escritor.println("=== Resultados da partida de Damas ===");
        escritor.flush();
    }

    /**
     * Resultados parciais: grava os eventos do log que ainda nao foram escritos.
     * Recebe o log completo do MotorJogo e escreve apenas o que e novo.
     */
    public void registrarParciais(List<String> logCompleto) {
        for (int i = eventosEscritos; i < logCompleto.size(); i++) {
            escritor.println((i + 1) + ". " + logCompleto.get(i));
        }
        eventosEscritos = logCompleto.size();
        escritor.flush();
    }

    /** Resultado final: vitoria de um jogador. */
    public void registrarVitoria(Jogador vencedor) {
        escritor.println("--- Fim de jogo ---");
        escritor.println("RESULTADO FINAL: vitoria de " + vencedor.getNome()
                + " (" + vencedor.getCor() + ").");
        escritor.flush();
    }

    /** Resultado final: empate por uma das regras de empate. */
    public void registrarEmpate(String motivo) {
        escritor.println("--- Fim de jogo ---");
        escritor.println("RESULTADO FINAL: empate. Motivo: " + motivo);
        escritor.flush();
    }

    @Override
    public void close() {
        escritor.close();
    }
}
