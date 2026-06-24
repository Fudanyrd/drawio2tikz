package edu.fudan.drawio2tikz;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.Assert;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class TestRewriter {
    protected ArrayList<Geometry> geometries;
    protected HashMap<String, Integer> idToIndex;
    protected NodeList mxCellNodes;

    public TestRewriter() {
        geometries = null;
        idToIndex = null;
        mxCellNodes = null;
    }

    /**
     * Prepares the test environment by parsing the provided XML string and
     * initializing the geometries, idToIndex, and mxCellNodes.
     */
    protected void prepare(String rootXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Element root = builder.parse(new ByteArrayInputStream(rootXml.getBytes())).getDocumentElement();

        mxCellNodes = root.getElementsByTagName("mxCell");
        geometries = new ArrayList<Geometry>();
        idToIndex = new HashMap<String, Integer>();

        for (int i = 0; i < mxCellNodes.getLength(); i++) {
            Geometry geometry = null;
            Element mxCellNode = (Element)mxCellNodes.item(i);
            try {
                geometry = GeometryFactory.createGeometry(mxCellNode);
                geometries.add(geometry);
                idToIndex.put(mxCellNode.getAttribute("id"), i);
            } catch (Exception ex) {
                Assert.fail(ex.getMessage());
            }
            Assert.assertNotNull(geometry);
        }
    }
}
