import ast.Program;

/**
 * Usage: java Main <program.scl>
 * Exit codes: 0 ok, 1 syntax or runtime errors, 2 usage or file problem, 3 not implemented yet.
 */
public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Main <program.scl>");
            System.exit(2);
        }
        try {
            Program program = Program.load(args[0]);
            if (program == null) System.exit(1);          // syntax errors: do not run
            boolean ok = program.run();
            System.exit(ok ? 0 : 1);
        } catch (UnsupportedOperationException todo) {
            System.out.println("Not implemented yet: " + todo.getMessage());
            System.exit(3);
        }
    }
}
