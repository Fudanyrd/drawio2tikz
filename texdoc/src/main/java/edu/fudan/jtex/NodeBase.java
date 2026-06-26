package edu.fudan.jtex;

import java.util.List;

/**
 * Base class for all document nodes.
 */
public abstract class NodeBase {
    public List<NodeBase> children;

    /**
     * @return false when the node be pasted as-is
     * during formatting. else, the formatter can
     * add/delete whitespaces or line breaks in the node.
     */
    public abstract boolean allowAutoBreak();

    public void appendTo(FormatterInterface formatter) {
        boolean br = allowAutoBreak();

        if (!br) {
            formatter.autoBreakOff();
        }
        if (children != null) {
            for (NodeBase child : children) {
                child.appendTo(formatter);
            }
        }
        if (!br) {
            formatter.autoBreakOn();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        CompactFormatter formatter = new CompactFormatter(sb);
        appendTo(formatter);
        return sb.toString();
    }
}
