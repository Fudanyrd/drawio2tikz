package edu.fudan.jtex;

/**
 * Generic document formatter (more than LaTex)
 *
 * <h3>Auto-Break Mode</h3>
 * When enabled, the formatter will ensure the following:
 * <ul>
 *   <li>No line starts or ends with whitespaces;</li>
 *   <li>There's no two adjacent whitespaces, i.e. no &quote;\x20\x20&quote;;</li>
 *   <li>There's no two adjacent linebreaks (unless forced linebreak), i.e. no &quote;\n\n&quote;;</li>
 *   <li>Length of each line is (normally) less than or equal to the configured line width
 *   (long words are kept in a single line);</li>
 * </ul>
 * Disable this for stuff that needed to be kept as-is.
 *
 * <h3>Dumping Text</h3>
 * <ul>
 *   <li>Use {@link Formatter#append} for non-blank text content;</li>
 *   <li>Use {@link Formatter#appendWhitespaces} for whitespaces;</li>
 *   <li>Use {@link Formatter#appendNewLine} for a line break and starting
 *   a new line</li>
 *   <li>Invoke {@link Formatter#finish} at the end of formatting to flush
 *   the buffer and close the writer.</li>
 * </ul>
 */
public class Formatter implements FormatterInterface {
    public static abstract class LineWriter {
        public abstract void write(String line);
        public abstract void close();
    }

    public static class InMemoryLineWriter extends LineWriter {
        private StringBuilder sb = new StringBuilder();

        @Override
        public void write(String line) {
            sb.append(line);
        }

        @Override
        public void close() {
            /* do nothing */
        }

        InMemoryLineWriter(StringBuilder sb) { this.sb = sb; }
    }

    private SmallQueue<String> queue;
    private Format config;
    private LineWriter writer;
    private int column = 0;            /* current column (not including linebreak) */
    private int autoBreakOffLevel = 0; /* if > 0, auto linebreak is disabled */
    private int indentLevel = 0;       /* current indentation level */
    private static final int QUEUE_VOLUME = 3;

    public Formatter(Format config, LineWriter writer) {
        this.config = config;
        queue = new SmallQueue<String>(QUEUE_VOLUME);
        this.writer = writer;
        if (writer == null) {
            throw new IllegalArgumentException("LineWriter cannot be null");
        }
        if (config == null) {
            /* use default values. */
            this.config = new Format();
        }
        queue.push("");
    }

    private void finishCurrentLine() {
        int length = queue.size();
        if (length == QUEUE_VOLUME) {
            String line = queue.getAt(0);
            writer.write(line);
            queue.pop();
        }
        queue.push("");
        column = 0;
    }

    @Override
    public void autoBreakOff() {
        autoBreakOffLevel++;
    }

    @Override
    public void autoBreakOn() {
        if (autoBreakOffLevel > 0) {
            autoBreakOffLevel--;
        }
    }

    @Override
    public boolean autoBreakEnabled() {
        return autoBreakOffLevel == 0;
    }

    /**
     * Write buffered lines to the output and close the writer.
     * This method should be called at the end of formatting.
     */
    @Override
    public void finish() {
        if (true) {
            /**
             * Check and append "\n" to the current line if it does not
             * end with a linebreak.
             */
            String line = currentLine();
            int l = line.length();
            if (l > 0 && line.charAt(l - 1) != '\n') {
                /* can happen at the last line of the document. */
                line = removeTrailingWhitespaces(line) + "\n";
                queue.setCurrent(line);
            }
        }
        int numLines = queue.size();
        for (int i = 0; i < numLines; i++) {
            String line = queue.getAt(i);
            if (line.equals("")) {
                continue;
            }
            writer.write(line);
        }
        writer.close();
    }

    public String currentLine() {
        if (queue.size() == 0) {
            return null;
        }
        return queue.getAt(-1);
    }

    public String lastLine() {
        if (queue.size() < 2) {
            return null;
        }
        return queue.getAt(-2);
    }

    private static String removeTrailingWhitespaces(String line) {
        int length = line.length();
        int i = length - 1;
        while (i >= 0 && Character.isWhitespace(line.charAt(i))) {
            i--;
        }
        return line.substring(0, i + 1);
    }

