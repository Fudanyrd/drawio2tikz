package edu.fudan.jtex;

import java.util.HashSet;
import java.util.List;

public class EnvironNode extends NodeBase {
    public String name;
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

    public EnvironNode(String name, List<ArgumentBase> restArguments) {
        this.name = name;
        this.restArguments = restArguments;
    }

    public String getBegin() { return "\\begin{" + name + "}"; }
    public String getEnd() { return "\\end{" + name + "}"; }

    @Override
    public boolean allowAutoBreak() {
        return !noAutoBreakEnvirons.contains(name);
    }

    @Override
    public void appendTo(FormatterInterface formatter) {
        formatter.append(getBegin());
        for (ArgumentBase arg : restArguments) {
            arg.appendTo(formatter);
        }
        formatter.appendNewLine(false);

        if (children != null) {
            for (NodeBase child : children) {
                child.appendTo(formatter);
            }
        }
    }
}
