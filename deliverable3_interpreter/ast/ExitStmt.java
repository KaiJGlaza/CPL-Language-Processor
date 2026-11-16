package ast;

import runtime.Interpreter;

/** exit */
public class ExitStmt extends Statement {
    public ExitStmt(int line) { super(line); }

    @Override
    public void execute(Interpreter in) {
        // TODO: in.halt()
        throw new UnsupportedOperationException("ExitStmt.execute");
    }
}
