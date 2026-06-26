package edu.fudan.jtex;

/**
 * The {@code CompactFormatter} class is a simple implementation of the
 * {@link FormatterInterface} that appends text to a {@link StringBuilder}
 * without any formatting or line breaks. It is used for debugging and
 * logging purposes, where the output needs to be compact and unformatted.
 */
public class CompactFormatter implements FormatterInterface {
    private StringBuilder sb;

    public CompactFormatter(StringBuilder sb) { this.sb = sb; }

    @Override
    public void finish() {}

    @Override
    public FormatterInterface appendWhitespaces(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public FormatterInterface appendNewLineImpl(boolean forceEmptyLine, boolean allowAdjacentEmptyLines) {
        sb.append("\n");
        return this;
    }

    @Override
    public FormatterInterface append(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public FormatterInterface appendNewLine(boolean forceEmptyLine) {
        return appendNewLineImpl(forceEmptyLine, true);
    }

    @Override
    public void autoBreakOn() {}

    @Override
    public void autoBreakOff() {}

    @Override
    public boolean autoBreakEnabled() {
        return false;
    }

    @Override
    public void enter() {}

    @Override
    public void leave() {}
}
