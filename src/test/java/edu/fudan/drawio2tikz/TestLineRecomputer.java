package edu.fudan.drawio2tikz;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestLineRecomputer extends TestRewriter {
    @Test
    public void testRewrite() {
        String squareUpper =
            "<mxCell id=\"upr\"><mxGeometry x=\"0\" y=\"0\" width=\"40\" height=\"40\" as=\"geometry\"/></mxCell>";

        String squareLower =
            "<mxCell id=\"lwr\"><mxGeometry x=\"0\" y=\"80\" width=\"40\" height=\"40\" as=\"geometry\"/></mxCell>";

        String line =
            "<mxCell id=\"line\" style=\"endArrow=block;startArrow=none;exitX=0.5;exitY=1;entryX=0.5;entryY=0.5\" "
            + "source=\"upr\" target=\"lwr\" edge=\"1\">"
            + "<mxGeometry as=\"geometry\" width=\"50\" height=\"50\" relative=\"1\">"
            + "<mxPoint x=\"480\" y=\"340\" as=\"sourcePoint\"/>"
            + "<mxPoint x=\"530\" y=\"290\" as=\"targetPoint\"/>"
            + "</mxGeometry>"
            + "</mxCell>";

        String topLevel = "<root>" + squareUpper + squareLower + line + "</root>";
        try {
            prepare(topLevel);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        new LineRecomputer(this.mxCellNodes, this.geometries, this.idToIndex);
        Line lineGeometry = (Line)geometries.get(idToIndex.get("line"));
        Assert.assertNotNull(lineGeometry);
        Assert.assertEquals(lineGeometry.x, 20.0, 1e-3);
        Assert.assertEquals(lineGeometry.y, 40.0, 1e-3);
        Assert.assertEquals(lineGeometry.width, 0.0, 1e-3);
        Assert.assertEquals(lineGeometry.height, 60.0, 1e-3);
    }
}
