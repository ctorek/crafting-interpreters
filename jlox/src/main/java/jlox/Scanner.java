package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jlox.Token.TokenType.*;

public class Scanner {
    // Map of keywords to tokens
    private static final Map<String, Token.TokenType> keywords;

    // Keywords are initialized only once for every instance of this class
    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("fun", FUN);
        keywords.put("for", FOR);
        keywords.put("IF", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    // Source code being scanned
    private final String source;

    // List of tokens resulting from scanned source code
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0; // Index of start of current token
    private int current = 0; // Index of current character of current token
    private int line = 0; // Current line number

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
                } else if (match('*')) {
                    blockComment();
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

            /* String literals */
            case '"': string(); break;

            /* Default case handles number literals, identifiers, or unexpected chars */
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    // Identifiers must not start with a number
                    identifier();
                } else {
                    // Lox interpreter error if a token is not recognized
                    Lox.error(line, "Unexpected character");
                }
                break;
        }
    }

    /**
     * Traverses an identifier containing alphanumeric characters. <br>
     * Must start with an alphabetic character and not a number.
     */
    private void identifier() {
        // Advance while the next character is another letter or number
        while (isAlphanumeric(peek())) advance();

        // Attempt to recognize as a keyword
        String text = source.substring(start, current);
        Token.TokenType type = keywords.get(text);

        // Tokenized as an identifier if it is not a keyword
        if (type == null) type = IDENTIFIER;

        addToken(type);
    }

    /**
     * Traverse a block comment without tokenizing any comment.
     * Throws a Lox error if the file ends without closing the comment.
     */
    private void blockComment() {
        // Before advancing, check that next chars are not end of comment
        while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // Throw error if end of file is found without closing comment
        if (isAtEnd()) {
            Lox.error(line, "Unterminated block comment");
            return;
        }

        // Advance twice because there are two closing characters to block comments
        advance(); advance();
    }

    /**
     * Traverse a numeric literal and tokenizes content into a double. <br>
     * Does not allow leading or trailing decimals.
     */
    private void number() {
        // Advance while the next character is another digit of the number
        while (isDigit(peek())) advance();

        // Only consume a '.' if numbers come after it
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            // Continue consuming numbers after '.' without allowing another '.'
            while (isDigit(peek())) advance();
        }

        // Numbers are only represented as doubles in jlox and not integers
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /**
     * Traverse a string literal until the closing quote and tokenizes content. <br>
     * Throws a Lox error if the string is not terminated by end of file. <br>
     * Allows multiline strings.
     */
    private void string() {
        // Before advancing, check that next char is not end of string
        while (peek() != '"' && !isAtEnd()) {
            // Advance line count if string contains newlines
            if (peek() == '\n') line++;
            advance();
        }

        // Throw error if end of file is found without closing quote
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string");
            return;
        }

        // After the above loop and if statement, next char must be closing quote
        advance();

        // Get the value of the string without the quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * Check if a character is a number or other character
     * @param c the character to check
     * @return true if the character is a digit, false otherwise
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Check if a character is a lowercase letter, uppercase letter, or underscore
     * @param c the character to check
     * @return true if the character is alphabetic, false otherwise
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_');
    }

    /**
     * Checks if a character is an alphabetic or numeric character. <br>
     * Based on the {@link #isAlpha(char)} isAlpha} and {@link #isDigit(char) isDigit} methods.
     */
    private boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Checks whether the current character being parsed is at the end of the file
     * @return true if at end of file, false otherwise
     */
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

    /**
     * Returns the second next character to be parsed without moving onto the next
     * @return char in source string at next index
     */
    private char peekNext() {
        // Return null if peeking past the end of the source code
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /**
     * Adds a token to the list of tokens representing the scanned code
     * @param type the type of token being added from {@link jlox.Token.TokenType}
     */
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
