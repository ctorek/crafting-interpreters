package jlox;

public class Interpreter implements Expression.Visitor<Object> {
    void interpret(Expression expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
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
                return (double) left / (double) right;

            // Plus can represent string concatenation or addition
            case PLUS:
                // Mathematical addition
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                // String concatenation
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
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

}
