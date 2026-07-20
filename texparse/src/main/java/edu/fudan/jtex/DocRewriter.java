package edu.fudan.jtex;

/**
 * A {@code DocRewriter} performs local
 * rewrite on a {@code NodeBase}. It is recommended
 * to code the rewriter as a pure class (i.e. does not
 * have side effects other than rewriting the given node).
 */
public abstract class DocRewriter {
    /**
     * Performs rewrite.
     *
     * @param context a stack which stores the path
     * from document root to current node (at the top).
     *
     * @return true if the {@code DocRewriter} should
     * descend into its children.
     */
    public abstract boolean rewrite(SmallStack<NodeBase> context);

    public abstract void parseArgs(String[] args /* not null */) throws Exception;

    /**
     * Recursively apply this rewriter to {@code document}.
     *
     * @param document: the root element of a LaTex document.
     */
    public void recurse(NodeBase document) {
        SmallStack<NodeBase> stack = new SmallStack<NodeBase>(null);
        stack.push(document);
        recurseImpl(stack);
        stack.pop(); /* pop document unnecessarity. */
    }

    private void recurseImpl(SmallStack<NodeBase> stack) {
        NodeBase top = stack.top();
        if (!top.allowAutoBreak()) {
            /* do not rewrite this node. */
            return;
        }
        if (rewrite(stack) && top.children != null) {
            /* recurse to its children. */
            for (NodeBase child : top.children) {
                stack.push(child);
                recurseImpl(stack);
                stack.pop();
            }
        }

        if (top instanceof CommandNode) {
            CommandNode cmd = (CommandNode)top;
            if (cmd.arguments != null) {
                for (ArgumentBase arg : cmd.arguments) {
                    if (arg.inner == null) {
                        /* some blank argument. */
                        continue;
                    }
                    stack.push(arg.inner);
                    recurseImpl(stack);
                    stack.pop();
                }
            }
        }
    }
}
