package jlox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private LoxClass loxClass;
    private final Map<String, Object> fields = new HashMap<>();

    LoxInstance(LoxClass loxClass)  {
        this.loxClass = loxClass;
    }

    /**
     * Retrieve a property or method of an instance of a class. <br>
     * Throws a {@link RuntimeError} if the property is not defined.
     * @param name the name of the property being accessed.
     * @return the property, if present
     */
    Object get(Token name) {
        // Find in fields
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        // Find in methods
        LoxFunction method = loxClass.findMethod(name.lexeme);
        // Bind function so that it can access its class instance
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property " + name.lexeme + ".");
    }

    /**
     * Set a property of an instance of a class to some value.
     * @param name the property being set
     * @param value the value being set to
     */
    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return loxClass.name + " instance";
    }
}
