package runtime;

import java.util.HashMap;
import java.util.Map;

/** Program memory: one slot per declared variable. */
public class Memory {
    private final Map<String, ValueType> types = new HashMap<>();
    private final Map<String, Value> values = new HashMap<>();

    /** Declare a variable. Per the docs, every defined variable gets a slot; it starts at 0 (0.0 for real). */
    public void declare(String name, ValueType type) {
        types.put(name, type);
        values.put(name, type == ValueType.INTEGER ? Value.of(0L) : Value.of(0.0));
    }
    public boolean isDeclared(String name) { return types.containsKey(name); }
    public ValueType typeOf(String name) { return types.get(name); }

    /** Read a variable. */
    public Value get(String name, int line) {
        Value v = values.get(name);
        if (v == null) throw new RuntimeError("variable '" + name + "' is not declared", line);
        return v;
    }

    /** Store a variable. */
    public void set(String name, Value v, int line) {
        ValueType declared = types.get(name);
        if (declared == null) throw new RuntimeError("variable '" + name + "' is not declared", line);
        if (v.type() != declared) {
            throw new RuntimeError("cannot assign " + typeName(v.type()) + " value to "
                    + typeName(declared) + " variable '" + name + "'", line);
        }
        values.put(name, v);
    }

    private static String typeName(ValueType t) { return t == ValueType.INTEGER ? "integer" : "real"; }
}