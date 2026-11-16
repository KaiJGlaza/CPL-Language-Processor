package ast;

import runtime.Memory;
import runtime.Value;

/** left op right, for + - * /. */
public class BinaryExpr extends Expr {
    public final char op;
    public final Expr left, right;

    public BinaryExpr(char op, Expr left, Expr right, int line) {
        super(line);
        this.op = op; this.left = left; this.right = right;
    }

    @Override
    public Value eval(Memory m) {
        // TODO: evaluate both sides
        // TODO: RuntimeError if the two sides are different types (no promotion)
        // TODO: + - * on long or double; '/' follows the integer-division decision
        // TODO: RuntimeError on division by zero
        throw new UnsupportedOperationException("BinaryExpr.eval");
    }
}
