package edu.fudan.jtex;

import org.junit.Assert;
import org.junit.Test;

/**
 * <b>Note</b>: Set timeout to break infinite loops.
 */
public class TestParser {
    @Test(timeout = 500)
    public void testEmptyDoc() {
        Parser parser = new Parser(new InMemoryTexSource(new String[] {}));
        NodeBase doc = parser.output;
        if (doc.children != null) {
            Assert.assertEquals(0, doc.children.size());
        }
    }

    @Test(timeout = 1000)
    public void testText() {
        Parser parser = new Parser(new InMemoryTexSource(new String[] {"hello\n"}));
        QuerySelector s = new QuerySelector(parser.output);
        s.next(QuerySelector.MATCH_TEXT, 1);

        Assert.assertNotNull(s.get());
        TextNode txt = (TextNode)s.get();
        Assert.assertEquals("hello", txt.text);
    }

    @Test(timeout = 1000)
    public void testCommand() {
        Parser parser = new Parser(new InMemoryTexSource(new String[] {"\\cmd[1]{2}[3]\\CMd\n"}));
        QuerySelector s = new QuerySelector(parser.output);

        /* check first cmd. */
        CommandNode cmd1 = (CommandNode)s.copy().next(s.MATCH_CMD, 1).get();
        Assert.assertEquals("\\cmd", cmd1.name);
        Assert.assertEquals(3, cmd1.arguments.size());
        Assert.assertTrue(cmd1.arguments.get(0) instanceof OptionalArgument);
        Assert.assertTrue(cmd1.arguments.get(2) instanceof OptionalArgument);
        Assert.assertTrue(cmd1.arguments.get(1) instanceof Argument);
        for (int i = 0; i < 3; i++) {
            Assert.assertEquals("" + (i + 1), cmd1.arguments.get(i).inner.toString());
        }

        /* check second cmd. */
        CommandNode cmd2 = (CommandNode)s.copy().next(s.MATCH_CMD, 2).get();
        Assert.assertEquals("\\CMd", cmd2.name);

        /* check last text node. */
        TextNode text = (TextNode)s.next(s.MATCH_TEXT, null).get();
        Assert.assertEquals("\n", text.text);
    }

    @Test(timeout = 1000)
    public void testEnviron() {
        Parser parser = new Parser(new InMemoryTexSource(new String[] {"\\begin{eNv}[0]{1}text\\end{eNv}\n"}));
        QuerySelector s = new QuerySelector(parser.output);

        /* check environ. */
        EnvironNode env = (EnvironNode)s.next(s.MATCH_ENV, 1).get();
        Assert.assertNotNull(env);
        Assert.assertEquals("eNv", env.name);
        Assert.assertEquals(2, env.restArguments.size());
        Assert.assertTrue(env.restArguments.get(0) instanceof OptionalArgument);
        Assert.assertTrue(env.restArguments.get(1) instanceof Argument);
        for (int i = 0; i < 2; i++) {
            Assert.assertEquals("" + i, env.restArguments.get(i).inner.toString());
        }

        /* text inside env. */
        TextNode innerText = (TextNode)s.next(s.MATCH_TEXT, null).get();
        Assert.assertEquals("text", innerText.text);

        /* check last text node. */
        TextNode text = (TextNode)parser.output.children.get(1);
        Assert.assertEquals("\n", text.text);
    }

    @Test(timeout = 1000)
    public void testDisplayMath() {
        Parser parser = new Parser(new InMemoryTexSource(new String[] {"\\[1+2\\]\n"}));
        QuerySelector s = new QuerySelector(parser.output);

        /* check environ. */
        EnvironNode env = (EnvironNode)s.next(s.MATCH_ENV, 1).get();
        Assert.assertEquals("displaymath", env.name);

        /* check inner text. */
        TextNode innerText = (TextNode)s.next(s.MATCH_TEXT, null).get();
        Assert.assertEquals("1+2", innerText.text);

        /* check last text node. */
        TextNode text = (TextNode)parser.output.children.get(1);
        Assert.assertEquals("\n", text.text);
    }

    @Test(timeout = 1000)
    public void testInlineMath() {
        Parser parser = new Parser(new InMemoryTexSource(new String[] {"$1+2$\n"}));

        /* check the inline math. */
        InlineMathNode math = (InlineMathNode)parser.output.children.get(0);
        Assert.assertEquals("1+2", math.children.get(0).toString());

        /* check last text node. */
        TextNode text = (TextNode)parser.output.children.get(1);
        Assert.assertEquals("\n", text.text);
    }
}
