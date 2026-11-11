package ast;

import runtime.Memory;

/** left relOp right, where relOp is ">", "<", or "==". */
public class Condition {
    public final String relOp;
    public final Expr left, right;
    public final int line;

    public Condition(Expr left, String relOp, Expr right, int line) {
        this.left = left; this.relOp = relOp; this.right = right; this.line = line;
    }

    public boolean eval(Memory m) {
        // TODO: evaluate both sides; RuntimeError if types differ; compare by relOp
        throw new UnsupportedOperationException("Condition.eval");
    }
}
