# Compilador - MACSLang

## Autores:
@[Ana Luísa Pacífico](https://github.com/anapacifico)

@[Julia Suriani de O. Silva](https://github.com/JuliaSuriani)

@[Mariana Teixeira Gonçalves](https://github.com/mariteixeiraa)

@[Mateus Mendes Mattos](https://github.com/Mateus0062)

## Objetivo do projeto

Criar um compilador que seja capaz de analisar (Léxico, sintático e semanticamente) a linguagem MACSLang e gerar o código para máquina.

Os parâmetros e descrição da linguagem estão definidos no arquivo [MACSLANG](src/MACSLang).

A gramática da linguagem está disponível no arquivo [gramatica](src/gramatica).

## Tecnologias utilizadas
Java. 
  - Programação orientada a objetos (cada fase do analisador possui uma classe).

## Como funciona o compilador
O compilador possui as seguintes fases de análise:
- **[Análise Léxica](src/AnalisadorLexico.java)**: transforma o código-fonte em uma sequência de tokens.
- **[Análise Sintática](src/AnalisadorSintatico.java)**: garante que o código siga as regras gramaticais da linguagem.
- **[Análise Semântica](src/AnalisadorSintatico.java)**: verifica a coerência e o significado do código, como a compatibilidade de tipos e a declaração correta de variáveis
- **[Gerador de código](src/GeradorCodigo.java)**: converte o código intermediário em código de destino.


## Como usar o projeto
Ao executar o compilador, o usuário pode escolher 4 formas de entrada:
1. Entrar com o código via console
2. Executar o código exemplo para visualizar a execução e geração de um código que esteja correto
3. Executar um modelo de código propositalmente errado, para visualizar o funcionamento do compilador ao encontrar erros
4. Realizar a leitura de um arquivo

Após definir a forma de entrada do código, o compilador irá prosseguir com a análise.

Será retornado ao usuário todos os tokens definidos e gerados durante a sessão.

Em seguida, o analisador sintático/semântico irá verificar se existem erros e, caso existam, irá retornar a seguinte estrutura: 

[Tipo do erro] + [posição onde o erro é encontrado]: + [Esclarecimento do erro encontrado] + [qual a informação inserida incorretamente]

Exemplo: 

- Erro sintático encontrado na posição 42: Esperado ';' no fim da atribuição. – Token atual: Token(RETURN, "return")

- Erro semântico encontrado na posição 59: Variável 'num' não declarada para input. – Token atual: Token(RPAREN, ")")


Caso sejam encontrados erros, o processo não avança para a geração de código.

Se não for encontrado nenhum erro, o processo prosseguirá para o gerador de código e, ao finalizar, é retornado ao usuário o código Assembly gerado a partir da entrada.
