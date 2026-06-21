package damas.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import damas.modelo.ConfiguracaoJogo;

/**
 * Centraliza a persistencia binaria da ConfiguracaoJogo (requisito RA3).
 * Usada pelo P1 (para salvar) e pelo P2 (para restaurar).
 */
public final class Persistencia {

    private Persistencia() { } // classe utilitaria: nao deve ser instanciada

    /**
     * Salva a configuracao em formato binario.
     * Metodo que REPASSA excecoes (throws, sem try-catch) — requisito RA2.
     */
    public static void salvar(ConfiguracaoJogo config, String caminho) throws IOException {
        ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(caminho));
        try {
            saida.writeObject(config);
        } finally {
            saida.close();
        }
    }

    /**
     * Restaura em memoria a configuracao salva em formato binario.
     * Metodo que REPASSA excecoes (throws, sem try-catch) — requisito RA2.
     */
    public static ConfiguracaoJogo carregar(String caminho)
            throws IOException, ClassNotFoundException {
        ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(caminho));
        try {
            return (ConfiguracaoJogo) entrada.readObject();
        } finally {
            entrada.close();
        }
    }
}
