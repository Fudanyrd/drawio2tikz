package edu.fudan.jtex;

import java.util.List;

public class TextBlockNode extends NodeBase {
    public TextBlockNode(List<NodeBase> children) { this.children = children; }

    public boolean allowAutoBreak() { return true; }

    @Override
    public void appendTo(FormatterInterface formatter) {
        formatter.append("{");
        super.appendTo(formatter);
        formatter.append("}");
    }
}
