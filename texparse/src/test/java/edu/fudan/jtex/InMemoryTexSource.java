package edu.fudan.jtex;

import java.io.IOException;

class InMemoryTexSource implements TexSourceBase {
    /**
     * Contents of a small tex file, with each line
     * ending with {@code ParserConfig#LINE_BREAK}.
     */
    public String[] lines;
    int current;

    public InMemoryTexSource(String[] lines) {
        this.lines = lines;
        current = 0;
    }

    @Override
    public String formatLOC(int line, Integer column) {
        return "<memory>" + line + ":" + column;
    }

    @Override
    public String readLine() throws IOException {
        if (current >= lines.length) {
            return null;
        }
        return lines[current++];
    }
}
