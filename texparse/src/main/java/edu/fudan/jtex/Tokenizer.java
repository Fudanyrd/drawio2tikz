package edu.fudan.jtex;

import java.io.IOException;

public final class Tokenizer {
    TexSourceBase src;
    String currentLine;
    int column; /* which is: current column */

    public Tokenizer(TexSourceBase source) {
        this.src = source;
        column = 0;
        currentLine = "";
    }

    private boolean refill() {
        if (currentLine != null && column < currentLine.length()) {
            return true;
        }
        try {
            currentLine = src.readLine();
            if (currentLine == null) {
                return false;
            }
            assert currentLine.length() > 0 : "at least \\n should be there";
            column = 0;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * This is used by {@code Tokenizer#next}, with
     * length of current line pre-computed.
     *
     * @param length: pre-computed length of current line.
     */
    private void jumpToNext(int length) {
        column++; /* skip start of current token */
        while (column < length) {
            char cur = currentLine.charAt(column);
            if (ParserConfig.TOKEN_START.contains(cur) || cur == '\\' /* also a token start */
                || ParserConfig.isWhitespace(cur)) {
                break;
            }
            column++;
        }
    }

    /**
     * <h3>Token kinds</h3>
     * <ul>
     *   <li>line break &quot;\n&quot;</li>
     *   <li>&quot;word&quot; &quot;\.&quot; or &quot;[\]{0,1}\w+&quot;</li>
     *   <li>whitespaces: &quot;[\ \t\xa0]+&quot;</li>
     *   <li>comment: &quot;%.*\n&quot;</li>
     *   <li>punctuation token, as listed in {@code ParserConfig#TOKEN_START}</li>
     * </ul>
     *
     * @return next token or null if no more.
     */
    public String next() {
        if (!refill()) {
            return null;
        }

        char start = currentLine.charAt(column);
        int startIdx = column;
        int length = currentLine.length();

        /* line break token */
        if (start == ParserConfig.LINE_BREAK) {
            column++;
            return "\n";
        }

        /* comment token */
        if (start == ParserConfig.COMMENT_START) {
            String ret = currentLine.substring(column, length);
            column = length;
            return ret;
        }

        /* punctuations (LaTex specific) */
        if (ParserConfig.TOKEN_START.contains(start)) {
            /* yield only the start character. */
            column++;
            return String.valueOf(start);
        }

        /* whitespace token */
        if (ParserConfig.isWhitespace(start)) {
            while (column < length && ParserConfig.isWhitespace(currentLine.charAt(column))) {
                column++;
            }
            return currentLine.substring(startIdx, column);
        }

        /* misc: */
        if (start != '\\') {
            /* it is an ordinary word. */
            jumpToNext(length);
            return currentLine.substring(startIdx, column);
        }

        /**
         * When it meets &quot;\&quot;, the tokenizer
         * has to determine whehter it wants to quote
         * a character (e.g. &quot;\[&quot; &quot;\[&quot; indicating
         * a multi-line formula) or a command (e.g. &quot;\include&quot;).
         */
        column++;
        char next = currentLine.charAt(column);
        if (next == '\n') {
            /* why there's a stand-alone backslash? Ignore it for now. */
            return "\\";
        }
        if (ParserConfig.isIdentifier(next)) {
            jumpToNext(length);
            return currentLine.substring(startIdx, column);
        }

        column++;
        return new String(new char[] {'\\', next});
    }
}
