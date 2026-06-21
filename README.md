# Jogo de Damas — Trabalho RA2 / RA3 (POO)

Aplicação orientada a objetos do Jogo de Damas, composta por dois programas:

- **P1** — lê os dados iniciais de um arquivo texto (csv), cria os objetos
  persistentes (`ConfiguracaoJogo`) e os salva em **formato binário**.
- **P2** — restaura os objetos do binário, interage com o usuário por
  **interface gráfica** e grava os resultados parciais e finais em texto.
  *(GUI em desenvolvimento — ver seção "Status".)*

## Estrutura de pacotes

```
damas/
  modelo/   <- dominio: Peca, PecaBranca, PecaPreta, Tabuleiro, Jogador,
               ConfiguracaoJogo, Movimentavel, MovimentoInvalidoException,
               JogadorInvalidoException
  logica/   <- MotorJogo (regras do jogo)
  app/      <- P1, Persistencia, RegistradorResultados   (P2: em breve)
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

> **Em desenvolvimento.** A restauração do binário e a gravação de resultados
> (`Persistencia`, `RegistradorResultados`) já estão prontas e testadas; falta a
> interface gráfica. Quando concluída, o comando será:

```bash
java -cp out damas.app.P2            # (ainda não disponível)
```

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

**RA3 (P1/P2/persistência/GUI)**
- [x] P1: csv -> objetos -> binário
- [x] P2: restauração do binário
- [x] P2: gravação de resultados parciais/finais em txt
- [ ] P2: interface gráfica (em desenvolvimento)
