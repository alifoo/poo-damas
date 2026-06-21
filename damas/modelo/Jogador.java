package damas.modelo;

import java.io.Serializable;

public class Jogador implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nome;
    private Peca.Cor cor;
    private int pecasCapturadas;
    private int pecasRestantes;

    public Jogador(String nome, Peca.Cor cor) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do jogador não pode ser vazio.");
        }
        this.nome = nome.trim();
        this.cor = cor;
        this.pecasCapturadas = 0;
        this.pecasRestantes = 12;
    }

    public String getNome()             { return nome; }
    public Peca.Cor getCor()            { return cor; }
    public int getPecasCapturadas()     { return pecasCapturadas; }
    public int getPecasRestantes()      { return pecasRestantes; }

    public void incrementarCaptura()    { pecasCapturadas++; }
    public void decrementarPecas()      { if (pecasRestantes > 0) pecasRestantes--; }

    public boolean perdeu() { return pecasRestantes == 0; }

    @Override
    public String toString() {
        return nome + " (" + cor + ") | Peças: " + pecasRestantes + " | Capturas: " + pecasCapturadas;
    }
}
