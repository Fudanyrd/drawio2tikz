package edu.fudan.jtex;

import java.util.List;

/**
 * Add line breaks after commands in the preface section (
 * the setup region before &quot;\begin{document}&quot;)
 *
 * For example, an empty comment will be added to the end
 * of each line:
 * <code>
 * -\\documentclass[screen,anonymous]{acm}
 * -\\usepackage{url}
 * +\\documentclass[screen,anonymous]{acm}%
 * +\\usepackage{url}%
 * </code>
 *
 * Why? if this {@link DocRewriter} is not provided, the {@link Formatter}
 * will try to pad everything till column limit is reached, e.g:
 * <code>
 * % arguably uglier!
 * \\documentclass[screen,anonymous]{acm} \\usepackage{
 * url}
 * </code>
 */
public class AddBreak extends DocRewriter {
    @Override
    public void parseArgs(String[] args) {}

    private static boolean isDocument(NodeBase node) {
        if (node instanceof EnvironNode) {
            return ((EnvironNode)node).name.equals("document");
        }
        return false;
    }

    @Override
    public boolean rewrite(SmallStack<NodeBase> context) {
        /* Only top level node will be rewritten. */
        if (context.size() > 1) {
            return false;
        }
        NodeBase top = context.top();
        List<NodeBase> children = top.children;
        int nChildren = children.size();

        /* Jump to the first {@code CommandNode}: it should be \documentclass ! */
        int i = 0;
        CommandNode first = null;
        for (; i < nChildren; i++) {
            NodeBase child = children.get(i);
            if (child instanceof CommandNode) {
                first = (CommandNode)child;
                break;
            }
        }
        if (first == null || (!first.name.equals("\\documentclass"))) {
            return false;
        }

        /* We do not comment on empty lines. */
        boolean prevIsLB = true;
        for (; i < nChildren; i++) {
            NodeBase child = children.get(i);
            if (isDocument(child)) {
                break;
            }
            if (child instanceof TextNode) {
                String text = ((TextNode)child).text;
                if (text.endsWith(String.valueOf(ParserConfig.LINE_BREAK))) {
                    /* Replace it with a &quot;\n&quot; */
                    if (!prevIsLB) {
                        children.set(i, new CommentNode("%\n"));
                    }
                    prevIsLB = true;
                } else {
                    prevIsLB = false;
                }
            } else if (child instanceof CommentNode) {
                /* comments ends with LB */
                prevIsLB = true;
            } else {
                prevIsLB = false;
            }
        }

        return false;
    }
}
