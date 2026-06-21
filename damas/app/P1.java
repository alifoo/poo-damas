package damas.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import damas.modelo.ConfiguracaoJogo;
import damas.modelo.Jogador;
import damas.modelo.JogadorInvalidoException;
import damas.modelo.Peca;

/**
 * Programa P1 (requisito RA3).
 *
 * Le os dados iniciais de um arquivo texto (csv), cria o conjunto inicial de
 * objetos persistentes (ConfiguracaoJogo, que ja monta o Tabuleiro) e os salva
 * em formato binario para que o P2 possa restaura-los depois.
 *
 * Uso:
 *   java damas.app.P1 [arquivoEntrada.csv] [arquivoSaida.dat]
 * Padroes:
 *   entrada = dados/jogadores.csv
 *   saida   = dados/configuracao.dat
 */
public class P1 {

    private static final String ENTRADA_PADRAO = "dados/jogadores.csv";
    private static final String SAIDA_PADRAO   = "dados/configuracao.dat";

    public static void main(String[] args) {
        String entrada = (args.length > 0) ? args[0] : ENTRADA_PADRAO;
        String saida   = (args.length > 1) ? args[1] : SAIDA_PADRAO;

        try {
            ConfiguracaoJogo config = lerConfiguracao(entrada);
            Persistencia.salvar(config, saida);

            System.out.println("P1: dados lidos de '" + entrada + "'.");
            System.out.println("P1: configuracao inicial salva em binario em '" + saida + "'.");
            System.out.println(config);
        } catch (IOException e) {
            System.err.println("P1: erro de entrada/saida: " + e.getMessage());
        } catch (JogadorInvalidoException e) {
            System.err.println("P1: dados de jogador invalidos: " + e.getMessage());
        }
    }

    /**
     * Le o arquivo csv e constroi a ConfiguracaoJogo.
     * Metodo que REPASSA excecoes (clausula throws, sem try-catch) — requisito RA2.
     */
    private static ConfiguracaoJogo lerConfiguracao(String caminho)
            throws IOException, JogadorInvalidoException {

        List<Jogador> jogadores = new ArrayList<>();

        BufferedReader leitor = new BufferedReader(new FileReader(caminho));
        try {
            String linha;
            int numeroLinha = 0;
            while ((linha = leitor.readLine()) != null) {
                numeroLinha++;
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) {
                    continue; // ignora linhas vazias e comentarios
                }
                jogadores.add(parsearJogador(linha, numeroLinha));
            }
        } finally {
            leitor.close();
        }

        validar(jogadores);

        Jogador branco = jogadores.get(0).getCor() == Peca.Cor.BRANCA
                ? jogadores.get(0) : jogadores.get(1);
        Jogador preto = jogadores.get(0).getCor() == Peca.Cor.PRETA
                ? jogadores.get(0) : jogadores.get(1);

        return new ConfiguracaoJogo(branco, preto);
    }

    /**
     * Converte uma linha "nome,cor" em um Jogador.
     * Lanca JogadorInvalidoException (throw) — requisito RA2.
     */
    private static Jogador parsearJogador(String linha, int numeroLinha)
            throws JogadorInvalidoException {

        String[] campos = linha.split(",");
        if (campos.length != 2) {
            throw new JogadorInvalidoException(
                "Linha " + numeroLinha + " mal formatada (esperado 'nome,cor'): " + linha);
        }

        String nome   = campos[0].trim();
        String corStr = campos[1].trim().toUpperCase();

        if (nome.isEmpty()) {
            throw new JogadorInvalidoException("Nome vazio na linha " + numeroLinha + ".");
        }

        Peca.Cor cor;
        if (corStr.equals("BRANCA")) {
            cor = Peca.Cor.BRANCA;
        } else if (corStr.equals("PRETA")) {
            cor = Peca.Cor.PRETA;
        } else {
            throw new JogadorInvalidoException(
                "Cor invalida '" + campos[1].trim() + "' (use BRANCA ou PRETA).", nome);
        }

        return new Jogador(nome, cor);
    }

    /**
     * Garante que ha exatamente dois jogadores, um de cada cor.
     * Lanca JogadorInvalidoException (throw) — requisito RA2.
     */
    private static void validar(List<Jogador> jogadores) throws JogadorInvalidoException {
        if (jogadores.size() != 2) {
            throw new JogadorInvalidoException(
                "Sao necessarios exatamente 2 jogadores; encontrados: " + jogadores.size() + ".");
        }
        if (jogadores.get(0).getCor() == jogadores.get(1).getCor()) {
            throw new JogadorInvalidoException(
                "Os dois jogadores devem ter cores diferentes (uma BRANCA e uma PRETA).");
        }
    }
}
