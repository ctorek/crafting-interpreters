package jlox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    // Enclosing environment used for nesting
    final Environment enclosing;

    // Hashmap of all variables present
    private Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    /**
     * Define a variable.
     * @param name the name of the variable being defined
     * @param value the value being assigned
     */
    void define(String name, Object value) {
        values.put(name, value);
    }

    /**
     * Retrieve an enclosing environment.
     * @param distance the distance from the current environment. Current environment is 0, enclosing is 1, etc
     * @return the environment at the specified distance
     */
    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    /**
     * Retrieve a variable from a specific enclosing environment rather than dynamically searching.
     * @param distance the distance from the current environment. Current environment is 0, enclosing is 1, etc
     * @param name the name of the variable being searched for
     * @return the value of the variable
     */
    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    /**
     * Assign a variable in a specific enclosing environment rather than the current environment.
     * @param distance the distance from the current environment. Current environment is 0, enclosing is 1, etc
     * @param name the name of the variable being assigned to
     * @param value the value of the variable
     */
    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    /**
     * Assign to a variable that already exists in the environment.
     * Throws a {@link RuntimeError} if a variable by that name does not exist.
     * @param name the tokenized name of the variable that is being assigned to
     * @param value the value being assigned to the variable
     */
    void assign(Token name, Object value) {
        // Assign the value if it is found
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        // Check enclosing scopes for variable
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        // Throw an error if the variable is not found
        throw new RuntimeError(name, "Undefined variable " + name.lexeme + ".");
    }

    /**
     * Retrieve a defined variable from the environment. <br>
     * Throws a {@link RuntimeError} if the variable is not found.
     * @param name the token that contains the name of the variable
     * @return the value of the variable if found
     */
    Object get(Token name) {
        // Return the value if it is found
        if(values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // Check enclosing scopes for value
        if (enclosing != null) return enclosing.get(name);

        // Throw an error if the variable is not found
        throw new RuntimeError(name, "Undefined variable " + name.lexeme + ".");
    }
}
