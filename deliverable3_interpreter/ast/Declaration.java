package ast;

import runtime.ValueType;

/** define <name> of type <type> */
public class Declaration {
    public final String name;
    public final ValueType type;
    public final int line;
    public Declaration(String name, ValueType type, int line) { this.name = name; this.type = type; this.line = line; }
}
