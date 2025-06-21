/*Analisador Sintatico e Semantico - Definição e função pro resto do compilador
O Analisador Sintático precisa:
- Receber os tokens gerados pelo Analisador Léxico.
- Percorrer esses tokens para verificar se a estrutura segue a gramática.
- Lançar erros sintáticos, se houver problemas de sequência ou formatação.
 */
import java.util.*;

public class AnalisadorSintatico {
    // Lista de tokens gerados pelo analisador léxico.
    private final List<AnalisadorLexico.Token> tokens;
    // Índice que aponta para o token atual.
    private int indiceTokenAtual = 0;

    // Tabelas de símbolos para variáveis e funções (nome -> tipo).
    private Map<String, String> symbolTable = new HashMap<>();
    private Map<String, String> symbolTableFunctions = new HashMap<>();

    // GETS utilizados pelo gerador de código
    public Map<String, String> getSymbolTable() {
        return symbolTable;
    }

    public Map<String, String> getFunctionTable() {
        return symbolTableFunctions;
    }

    // Tipo de retorno da função atual
    private String currentFunctionReturnType = null;

    public AnalisadorSintatico(List<AnalisadorLexico.Token> tokens) {
        this.tokens = tokens;
    }

    // Verifica se o token atual é o esperado (sem consumi-lo).
    private boolean match(AnalisadorLexico.TokenType esperado) {
        return indiceTokenAtual < tokens.size() &&
                tokens.get(indiceTokenAtual).type == esperado;
    }

    // Consome o token e avança o índice se coincidir.
    private boolean consumir(AnalisadorLexico.TokenType esperado) {
        if (match(esperado)) {
            indiceTokenAtual++;
            return true;
        }
        return false;
    }

    // Exibe mensagem de erro sintático com detalhes.
    private boolean erroSintatico(String msg) {
        System.err.println("Erro sintático encontrado na posição " + indiceTokenAtual + ": " + msg +
                " – Token atual: " + (indiceTokenAtual < tokens.size() ? tokens.get(indiceTokenAtual) : "EOF"));
        return false;
    }

    // Exibe mensagem de erro semântico com detalhes.
    private boolean erroSemantico(String msg) {
        System.err.println("Erro semântico encontrado na posição " + indiceTokenAtual + ": " + msg +
                " – Token atual: " + (indiceTokenAtual < tokens.size() ? tokens.get(indiceTokenAtual) : "EOF"));
        return false;
    }

    /// Inicia a análise processando todos os comandos.
    public void analisarPrograma() {
        while (indiceTokenAtual < tokens.size()) {
            analisarComando();
        }
    }

    // Seleciona e processa o comando apropriado conforme o token atual.
    private void analisarComando() {
        if (match(AnalisadorLexico.TokenType.VAR)) {
            analisarDeclaracaoVariavel();
        } else if (match(AnalisadorLexico.TokenType.FUNC)) {
            analisarFuncao();
        } else if (match(AnalisadorLexico.TokenType.RETURN)) {
            analisarReturn();
        } else if (match(AnalisadorLexico.TokenType.IDENTIFIER)) {
            // Se o próximo token for '=' trata como atribuição; caso contrário, processa a expressão (ex.: chamada de função).
            if (peekNextTokenType() == AnalisadorLexico.TokenType.ASSIGN) {
                analisarAtribuicao();
            } else {
                analisarExpressao();
                consumir(AnalisadorLexico.TokenType.SEMICOLON);
            }
        } else if (match(AnalisadorLexico.TokenType.IF)) {
            analisarControleFluxo();
        } else if (match(AnalisadorLexico.TokenType.WHILE) ||
                match(AnalisadorLexico.TokenType.FOR)) {
            analisarLaco();
        } else if (match(AnalisadorLexico.TokenType.PRINT) ||
                match(AnalisadorLexico.TokenType.INPUT)) {
            analisarEntradaSaida();
        } else {
            erroSintatico("Comando inesperado.");
            indiceTokenAtual++;
        }
    }

    // Retorna o tipo do próximo token sem avançar.
    private AnalisadorLexico.TokenType peekNextTokenType() {
        if (indiceTokenAtual + 1 < tokens.size()) {
            return tokens.get(indiceTokenAtual + 1).type;
        }
        return null;
    }


