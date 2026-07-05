package edu.fudan.jtex;

import java.util.List;

public class QuerySelector {
    private NodeBase iterator;

    public QuerySelector(NodeBase document) { this.iterator = document; }

    public QuerySelector copy() { return new QuerySelector(iterator); }

    public static final ClassWrapBase MATCH_CMD = new ClassWrap<CommandNode>(CommandNode.class);
    public static final ClassWrapBase MATCH_ENV = new ClassWrap<EnvironNode>(EnvironNode.class);
    public static final ClassWrapBase MATCH_TEXT = new ClassWrap<TextNode>(TextNode.class);
    public static final ClassWrapBase MATCH_ANY = null;

    public QuerySelector nextCommandNode(Integer nthChild) { return next(MATCH_CMD, nthChild); }

    public QuerySelector nextEnvironNode(Integer nthChild) { return next(MATCH_ENV, nthChild); }

    public QuerySelector next(ClassWrapBase classObject, Integer nthChild) {
        if (iterator == null) {
            return this;
        }

        if (nthChild == null) {
            /* default to 1st child */
            nthChild = 1;
        }
        nthChild = nthChild - 1; /* convert to 0-based index */

        NodeBase parent = iterator;
        iterator = null;
        if (parent.children == null) {
            return this;
        }

        for (NodeBase child : parent.children) {
            if (classObject == null || classObject.isInstance(child)) {
                if (nthChild == 0) {
                    iterator = child;
                    return this;
                }
                nthChild--;
            }
        }

        return this;
    }

    public NodeBase get() { return iterator; }

    /**
     * Descend into the argument of {@code EnvironNode} or {@code CommandNode}.
     * Unlike {@code QuerySelector#next} method, this supports negative
     * indexing for convenience, e.g. -1 indicates last argument.
     *
     * <p>If {@code nthChild} is out of bound, no exception will be thrown
     * but current node will be set to {@code null} (indicating no matching result).
     * </p>
     *
     * @throw {@code RuntimeException} if current node is not null
     * but not {@code EnvironNode} or {@code CommandNode}.
     */
    public QuerySelector arg(Integer nthChild) {
        if (iterator == null) {
            return this;
        }
        boolean isEnv = (iterator instanceof EnvironNode);
        boolean isCmd = (iterator instanceof CommandNode);
        if (!isEnv && !isCmd) {
            throw new RuntimeException("Current node is not EnvironNode or CommandNode");
        }

        List<ArgumentBase> args = null;
        if (isEnv) {
            args = ((EnvironNode)iterator).restArguments;
        } else {
            args = ((CommandNode)iterator).arguments;
        }
        if (args == null) {
            iterator = null;
            return this;
        }

        int idx = nthChild;
        if (idx < 0) {
            /* try negative indexing */
            idx += args.size();
        } else {
            /* The convention of {@code nthChild} is 1-based. */
            idx -= 1;
        }
        if (idx < 0 || idx >= args.size()) {
            iterator = null;
            return this;
        }
        iterator = args.get(idx).inner;
        return this;
    }
}
