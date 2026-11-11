package ast;

import runtime.Memory;
import runtime.Value;

/** Base class for expressions. */
public abstract class Expr {
    public final int line;
    protected Expr(int line) { this.line = line; }

    public abstract Value eval(Memory m);
}
