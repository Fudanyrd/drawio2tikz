package edu.fudan.jtex;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class TestEnvironNode {
    private static EnvironNode create(String name, String... args) {
        ArrayList<ArgumentBase> argList = new ArrayList<ArgumentBase>();
        for (String arg : args) {
            NodeBase node = new TextNode(arg);
            argList.add(new OptionalArgument(node));
        }
        return new EnvironNode(name, argList);
    }

    private static void addChild(NodeBase parent, NodeBase child) {
        if (parent.children == null) {
            parent.children = new ArrayList<NodeBase>();
        }
        parent.children.add(child);
    }

    @Test
    public void regression() {
        Format config = new Format();
        LineKeeper keeper = new LineKeeper();
        config.lineWidth = 12;
        config.indentWidth = 3;
        Formatter formatter = new Formatter(config, keeper);

        NodeBase environ = create("includegraphics", "ht");
        addChild(environ, new CommandNode("\\centering", null));
        environ.appendTo(formatter);
        formatter.finish();

        ArrayList<String> actual = keeper.lines;
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("\\begin{includegraphics}[ht]\n", actual.get(0));
        Assert.assertEquals("   \\centering\n", actual.get(1));
        Assert.assertEquals("\\end{includegraphics}\n", actual.get(2));
    }

    @Test
    public void regressionForListing() {
        Format config = new Format();
        LineKeeper keeper = new LineKeeper();
        config.indentWidth = 3;
        Formatter formatter = new Formatter(config, keeper);

        /**
         * Though the tokenizer in my mind should
         * tokenize the code into &quot;def&quot;, &quot; &quot;,
         * &quot;add&quot; etc., we keep them in one text node for
         * brevity.
         */
        NodeBase environ = create("minted", "language=Python");
        addChild(environ, new TextNode("\n"));
        addChild(environ, new TextNode("def add(a,b): "));
        addChild(environ, new TextNode("\n"));
        addChild(environ, new TextNode("\treturn a + b"));
        addChild(environ, new TextNode("\n"));
        environ.appendTo(formatter);
        formatter.finish();

        /**
         * Check that the autobreak is off when formatting
         * the listing.
         */
        ArrayList<String> actual = keeper.lines;
        Assert.assertEquals(4, actual.size());
        Assert.assertEquals("\\begin{minted}[language=Python]\n", actual.get(0));
        Assert.assertEquals("def add(a,b): \n", actual.get(1));
        Assert.assertEquals("\treturn a + b\n", actual.get(2));
        Assert.assertEquals("\\end{minted}\n", actual.get(3));
    }
}
