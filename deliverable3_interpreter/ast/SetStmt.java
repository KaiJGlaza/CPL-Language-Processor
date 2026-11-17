package ast;

import runtime.Interpreter;
import runtime.Value;

/** set <name> = <expression> */
public class SetStmt extends Statement {
    public final String name;
    public final Expr expr;
    public SetStmt(String name, Expr expr, int line) { super(line); this.name = name; this.expr = expr; }

    @Override
    public void execute(Interpreter in) {
        Value v = expr.eval(in.memory());
        in.memory().set(name, v, line);   // RuntimeError if the type does not match the variable
    }
}