    private static boolean isBlank(String line) {
        int length = line.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean endsWithWhitespace(String line) {
        int length = line.length();
        if (length == 0) {
            return true;
        }
        return Character.isWhitespace(line.charAt(length - 1));
    }

    private void handleLongLine(String currentLine) {
        assert this.autoBreakEnabled() : "auto-break is disabled, shouldn't be here.";
        /* create a break at the end of current line (which is non-empty) */
        if (currentLine.charAt(currentLine.length() - 1) != '\n') {
            currentLine = removeTrailingWhitespaces(currentLine);
            currentLine += "\n";
        }
        queue.setCurrent(currentLine);
        finishCurrentLine();
    }

    private String padIndent(int missing) {
        String line = currentLine();
        for (int i = 0; i < missing; i++) {
            line += " ";
            column++;
        }
        queue.setCurrent(line);
        return line;
    }

    /**
     * Append text to the current line. If the current line is full
     * and autobreak is enabled, finish it and start a new line.
     */
    @Override
    public FormatterInterface append(String text) {
        if (text.length() == 0) {
            return this;
        }
        /* comments end with line break. treat them differently. */
        int lastIsBr = text.charAt(text.length() - 1) == '\n' ? 1 : 0;

        String line = currentLine();
        if (!autoBreakEnabled()) {
            line += text;
            queue.setCurrent(line);
            column += text.length();
            if (lastIsBr == 1) {
                finishCurrentLine();
            }
            return this;
        }

        if (column + text.length() > lastIsBr + config.lineWidth && (!isBlank(line))) {
            handleLongLine(line);
            line = currentLine();
        } else {
            line = padIndent(indentLevel * config.indentWidth - column);
        }
        line += text;
        queue.setCurrent(line);
        column += text.length();

        if (lastIsBr == 1) {
            finishCurrentLine();
        }

        return this;
    }

    /**
     * Append whitespace to the current line.
     * If autobreak is enabled:
     * <ul>
     *   <li>If length of current line is long enough, finish it and start a new line.</li>
     *   <li>If current line ends with whitespace, do nothing.</li>
     *   <li>Normally append only one whitespace character.</li>
     * </ul>
     */
    @Override
    public FormatterInterface appendWhitespaces(String text) {
        String line = currentLine();
        if (!autoBreakEnabled()) {
            line += text;
            queue.setCurrent(line);
            column += text.length();
            return this;
        }

        if (column + 1 > config.lineWidth && (!isBlank(line))) {
            handleLongLine(line);
            line = currentLine();
        }

        if (endsWithWhitespace(line)) {
            return this;
        }
        line += " ";
        column++;
        queue.setCurrent(line);
        return this;
    }

    /**
     * Append a linebreak to the current line and
     * reset the column counter.
     */
    @Override
    public FormatterInterface appendNewLineImpl(boolean forceEmptyLine, boolean allowAdjacentEmptyLines) {
        String line = currentLine();
        if (!autoBreakEnabled()) {
            line += "\n";
            queue.setCurrent(line);
            finishCurrentLine();
            return this;
        }
        line = removeTrailingWhitespaces(line);
        boolean isEmpty = line.equals("");
        boolean clear = false;
        if (forceEmptyLine) {
            String last = queue.size() > 1 ? queue.getAt(-2) : null;
            clear = (!allowAdjacentEmptyLines) && (last == null || last.equals("\n"));
        } else {
            clear = isEmpty;
        }

        if (clear) {
            /* clear current line and reset column counter */
            queue.setCurrent("");
            column = 1;
        } else {
            line += "\n";
            queue.setCurrent(line);
            finishCurrentLine();
        }
        return this;
    }

    @Override
    public FormatterInterface appendNewLine(boolean forceEmptyLine) {
        return appendNewLineImpl(forceEmptyLine, false);
    }

    @Override
    public void enter() {
        indentLevel++;
    }

    @Override
    public void leave() {
        if (indentLevel > 0) {
            indentLevel--;
        }
    }
}
