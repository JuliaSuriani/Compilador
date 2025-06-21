import java.util.Map;

public class GeradorCodigo {
    // Tabelas de símbolos construídas na análise semântica.
    private Map<String, String> varTable;    // Mapeia nome da variável para seu tipo (ex.: "a" -> "int")
    private Map<String, String> funcTable;   // Mapeia nome da função para seu tipo de retorno (ex.: "fatorial" -> "int")

    // Se for necessário usar o código-fonte original para alguma informação ou para referências,
    // você poderá recebê-lo também.
    private String codigoFonte;

    /**
     * Construtor para o gerador de código.
     * varTable Tabela de variáveis gerada durante a análise semântica.
     * funcTable Tabela de funções gerada durante a análise semântica.
     * codigoFonte O código fonte original (opcional para futuras referências).
     */
    public GeradorCodigo(Map<String, String> varTable, Map<String, String> funcTable, String codigoFonte) {
        this.varTable = varTable;
        this.funcTable = funcTable;
        this.codigoFonte = codigoFonte;
    }

    /** Gera o código assembly NASM para a arquitetura x86.
     * A geração é dividida em seções: header, data, bss e text.
     */
    public String gerarCodigo() {
        StringBuilder asm = new StringBuilder();
        asm.append(gerarHeader());
        asm.append(gerarDataSection());
        asm.append(gerarBssSection());
        asm.append(gerarTextSection());
        return asm.toString();
    }

    // Gera a seção de cabeçalho com definições globais e declarações externas.
    private String gerarHeader() {
        StringBuilder header = new StringBuilder();
        header.append("global main\n");
        header.append("extern printf, scanf\n\n");
        return header.toString();
    }

    // Gera a seção .data com constantes e mensagens (aqui, você pode adequar as mensagens conforme
    // funções de entrada/saída definidas na linguagem).
    private String gerarDataSection() {
        StringBuilder data = new StringBuilder();
        data.append("section .data\n");
        // Exemplos de constantes – você pode ajustar estes textos conforme sua necessidade.
        data.append("msg_input db \"Digite um valor: \", 0\n");
        data.append("fmt_int db \"%d\", 0\n");
        data.append("msg_result db \"Resultado: %d\", 10, 0\n\n");
        return data.toString();
    }

    // Gera a seção .bss para alocar espaço para as variáveis declaradas.
    private String gerarBssSection() {
        StringBuilder bss = new StringBuilder();
        bss.append("section .bss\n");
        // Para cada variável declarada, reserva espaço
        for (Map.Entry<String, String> entry : varTable.entrySet()) {
            String varName = entry.getKey();
            // Se desejar, você pode ajustar o tamanho conforme o tipo armazenado.
            bss.append(varName).append(" resd 1\n");
        }
        bss.append("\n");
        return bss.toString();
    }

    // Gera a seção .text com a definição da função main e os stubs para funções definidas.
    // Essa função deve ser expandida para percorrer a AST e gerar código de cada comando,
    // mas este esqueleto integra as partes fundamentais: início do main e geração de stubs
    private String gerarTextSection() {
        StringBuilder text = new StringBuilder();
        text.append("section .text\n");
        text.append("main:\n");
        text.append("    ; Prologo\n");
        text.append("    push ebp\n");
        text.append("    mov ebp, esp\n\n");

        // Integração do código gerado a partir dos comandos da linguagem
        // traduzir as declarações, atribuições, estruturas de controle, etc
        // Nesse teste, inserimos um comentário genérico
        text.append("    ; ... Código gerado a partir da análise de MACSLang ...\n\n");

        // Epilogo do main:
        text.append("    mov eax, 0\n");
        text.append("    mov esp, ebp\n");
        text.append("    pop ebp\n");
        text.append("    ret\n\n");

        // Gera stubs para cada função declarada.
        for (Map.Entry<String, String> entry : funcTable.entrySet()) {
            String funcName = entry.getKey();
            text.append(funcName).append(":\n");
            text.append("    ; Stub da funcao ").append(funcName).append("\n");
            // O stub deve ser substituído pela implementação gerada a partir da AST dessa função.
            text.append("    mov eax, 0\n");
            text.append("    ret\n\n");
        }

        return text.toString();
    }
}