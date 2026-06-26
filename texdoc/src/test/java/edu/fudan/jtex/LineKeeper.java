package edu.fudan.jtex;

import java.util.ArrayList;

public class LineKeeper extends Formatter.LineWriter {
    public ArrayList<String> lines;

    @Override
    public void write(String line) {
        lines.add(line);
    }

    @Override
    public void close() {
        /* do nothing */
    }

    LineKeeper() {
        super();
        lines = new ArrayList<String>();
    }
}
