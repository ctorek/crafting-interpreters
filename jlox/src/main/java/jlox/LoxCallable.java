package jlox;

import java.util.List;

interface LoxCallable {
    /** The number of arguments expected by the function */
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
