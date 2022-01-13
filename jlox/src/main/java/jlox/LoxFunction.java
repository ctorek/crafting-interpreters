package jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Statement.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Statement.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    /**
     * Bind a method to an instance of a class so that it can access "this".
     * @param instance the instance being bound to
     * @return a LoxFunction bound to that instance
     */
    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // Global environment is inherited with function arguments
        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); i++) {
            // Match each parameter with corresponding argument and define in environment
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        // Exit the execution upon a return statement
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            // Return without a return value is allowed in initializers
            if (isInitializer) return closure.getAt(0, "this");

            return returnValue.value;
        }

        // Initializer functions always return the instance of the class
        if (isInitializer) return closure.getAt(0, "this");

        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<function " + declaration.name.lexeme + ">";
    }
}
