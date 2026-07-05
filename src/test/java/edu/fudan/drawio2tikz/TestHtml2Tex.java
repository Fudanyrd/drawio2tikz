package edu.fudan.drawio2tikz;

import edu.fudan.jtex.Argument;
import edu.fudan.jtex.ArgumentBase;
import edu.fudan.jtex.ClassWrap;
import edu.fudan.jtex.CommandNode;
import edu.fudan.jtex.ContainerNode;
import edu.fudan.jtex.EnvironNode;
import edu.fudan.jtex.NodeBase;
import edu.fudan.jtex.OptionalArgument;
import edu.fudan.jtex.ParagraphNode;
import edu.fudan.jtex.QuerySelector;
import edu.fudan.jtex.TextNode;
import java.io.StringReader;
import javax.swing.text.html.parser.ParserDelegator;
import org.junit.Assert;
import org.junit.Test;

public class TestHtml2Tex {
    private static NodeBase convert(String html) throws Exception {
        ParserDelegator parserDelegator = new ParserDelegator();
        Html2Tex.Callback callback = new Html2Tex.Callback();
        parserDelegator.parse(new StringReader(html), callback, false);
        return callback.topLevel;
    }
    private static final ClassWrap<ParagraphNode> MATCH_PAR = new ClassWrap<ParagraphNode>(ParagraphNode.class);

    @Test
    public void testRawText() {
        NodeBase node = null;
        /**
         * Default text alignment is center, so the result is:
         * <code>\begin{center}
         * Hello
         * \end{center}</code>
         */
        try {
            node = convert("Hello");
        } catch (Exception ex) {
            Assert.fail("Exception thrown: " + ex.getMessage());
        }

        QuerySelector selector = new QuerySelector(node);
        EnvironNode env = (EnvironNode)selector.nextEnvironNode(null).get();
        Assert.assertNotNull(env);
        Assert.assertEquals("center", env.name);

        TextNode text = (TextNode)selector.next(QuerySelector.MATCH_TEXT, null).get();
        Assert.assertNotNull(text);
        Assert.assertEquals("Hello", text.text);
    }

    @Test
    public void testParagraph() {
        NodeBase node = null;
        /**
         * Result is (pseudo tex code):
         * <code>\par \begin{center}1\end{center}
         * \par \begin{center}2\end{center}
         * \par \begin{center}3\end{center}</code>
         */
        try {
            node = convert("<div>1</div><span>2</span><p>3</p>");
        } catch (Exception ex) {
            Assert.fail("Exception thrown: " + ex.getMessage());
        }

        for (int i = 1; i <= 3; i++) {
            QuerySelector s = new QuerySelector(node);
            s = s.next(MATCH_PAR, i).nextEnvironNode(null).next(QuerySelector.MATCH_TEXT, null);
            TextNode text = (TextNode)s.get();
            Assert.assertNotNull(text);
            Assert.assertEquals("" + i, text.text);
        }
    }

    /**
     * Checks boldening and italics.
     */
    @Test
    public void testTextManipulate() {
        NodeBase node = null;
        try {
            node = convert("<div style=\"text-align: left\"><b>1</b><i>2</i></div>");
        } catch (Exception ex) {
            Assert.fail("Exception thrown: " + ex.getMessage());
        }

        QuerySelector s = new QuerySelector(node);
        s = s.next(MATCH_PAR, null);
        CommandNode cmd1 = (CommandNode)(s.copy().nextCommandNode(1).get());
        Assert.assertEquals("\\textbf", cmd1.name);

        CommandNode cmd2 = (CommandNode)(s.nextCommandNode(2).get());
        Assert.assertEquals("\\textit", cmd2.name);
    }

    /**
     * Test coloring text.
     */
    @Test
    public void testTextManipulate2() {
        NodeBase node = null;
        try {
            node = convert("<div style=\"text-align: left\"><font style=\"color: rgb(255,255,255)\">white</font>"
                           + "<font style=\"color: rgb(0,0,0)\">black</font></div>");
        } catch (Exception ex) {
            Assert.fail("Exception thrown: " + ex.getMessage());
        }

        QuerySelector s = new QuerySelector(node);
        s = s.next(MATCH_PAR, null);
        QuerySelector s1 = s.copy();

        CommandNode cmd1 = (CommandNode)(s1.nextCommandNode(1).get());
        Assert.assertEquals("\\textcolor", cmd1.name);
        NodeBase colorSpec1 = s1.copy().arg(2).get();
        Assert.assertEquals("FFFFFF", colorSpec1.toString());
        Assert.assertEquals("white", s1.arg(-1).get().toString());

        /* Black is LaTex's default color, so a command is not needed. */
        Assert.assertEquals("black", s.next(QuerySelector.MATCH_ANY, 2).get().toString());
    }
}
