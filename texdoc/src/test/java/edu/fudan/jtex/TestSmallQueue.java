package edu.fudan.jtex;

import org.junit.Assert;
import org.junit.Test;

public class TestSmallQueue {
    @Test
    public void testPushAndPop() {
        SmallQueue<Integer> queue = new SmallQueue<Integer>(3);
        queue.push(1);
        queue.push(2);
        queue.push(3);

        Assert.assertEquals(Integer.valueOf(1), queue.getAt(0));
        Assert.assertEquals(Integer.valueOf(2), queue.getAt(1));
        Assert.assertEquals(Integer.valueOf(2), queue.getAt(-2));
        Assert.assertEquals(Integer.valueOf(3), queue.getAt(2));

        queue.pop();
        Assert.assertEquals(Integer.valueOf(2), queue.getAt(0));
        Assert.assertEquals(Integer.valueOf(3), queue.getAt(1));
        Assert.assertEquals(Integer.valueOf(3), queue.getAt(-1));

        queue.pop();
        Assert.assertEquals(Integer.valueOf(3), queue.getAt(0));
        Assert.assertEquals(Integer.valueOf(3), queue.getAt(-1));

        queue.pop();
        Assert.assertEquals(0, queue.size());
    }

    @Test(expected = IllegalStateException.class)
    public void testOverflow() {
        SmallQueue<Integer> queue = new SmallQueue<Integer>(2);
        queue.push(1);
        queue.push(2);
        queue.push(3); /* This should throw an exception */
    }

    @Test(expected = IllegalStateException.class)
    public void testUnderflow() {
        SmallQueue<Integer> queue = new SmallQueue<Integer>(2);
        queue.pop(); /* This should throw an exception */
    }

    @Test
    public void testInplaceOp() {
        SmallQueue<String> queue = new SmallQueue<String>(2);
        queue.push("foo");
        /* queue.getAt(0) += "bar"; */
        do {
            String s = queue.getAt(0);
            s += "bar";
            queue.setCurrent(s);
        } while (false);
        Assert.assertEquals("foobar", queue.getAt(-1));
    }
}
