package edu.fudan.drawio2tikz;

import java.io.ByteArrayInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Assert;
import org.junit.Test;

public class TestGeometryFactory {
    private static String drawIOLineCode =
        "<mxCell id=\"lTB81YqXVApzBoE-dytH-6\" edge=\"1\" parent=\"1\" "
        + "style=\"endArrow=none;html=1;rounded=0;strokeColor=#0000FF;strokeWidth=2;startArrow=open;startFill=0;\" "
        + "value=\"\">\n  <mxGeometry height=\"50\" relative=\"1\" width=\"50\" as=\"geometry\">\n    <mxPoint "
        + "x=\"40\" y=\"160\" as=\"sourcePoint\" />\n    <mxPoint x=\"40\" y=\"40\" as=\"targetPoint\" />\n  "
        + "</mxGeometry>\n</mxCell>";
    private static String drawIOShapeCode =
        "<mxCell "
        + "style=\"ellipse;whiteSpace=wrap;html=1;fillColor=#FF0D05;rotation=37.5;strokeColor=#0000CC;gradientColor=#"
        + "CC0000;gradientDirection=north;strokeWidth=2;\"><mxGeometry height=\"60\" width=\"80\" x=\"50\" y=\"70\" "
        + "/></mxCell>";

    private static double DELTA = 1e-3;

    private static Geometry createGeometryFromCode(String code) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return GeometryFactory.createGeometry(
            builder.parse(new ByteArrayInputStream(code.getBytes())).getDocumentElement());
    }

    @Test
    public void testCreateLine() throws Exception {
        Geometry geometry = createGeometryFromCode(drawIOLineCode);

        Assert.assertTrue(geometry instanceof Line);
        Line line = (Line)geometry;

        Assert.assertEquals(40, line.x, DELTA);
        Assert.assertEquals(160, line.y, DELTA);
        Assert.assertEquals(0, line.width, DELTA);
        Assert.assertEquals(-120, line.height, DELTA);
        Assert.assertEquals(new Color("0000FF"), line.drawColor);
        Assert.assertTrue(line.startArrow);
        Assert.assertFalse(line.endArrow);
    }

    @Test
    public void testCreateShape() {
        Geometry geometry = null;
        try {
            geometry = createGeometryFromCode(drawIOShapeCode);
        } catch (Exception e) {
            Assert.fail(String.format("Failed to create shape from code: %s.", e.getMessage()));
        }

        Assert.assertTrue(geometry instanceof Shape);
        Shape shape = (Shape)geometry;

        Assert.assertEquals(50, shape.x, DELTA);
        Assert.assertEquals(70, shape.y, DELTA);
        Assert.assertEquals(80, shape.width, DELTA);
        Assert.assertEquals(60, shape.height, DELTA);
        Assert.assertEquals(new Color("0000CC"), shape.drawColor);
        Assert.assertEquals(new Color("FF0D05"), shape.fillColor);
        Assert.assertEquals(Shape.ShapeKind.ELLIPSE, shape.shapeKind);
        Assert.assertEquals(37.5, shape.rotate, DELTA);

        Shape.Gradient expectedGradient = new Shape.Gradient(Shape.Gradient.Direction.NORTH, new Color("CC0000"));
        Assert.assertEquals(expectedGradient, shape.gradient);
    }
}
