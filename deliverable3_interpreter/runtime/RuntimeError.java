package runtime;

/** A runtime error. Thrown by evaluation/execution, caught once in Program.run(). */
public class RuntimeError extends RuntimeException {
    public final int line;

    public RuntimeError(String message, int line) {
        super("Runtime error (line " + line + "): " + message);
        this.line = line;
    }
}
