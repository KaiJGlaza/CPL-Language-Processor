package ast;

import runtime.Interpreter;

/** set <name> = <expression> */
public class SetStmt extends Statement {
    public final String name;
    public final Expr expr;
    public SetStmt(String name, Expr expr, int line) { super(line); this.name = name; this.expr = expr; }

    @Override
    public void execute(Interpreter in) {
        // TODO: v = expr.eval(in.memory()); in.memory().set(name, v, line)
        throw new UnsupportedOperationException("SetStmt.execute");
    }
}
