package edu.fudan.jtex;

import java.io.IOException;

public interface TexSourceBase {
    /**
     * Read line from this source of LaTex code.
     *
     * @return next line (ending with \n) or null (indicating EOF)
     */
    public String readLine() throws IOException;

    /**
     * Formats line information. Useful for emitting diagnostics.
     *
     * @param line: current line
     * @param column: optionally provided current column
     *
     * @return formatted source code location,
     * e.g. &quot;input.tex:12:4&quot;
     */
    public String formatLOC(int line, Integer column);
}
