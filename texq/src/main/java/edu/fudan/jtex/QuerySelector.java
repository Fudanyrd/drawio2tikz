package edu.fudan.jtex;

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
}
