package edu.fudan.jtex;

import java.util.List;

/**
 * <h3>Strip or Replace One Kind of {@link EnvironNode}</h3>
 *
 * Arguments:
 * <ul>
 *   <li>--kind=KIND (REQUIRED), where KIND is tne name of a environ node.</li>
 *   <li>--replace=CODE (OPTIONAL), where code is the LaTex code after replacement (default
 *    to an empty comment string &quot;%&quot;</li>
 *   <li>--skip-numbered/--skip-unnumbered (OPTIONAL), no action for numbered/unnumbered environ node,
 *    even if the kind matches.</li>
 * </ul>
 */
public class EnvStrip extends DocRewriter {
    private NodeBase after;
    private boolean skipNumbered = false;
    private boolean skipUnnumbered = false;
    private String kind = null;

    private static NodeBase replacement(String replaceArg) {
        /* assuming it is quite simple, we do not use a {@link Parser} */
        if (replaceArg.startsWith("%")) {
            if (!replaceArg.endsWith("\n")) {
                replaceArg += "\n";
            }
            return new CommentNode(replaceArg);
        }
        return new TextNode(replaceArg);
    }

    @Override
    public void parseArgs(String[] args /* not null */) throws Exception {
        after = new CommentNode("%\n");
        for (String arg : args) {
            if (arg.startsWith("--kind=")) {
                kind = arg.substring("--kind=".length());
            } else if (arg.startsWith("--replace=")) {
                after = replacement(arg.substring("--replace=".length()));
            } else if (arg.equals("--skip-numbered")) {
                skipNumbered = true;
            } else if (arg.equals("--skip-unnumbered")) {
                skipUnnumbered = true;
            }
        }
    }

    @Override
    public boolean rewrite(SmallStack<NodeBase> context) {
        NodeBase top = context.top();
        if (top instanceof EnvironNode && ((EnvironNode)top).name.equals(kind)) {
            /* The environ's kind matches, but skipped, do not recurse its children. */
            return false;
        }

        List<NodeBase> children = top.children;
        if (children == null) {
            return false;
        }
        int size = children.size();

        for (int i = 0; i < size; i++) {
            NodeBase child = children.get(i);
            if (!(child instanceof EnvironNode)) {
                continue;
            }
            EnvironNode env = (EnvironNode)child;
            if (!env.name.equals(kind)) {
                continue;
            }
            if (skipNumbered && env.isNumbered) {
                continue;
            }
            if (skipUnnumbered && !env.isNumbered) {
                continue;
            }
            children.set(i, after);
        }
        return true;
    }
}
