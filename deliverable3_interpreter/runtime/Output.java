package runtime;

import java.io.IOException;
import java.io.PrintWriter;

/** Prints to the console and a log file at once. */
public class Output implements AutoCloseable {
    private final PrintWriter log;

    public Output(String logPath) throws IOException {
        this.log = new PrintWriter(logPath, "UTF-8");
    }

    public void println(String s) {
        System.out.println(s);
        log.println(s);
    }

    @Override
    public void close() { log.close(); }
}
