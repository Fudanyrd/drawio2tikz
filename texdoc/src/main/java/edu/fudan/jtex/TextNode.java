package edu.fudan.jtex;

public class TextNode extends NodeBase {
    public String text;

    public TextNode(String text) {
        this.text = text;
        children = null;
    }

    public boolean allowAutoBreak() { return true; }

    public static boolean isWhitespace(char c) { return c == ' ' || c == '\t' || ((int)c) == 0xa0; }

    @Override
    public void appendTo(FormatterInterface formatter) {
        if (text.length() == 0) {
            return;
        }

        char firstChar = text.charAt(0);
        if (isWhitespace(firstChar)) {
            formatter.appendWhitespaces(text);
        } else if (firstChar == '\n') {
            formatter.appendNewLine(false);
        } else {
            formatter.append(text);
        }
    }
}
