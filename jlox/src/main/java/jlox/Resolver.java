package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    // Tracks whether currently inside a function or top level
    private FunctionType currentFunction = FunctionType.NONE;

    // Tracks whether currently inside a class or top level
    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE, FUNCTION, INITIALIZER, METHOD
    }

    private enum ClassType {
        NONE, CLASS
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        beginScope();
        resolve(statement.statements);
        endScope();

        return null;
    }

    @Override
    public Void visitClassStatement(Statement.Class statement) {
        // Tracks whether in a class or top-level for use of "this" keyword
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(statement.name);
        define(statement.name);

        // "this" keyword added to every class scope
        beginScope();
        scopes.peek().put("this", true);

        for (Statement.Function method: statement.methods) {
            // Functions in classes other than initializers are methods
            FunctionType declaration = FunctionType.METHOD;

            // Function defined in class will be initializer if named "init"
            if (method.name.lexeme.equals("init")) {
                declaration = FunctionType.INITIALIZER;
            }

            resolveFunction(method, declaration);
        }

        endScope();
        currentClass = enclosingClass;

        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        declare(statement.name);
        define(statement.name);

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        resolve(statement.condition);
        resolve(statement.thenStmt);

        if (statement.elseStmt != null) resolve(statement.elseStmt);
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        // Return statements are not allowed in the top level
        if (currentFunction == FunctionType.NONE) {
            Lox.error(statement.keyword, "Cannot return from top level.");
        }

        if (statement.value != null) {
            // Returning with value is not allowed in init, returning without value is allowed
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(statement.keyword, "Can't return a value from inside an initializer.");
            }

            resolve(statement.value);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        resolve(statement.condition);
        resolve(statement.body);
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        declare(statement.name);
        if (statement.init != null) {
            resolve(statement.init);
        }
        define(statement.name);

        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.Variable expression) {
        // Variable is declared (in scope) and not defined (map value is false)
        if (!scopes.isEmpty() && scopes.peek().get(expression.name.lexeme) == Boolean.FALSE) {
            Lox.error(expression.name, "Cannot read variable in its own initializer.");
        }

        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitAssignExpression(Expression.Assign expression) {
        resolve(expression.value);
        resolveLocal(expression, expression.name);
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expression) {
        resolve(expression.callee);

        for (Expression argument : expression.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGetExpression(Expression.Get expression) {
        resolve(expression.object);
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitSetExpression(Expression.Set expression) {
        resolve(expression.value);
        resolve(expression.object);
        return null;
    }

    @Override
    public Void visitThisExpression(Expression.This expression) {
        // Class type is none when not inside a class declaration
        if (currentClass == ClassType.NONE) {
            Lox.error(expression.keyword, "Can't use 'this' outside of a class.");
        }

        resolveLocal(expression, expression.keyword);
        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.expression);
        return null;
    }

    void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();

        // Variables with the same name cannot be declared multiple times
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "A variable with this name is already in scope.");
        }


        // False represents the variable is not ready to be accessed
        scope.put(name.lexeme, false);
    }

    void define(Token name) {
        if (scopes.isEmpty()) return;
        // True represents that the variable is ready to be accessed
        scopes.peek().put(name.lexeme, true);
    }

    void resolve(List<Statement> statements) {
        for (Statement statement: statements) {
            resolve(statement);
        }
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void resolveLocal(Expression expression, Token name) {
        // Start at innermost scope and work outwards
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Statement.Function statement, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param: statement.params) {
            declare(param);
            define(param);
        }

        resolve(statement.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }
}
