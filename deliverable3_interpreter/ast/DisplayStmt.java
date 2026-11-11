package ast;

import java.util.List;
import runtime.Interpreter;

/** display <item> { , <item> } where an item is a string literal or an expression. */
public class DisplayStmt extends Statement {
    /** Exactly one of text / expr is non-null. */
    public static class Item {
        public final String text;
        public final Expr expr;
        public Item(String text, Expr expr) { this.text = text; this.expr = expr; }
    }

    public final List<Item> items;
    public DisplayStmt(List<Item> items, int line) { super(line); this.items = items; }

    @Override
    public void execute(Interpreter in) {
        // TODO: build the output text from items (separator and real formatting: team decision)
        // TODO: print through in.out().println(...)
        throw new UnsupportedOperationException("DisplayStmt.execute");
    }
}
