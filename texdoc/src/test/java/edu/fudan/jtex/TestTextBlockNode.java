package edu.fudan.jtex;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class TestTextBlockNode {
    @Test
    public void testToString() {
        ArrayList<NodeBase> children = new ArrayList<NodeBase>();
        children.add(new TextNode("foo"));

        /* Test that curly braces are added. */
        NodeBase block = new TextBlockNode(children);
        Assert.assertEquals("{foo}", block.toString());
    }
}
