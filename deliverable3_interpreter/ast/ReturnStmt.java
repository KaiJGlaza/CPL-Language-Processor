package ast;

import runtime.Interpreter;

/** return <expression> */
public class ReturnStmt extends Statement {
    public final Expr expr;
    public ReturnStmt(Expr expr, int line) { super(line); this.expr = expr; }

    @Override
    public void execute(Interpreter in) {
        // TODO: evaluate expr, in.setReturnValue(...), in.halt()
        // TODO: decide whether the value's type must match the function's declared return type, and what (if anything) gets printed
        throw new UnsupportedOperationException("ReturnStmt.execute");
    }
}
