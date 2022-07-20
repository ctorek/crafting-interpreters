package jlox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    final LoxClass superclass;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    /**
     * Find a method of a class by its name.
     * @param name the name of the method
     * @return the {@link LoxFunction} if it is found, otherwise null
     */
    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        // Check if method is inherited
        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);

        // Check for user-defined initializer
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public int arity() {
        // Default constructors will have an arity of 0
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;

        // User defined constructors can have a custom arity
        return initializer.arity();
    }

    @Override
    public String toString() {
        return name;
    }
}