    // Análise de expressões (retornam o tipo da expressão)
    private String analisarExpressao() {
        String leftType = analisarTermo();
        if (leftType == null) return null;

        while (match(AnalisadorLexico.TokenType.PLUS)    ||
                match(AnalisadorLexico.TokenType.MINUS)   ||
                match(AnalisadorLexico.TokenType.MULTIPLY)||
                match(AnalisadorLexico.TokenType.DIVIDE)  ||
                match(AnalisadorLexico.TokenType.MODULO)  ||
                match(AnalisadorLexico.TokenType.EQUAL)   ||
                match(AnalisadorLexico.TokenType.NOTEQUAL)||
                match(AnalisadorLexico.TokenType.LESSTHAN)||
                match(AnalisadorLexico.TokenType.GREATERTHAN)||
                match(AnalisadorLexico.TokenType.LESSEQUAL) ||
                match(AnalisadorLexico.TokenType.GREATEREQUAL)) {

            AnalisadorLexico.TokenType op = tokens.get(indiceTokenAtual).type;
            consumir(op);
            String rightType = analisarTermo();
            if (rightType == null) return null;

            // Se o operador é '+'
            if (op == AnalisadorLexico.TokenType.PLUS) {
                // Permite concatenação se qualquer operando for string.
                if (leftType.equals("string") || rightType.equals("string")) {
                    leftType = "string";
                } else if ((leftType.equals("int") || leftType.equals("float")) &&
                        (rightType.equals("int") || rightType.equals("float"))) {
                    leftType = (leftType.equals("float") || rightType.equals("float")) ? "float" : "int";
                } else {
                    erroSemantico("Operador '+' aplicado a tipos incompatíveis: " + leftType + " e " + rightType);
                    return null;
                }
            }
            // Para os demais operadores aritméticos
            else if (op == AnalisadorLexico.TokenType.MINUS ||
                    op == AnalisadorLexico.TokenType.MULTIPLY ||
                    op == AnalisadorLexico.TokenType.DIVIDE  ||
                    op == AnalisadorLexico.TokenType.MODULO) {

                if ((leftType.equals("int") || leftType.equals("float")) &&
                        (rightType.equals("int") || rightType.equals("float"))) {
                    leftType = (leftType.equals("float") || rightType.equals("float")) ? "float" : "int";
                } else {
                    erroSemantico("Operador aritmético aplicado a tipos incompatíveis: " + leftType + " e " + rightType);
                    return null;
                }
            }
            // Para operadores relacionais, o resultado deve ser bool
            else {
                if (leftType.equals(rightType)) {
                    leftType = "bool";
                } else {
                    erroSemantico("Operador relacional aplicado a tipos incompatíveis: " + leftType + " e " + rightType);
                    return null;
                }
            }
        }
        return leftType;
    }

    private String analisarTermo() {
        if (match(AnalisadorLexico.TokenType.IDENTIFIER)) {
            String nome = tokens.get(indiceTokenAtual).value;
            consumir(AnalisadorLexico.TokenType.IDENTIFIER);
            // Se for chamada de função
            if (match(AnalisadorLexico.TokenType.LPAREN)) {
                consumir(AnalisadorLexico.TokenType.LPAREN);
                if (!match(AnalisadorLexico.TokenType.RPAREN)) {
                    String paramType = analisarExpressao();
                    if (paramType == null) return null;
                    while (match(AnalisadorLexico.TokenType.COMMA)) {
                        consumir(AnalisadorLexico.TokenType.COMMA);
                        String t = analisarExpressao();
                        if (t == null) return null;
                    }
                }
                if (!consumir(AnalisadorLexico.TokenType.RPAREN)) {
                    erroSintatico("Esperado ')' para fechar chamada de função.");
                    return null;
                }
                String funcType = symbolTableFunctions.get(nome);
                if (funcType == null) {
                    erroSemantico("Função '" + nome + "' não declarada.");
                    return "unknown";
                }
                return funcType;
            } else {
                String varType = symbolTable.get(nome);
                if (varType == null) {
                    erroSemantico("Variável '" + nome + "' não declarada.");
                    return "unknown";
                }
                return varType;
            }
        } else if (match(AnalisadorLexico.TokenType.NUMBER)) {
            String num = tokens.get(indiceTokenAtual).value;
            consumir(AnalisadorLexico.TokenType.NUMBER);
            return (num.contains(".")) ? "float" : "int";
        } else if (match(AnalisadorLexico.TokenType.STRING)) {
            consumir(AnalisadorLexico.TokenType.STRING);
            return "string";
        } else if (match(AnalisadorLexico.TokenType.BOOL)) {
            consumir(AnalisadorLexico.TokenType.BOOL);
            return "bool";
        } else if (match(AnalisadorLexico.TokenType.LPAREN)) {
            consumir(AnalisadorLexico.TokenType.LPAREN);
            String exprType = analisarExpressao();
            if (exprType == null) return null;
            if (!consumir(AnalisadorLexico.TokenType.RPAREN)) {
                erroSintatico("Esperado ')' para fechar expressão.");
                return null;
            }
            return exprType;
        }
        erroSintatico("Termo inesperado.");
        return null;
    }


