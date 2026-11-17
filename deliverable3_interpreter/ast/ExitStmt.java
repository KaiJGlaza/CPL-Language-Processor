package ast;

import runtime.Interpreter;

/** exit */
public class ExitStmt extends Statement {
    public ExitStmt(int line) { super(line); }

    @Override
    public void execute(Interpreter in) {
        in.halt();   // executeBlock stops at every level, even from inside a nested if
    }
}