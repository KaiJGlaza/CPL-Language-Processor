package ast;

import runtime.Interpreter;

/** Base class for all statements. */
public abstract class Statement {
    public final int line;
    protected Statement(int line) { this.line = line; }

    /** Execute this statement. Throw RuntimeError on a runtime problem. */
    public abstract void execute(Interpreter in);
}
