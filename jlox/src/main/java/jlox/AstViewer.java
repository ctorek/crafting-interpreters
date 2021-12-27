package jlox;

/**
 * Class for printing expressions in Lisp notation.
 */
public class AstViewer implements Expression.Visitor<String> {
    /**
     * Create a string representation an expression in Lisp notation.
     * @param expression the expression
     * @return a string representing the expression
     */
    String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitLogicalExpression(Expression.Logical expression) {
        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
    }

    @Override
    public String visitAssignExpression(Expression.Assign expression) {
        return parenthesize("set", expression, expression.value);
    }

    @Override
    public String visitVariableExpression(Expression.Variable expression) {
        return expression.name.lexeme;
    }

    @Override
    public String visitBinaryExpression(Expression.Binary expression) {
        // Binary expressions are represented by a left expression, an operator, and a right expression
        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expression) {
        return parenthesize("group", expression.expression);
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        if (expression.value == null) return "nil";
        return expression.value.toString();
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parenthesize(expression.operator.lexeme, expression.expression);
    }

    /**
     * Wraps an expression in parentheses using Lisp notation.
     * @param name the name of the expression or operation performed on it
     * @param expressions the contents of the expression
     * @return a string representing the expression
     */
    private String parenthesize(String name, Expression... expressions) {
        StringBuilder builder = new StringBuilder();

        // Operator or type of expression
        builder.append("(").append(name);

        // Contents of expression
        for (Expression expression: expressions) {
            builder.append(" ");
            builder.append(expression.accept(this));
        }

        builder.append(")");
        return builder.toString();
    }
}
