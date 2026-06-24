package edu.fudan.drawio2tikz;

import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LineRecomputer extends Rewriter {
    public LineRecomputer(NodeList mxCellNodes, List<Geometry> geometries, Map<String, Integer> idToIndex) {
        super(mxCellNodes, geometries, idToIndex);
    }

    private static Double getEntryOrExitAttribute(Map<String, String> style, String key) {
        if (!style.containsKey(key)) {
            return null;
        }
        try {
            double ret = Double.parseDouble(style.get(key));
            if (ret < 0.0 || ret > 1.0) {
                throw new IllegalArgumentException(String.format("style attribute %s is not in [0, 1].", key));
            }
            return ret;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("style attribute %s is not a valid double.", key));
        }
    }

    @Override
    public void rewrite(int idx) {
        Element mxCellNode = (Element)mxCellNodes.item(idx);
        Geometry geometry = geometries.get(idx);
        if (geometry == null) {
            return;
        }
        if (!(geometry instanceof Line)) {
            /**
             * Not a line.
             * Do not have to rewrite it.
             */
            return;
        }
        String sourceId = mxCellNode.getAttribute("source");
        String targetId = mxCellNode.getAttribute("target");
        if (sourceId.equals("") || targetId.equals("")) {
            /**
             * Not a line connecting two geometries.
             * Do not have to rewrite it.
             */
            return;
        }
        if (!idToIndex.containsKey(sourceId) || !idToIndex.containsKey(targetId)) {
            /**
             * The source or target geometry is not in the list of geometries.
             * Rewrite is not possible, delete this line.
             */
            geometries.set(idx, null);
            return;
        }
        Geometry source = geometries.get(idToIndex.get(sourceId));
        Geometry target = geometries.get(idToIndex.get(targetId));
        assert source != null && target != null : "source and target geometries must not be null";
        Line line = (Line)geometry;
        Map<String, String> styleMap = GeometryFactory.parseStyle(mxCellNode.getAttribute("style"));

        Double exitX;
        try {
            exitX = getEntryOrExitAttribute(styleMap, "exitX");
        } catch (IllegalArgumentException e) {
            System.err.println(
                String.format("Failed to parse exitX for line %s. Deleting this line.", mxCellNode.getAttribute("id")));
            geometries.set(idx, null);
            return;
        }

        Double exitY;
        try {
            exitY = getEntryOrExitAttribute(styleMap, "exitY");
        } catch (IllegalArgumentException e) {
            System.err.println(
                String.format("Failed to parse exitY for line %s. Deleting this line.", mxCellNode.getAttribute("id")));
            geometries.set(idx, null);
            return;
        }

        Double entryX;
        try {
            entryX = getEntryOrExitAttribute(styleMap, "entryX");
        } catch (IllegalArgumentException e) {
            System.err.println(String.format("Failed to parse entryX for line %s. Deleting this line.",
                                             mxCellNode.getAttribute("id")));
            geometries.set(idx, null);
            return;
        }

        Double entryY;
        try {
            entryY = getEntryOrExitAttribute(styleMap, "entryY");
        } catch (IllegalArgumentException e) {
            System.err.println(String.format("Failed to parse entryY for line %s. Deleting this line.",
                                             mxCellNode.getAttribute("id")));
            geometries.set(idx, null);
            return;
        }

        if (exitX != null && exitY != null) {
            if (entryX == null) {
                /**
                 * Assume that the average of exitX, entryX
                 * is 0.5
                 */
                entryX = 1.0 - exitX;
            }
            if (entryY == null) {
                /**
                 * Assume that the average of exitY, entryY
                 * is 0.5
                 */
                entryY = 1.0 - exitY;
            }
        } else {
            if (entryX == null || entryY == null) {
                /**
                 * If entryX or entryY is not specified,
                 * then exitX and exitY must be specified.
                 */
                System.err.println(String.format(
                    "Line %s does not have enough information to compute its geometry. Deleting this line.",
                    mxCellNode.getAttribute("id")));
                geometries.set(idx, null);
                return;
            }
            if (exitX == null) {
                exitX = 1.0 - entryX;
            }
            if (exitY == null) {
                exitY = 1.0 - entryY;
            }
        }

        double sourceX = source.x + exitX * source.width;
        double sourceY = source.y + exitY * source.height;
        double targetX = target.x + entryX * target.width;
        double targetY = target.y + entryY * target.height;
        line.x = sourceX;
        line.y = sourceY;
        line.width = targetX - sourceX;
        line.height = targetY - sourceY;
        geometries.set(idx, line);
    }
}
