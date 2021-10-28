package jlox;

import java.util.ArrayList;
import java.util.List;

import static jlox.Token.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 0;

    Scanner(String source) {
        this.source = source;
    }

    /**
     * Parses a list of tokens from the source string
     * @return a List of all the tokens in the source string
     */
    public List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * Parses a token from the source string at the current indices
     */
    public void scanToken() {
        char c = advance();

        switch (c) {
            /* Check for one-character tokens */
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(RIGHT_BRACE); break;
            case '}': addToken(LEFT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            /* Checks for tokens that can be one character or two characters */
            case '!':
                // If the token after ! is =, it is one token !=
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL: GREATER);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;

            /* Checks for comments vs a slash */
            case '/':
                if(match('/')) {
                    // Continue until the end of the line if it is a comment
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;

            /* Skips any whitespace */
            case ' ':
            case '\r':
            case '\t':
                // Performs no parsing, when this method is run again it moves onto the next char
                break;

            /* Increments line count on new lines */
            case '\n':
                // No parsing is done here
                line++;
                break;

            /* Lox interpreter error if a token is not recognized */
            default:
                Lox.error(line, "Unexpected character");
                break;
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Returns the current character of the source string and moves onto the next
     * @return char in source string at current index
     */
    private char advance() {
        return source.charAt(current++);
    }

    /**
     * Returns the next character to be parsed without moving onto the next one
     * @return char in source string at current index
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void addToken(Token.TokenType type) {
        addToken(type, null);
    }

    private void addToken(Token.TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    /**
     * Checks if the next character is what is expected when part of a multi-character token.
     * Moves onto the next character if it is a match.
     * @param expected the character expected after the current one
     * @return true if the next char is equal to expected, false otherwise
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        // Since the current and expected character are part of one token, it can safely move onto the next char
        current++;
        return true;
    }

}
