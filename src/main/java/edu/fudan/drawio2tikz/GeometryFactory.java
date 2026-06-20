package edu.fudan.drawio2tikz;

import java.util.HashMap;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GeometryFactory {
    private static double getCoordinateAttr(String attrName, Node pointNode) {
        try {
            return Double.parseDouble(pointNode.getAttributes().getNamedItem(attrName).getNodeValue());
        } catch (DOMException e) {
            throw new IllegalArgumentException(String.format("mxPoint node does not have attribute %s.", attrName));
        }
    }

    private static HashMap<String, String> parseStyle(String style) {
        HashMap<String, String> styleMap = new HashMap<>();
        String[] styleItems = style.split(";");
        for (String item : styleItems) {
            String[] keyValue = item.split("=");
            if (keyValue.length == 2) {
                styleMap.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                styleMap.put(item, null);
            }
        }
        return styleMap;
    }

    private static Line createLine(Element mxCellNode, Node sourcePointNode, Node targetPointNode) {
        double srcX = getCoordinateAttr("x", sourcePointNode);
        double srcY = getCoordinateAttr("y", sourcePointNode);
        double tgtX = getCoordinateAttr("x", targetPointNode);
        double tgtY = getCoordinateAttr("y", targetPointNode);
        HashMap<String, String> styleMap = parseStyle(mxCellNode.getAttribute("style"));
        String colorSpec = styleMap.get("strokeColor");
        Color drawColor = colorSpec == null ? null : /*remove the beginning hashtag. */
                              new Color(colorSpec.substring(1));

        Line ret = new Line(srcX, srcY, tgtX - srcX, tgtY - srcY, drawColor);
        boolean startArrow = styleMap.getOrDefault("startArrow", "none").equals("none") ? false : true;
        boolean endArrow = styleMap.getOrDefault("endArrow", "none").equals("none") ? false : true;
        ret.setStartArrow(startArrow).setEndArrow(endArrow);
        if (styleMap.containsKey("strokeWidth")) {
            try {
                ret.setStrokeWidth(Integer.parseInt(styleMap.get("strokeWidth")));
            } catch (NumberFormatException e) {
                /* ignore invalid strokeWidth. */
            }
        }
        return ret;
    }

    private static Shape createShape(Element mxCellNode, Element mxGeometryNode) {
        /* not implemented. */
        double x = getCoordinateAttr("x", mxGeometryNode);
        double y = getCoordinateAttr("y", mxGeometryNode);
        double width = getCoordinateAttr("width", mxGeometryNode);
        double height = getCoordinateAttr("height", mxGeometryNode);
        HashMap<String, String> styleMap = parseStyle(mxCellNode.getAttribute("style"));
        String colorSpec = styleMap.get("strokeColor");
        Color drawColor = colorSpec == null ? null : /*remove the beginning hashtag. */
                              new Color(colorSpec.substring(1));

        Shape ret = new Shape(x, y, width, height, drawColor);
        ret.shapeKind = Shape.createShapeKind(styleMap);
        ret.fillColor = styleMap.containsKey("fillColor") ? new Color(styleMap.get("fillColor").substring(1)) : null;
        ret.gradient = Shape.Gradient.createGradientIfSpecified(styleMap);
        ret.roundedCorners = styleMap.containsKey("rounded") && styleMap.get("rounded").equals("1");
        ret.rotate = styleMap.containsKey("rotation") ? Double.parseDouble(styleMap.get("rotation")) : 0;
        if (styleMap.containsKey("strokeWidth")) {
            try {
                ret.strokeWidth = Integer.parseInt(styleMap.get("strokeWidth"));
            } catch (NumberFormatException e) {
                /* ignore invalid strokeWidth. */
            }
        }
        if (mxCellNode.hasAttribute("value")) {
            try {
                String html = mxCellNode.getAttribute("value");
                // System.err.println(html);
                String innerTexCode = Html2Tex.convert(html);
                if (!innerTexCode.isEmpty()) {
                    ret.innerTexCode = "\\begin{tabular}{p{\\textwidth}}\n" + innerTexCode + "\n\\end{tabular}";
                }
            } catch (Exception ex) {
                /* ignore exceptions during inner text conversion. */
                System.err.println(ex.getMessage());
            }
        }
        return ret;
    }

    public static Geometry createGeometry(Element node) {
        assert node.getNodeName().equals("mxCell");
        Element mxGeometryNode = (Element)node.getElementsByTagName("mxGeometry").item(0);
        if (mxGeometryNode == null) {
            return null;
        }

        NodeList mxPointNodes = mxGeometryNode.getElementsByTagName("mxPoint");
        int numMxPointNodes = mxPointNodes.getLength();

        String asAttr = "";
        try {
            Element firstChild = (Element)mxPointNodes.item(0);
            if (firstChild != null) {
                asAttr = firstChild.getAttributes().getNamedItem("as").getNodeValue();
            }
        } catch (DOMException e) {
            return null;
        }

        if (asAttr.equals("sourcePoint") || asAttr.equals("targetPoint")) {
            if (numMxPointNodes != 2) {
                /* line node has only two mxPoint nodes, representing source and target. */
                return null;
            }
            int sourcePointIdx, targetPointIdx;
            if (asAttr.equals("sourcePoint")) {
                sourcePointIdx = 0;
                targetPointIdx = 1;
            } else {
                sourcePointIdx = 1;
                targetPointIdx = 0;
            }
            return createLine(node, mxPointNodes.item(sourcePointIdx), mxPointNodes.item(targetPointIdx));
        }

        return createShape(node, mxGeometryNode);
    }
}
