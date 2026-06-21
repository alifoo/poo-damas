package damas.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import damas.modelo.ConfiguracaoJogo;

public final class Persistencia {

    private Persistencia() { }

    public static void salvar(ConfiguracaoJogo config, String caminho) throws IOException {
        ObjectOutputStream saida = new ObjectOutputStream(new FileOutputStream(caminho));
        try {
            saida.writeObject(config);
        } finally {
            saida.close();
        }
    }

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
