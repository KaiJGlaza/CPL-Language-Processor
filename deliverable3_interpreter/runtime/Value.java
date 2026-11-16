package runtime;

/** A runtime value: either an integer (long) or a real (double). No promotion between them. */
public class Value {
    private final ValueType type;
    private final long l;
    private final double d;

    private Value(ValueType type, long l, double d) { this.type = type; this.l = l; this.d = d; }

    public static Value of(long v) { return new Value(ValueType.INTEGER, v, 0); }
    public static Value of(double v) { return new Value(ValueType.REAL, 0, v); }

    public ValueType type() { return type; }
    public long asLong() { return l; }
    public double asDouble() { return d; }

    @Override
    public String toString() {
        // TODO: decide real formatting (45.95, not 4.595E1)
        return type == ValueType.INTEGER ? Long.toString(l) : Double.toString(d);
    }
}
