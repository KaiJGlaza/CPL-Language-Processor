package runtime;

import ast.Statement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/** Executor state plus the statement loop. Statements call back into this. */
public class Interpreter {
    private final Memory memory;
    private final Output out;
    private final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
    private boolean halted = false;
    private Value returnValue = null;

    public Interpreter(Memory memory, Output out) {
        this.memory = memory;
        this.out = out;
    }

    public Memory memory() { return memory; }
    public Output out() { return out; }
    public boolean isHalted() { return halted; }
    public void halt() { halted = true; }
    public Value returnValue() { return returnValue; }
    public void setReturnValue(Value v) { returnValue = v; }

    /**
     * Read one line of user input. Returns null at end of input (or if reading fails).
     * Decision: end of input is a run-time error for the input statement. InputStmt turns the
     * null into "Runtime error (line N): no input available for 'x'", which stops the program.
     */
    public String readLine() {
        try { return stdin.readLine(); } catch (IOException e) { return null; }
    }

    /** Run statements in order, stopping as soon as halted is set (exit/return, even from a nested if). */
    public void executeBlock(List<Statement> stmts) {
        for (Statement s : stmts) {
            if (halted) return;
            s.execute(this);
        }
    }
}