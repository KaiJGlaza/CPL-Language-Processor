package ast;

import runtime.Interpreter;

/** return <expression> */
public class ReturnStmt extends Statement {
    public final Expr expr;
    public ReturnStmt(Expr expr, int line) { super(line); this.expr = expr; }

    @Override
    public void execute(Interpreter in) {
        // Default: evaluate, remember the value, stop. Nothing is printed and the type is not
        // checked against the declared return type.
        in.setReturnValue(expr.eval(in.memory()));
        in.halt();
    }
}