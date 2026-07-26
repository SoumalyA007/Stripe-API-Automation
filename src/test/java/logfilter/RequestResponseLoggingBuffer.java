package logfilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread-local buffer that accumulates formatted request/response log lines
 * for the currently-executing test. On test failure the listener drains this
 * buffer into the Log4j file. On test success it is simply cleared.
 *
 * <p>Designed for multi-threaded parallel test runs: each thread owns its own
 * independent list, so there is no cross-test contamination.</p>
 */
public final class RequestResponseLoggingBuffer {

    private RequestResponseLoggingBuffer() { /* utility class */ }

    private static final ThreadLocal<List<String>> BUFFER =
            ThreadLocal.withInitial(ArrayList::new);

    /** Append a pre-formatted log line to the current thread's buffer. */
    public static void append(String line) {
        BUFFER.get().add(line);
    }

    /**
     * Drain and return all buffered lines for the current thread, then clear
     * the buffer so the next test starts clean.
     */
    public static List<String> drainAndClear() {
        List<String> snapshot = new ArrayList<>(BUFFER.get());
        BUFFER.get().clear();
        return snapshot;
    }

    /** Discard all buffered lines without returning them (used on test pass). */
    public static void clear() {
        BUFFER.get().clear();
    }
}