    /// Processamento de comandos e estruturas de controle


    // Atribuição fora de um laço (consome o ponto‑vírgula final).
    private boolean analisarAtribuicao() {
        String varName = tokens.get(indiceTokenAtual).value;
        if (!consumir(AnalisadorLexico.TokenType.IDENTIFIER))
            return erroSintatico("Esperado identificador para atribuição.");
        if (!consumir(AnalisadorLexico.TokenType.ASSIGN))
            return erroSintatico("Esperado '=' na atribuição.");
        String exprType = analisarExpressao();
        if (exprType == null)
            return false;
        String varType = symbolTable.get(varName);
        if (varType == null) {
            erroSemantico("Variável '" + varName + "' não declarada antes da atribuição.");
        } else if (!tipoCompatível(varType, exprType)) {
            erroSemantico("Incompatibilidade de tipos na atribuição de '" + varName +
                    "'. Declarado: " + varType + ", atribuído: " + exprType);
        }
        if (!consumir(AnalisadorLexico.TokenType.SEMICOLON))
            return erroSintatico("Esperado ';' no fim da atribuição.");
        return true;
    }

    // Declaração de variável fora de for (consome o ';' final).
    private boolean analisarDeclaracaoVariavel() {
        consumir(AnalisadorLexico.TokenType.VAR);
        String varName = tokens.get(indiceTokenAtual).value;
        if (!consumir(AnalisadorLexico.TokenType.IDENTIFIER))
            return erroSintatico("Esperado o nome da variável na declaração.");
        if (!consumir(AnalisadorLexico.TokenType.COLON))
            return erroSintatico("Esperado ':' na declaração da variável.");
        String varType = tokens.get(indiceTokenAtual).value;
        if (!consumir(AnalisadorLexico.TokenType.TYPE))
            return erroSintatico("Tipo inválido na declaração da variável.");
        if (symbolTable.containsKey(varName)) {
            erroSemantico("Variável '" + varName + "' já declarada.");
        } else {
            symbolTable.put(varName, varType);
        }
        if (match(AnalisadorLexico.TokenType.ASSIGN)) {
            consumir(AnalisadorLexico.TokenType.ASSIGN);
            String exprType = analisarExpressao();
            if (exprType == null) return false;
            if (!tipoCompatível(varType, exprType)) {
                erroSemantico("Incompatibilidade de tipos na declaração de '" + varName +
                        "'. Declarado: " + varType + ", atribuído: " + exprType);
            }
        }
        if (!consumir(AnalisadorLexico.TokenType.SEMICOLON))
            return erroSintatico("Esperado ';' no fim da declaração de variável.");
        return true;
    }

    //Versões para uso no 'for' (não consomem o ';' final).

    private boolean analisarDeclaracaoVariavelFor() {
        consumir(AnalisadorLexico.TokenType.VAR);
        String varName = tokens.get(indiceTokenAtual).value;
        if (!consumir(AnalisadorLexico.TokenType.IDENTIFIER))
            return erroSintatico("Esperado o nome da variável na declaração.");
        if (!consumir(AnalisadorLexico.TokenType.COLON))
            return erroSintatico("Esperado ':' na declaração da variável.");
        String varType = tokens.get(indiceTokenAtual).value;
        if (!consumir(AnalisadorLexico.TokenType.TYPE))
            return erroSintatico("Tipo inválido na declaração da variável.");
        if (symbolTable.containsKey(varName))
            erroSemantico("Variável '" + varName + "' já declarada.");
        else
            symbolTable.put(varName, varType);
        if (match(AnalisadorLexico.TokenType.ASSIGN)) {
            consumir(AnalisadorLexico.TokenType.ASSIGN);
            String exprType = analisarExpressao();
            if (exprType == null) return false;
            if (!tipoCompatível(varType, exprType))
                erroSemantico("Incompatibilidade de tipos na declaração de '" + varName +
                        "'. Declarado: " + varType + ", atribuído: " + exprType);
        }
        // Não consome ';' para encaixar na sintaxe do 'for'
        return true;
    }

