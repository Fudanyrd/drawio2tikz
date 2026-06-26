package edu.fudan.jtex;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class TestTextNode {
    @Test
    public void regression() {
        LineKeeper keeper = new LineKeeper();
        Formatter formatter = new Formatter(null, keeper);
        char[] a0 = {0xa0};
        Assert.assertTrue(TextNode.isWhitespace(new String(a0).charAt(0)));

        (new TextNode("1")).appendTo(formatter);
        (new TextNode("\t ")).appendTo(formatter);
        (new TextNode("2")).appendTo(formatter);
        (new TextNode(new String(a0))).appendTo(formatter);
        (new TextNode("3")).appendTo(formatter);
        (new TextNode("\n")).appendTo(formatter);
        (new TextNode("4")).appendTo(formatter);
        (new TextNode(" ")).appendTo(formatter);
        formatter.finish();

        ArrayList<String> lines = keeper.lines;
        Assert.assertEquals(2, lines.size());
        Assert.assertEquals("1 2 3\n", lines.get(0));
        Assert.assertEquals("4\n", lines.get(1));
    }
}
