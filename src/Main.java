/* Compilador - Trabalho final da UC Teoria da computação e compiladores
Autores (e seus RA's):
Ana Luísa Pacífico - 1232023412
Júlia Suriani de O. Silva - 12319522
Mariana Teixeira Gonçalves - 123115663
Mateus Mendes Mattos - 123117292
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String codigo = null;

        // Exibe o menu para o usuário escolher a fonte do código.
        System.out.println("Selecione a opção desejada para inserir o código:");
        System.out.println("1. Inserir código via console");
        System.out.println("2. Usar código de exemplo do professor");
        System.out.println("3. Usar código de exemplo com erros propositais");
        System.out.println("4. Carregar código de um arquivo");
        System.out.print("Opção: ");

        int opcao = scanner.nextInt();
        scanner.nextLine(); // Consome a quebra de linha remanescente

        // O código é obtido conforme a opção escolhida.
        if (opcao == 1) {
            System.out.println("Digite o código (digite 'END' em uma linha separada para finalizar):");
            StringBuilder sb = new StringBuilder();
            while (true) {
                String linha = scanner.nextLine();
                if (linha.trim().equalsIgnoreCase("END")) {
                    break;
                }
                sb.append(linha).append("\n");
            }
            codigo = sb.toString();
        } else if (opcao == 2) {
            //código exemplo enviado pelo professor, deve ser executado sem erros
            codigo = """
                func fatorial(n: int): int {
                    var resultado: int = 1;
                    for (var i: int = 1; i <= n; i = i + 1) {
                        resultado = resultado * i;
                    }
                    return resultado;
                }
                
                print("Digite um número para calcular o fatorial:");
                var numero: int;
                input(numero);
                
                var fat: int = fatorial(numero);
                print("O fatorial de " + numero + " é " + fat);
                """;
        } else if (opcao == 3) {
            // Código propositadamente com erros (falta de ponto e vírgula, bloco mal formado e variável inexistente)
            codigo = """
                func fatorial(n: int): int {
                    var resultado: int = 1;
                    for (var i: int = 1; i <= n; i = i + 1) {
                        resultado = resultado * i
                    return resultado;
                }
                
                print("Digite um número para calcular o fatorial:");
                var numero: int;
                input(num);
                
                var fat: int = fatorial(numero);
                print("O fatorial de " + numero + " é " + fat);
                """;
        } else if (opcao == 4) {
            System.out.print("Digite o caminho do arquivo: ");
            String caminho = scanner.nextLine();
            try {
                codigo = Files.readString(Paths.get(caminho));
            } catch (IOException e) {
                System.err.println("Erro ao ler o arquivo: " + e.getMessage());
                return;
            }
        } else {
            System.err.println("Opção inválida. Encerrando.");
            return;
        }

        // Exibe o código-fonte que será analisado.
        System.out.println("\nCódigo a ser compilado:");
        System.out.println(codigo);

        // FASE 1 – Análise Léxica:
        List<AnalisadorLexico.Token> tokens = AnalisadorLexico.analisar(codigo);
        System.out.println("\n📌 Tokens gerados:");
        for (AnalisadorLexico.Token token : tokens) {
            System.out.println(token);
        }

        // FASE 2 – Análise Sintática/Semântica:
        // Redireciona o System.err para capturar os erros durante o processo.
        ByteArrayOutputStream bufErr = new ByteArrayOutputStream();
        PrintStream oldErr = System.err;
        System.setErr(new PrintStream(bufErr));

        AnalisadorSintatico sintatico = new AnalisadorSintatico(tokens);
        sintatico.analisarPrograma();

        System.err.flush();
        System.setErr(oldErr);
        String erros = bufErr.toString().trim();

        // Verifica e classifica os erros encontrados.
        boolean sintaticoEncontrado = erros.contains("Erro sintático encontrado");
        boolean semanticoEncontrado = erros.contains("Erro semântico encontrado");

        System.out.println();
        if (erros.isEmpty()) {
            System.out.println("✅ Nenhum erro sintático e/ou semântico foi encontrado.");
        } else if (sintaticoEncontrado && semanticoEncontrado) {
            System.out.println("Foram encontrados erros sintáticos e semânticos:");
            System.out.println(erros);
        } else if (sintaticoEncontrado) {
            System.out.println("Foram encontrados os erros sintáticos a seguir:");
            System.out.println(erros);
        } else if (semanticoEncontrado) {
            System.out.println("Foram encontrados os erros semânticos a seguir:");
            System.out.println(erros);
        }

        // FASE 3 – Geração de Código:
        if (erros.isEmpty()) {
            System.out.println("\n✅ Compilação bem-sucedida! Gerando código para máquina...");

            // Obtém as tabelas de símbolos a partir do AnalisadorSintatico.
            Map<String, String> varTable = sintatico.getSymbolTable();
            Map<String, String> funcTable = sintatico.getFunctionTable();

            // Cria e invoca o gerador de código para gerar o assembly.
            GeradorCodigo gerador = new GeradorCodigo(varTable, funcTable, codigo);
            String assembly = gerador.gerarCodigo();
            System.out.println("\nCódigo Assembly Gerado:\n");
            System.out.println(assembly);
        } else {
            System.out.println("\n❌ Não foi possível gerar código para máquina devido a erros na análise.");
        }
    }
}