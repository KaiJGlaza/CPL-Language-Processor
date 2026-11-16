package ast;

import runtime.Memory;
import runtime.Value;

/** A variable used in an expression. */
public class VarRef extends Expr {
    public final String name;
    public VarRef(String name, int line) { super(line); this.name = name; }

    @Override
    public Value eval(Memory m) {
        // TODO: return m.get(name, line)
        throw new UnsupportedOperationException("VarRef.eval");
    }
}
