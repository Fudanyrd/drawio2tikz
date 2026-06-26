package edu.fudan.jtex;

import java.util.HashSet;
import java.util.List;

public class CommandNode extends NodeBase {
    /**
     * The name of the command (including leading "\").
     */
    public String name;

    public List<ArgumentBase> arguments;

    public static final HashSet<String> noAutoBreakCommands = new HashSet<String>();
    static {
        /* citations */
        noAutoBreakCommands.add("\\cite");
        noAutoBreakCommands.add("\\citep");
        noAutoBreakCommands.add("\\citenum");
        noAutoBreakCommands.add("\\citet");
        noAutoBreakCommands.add("\\citeauthor");
        noAutoBreakCommands.add("\\citeyear");

        /* path/url */
        noAutoBreakCommands.add("\\path");
        noAutoBreakCommands.add("\\url");
        noAutoBreakCommands.add("\\includegraphics");
        noAutoBreakCommands.add("\\input");
        noAutoBreakCommands.add("\\include");

        /* documentclass */
        noAutoBreakCommands.add("\\documentclass");

        /* text manipulation */
        noAutoBreakCommands.add("\\textbf");
        noAutoBreakCommands.add("\\textit");
        noAutoBreakCommands.add("\\emph");
        noAutoBreakCommands.add("\\texttt");
    }

    public CommandNode(String name, List<ArgumentBase> arguments) {
        this.name = name;
        this.arguments = arguments;
        children = null;

        if (name.length() == 0 || name.charAt(0) != '\\') {
            throw new IllegalArgumentException("Command name must start with '\\'");
        }
    }

    @Override
    public boolean allowAutoBreak() {
        return !noAutoBreakCommands.contains(name);
    }

    @Override
    public void appendTo(FormatterInterface formatter) {
        boolean allowBr = allowAutoBreak();
        if (!allowBr) {
            formatter.autoBreakOff();
        }

        formatter.append(name);
        for (ArgumentBase arg : arguments) {
            arg.appendTo(formatter);
        }

        if (!allowBr) {
            formatter.autoBreakOn();
        }
    }
}
