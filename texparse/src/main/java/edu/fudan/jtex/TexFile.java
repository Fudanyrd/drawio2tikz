package edu.fudan.jtex;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class TexFile implements TexSourceBase {
    private final String filename;
    private final BufferedReader reader;

    public TexFile(String filename) {
        this.filename = filename;
        try {
            this.reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String readLine() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            reader.close();
        } else {
            line += ParserConfig.LINE_BREAK; /* append required line break */
        }
        return line;
    }

    @Override
    public String formatLOC(int line, Integer column) {
        if (column != null) {
            return String.format("%s:%d:%d", filename, line, column);
        } else {
            return String.format("%s:%d", filename, line);
        }
    }
}
