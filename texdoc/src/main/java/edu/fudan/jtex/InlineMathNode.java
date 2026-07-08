package edu.fudan.jtex;

public class InlineMathNode extends NodeBase {
    public InlineMathNode() { children = null; }

    @Override
    public boolean allowAutoBreak() {
        return false;
    }

    @Override
    public void appendTo(FormatterInterface formatter) {
        formatter.append("$");
        if (children != null) {
            for (NodeBase child : children) {
                child.appendTo(formatter);
            }
        }
        formatter.append("$");
    }
}
