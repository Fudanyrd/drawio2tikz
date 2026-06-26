package edu.fudan.jtex;

public class TextBlockNode extends NodeBase {
    public String text;

    TextBlockNode(String text) {
        this.text = text;
        children = null;
    }

    public boolean allowAutoBreak() { return true; }
}
