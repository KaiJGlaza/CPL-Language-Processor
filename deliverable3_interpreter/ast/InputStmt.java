package ast;

import runtime.Interpreter;

/** input <prompt string> , <name> */
public class InputStmt extends Statement {
    public final String prompt, name;
    public InputStmt(String prompt, String name, int line) { super(line); this.prompt = prompt; this.name = name; }

    @Override
    public void execute(Interpreter in) {
        // TODO: print the prompt, text = in.readLine()
        // TODO: parse as the variable's type (long or double); not numeric, or a real typed into an integer -> RuntimeError (report and stop)
        // TODO: in.memory().set(name, value, line)
        throw new UnsupportedOperationException("InputStmt.execute");
    }
}
