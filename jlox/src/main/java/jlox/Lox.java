package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    // Tracks whether an error was found while code is run
    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader stream = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = stream.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expression expression = parser.parse();

        // Stop if there is an error
        if (hadError) return;

        System.out.println(new AstViewer().print(expression));
    }

    /**
     * Report an error at a specific line and location.
     * @param line the line number where the error is located
     * @param message the error message
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * Report an error at a specific token rather than a line.
     * @param token the token where the error is located
     * @param message the error message
     */
    static void error(Token token, String message) {
        if (token.type == Token.TokenType.EOF) {
            report(token.line, "at end", message);
        } else {
            report(token.line, "at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] error " + where + ": " + message);
        hadError = true;
    }
}
