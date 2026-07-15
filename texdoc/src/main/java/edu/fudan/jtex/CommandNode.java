package edu.fudan.jtex;

import java.util.HashSet;
import java.util.List;

public class CommandNode extends NodeBase {
    /**
     * The name of the command (including leading "\").
     */
    public String name;

    public boolean isNumbered;
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
        noAutoBreakCommands.add("\\textsf");
        noAutoBreakCommands.add("\\textsc");
    }

    public CommandNode(String token, List<ArgumentBase> arguments) {
        if (token.length() == 0 || token.charAt(0) != '\\') {
            throw new IllegalArgumentException("Command name must start with '\\'");
        }

        int len = token.length();
        if (token.charAt(len - 1) == '*') {
            name = token.substring(0, len - 1);
            isNumbered = true;
        } else {
            name = token;
            isNumbered = false;
        }

        this.arguments = arguments;
        children = null;
    }

    private String getToken() {
        if (isNumbered) {
            return name + "*";
        }
        return name;
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

        formatter.append(getToken());
        if (arguments != null) {
            for (ArgumentBase arg : arguments) {
                arg.appendTo(formatter);
            }
        }

        if (!allowBr) {
            formatter.autoBreakOn();
        }
    }
}
