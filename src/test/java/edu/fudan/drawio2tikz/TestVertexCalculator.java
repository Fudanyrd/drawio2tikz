package edu.fudan.drawio2tikz;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class TestVertexCalculator {

    private static void comparePoints(Point[] expected, List<Point> actual) {
        Assert.assertEquals(expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(expected[i].x, actual.get(i).x, 1e-3);
            Assert.assertEquals(expected[i].y, actual.get(i).y, 1e-3);
        }
    }

    @Test
    public void testRectangle() {
        final double width = 60;
        final double height = 80;
        Point[] expected = new Point[] {
            new Point(-30, -40),
            new Point(30, -40),
            new Point(30, 40),
            new Point(-30, 40),
        };

        List<Point> actual = Shape.VertexCalculator.verticesOfRectangle(width, height);
        comparePoints(expected, actual);
    }

    @Test
    public void testEllipse() {
        final double width = 60;
        final double height = 80;

        List<Point> actual = Shape.VertexCalculator.vertices(Shape.ShapeKind.ELLIPSE, width, height, null);
        Assert.assertNull(actual);
    }

    @Test
    public void testTriangle() {
        final double width = 60;
        final double height = 80;
        Point[] expected = new Point[] {
            new Point(-30, -40),
            new Point(30, 0),
            new Point(-30, 40),
        };

        List<Point> actual = Shape.VertexCalculator.verticesOfTriangle(width, height);
        comparePoints(expected, actual);
    }

    @Test
    public void testParallelogram() {
        final double width = 60;
        final double height = 80;
        final double size = 40;
        Point[] expected = new Point[] {
            new Point(-30, -40),
            new Point(-10, -40),
            new Point(30, 40),
            new Point(10, 40),
        };

        List<Point> actual = Shape.VertexCalculator.verticesOfParallelogram(width, height, size);
        comparePoints(expected, actual);

        try {
            Shape.VertexCalculator.verticesOfParallelogram(width, height, width * 2);
            Assert.fail("Expected IllegalArgumentException for incorrect size.");
        } catch (IllegalArgumentException e) {
            /* Expected exception */
        }
    }

    @Test
    public void testHexagon() {
        final double width = 60;
        final double height = 80;
        final double size = 20;
        Point[] expected = new Point[] {
            new Point(-30, 0), new Point(-10, -40), new Point(10, -40),
            new Point(30, 0),  new Point(10, 40),   new Point(-10, 40),
        };

        List<Point> actual = Shape.VertexCalculator.verticesOfHexagon(width, height, size);
        comparePoints(expected, actual);

        try {
            Shape.VertexCalculator.verticesOfHexagon(width, height, width);
            Assert.fail("Expected IllegalArgumentException for incorrect size.");
        } catch (IllegalArgumentException e) {
            /* Expected exception */
        }
    }

    @Test
    public void testTrapezoid() {
        final double width = 60;
        final double height = 80;
        final double size = 20;
        Point[] expected = new Point[] {
            new Point(-30, -40),
            new Point(30, -40),
            new Point(10, 40),
            new Point(-10, 40),
        };

        List<Point> actual = Shape.VertexCalculator.verticesOfTrapezoid(width, height, size);
        comparePoints(expected, actual);

        try {
            Shape.VertexCalculator.verticesOfTrapezoid(width, height, width);
            Assert.fail("Expected IllegalArgumentException for incorrect size.");
        } catch (IllegalArgumentException e) {
            /* Expected exception */
        }
    }

    @Test
    public void testRhombus() {
        final double width = 60;
        final double height = 80;
        Point[] expected = new Point[] {
            new Point(-30, 0),
            new Point(0, -40),
            new Point(30, 0),
            new Point(0, 40),
        };

        List<Point> actual = Shape.VertexCalculator.verticesOfDiamond(width, height);
        comparePoints(expected, actual);
    }
}
