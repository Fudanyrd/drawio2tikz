package edu.fudan.jtex;

import org.junit.Assert;
import org.junit.Test;

public class TestCommentNode {
    @Test
    public void testToString() {
        CommentNode node = new CommentNode("% This is a comment\n");
        String expected = "% This is a comment\n";
        Assert.assertEquals(expected, node.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidComment() {
        new CommentNode("This is not a comment");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidComment2() {
        new CommentNode(""); /* construct with empty string */
    }
}