    private boolean analisarAtribuicaoFor() {
        String varName = tokens.get(indiceTokenAtual).value;
        if (!consumir(AnalisadorLexico.TokenType.IDENTIFIER))
            return erroSintatico("Esperado identificador para atribuição.");
        if (!consumir(AnalisadorLexico.TokenType.ASSIGN))
            return erroSintatico("Esperado '=' na atribuição.");
        String exprType = analisarExpressao();
        if (exprType == null) return false;
        String varType = symbolTable.get(varName);
        if (varType == null)
            erroSemantico("Variável '" + varName + "' não declarada.");
        else if (!tipoCompatível(varType, exprType))
            erroSemantico("Incompatibilidade de tipos na atribuição de '" + varName +
                    "'. Declarado: " + varType + ", atribuído: " + exprType);
        // Não consome ';' aqui
        return true;
    }

    /// Entrada e saída.
    private boolean analisarEntradaSaida() {
        AnalisadorLexico.TokenType cmd = match(AnalisadorLexico.TokenType.PRINT)
                ? AnalisadorLexico.TokenType.PRINT
                : AnalisadorLexico.TokenType.INPUT;
        consumir(cmd);
        if (!consumir(AnalisadorLexico.TokenType.LPAREN))
            return erroSintatico("Esperado '(' após " + cmd + ".");
        if (cmd == AnalisadorLexico.TokenType.PRINT) {
            String exprType = analisarExpressao();
            if (exprType == null) return false;
        } else {
            String varName = tokens.get(indiceTokenAtual).value;
            if (!consumir(AnalisadorLexico.TokenType.IDENTIFIER))
                return erroSintatico("Esperado identificador em input.");
            if (!symbolTable.containsKey(varName))
                erroSemantico("Variável '" + varName + "' não declarada para input.");
        }
        if (!consumir(AnalisadorLexico.TokenType.RPAREN))
            return erroSintatico("Esperado ')' para fechar " + cmd + ".");
        if (!consumir(AnalisadorLexico.TokenType.SEMICOLON))
            return erroSintatico("Esperado ';' após " + cmd + ".");
        return true;
    }

    /// Controle condicional 'if' [ 'else' ].
    private boolean analisarControleFluxo() {
        consumir(AnalisadorLexico.TokenType.IF);
        if (!consumir(AnalisadorLexico.TokenType.LPAREN))
            return erroSintatico("Esperado '(' após 'if'.");
        String condType = analisarExpressao();
        if (condType == null) return false;
        if (!condType.equals("bool"))
            erroSemantico("Condição do 'if' deve ser do tipo bool, mas obteve: " + condType);
        if (!consumir(AnalisadorLexico.TokenType.RPAREN))
            return erroSintatico("Esperado ')' após a condição do 'if'.");
        processarBlocoCodigo("if");
        if (match(AnalisadorLexico.TokenType.ELSE)) {
            consumir(AnalisadorLexico.TokenType.ELSE);
            processarBlocoCodigo("else");
        }
        return true;
    }

    // Seleciona o laço: 'while' ou 'for'
    private boolean analisarLaco() {
        if (match(AnalisadorLexico.TokenType.WHILE))
            return processarLacoWhile();
        else
            return processarLacoFor();
    }

    // Laço 'while'
    private boolean processarLacoWhile() {
        consumir(AnalisadorLexico.TokenType.WHILE);
        if (!consumir(AnalisadorLexico.TokenType.LPAREN))
            return erroSintatico("Esperado '(' após 'while'.");
        String condType = analisarExpressao();
        if (condType == null) return false;
        if (!condType.equals("bool"))
            erroSemantico("Condição do 'while' deve ser do tipo bool, mas obteve: " + condType);
        if (!consumir(AnalisadorLexico.TokenType.RPAREN))
            return erroSintatico("Esperado ')' após a condição do 'while'.");
        return processarBlocoCodigo("while");
    }

