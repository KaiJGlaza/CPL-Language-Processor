package ast;

import runtime.Interpreter;
import runtime.ValueType;
import runtime.Value;
import runtime.RuntimeError;

/** input <prompt string> , <name> */
public class InputStmt extends Statement {
    public final String prompt, name;
    public InputStmt(String prompt, String name, int line) { super(line); this.prompt = prompt; this.name = name; }

    @Override
    public void execute(Interpreter in) {
        in.out().println(prompt);
        String text = in.readLine();
        if (text == null) {
            throw new RuntimeError("no input available for '" + name + "'", line);
        }
        text = text.trim();

        // Parse as the variable's own type. Bad input is reported and stops the program.
        Value value;
        if (in.memory().typeOf(name) == ValueType.INTEGER) {
            if (!text.matches("[+-]?[0-9]+")) {
                throw new RuntimeError("invalid input '" + text + "' for integer variable '" + name + "'", line);
            }
            try {
                value = Value.of(Long.parseLong(text));
            } catch (NumberFormatException tooBig) {
                throw new RuntimeError("input '" + text + "' is too large for integer variable '" + name + "'", line);
            }
        } else {
            if (!text.matches("[+-]?[0-9]+(\\.[0-9]+)?")) {
                throw new RuntimeError("invalid input '" + text + "' for real variable '" + name + "'", line);
            }
            value = Value.of(Double.parseDouble(text));   // "5" typed into a real is fine: 5.0
        }
        in.memory().set(name, value, line);
    }
}