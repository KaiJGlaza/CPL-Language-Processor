package ast;

import runtime.Memory;
import runtime.Value;

/** An INTEGER_CONST or REAL_CONST. (Done: simplest node, so statement code can be tested first.) */
public class NumberLit extends Expr {
    private final Value value;
    public NumberLit(Value value, int line) { super(line); this.value = value; }
    @Override public Value eval(Memory m) { return value; }
}
