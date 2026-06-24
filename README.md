# Jogo de Damas — Trabalho RA2 / RA3 (POO)

Versão com comentários.

Aplicação orientada a objetos do Jogo de Damas, composta por dois programas:

- **P1** — lê os dados iniciais de um arquivo texto (csv), cria os objetos
  persistentes (`ConfiguracaoJogo`) e os salva em **formato binário**.
- **P2** — restaura os objetos do binário, interage com o usuário por
  **interface gráfica** (Swing) e grava os resultados parciais e finais em texto.

## Estrutura de pacotes

```
damas/
  modelo/   <- dominio: Peca, PecaBranca, PecaPreta, Tabuleiro, Jogador,
               ConfiguracaoJogo, Movimentavel, MovimentoInvalidoException,
               JogadorInvalidoException
  logica/   <- MotorJogo (regras do jogo)
  app/      <- P1, P2, TabuleiroPanel, Persistencia, RegistradorResultados
dados/
  jogadores.csv       <- dados iniciais (entrada do P1)
  configuracao.dat    <- binario gerado pelo P1 (entrada do P2)
  resultados.txt      <- resultados parciais/finais (saida do P2)
```

## Pré-requisitos

- JDK 17 ou superior (testado com OpenJDK 25). Verifique com:

```bash
javac -version
```

## 1. Compilar

A partir da raiz do projeto (a pasta que contém `damas/` e `dados/`):

```bash
javac -d out damas/modelo/*.java damas/logica/*.java damas/app/*.java
```

Os `.class` são gerados em `out/` (mantendo a estrutura de pacotes).

## 2. Rodar o P1 (cria o binário a partir do csv)

Usando os arquivos padrão (`dados/jogadores.csv` -> `dados/configuracao.dat`):

```bash
java -cp out damas.app.P1
```

Ou informando entrada e saída explicitamente:

```bash
java -cp out damas.app.P1 dados/jogadores.csv dados/configuracao.dat
```

Saída esperada: confirmação da leitura, do salvamento e a configuração criada
(Alice/BRANCA vs Bob/PRETA, 12 peças cada).

### Conferir o binário gerado

```bash
ls -l dados/configuracao.dat
file dados/configuracao.dat        # deve indicar "Java serialization data"
```

### Testar as validações do P1 (exceções)

Cor inválida (lança `JogadorInvalidoException`):

```bash
printf 'Alice,AZUL\nBob,PRETA\n' > /tmp/ruim.csv
java -cp out damas.app.P1 /tmp/ruim.csv /tmp/saida.dat
```

Arquivo inexistente (erro de E/S tratado):

```bash
java -cp out damas.app.P1 dados/naoexiste.csv /tmp/saida.dat
```

## 3. Rodar o P2 (restaura o binário + GUI)

Requer o `dados/configuracao.dat` gerado pelo P1 (passo 2). Com arquivos padrão
(`dados/configuracao.dat` -> `dados/resultados.txt`):

```bash
java -cp out damas.app.P2
```

Ou informando binário de entrada e arquivo de resultados:

```bash
java -cp out damas.app.P2 dados/configuracao.dat dados/resultados.txt
```

### Como jogar

- O P2 abre uma janela com o tabuleiro 8×8. As **brancas começam**.
- Clique na **peça de origem** (fica destacada) e depois na **casa de destino**.
- A barra inferior mostra de quem é a vez, o placar de capturas e avisa
  **CAPTURA OBRIGATÓRIA** quando houver — nesse caso as peças obrigadas a jogar
  (as que capturam o **maior número** de peças) ganham uma borda vermelha, e o
  motor recusa qualquer outro lance.
- Movimentos inválidos aparecem em vermelho na barra de status (sem travar o jogo).
- Em uma captura múltipla, a mesma peça permanece selecionada até encerrar a sequência.
- Ao promover, a peça vira **Dama** (anel dourado + "D").
- No fim de jogo, uma janela anuncia o vencedor (ou empate) e o resultado é
  gravado em `dados/resultados.txt`.

> Requer um ambiente com interface gráfica (não funciona em terminal headless).

## Formato do arquivo de entrada (`dados/jogadores.csv`)

```
# linhas iniciadas por # sao comentarios e sao ignoradas
# formato: nome,cor   (cor = BRANCA ou PRETA)
Alice,BRANCA
Bob,PRETA
```

Regras: exatamente 2 jogadores, um de cada cor. As brancas começam a partida.

## Limpeza

```bash
rm -rf out                          # remove os .class compilados
```

## Status do trabalho

**RA2 (conceitos de POO) — 10/10**
- 2 exceções customizadas, exceções repassadas (`throws`), 2+ pacotes,
  encapsulamento, herança, polimorfismo, classe e método abstratos,
  interface e chamada polimórfica.

**RA3 (P1/P2/persistência/GUI) — completo**
- [x] P1: csv -> objetos -> binário
- [x] P2: restauração do binário
- [x] P2: interface gráfica (Swing)
- [x] P2: gravação de resultados parciais/finais em txt

**Regras de Damas implementadas**
- Movimento da peça comum (1 casa, só para frente) e da dama (várias casas,
  frente/trás, caminho livre).
- Captura obrigatória, para frente e para trás; dama captura à distância.
- **Captura de maior número** obrigatória (escolhe a sequência que come mais).
- Tomada em cadeia obrigatória até não haver mais captura.
- Promoção a dama apenas ao **parar** na última linha (não no meio de uma cadeia).
- Empate: 20 lances de damas / tripla repetição / 3 damas × 1 dama.
- Tabuleiro orientado com a casa escura à esquerda de cada jogador (grande
  diagonal A1–H8 escura).
