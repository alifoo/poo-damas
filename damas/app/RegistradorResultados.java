package damas.app;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import damas.modelo.Jogador;

public class RegistradorResultados implements Closeable {

    private final PrintWriter escritor;
    private int eventosEscritos;

    public RegistradorResultados(String caminho) throws IOException {
        this.escritor = new PrintWriter(new FileWriter(caminho));
        this.eventosEscritos = 0;
        escritor.println("=== Resultados da partida de Damas ===");
        escritor.flush();
    }

    public void registrarParciais(List<String> logCompleto) {
        for (int i = eventosEscritos; i < logCompleto.size(); i++) {
            escritor.println((i + 1) + ". " + logCompleto.get(i));
        }
        eventosEscritos = logCompleto.size();
        escritor.flush();
    }

    public void registrarVitoria(Jogador vencedor) {
        escritor.println("--- Fim de jogo ---");
        escritor.println("RESULTADO FINAL: vitoria de " + vencedor.getNome()
                + " (" + vencedor.getCor() + ").");
        escritor.flush();
    }

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
