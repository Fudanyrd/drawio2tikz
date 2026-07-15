package edu.fudan.jtex;

import java.util.List;

/**
 * {@code ParagraphNode} represents a paragraph in
 * a LaTex document.
 */
public class ParagraphNode extends NodeBase {
    public ParagraphNode(List<NodeBase> children) { this.children = children; }

    @Override
    public boolean allowAutoBreak() {
        return true;
    }

    @Override
    public void appendTo(FormatterInterface formatter) {
        boolean free = formatter.autoBreakEnabled();
        if (free) {
            formatter.appendNewLine(true);
        }
        super.appendTo(formatter);
        if (free) {
            formatter.appendNewLine(true);
        }
    }
}
