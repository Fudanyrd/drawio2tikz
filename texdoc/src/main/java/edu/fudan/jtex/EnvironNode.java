package edu.fudan.jtex;

import java.util.HashSet;
import java.util.List;

public class EnvironNode extends NodeBase {
    public String name;
    public boolean isNumbered;
    public List<ArgumentBase> restArguments;

    public static final HashSet<String> noAutoBreakEnvirons = new HashSet<String>();
    static {
        /* verbatim */
        noAutoBreakEnvirons.add("verbatim");
        noAutoBreakEnvirons.add("Verbatim");

        /* listings/DSL */
        noAutoBreakEnvirons.add("lstlisting");
        noAutoBreakEnvirons.add("minted");
        noAutoBreakEnvirons.add("tikzpicture");
    }

    public EnvironNode(String token, List<ArgumentBase> restArguments) {
        int len = token.length();
        if (token.charAt(len - 1) == '*') {
            name = token.substring(0, len - 1);
            isNumbered = true;
        } else {
            name = token;
            isNumbered = false;
        }
        this.restArguments = restArguments;
    }

    private String getToken() {
        if (isNumbered) {
            return name + "*";
        }
        return name;
    }
    public String getBegin() { return "\\begin{" + getToken() + "}"; }
    public String getEnd() { return "\\end{" + getToken() + "}"; }

    @Override
    public boolean allowAutoBreak() {
        return !noAutoBreakEnvirons.contains(name);
    }

    public boolean padIndent() {
        boolean nopad = name.equals("document") || name.equals("enumerate") || name.equals("itemize");
        return !nopad;
    }

    @Override
    public void appendTo(FormatterInterface formatter) {
        formatter.autoBreakOff();
        formatter.append(getBegin());
        if (restArguments != null) {
            for (ArgumentBase arg : restArguments) {
                arg.appendTo(formatter);
            }
        }
        formatter.autoBreakOn();

        boolean allowBr = allowAutoBreak() && formatter.autoBreakEnabled();
        boolean pad = padIndent();
        if (allowBr) {
            /* create a line break after the \\begin statement. */
            formatter.appendNewLine(false);
        }

        /* dump everything in between */
        if (!allowBr) {
            formatter.autoBreakOff();
        }
        if (children != null) {
            if (pad) {
                formatter.enter();
            }
            for (NodeBase child : children) {
                child.appendTo(formatter);
            }
            if (pad) {
                formatter.leave();
            }
        }
        if (!allowBr) {
            formatter.autoBreakOn();
        }

        if (allowBr) {
            /* create a line break before the \\end statement. */
            formatter.appendNewLine(false);
        }
        formatter.append(getEnd());
    }
}
