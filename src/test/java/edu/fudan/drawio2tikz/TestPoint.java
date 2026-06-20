package edu.fudan.drawio2tikz;

import org.junit.Assert;
import org.junit.Test;

public class TestPoint {
    @Test
    public void testRotateBy() {
        Point p = new Point(1, 0);
        p.rotateBy(-45); /* clockwise rotation by 45 degree */
        Assert.assertEquals(p.x, p.y, 1e-6);
        p.rotateBy(-45);
        Assert.assertEquals(0.0, p.x, 1e-6);
        Assert.assertEquals(1.0, p.y, 1e-6);

        Point p2 = new Point(1, 0);
        p2.rotateBy(-90);
        Assert.assertEquals(p2.x, 0.0, 1e-6);
        Assert.assertEquals(p2.y, 1.0, 1e-6);
    }
}