    // Laço 'for' atualizado: utiliza métodos For para não consumir ';' a mais
    private boolean processarLacoFor() {
        consumir(AnalisadorLexico.TokenType.FOR);
        if (!consumir(AnalisadorLexico.TokenType.LPAREN))
            return erroSintatico("Esperado '(' após 'for'.");
        // Inicialização — pode ser declaração ou atribuição sem o ';' final
        if (match(AnalisadorLexico.TokenType.VAR))
            analisarDeclaracaoVariavelFor();
        else
            analisarAtribuicaoFor();
        if (!consumir(AnalisadorLexico.TokenType.SEMICOLON))
            return erroSintatico("Esperado ';' após a inicialização no 'for'.");
        // Condição deve ser booleana
        String condType = analisarExpressao();
        if (condType == null) return false;
        if (!condType.equals("bool"))
            erroSemantico("Condição do 'for' deve ser do tipo bool, mas obteve: " + condType);
        if (!consumir(AnalisadorLexico.TokenType.SEMICOLON))
            return erroSintatico("Esperado ';' após a condição no 'for'.");
        // Atualização — atribuição sem consumir ';' final
        analisarAtribuicaoFor();
        if (!consumir(AnalisadorLexico.TokenType.RPAREN))
            return erroSintatico("Esperado ')' após a atualização no 'for'.");
        return processarBlocoCodigo("for");
    }

    // Processa um bloco delimitado por '{' e '}'
    private boolean processarBlocoCodigo(String contexto) {
        if (!consumir(AnalisadorLexico.TokenType.LBRACE))
            return erroSintatico("Esperado '{' para iniciar bloco de " + contexto + ".");
        while (!match(AnalisadorLexico.TokenType.RBRACE) && indiceTokenAtual < tokens.size()) {
            analisarComando();
        }
        if (!consumir(AnalisadorLexico.TokenType.RBRACE))
            return erroSintatico("Esperado '}' para fechar bloco de " + contexto + ".");
        return true;
    }

    /// Declaração de função
    private boolean analisarFuncao() {
        consumir(AnalisadorLexico.TokenType.FUNC);
        String funcName = tokens.get(indiceTokenAtual).value;
        if (!consumir(AnalisadorLexico.TokenType.IDENTIFIER))
            return erroSintatico("Esperado nome da função após 'func'.");
        processarParametrosFuncao();
        if (!consumir(AnalisadorLexico.TokenType.COLON))
            return erroSintatico("Esperado ':' após os parâmetros da função.");
        String returnType = tokens.get(indiceTokenAtual).value;
        if (!consumir(AnalisadorLexico.TokenType.TYPE))
            return erroSintatico("Esperado tipo de retorno da função.");
        if (symbolTableFunctions.containsKey(funcName)) {
            erroSemantico("Função '" + funcName + "' já declarada.");
        } else {
            symbolTableFunctions.put(funcName, returnType);
        }
        String anteriorFunctionReturnType = currentFunctionReturnType;
        currentFunctionReturnType = returnType;
        processarBlocoCodigo("função");
        currentFunctionReturnType = anteriorFunctionReturnType;
        return true;
    }

    // Processa os parâmetros de função
    private boolean processarParametrosFuncao() {
        if (!consumir(AnalisadorLexico.TokenType.LPAREN))
            return erroSintatico("Esperado '(' para iniciar os parâmetros da função.");
        while (match(AnalisadorLexico.TokenType.IDENTIFIER)) {
            String paramName = tokens.get(indiceTokenAtual).value;
            consumir(AnalisadorLexico.TokenType.IDENTIFIER);
            if (!consumir(AnalisadorLexico.TokenType.COLON))
                return erroSintatico("Esperado ':' após o parâmetro.");
            String paramType = tokens.get(indiceTokenAtual).value;
            if (!consumir(AnalisadorLexico.TokenType.TYPE))
                return erroSintatico("Esperado tipo do parâmetro.");
            symbolTable.put(paramName, paramType);
            if (match(AnalisadorLexico.TokenType.COMMA))
                consumir(AnalisadorLexico.TokenType.COMMA);
            else break;
        }
        if (!consumir(AnalisadorLexico.TokenType.RPAREN))
            return erroSintatico("Esperado ')' para finalizar os parâmetros da função.");
        return true;
    }

    // Processa o comando 'return'
    private boolean analisarReturn() {
        consumir(AnalisadorLexico.TokenType.RETURN);
        String exprType = analisarExpressao();
        if (exprType == null) return false;
        if (currentFunctionReturnType != null && !tipoCompatível(currentFunctionReturnType, exprType)) {
            erroSemantico("Retorno incompatível. Função espera: " + currentFunctionReturnType +
                    ", mas obteve: " + exprType);
        }
        if (!consumir(AnalisadorLexico.TokenType.SEMICOLON))
            return erroSintatico("Esperado ';' após o comando 'return'.");
        return true;
    }

    // Verifica se os tipos são compatíveis (igualdade exata aqui)
    private boolean tipoCompatível(String tipoEsperado, String tipoObtido) {
        return tipoEsperado.equals(tipoObtido);
    }
}