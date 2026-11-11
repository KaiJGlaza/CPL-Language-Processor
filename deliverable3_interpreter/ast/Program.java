package ast;

import java.util.List;
import runtime.ValueType;

/** A parsed SCL program, plus load() and run() as the course guidelines describe. */
public class Program {
    public final String importName;
    public final ValueType returnType;
    public final List<Declaration> declarations;
    public final List<Statement> statements;

    public Program(String importName, ValueType returnType,
                   List<Declaration> declarations, List<Statement> statements) {
        this.importName = importName;
        this.returnType = returnType;
        this.declarations = declarations;
        this.statements = statements;
    }

    /**
     * Read the file, scan, parse. Return the Program, or null if the scanner or parser reported any error
     * (a program with errors must never run).
     */
    public static Program load(String path) {
        // TODO: read the file as UTF-8 (missing/unreadable file -> message, return null)
        // TODO: tokens = new SclScanner().scan(text); drop UNKNOWN tokens (the scanner already reported them)
        // TODO: parser = new Parser(tokens); program = parser.begin()
        // TODO: return null if scanner.errors() or parser.errorCount() is non-zero
        throw new UnsupportedOperationException("Program.load");
    }

    /** Execute the program. Returns true if it finished without a runtime error. */
    public boolean run() {
        // TODO: Memory m = new Memory(); declare every variable from declarations
        // TODO: open Output (log file), build Interpreter, in.executeBlock(statements)
        // TODO: catch RuntimeError -> print its message, return false
        // TODO: close the Output
        throw new UnsupportedOperationException("Program.run");
    }
}
