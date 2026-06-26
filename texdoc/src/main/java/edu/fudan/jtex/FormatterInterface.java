package edu.fudan.jtex;

public interface FormatterInterface {
    public void finish();
    public FormatterInterface appendWhitespaces(String text);
    public FormatterInterface appendNewLineImpl(boolean forceEmptyLine, boolean allowAdjacentEmptyLines);
    public FormatterInterface append(String text);
    public FormatterInterface appendNewLine(boolean forceEmptyLine);

    public void autoBreakOn();
    public void autoBreakOff();
    public boolean autoBreakEnabled();

    /**
     * Indentation level increment.
     */
    public void enter();
    public void leave();
}
