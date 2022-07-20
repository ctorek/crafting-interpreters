package jlox;

import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jlox.Token.TokenType.*;

/**
 *
 */
public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        // Advance while next token is not a closing brace or end of file
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        // Right brace is required to end block statement
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError err) {
            synchronize();
            return null;
        }
    }

    private Statement classDeclaration() {
        // Class names are required
        Token name = consume(IDENTIFIER, "Expect class name.");

        // Superclass is optional and specified with a less than
        Expression.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expression.Variable(previous());
        }

        // Braces are required around class body
        consume(LEFT_BRACE, "Expect '{' before class body.");

        List<Statement.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        // Braces are required around class body
        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Statement.Class(name, superclass, methods);
    }

    private Statement varDeclaration() {
        // Name must be present in a variable declaration
        Token name = consume(IDENTIFIER, "Expect variable name.");

        // Initialize the variable if a value is provided
        Expression init = null;
        if (match(EQUAL)) {
            init = expression();
        }

        // Semicolon required after variable declaration
        consume(SEMICOLON, "Expect ';' after variable declaration.");

        // Create a new variable declaration
        return new Statement.Var(name, init);
    }

    private Statement whileStatement() {
        // Condition should be contained in parentheses
        consume(LEFT_PAREN, "Expect '(' before condition.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");

        Statement body = statement();

        return new Statement.While(condition, body);
    }

    private Statement statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Statement.Block(block());

        return expressionStatement();
    }

    private Statement forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        // Initializer is first part of for loop header
        Statement initializer;
        if (match(SEMICOLON)) {
            // Initializer is not required to be present
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        // Expression is second part of for loop header
        Expression condition = null;
        if (!check(SEMICOLON)) {
            // Condition is also not required
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        // Increment is third part of for loop header
        Expression increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expect ')' after for loop header.");

        Statement body = statement();

        // Include increment in body if present
        if (increment != null) {
            body = new Statement.Block(
                    Arrays.asList(body, new Statement.Expr(increment))
            );
        }

        // Condition should always be true if not present
        if (condition == null) condition = new Expression.Literal(true);
        body = new Statement.While(condition, body);

        // If an initializer is present, run once before looping
        if (initializer != null) {
            body = new Statement.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private Statement ifStatement() {
        // Condition should be contained in parentheses
        consume(LEFT_PAREN, "Expect '(' before condition.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");

        Statement thenStmt = statement();

        // Else is optional
        Statement elseStmt = null;
        if (match(ELSE)) elseStmt = statement();

        return new Statement.If(condition, thenStmt, elseStmt);
    }

    private Statement printStatement() {
        Expression expression = expression();
        // Semicolon is required or error will be thrown
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Print(expression);
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expression value = null;

        // Return value is present if the return statement is not followed by a semicolon
        if (!check(SEMICOLON)) {
            value = expression();
        }

        // Semicolon is required after return statement
        consume(SEMICOLON, "Expect ';' after return value.");

        return new Statement.Return(keyword, value);
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        // Semicolon is required or error will be thrown
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expr(expression);
    }

    private Statement.Function function(String kind) {
        // Name of function is required in function definition
        Token name = consume(IDENTIFIER, "Expect " + kind + " name.");

        // Parentheses are required in function definition
        consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");

        List<Token> parameters = new ArrayList<>();

        // Only run if the next token is a right parenthesis, meaning no parameters in definition
        if (!check(RIGHT_PAREN)) {
            // If the next token is not a right parenthesis, at least one parameter must be present
            do {
                // Max number of parameters is 255
                if (parameters.size() >= 255) {
                    error(peek(), "Max number of parameters is 255.");
                }

                // Name of parameter is required
                parameters.add(consume(IDENTIFIER, "Expect parameter name."));
            } while (match(COMMA));
        }

        // Parentheses are required in function definition
        consume(RIGHT_PAREN, "Expect ')' after parameters.");

        // Braces are required around function body
        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Statement> body = block();

        return new Statement.Function(name, parameters, body);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expression).name;
                return new Expression.Assign(name, value);
            } else if (expression instanceof Expression.Get) {
                // Setters are only called on properties
                Expression.Get get = (Expression.Get) expression;
                return new Expression.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private Expression or() {
        Expression expression = and();

        while (match(OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();

        while (match(AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression equality() {
        // First token of the equality expression
        Expression expression = comparison();

        // Continue while the current token is not an equality operator
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression comparison() {
        // First token of the comparison expression
        Expression expression = term();

        // Continue while the current token is not a comparison operator
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term() {
        // First token of the term expression
        Expression expression = factor();

        // Continue while the current token is not a term operator
        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        // First token of the factor expression
        Expression expression = unary();

        // Continue while the current token is not a factor operator
        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        // Return a unary expression if a prefix operator is matched
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expression expression = unary();
            return new Expression.Unary(operator, expression);
        }

        // Otherwise return a function call expression
        return call();
    }

    private Expression call() {
        // Object that function is being called on
        Expression expression = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                // When left parenthesis is found, parse arguments and create call expression
                expression = finishCall(expression);
            } else if (match(DOT)) {
                // Name is required when accessing property of class
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expression = new Expression.Get(expression, name);
            } else {
                break;
            }
        }

        return expression;
    }

    private Expression finishCall(Expression callee) {
        List<Expression> arguments = new ArrayList<>();

        // Check for arguments if the right parenthesis is not the next token
        if (!check(RIGHT_PAREN)) {
            // If the right parenthesis is not the next token, there must be at least one argument
            do {
                // Max number of arguments is 255, or 254 with implicit 'this'
                if (arguments.size() >= 255) {
                    error(peek(), "Max number of arguments is 255.");
                }
                // Parse an expression for the arguments
                arguments.add(expression());
            } while (match(COMMA));
        }

        // Right parenthesis required after arguments
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
    }

    /**
     * Parses primary expressions into a literal expression.
     * @return the parsed {@link Expression.Literal}
     */
    private Expression primary() {
        // True, false, and nil represent literal expressions with their corresponding values in Java
        if (match(FALSE)) return new Expression.Literal(false);
        if (match(TRUE)) return new Expression.Literal(true);
        if (match(NIL)) return new Expression.Literal(null);

        // Number or string literals are literal expressions of their value
        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expect '.' after super.");
            Token method = consume(IDENTIFIER, "Expect superclass method name.");

            return new Expression.Super(keyword, method);
        }

        // "this" keyword is used in classes
        if (match(THIS)) return new Expression.This(previous());

        // Match identifiers to values of variables
        if (match(IDENTIFIER)) {
            return new Expression.Variable(previous());
        }

        // Match a left parenthesis with a right parenthesis to form a grouping expression
        if (match(LEFT_PAREN)) {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        // Throw an error if an expression is not found
        throw error(peek(), "Expect expression.");
    }

    /**
     * Will check the next token for a specific type and throw an error if it is not found.
     * @param type the type of token that must be identified to proceed without error
     * @param message the error message thrown if the types do not match
     */
    private Token consume(Token.TokenType type, String message) {
        // Do not throw an error if the correct token is found and consumed
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * Reports an error in the Lox interpreter and creates a new ParseError.
     * @param token the token that causes the error
     * @param message the error message
     * @return a new {@link ParseError}
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Discards tokens and advances until it has found the boundary of the next statement.
     */
    private void synchronize() {
        advance();

        // Continue advancing until the next statement or end of file is found
        while (!isAtEnd()) {
            // Semicolon signifies the end of a statement
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    /**
     * Advances if the next token is equal one of the tokens passed in.
     * @param types the tokens being checked for
     * @return true if at least one match is found, false otherwise
     */
    private boolean match(Token.TokenType... types) {
        for (Token.TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Peek the next token without advancing and check that it is the same type as the parameter.
     * @param type the token being checked
     * @return true if it has the same type as the next token, false otherwise
     */
    private boolean check(Token.TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * Return the current index of the token list and move the index forwards by one.
     * @return the token at the index before advancing
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * Retrieve the token at the current index of the list without advancing and consuming the token.
     * @return the token at the current index
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Retrieve the token at the previous index of the list without moving the current index.
     * @return the token at the index before the current index
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * Checks whether the current index is greater than the length of the list of tokens.
     * @return true if the current index is at the end of file, false otherwise
     */
    private boolean isAtEnd() {
        return peek().type == EOF;
    }
}
