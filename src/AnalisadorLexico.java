/*Analisador Lexico - Definição e função pro resto do compilador

 */
// AnalisadorLexico.java
import java.util.*;
import java.util.regex.*;

public class AnalisadorLexico {
    public enum TokenType {
        VAR, PRINT, INPUT, IF, ELSE, WHILE, FOR, FUNC, RETURN,
        IDENTIFIER, TYPE, ASSIGN, NUMBER, STRING, BOOL, COMMA,
        LPAREN, RPAREN, LBRACE, RBRACE, SEMICOLON, COLON,
        PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
        EQUAL, NOTEQUAL, LESSEQUAL, GREATEREQUAL, LESSTHAN, GREATERTHAN,
        COMMENT, UNKNOWN
    }

    public static class Token {
        public final TokenType type;
        public final String    value;
        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }
        public String toString() {
            return String.format("Token(%s, \"%s\")", type, value);
        }
    }

    private static final String TOKEN_REGEX =
            "(?<VAR>\\bvar\\b)"
                    + "|(?<PRINT>\\bprint\\b)"
                    + "|(?<INPUT>\\binput\\b)"
                    + "|(?<IF>\\bif\\b)"
                    + "|(?<ELSE>\\belse\\b)"
                    + "|(?<WHILE>\\bwhile\\b)"
                    + "|(?<FOR>\\bfor\\b)"
                    + "|(?<FUNC>\\bfunc\\b)"
                    + "|(?<RETURN>\\breturn\\b)"
                    + "|(?<TYPE>\\bint\\b|\\bfloat\\b|\\bchar\\b|\\bbool\\b|\\bstring\\b)"
                    + "|(?<BOOL>\\btrue\\b|\\bfalse\\b)"
                    + "|(?<NUMBER>\\b\\d+(\\.\\d+)?\\b)"
                    + "|(?<STRING>\"(.*?)\")"
                    + "|(?<IDENTIFIER>\\b[a-zA-Z_][a-zA-Z0-9_]*\\b)"
                    // operadores compostos primeiro:
                    + "|(?<EQUAL>==)"
                    + "|(?<NOTEQUAL>!=)"
                    + "|(?<LESSEQUAL><=)"
                    + "|(?<GREATEREQUAL>>=)"
                    + "|(?<ASSIGN>=)"
                    + "|(?<PLUS>\\+)"
                    + "|(?<MINUS>-)"
                    + "|(?<MULTIPLY>\\*)"
                    + "|(?<DIVIDE>/)"
                    + "|(?<MODULO>% )"
                    + "|(?<LESSTHAN><)"
                    + "|(?<GREATERTHAN>>)"
                    + "|(?<COLON>:)"
                    + "|(?<SEMICOLON>;)"
                    + "|(?<COMMA>,)"
                    + "|(?<LPAREN>\\()"
                    + "|(?<RPAREN>\\))"
                    + "|(?<LBRACE>\\{)"
                    + "|(?<RBRACE>\\})"
                    + "|(?<COMMENT>//[^\\n]*|/\\*[\\s\\S]*?\\*/)"
            ;

    public static List<Token> analisar(String codigo) {
        List<Token> tokens = new ArrayList<>();
        Pattern p = Pattern.compile(TOKEN_REGEX);
        Matcher m = p.matcher(codigo);

        while (m.find()) {
            if (m.group("VAR")        != null) tokens.add(new Token(TokenType.VAR, m.group()));
            else if (m.group("PRINT")  != null) tokens.add(new Token(TokenType.PRINT, m.group()));
            else if (m.group("INPUT")  != null) tokens.add(new Token(TokenType.INPUT, m.group()));
            else if (m.group("IF")     != null) tokens.add(new Token(TokenType.IF, m.group()));
            else if (m.group("ELSE")   != null) tokens.add(new Token(TokenType.ELSE, m.group()));
            else if (m.group("WHILE")  != null) tokens.add(new Token(TokenType.WHILE, m.group()));
            else if (m.group("FOR")    != null) tokens.add(new Token(TokenType.FOR, m.group()));
            else if (m.group("FUNC")   != null) tokens.add(new Token(TokenType.FUNC, m.group()));
            else if (m.group("RETURN") != null) tokens.add(new Token(TokenType.RETURN, m.group()));
            else if (m.group("TYPE")   != null) tokens.add(new Token(TokenType.TYPE, m.group()));
            else if (m.group("NUMBER") != null) tokens.add(new Token(TokenType.NUMBER, m.group()));
            else if (m.group("STRING") != null) tokens.add(new Token(TokenType.STRING, m.group()));
            else if (m.group("BOOL")   != null) tokens.add(new Token(TokenType.BOOL, m.group()));
            else if (m.group("IDENTIFIER") != null) tokens.add(new Token(TokenType.IDENTIFIER, m.group()));
            else if (m.group("EQUAL")      != null) tokens.add(new Token(TokenType.EQUAL, m.group()));
            else if (m.group("NOTEQUAL")   != null) tokens.add(new Token(TokenType.NOTEQUAL, m.group()));
            else if (m.group("LESSEQUAL")  != null) tokens.add(new Token(TokenType.LESSEQUAL, m.group()));
            else if (m.group("GREATEREQUAL")!=null) tokens.add(new Token(TokenType.GREATEREQUAL, m.group()));
            else if (m.group("ASSIGN")     != null) tokens.add(new Token(TokenType.ASSIGN, m.group()));
            else if (m.group("PLUS")       != null) tokens.add(new Token(TokenType.PLUS, m.group()));
            else if (m.group("MINUS")      != null) tokens.add(new Token(TokenType.MINUS, m.group()));
            else if (m.group("MULTIPLY")   != null) tokens.add(new Token(TokenType.MULTIPLY, m.group()));
            else if (m.group("DIVIDE")     != null) tokens.add(new Token(TokenType.DIVIDE, m.group()));
            else if (m.group("MODULO")     != null) tokens.add(new Token(TokenType.MODULO, m.group()));
            else if (m.group("LESSTHAN")   != null) tokens.add(new Token(TokenType.LESSTHAN, m.group()));
            else if (m.group("GREATERTHAN")!= null) tokens.add(new Token(TokenType.GREATERTHAN, m.group()));
            else if (m.group("COLON")      != null) tokens.add(new Token(TokenType.COLON, m.group()));
            else if (m.group("SEMICOLON")  != null) tokens.add(new Token(TokenType.SEMICOLON, m.group()));
            else if (m.group("COMMA")      != null) tokens.add(new Token(TokenType.COMMA, m.group()));
            else if (m.group("LPAREN")     != null) tokens.add(new Token(TokenType.LPAREN, m.group()));
            else if (m.group("RPAREN")     != null) tokens.add(new Token(TokenType.RPAREN, m.group()));
            else if (m.group("LBRACE")     != null) tokens.add(new Token(TokenType.LBRACE, m.group()));
            else if (m.group("RBRACE")     != null) tokens.add(new Token(TokenType.RBRACE, m.group()));
            else if (m.group("COMMENT")    != null) {/* opcional: skip ou armazenar */}
            else {
                System.err.printf("Erro Léxico: token desconhecido \"%s\" na posição %d%n",
                        m.group(), m.start());
                tokens.add(new Token(TokenType.UNKNOWN, m.group()));
            }
        }
        return tokens;
    }
}

