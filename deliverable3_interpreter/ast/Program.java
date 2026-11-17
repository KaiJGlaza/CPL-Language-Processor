package ast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import parser.Parser;
import scanner.SclScanner;
import scanner.Token;
import scanner.TokenType;
import runtime.Interpreter;
import runtime.Memory;
import runtime.Output;
import runtime.RuntimeError;
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
        // 1. Read the file as UTF-8. Anything unreadable gets a message and null.
        String text;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (NoSuchFileException e) {
            System.out.println("Error: no such file: '" + path + "'");
            return null;
        } catch (CharacterCodingException e) {
            System.out.println("Error: '" + path + "' is not a readable text file (UTF-8 expected)");
            return null;
        } catch (IOException | RuntimeException e) {
            System.out.println("Error: could not read '" + path + "': " + e.getMessage());
            return null;
        }
        if (text.startsWith("\uFEFF")) text = text.substring(1);   // editors (Notepad) may add a BOM

        // 2. Scan. UNKNOWN tokens are dropped: the scanner already reported them.
        SclScanner scanner = new SclScanner();
        List<Token> tokens = new ArrayList<>();
        for (Token t : scanner.scan(text)) {
            if (t.type != TokenType.UNKNOWN) tokens.add(t);
        }

        // 3. Parse.
        Parser parser = new Parser(tokens);
        Program program = parser.begin();

        // 4. A program with any error must never run.
        int total = scanner.errors().size() + parser.errorCount();
        if (total > 0) {
            System.out.println();
            System.out.println(total + " error(s) found. Program not run.");
            return null;
        }
        return program;
    }

    /** Where program output is logged when run() is called without a path. */
    public static final String DEFAULT_LOG = "ProgramOutput.txt";

    /** Execute the program. Returns true if it finished without a runtime error. */
    public boolean run() {
        return run(DEFAULT_LOG);
    }

    /** Same, logging to the given file. Output goes to the console and the log at once. */
    public boolean run(String logPath) {
        // Every declared variable gets a slot in memory (0 / 0.0 until assigned).
        Memory memory = new Memory();
        for (Declaration d : declarations) memory.declare(d.name, d.type);

        try (Output out = new Output(logPath)) {
            Interpreter interpreter = new Interpreter(memory, out);
            try {
                interpreter.executeBlock(statements);
                return true;
            } catch (RuntimeError error) {
                out.println(error.getMessage());   // shown on the console and saved in the log
                return false;
            }
        } catch (IOException problem) {
            System.out.println("Error: could not write '" + logPath + "': " + problem.getMessage());
            return false;
        }
    }
}