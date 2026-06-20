package edu.fudan.drawio2tikz;

import java.util.HashSet;

/**
 * A data class that tracks all tikz libraries and
 * colors required for a successful conversion.
 */
public class Context {
    public HashSet<String> tikzLibraries;
    public HashSet<Color> colors;

    public Context() {
        this.tikzLibraries = new HashSet<>();
        tikzLibraries.add("positioning");
        this.colors = new HashSet<>();
    }
}
