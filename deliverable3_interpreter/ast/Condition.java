package ast;

import runtime.Memory;
import runtime.RuntimeError;
import runtime.Value;
import runtime.ValueType;

/** left relOp right, where relOp is ">", "<", or "==". */
public class Condition {
    public final String relOp;
    public final Expr left, right;
    public final int line;

    public Condition(Expr left, String relOp, Expr right, int line) {
        this.left = left; this.relOp = relOp; this.right = right; this.line = line;
    }

    public boolean eval(Memory m) {
        Value a = left.eval(m);
        Value b = right.eval(m);
        if (a.type() != b.type()) {
            throw new RuntimeError("cannot compare integer and real in '" + a + " " + relOp + " " + b + "'", line);
        }
        // Compare as long or double depending on the shared type.
        boolean gt, lt;
        if (a.type() == ValueType.INTEGER) { gt = a.asLong() > b.asLong(); lt = a.asLong() < b.asLong(); }
        else { gt = a.asDouble() > b.asDouble(); lt = a.asDouble() < b.asDouble(); }
        switch (relOp) {
            case ">":  return gt;
            case "<":  return lt;
            case "==": return !gt && !lt;
        }
        throw new IllegalStateException("unknown relational operator '" + relOp + "'");
    }
}