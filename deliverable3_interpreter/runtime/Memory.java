package runtime;

import java.util.HashMap;
import java.util.Map;

/** Program memory: one slot per declared variable. */
public class Memory {
    private final Map<String, ValueType> types = new HashMap<>();
    private final Map<String, Value> values = new HashMap<>();

    public void declare(String name, ValueType type) { types.put(name, type); }
    public boolean isDeclared(String name) { return types.containsKey(name); }
    public ValueType typeOf(String name) { return types.get(name); }

    /** Read a variable. */
    public Value get(String name, int line) {
        // TODO: RuntimeError if the variable was never assigned (pending decision: error or default 0)
        throw new UnsupportedOperationException("Memory.get");
    }

    /** Store a variable. */
    public void set(String name, Value v, int line) {
        // TODO: RuntimeError if v.type() != typeOf(name) (no promotion between integer and real)
        throw new UnsupportedOperationException("Memory.set");
    }
}
