package edu.fudan.jtex;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class TestQuerySelector {
    /**
     * Root node of a read-only document tree.
     *
     * Tree structure:
     * <body> <!-- topLevel -->
     *   <font>Hello</font><br>
     *   <font style="text-align: center">Centered</font><br>
     *   <b>Bold</b><br> <!-- textbf command node -->
     *   <font>Bye</font>
     * </body>
     */
    private static final NodeBase topLevel = new ContainerNode();
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
    static {
        /* create a toplevel document for testing. */
        addChild(topLevel, new TextNode("Hello"));

        EnvironNode centerEnv = new EnvironNode("center", null);
        addChild(centerEnv, new TextNode("Centered"));
        addChild(topLevel, centerEnv);

        CommandNode boldCmd = new CommandNode("\\textbf", null);
        boldCmd.arguments = new ArrayList<ArgumentBase>();
        boldCmd.arguments.add(new Argument(new TextNode("Bold")));
        addChild(topLevel, boldCmd);

        addChild(topLevel, new TextNode("Bye"));
    }

    public TestQuerySelector() {}

    @Test
    public void testNthChild() {
        NodeBase firstTextNode = (new QuerySelector(topLevel)).next(QuerySelector.MATCH_TEXT, null).get();
        Assert.assertTrue(firstTextNode instanceof TextNode);
        Assert.assertEquals("Hello", ((TextNode)firstTextNode).text);

        NodeBase secondTextNode = (new QuerySelector(topLevel)).next(QuerySelector.MATCH_TEXT, 2).get();
        Assert.assertTrue(secondTextNode instanceof TextNode);
        Assert.assertEquals("Bye", ((TextNode)secondTextNode).text);

        NodeBase thirdTextNode = (new QuerySelector(topLevel)).next(QuerySelector.MATCH_TEXT, 3).get();
        Assert.assertEquals(null, thirdTextNode);
    }

    @Test
    public void testDeeper() {
        NodeBase centeredTextNode =
            (new QuerySelector(topLevel)).nextEnvironNode(null).next(QuerySelector.MATCH_TEXT, null).get();

        Assert.assertTrue(centeredTextNode instanceof TextNode);
        Assert.assertEquals("Centered", ((TextNode)centeredTextNode).text);

        NodeBase nonexist = (new QuerySelector(topLevel))
                                .nextEnvironNode(null)
                                .next(null /* match any */, null) /* is centeredTextNode */
                                .next(null, 1)                    /* null */
                                .get();
        Assert.assertEquals(null, nonexist);
    }

    @Test
    public void testSelectFromNull() {
        NodeBase result = (new QuerySelector(null)).next(QuerySelector.MATCH_ANY, 1).get();
        Assert.assertEquals(null, result);
    }

    @Test
    public void testMatchAny() {
        int numChildren = topLevel.children.size();

        for (int i = 0; i < numChildren; i++) {
            NodeBase child = (new QuerySelector(topLevel)).next(QuerySelector.MATCH_ANY, i + 1).get();
            Assert.assertEquals(topLevel.children.get(i), child);
        }
    }

    // TODO: add test for method "arg".
}
