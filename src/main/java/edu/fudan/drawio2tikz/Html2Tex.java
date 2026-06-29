package edu.fudan.drawio2tikz;

import edu.fudan.jtex.Argument;
import edu.fudan.jtex.ArgumentBase;
import edu.fudan.jtex.CommandNode;
import edu.fudan.jtex.ContainerNode;
import edu.fudan.jtex.EnvironNode;
import edu.fudan.jtex.NodeBase;
import edu.fudan.jtex.OptionalArgument;
import edu.fudan.jtex.ParagraphNode;
import edu.fudan.jtex.TextNode;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2Tex {
    public static class StyleSheet {
        public Color textColor;
        public enum TextAlign { NONE, LEFT, CENTER, RIGHT }
        ;

        public TextAlign textAlign;
        public Integer fontSize;

        public String texEnvironBegin() { return ""; }

        public String texEnvironAfter() { return ""; }

        public StyleSheet(String style) {
            textColor = null;
            textAlign = TextAlign.NONE; /* LaTex default or follow upper styles */
            fontSize = null;            /* Use default value. */

            if (style == null || style.isEmpty()) {
                /* use default values. */
                return;
            }

            String[] styleItems = style.split(";");
            for (String item : styleItems) {
                String[] keyValue = item.split(":");
                /* strip leading and trailing whitespaces. */
                if (keyValue.length == 2) {
                    keyValue[0] = keyValue[0].trim();
                    keyValue[1] = keyValue[1].trim();
                    switch (keyValue[0]) {
                    case "color": {
                        textColor = Color.fromHTMLStyle(keyValue[1]);
                        break;
                    }
                    case "text-align": {
                        String alignStr = keyValue[1].toLowerCase();
                        switch (alignStr) {
                        case "left":
                            textAlign = TextAlign.LEFT;
                            break;
                        case "center":
                            textAlign = TextAlign.CENTER;
                            break;
                        case "right":
                            textAlign = TextAlign.RIGHT;
                            break;
                        default:
                            /* ignore invalid textAlign. */
                            break;
                        }
                        break;
                    }
                    case "font-size": {
                        String sizeStr = keyValue[1];
                        if (sizeStr.endsWith("px")) {
                            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
                        }
                        try {
                            fontSize = Integer.parseInt(sizeStr);
                        } catch (NumberFormatException e) {
                            /* ignore invalid fontSize. */
                        }
                        break;
                    }
                    default:
                        /* ignore unknown style item. */
                        break;
                    }
                } else {
                    System.err.println(String.format("Invalid style item: %s", item));
                }
            }
        }
    };

    private static class ToTexParserCallback extends HTMLEditorKit.ParserCallback {
        private StringBuilder sb;
        private Stack<StyleSheet> styleStack = new Stack<>();

        public ToTexParserCallback(StringBuilder sb) { this.sb = sb; }

        @Override
        public void handleText(char[] data, int pos) {
            if (data.length == 1 && (data[0] == ' ' || (int)data[0] == 0xa0)) {
                data[0] = '~'; /* non-breaking space in LaTeX. */
            }
            sb.append(data);
        }

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            StyleSheet ss = new StyleSheet((String)attributes.getAttribute(HTML.Attribute.STYLE));
            styleStack.push(ss);
            sb.append(ss.texEnvironBegin());

            if (tag == HTML.Tag.B) {
                sb.append("\\textbf{");
            } else if (tag == HTML.Tag.I) {
                sb.append("\\textit{");
            }
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int pos) {
            if (tag == HTML.Tag.P || tag == HTML.Tag.SPAN) {
                sb.append("\\\\\n");
            }
            if (tag == HTML.Tag.B || tag == HTML.Tag.I) {
                sb.append("}");
            }

            if (!styleStack.isEmpty()) {
                StyleSheet ss = styleStack.peek();
                sb.append(ss.texEnvironAfter());
                styleStack.pop();
            } else {
                throw new IllegalStateException("Style stack underflow: unmatched end tag " + tag);
            }
        }

        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            if (tag == HTML.Tag.BR) {
                sb.append("\\\\\n");
            }
        }
    };

    public static class Callback extends HTMLEditorKit.ParserCallback {
        public NodeBase topLevel;
        private Stack<NodeBase[]> stack;

        public Callback() {
            topLevel = new ContainerNode();
            stack = new Stack<>();
            stack.push(new NodeBase[] {topLevel});
        }

        private NodeBase getCurrent() {
            NodeBase[] nodes = stack.peek();
            NodeBase last = nodes[nodes.length - 1];

            if (last instanceof CommandNode) {
                CommandNode cmdNode = (CommandNode)last;
                if (cmdNode.arguments != null && !cmdNode.arguments.isEmpty()) {
                    ArgumentBase lastArg = cmdNode.arguments.get(cmdNode.arguments.size() - 1);
                    return lastArg.inner;
                }
            }
            return last;
        }

        private static NodeBase addChild(NodeBase parent, NodeBase child) {
            if (parent == null) {
                return child;
            }
            assert !(parent instanceof CommandNode);
            if (parent.children == null) {
                parent.children = new ArrayList<NodeBase>();
            }
            parent.children.add(child);
            return child;
        }

        public static NodeBase[] makeStackElement(HTML.Tag tag, StyleSheet ss) {
            /* (paragraph) -> (alignment) -> (color) -> (bold/italics) */
            ArrayList<NodeBase> nodes = new ArrayList<NodeBase>();
            NodeBase cur = null;

            StyleSheet.TextAlign align = ss.textAlign;
            if (tag == HTML.Tag.DIV || tag == HTML.Tag.P || tag == HTML.Tag.SPAN) {
                cur = new ParagraphNode(new ArrayList<NodeBase>());
                nodes.add(cur);
                /**
                 * Default paragraph alignment is center.
                 */
                if (align == StyleSheet.TextAlign.NONE) {
                    align = StyleSheet.TextAlign.CENTER;
                }
            }

            switch (align) {
            case CENTER: {
                NodeBase centerNode = new EnvironNode("center", new ArrayList<ArgumentBase>());
                nodes.add(centerNode);
                cur = addChild(cur, centerNode);
                break;
            }
            case RIGHT: {
                NodeBase flushrightNode = new EnvironNode("flushright", new ArrayList<ArgumentBase>());
                nodes.add(flushrightNode);
                cur = addChild(cur, flushrightNode);
                break;
            }
            }

            if (ss.textColor != null && !ss.textColor.equals(Color.BLACK)) {
                CommandNode colorNode = new CommandNode("\\textcolor", new ArrayList<ArgumentBase>());
                NodeBase placeHolder = new ContainerNode();
                colorNode.arguments.add(new OptionalArgument(new TextNode("HTML")));
                colorNode.arguments.add(new Argument(new TextNode(ss.textColor.toString())));
                colorNode.arguments.add(new Argument(placeHolder));
                nodes.add(colorNode);
                addChild(cur, colorNode);
                cur = placeHolder;
            }

            if (tag == HTML.Tag.B) {
                CommandNode boldNode = new CommandNode("\\textbf", new ArrayList<ArgumentBase>());
                NodeBase placeHolder = new ContainerNode();
                boldNode.arguments.add(new Argument(placeHolder));
                nodes.add(boldNode);
                cur = placeHolder;
            } else if (tag == HTML.Tag.I) {
                CommandNode italicsNode = new CommandNode("\\textit", new ArrayList<ArgumentBase>());
                NodeBase placeHolder = new ContainerNode();
                italicsNode.arguments.add(new Argument(placeHolder));
                nodes.add(italicsNode);
                cur = placeHolder;
            }

            int size = nodes.size();
            if (size == 0) {
                /* no special node created, just return a placeholder. */
                return new NodeBase[] {new ContainerNode()};
            }
            return nodes.toArray(new NodeBase[size]);
        }

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            if (tag == HTML.Tag.HTML || tag == HTML.Tag.HEAD || tag == HTML.Tag.BODY) {
                return;
            }

            // System.out.println(tag + "");
            StyleSheet ss = new StyleSheet((String)attributes.getAttribute(HTML.Attribute.STYLE));
            NodeBase[] newStackElement = makeStackElement(tag, ss);
            NodeBase current = getCurrent();
            addChild(current, newStackElement[0]);
            stack.push(newStackElement);
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int pos) {
            if (tag == HTML.Tag.HTML || tag == HTML.Tag.HEAD || tag == HTML.Tag.BODY) {
                return;
            }

            // System.out.println(tag + "/");
            stack.pop();
        }

        @Override
        public void handleText(char[] data, int pos) {
            if (data.length >= 1 && (data[0] == ' ' || (int)data[0] == 0xa0)) {
                data[0] = '~'; /* non-breaking space in LaTeX. */
            }
            NodeBase current = getCurrent();
            if (current == topLevel) {
                /* default alignment: center */
                EnvironNode centerNode = new EnvironNode("center", new ArrayList<ArgumentBase>());
                addChild(current, centerNode);
                addChild(centerNode, new TextNode(new String(data)));
            } else {
                addChild(current, new TextNode(new String(data)));
            }
        }

        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {}
    }

    public static String convert(String html) throws Exception {
        ParserDelegator parserDelegator = new ParserDelegator();
        Callback callback = new Callback();
        parserDelegator.parse(new StringReader(html), callback, false);
        /**
         * TODO:
         * use manual linebreak to separate each paragraph (children of topLevel).
         */
        return callback.topLevel.toString();
    }
}
