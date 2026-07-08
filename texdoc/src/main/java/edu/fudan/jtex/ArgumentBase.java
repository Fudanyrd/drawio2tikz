package edu.fudan.jtex;

/**
 * Base class for all LaTex arguments.
 */
public abstract class ArgumentBase {
    public NodeBase inner;

    public abstract String before();
    public abstract String after();

    public void appendTo(FormatterInterface formatter) {
        formatter.autoBreakOff(); /* [ or { shall not be in a new line. */
        formatter.append(before());
        formatter.autoBreakOn();
        inner.appendTo(formatter);
        formatter.append(after());
    }
}
