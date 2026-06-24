package edu.fudan.drawio2tikz;

import java.util.ArrayList;
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

    public static HashMap<String, String> parseStyle(String style) {
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

    private static Line createLine(Element mxCellNode, Node sourcePointNode, Node targetPointNode, Node arrayNode) {
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
        if (arrayNode != null) {
            NodeList mxPointNodes = ((Element)arrayNode).getElementsByTagName("mxPoint");
            ret.array = new ArrayList<>();
            for (int i = 0; i < mxPointNodes.getLength(); i++) {
                Node mxPointNode = mxPointNodes.item(i);
                double x = getCoordinateAttr("x", mxPointNode);
                double y = getCoordinateAttr("y", mxPointNode);
                ret.array.add(new Point(x, y));
            }
        }
        return ret;
    }

    private static Color getColorOrDefault(HashMap<String, String> styleMap, String key, Color defaultColor) {
        String colorSpec = styleMap.get(key);
        if (colorSpec == null) {
            return defaultColor;
        }
        if (colorSpec.equals("none")) {
            /**
             * Really do not use any color.
             */
            return null;
        }
        return new Color(colorSpec.substring(1));
    }

    private static Shape createShape(Element mxCellNode, Element mxGeometryNode) {
        /* not implemented. */
        double x = getCoordinateAttr("x", mxGeometryNode);
        double y = getCoordinateAttr("y", mxGeometryNode);
        double width = getCoordinateAttr("width", mxGeometryNode);
        double height = getCoordinateAttr("height", mxGeometryNode);
        HashMap<String, String> styleMap = parseStyle(mxCellNode.getAttribute("style"));
        Color drawColor = getColorOrDefault(styleMap, "strokeColor", new Color("000000"));

        Shape ret = new Shape(x, y, width, height, drawColor);
        ret.shapeKind = Shape.createShapeKind(styleMap);
        ret.fillColor = getColorOrDefault(styleMap, "fillColor", new Color("FFFFFF"));
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
        if (Shape.requireStyleMap(ret.shapeKind)) {
            ret.restStyle = styleMap;
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

        int sourcePointIdx = -1;
        int targetPointIdx = -1;
        try {
            int numMxPointNodes = mxPointNodes.getLength();
            for (int i = 0; i < numMxPointNodes; i++) {
                Element mxPointNode = (Element)mxPointNodes.item(i);
                Node namedItem = mxPointNode.getAttributes().getNamedItem("as");
                if (namedItem == null) {
                    continue;
                }
                String asValue = namedItem.getNodeValue();
                if (asValue.equals("sourcePoint")) {
                    sourcePointIdx = i;
                } else if (asValue.equals("targetPoint")) {
                    targetPointIdx = i;
                }
            }
        } catch (DOMException e) {
            return null;
        }

        if (sourcePointIdx >= 0) {
            assert targetPointIdx >= 0;
            NodeList arrayNodes = mxGeometryNode.getElementsByTagName("Array");
            Node arrayNode = arrayNodes.getLength() > 0 ? arrayNodes.item(0) : null;
            return createLine(node, mxPointNodes.item(sourcePointIdx), mxPointNodes.item(targetPointIdx), arrayNode);
        }

        return createShape(node, mxGeometryNode);
    }
}
