package jlox;

import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {
    // Defines and retrieves variables
    private Environment environment = new Environment();

    void interpret(List<Statement> statements) {
        try {
            for (Statement statement: statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.thenStmt);
        } else if (statement.elseStmt != null) {
            execute(statement.elseStmt);
        }

        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        // Check for value of variable
        Object value = null;
        if (statement.init != null) {
            // Evaluate the initializer if present
            value = evaluate(statement.init);
        }

        // Define variable
        environment.define(statement.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        while (isTruthy(evaluate(statement.condition))) {
            execute(statement.body);
        }

        return null;
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement) {
        evaluate(statement.expression);

        // Null return is necessary because capital-V void
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));

        // Null return is necessary because capital-V void
        return null;
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expression) {
        Object value = evaluate(expression.value);
        environment.assign(expression.name, value);

        return value;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return environment.get(expression.name);
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        Object left = evaluate(expression.left);

        if (expression.operator.type == Token.TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expression.right);
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        // Evaluate the right expression of the unary expression
        Object right = evaluate(expression.expression);

        // Apply the operator to the evaluated expression
        switch (expression.operator.type) {
            // Unary exclamation mark takes the inverse of a boolean
            case BANG: return !isTruthy(right);
            // Unary minus negates value of a number
            case MINUS:
                checkNumberOperand(expression.operator, right);
                return -(double) right;
        }

        return null;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression)  {
        // Evaluate the left and right expressions of the binary expression
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);

        switch (expression.operator.type) {
            /* Arithmetic operators */
            // All arithmetic in Lox is performed as doubles
            case MINUS:
                checkNumberOperands(expression.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expression.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expression.operator, left, right);

                // Check for division by 0
                if ((double) right == 0) throw new RuntimeError(expression.operator, "Cannot divide by 0");

                return (double) left / (double) right;

            // Plus can represent string concatenation or addition
            case PLUS:
                // Mathematical addition
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                // String concatenation
                if (left instanceof String || right instanceof String) {
                    // Allows concatenation with objects other than strings when either one is string
                    return stringify(left) + stringify(right);
                }

                // If both operands do not match as numbers or strings, throw an error
                throw new RuntimeError(expression.operator, "Operands must be two numbers or two strings");

            /* Comparison operators */
            case GREATER:
                checkNumberOperands(expression.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expression.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double) left <= (double) right;

            /* Equality operators */
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }

        return null;
    }

    /**
     * Converts an object to a string, with special cases for handling Lox's nil and numbers with no decimals.
     * @param object the object being converted
     * @return a String representing the object
     */
    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();

            // If the number is an integer, display without the decimal
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return object.toString();
    }

    /**
     * Check that an operand is a number when being used on a number operator. <br>
     * If the types do not match, throws a {@link RuntimeError}.
     * @param operator the operator being applied
     * @param operand the operand being checked
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    /**
     * Check that both operands are numbers when being used on a binary number operator. <br>
     * If the types do not match, throws a {@link RuntimeError}.
     * @param operator the operator being applied
     * @param left the operand to the left of the operator
     * @param right the operand to the right of the operator
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Both operands must be numbers");
    }

    /**
     * Determine whether two objects are equal.
     * @param a the first object
     * @param b the second object
     * @return true if both null or their equals methods returns true, false otherwise
     */
    private boolean isEqual(Object a, Object b) {
        // If both are null they are equal to each other
        if (a == null && b == null) return true;

        // Only check if a is null because it has the equals method performed on it
        if (a == null) return false;

        return a.equals(b);
    }

    /**
     * Returns whether an object is considered true when evaluated as a Boolean. <br>
     * Null is the only inherently false value other than false.
     * @param object the object being evaluated
     * @return false if the object is null or a false Boolean, true otherwise
     */
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    /**
     * Evaluate the value of a Lox expression.
     * @param expression the expression to evaluate
     * @return the object it evaluates to
     */
    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    /**
     * Execute a Lox statement.
     * @param statement the statement to execute
     */
    private void execute(Statement statement) {
        statement.accept(this);
    }

    private void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;

        try {
            this.environment = environment;

            for (Statement statement: statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

}
