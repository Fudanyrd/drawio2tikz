package edu.fudan.drawio2tikz;

import java.util.HashMap;

/**
 * Represents a line of drawio.
 *
 * <h3>Implemented Attributes</h3>
 * <ul>
 *   <li>sourcePoint</li>
 *   <li>targetPoint</li>
 *   <li>strokeWidth</li>
 *   <li>strokeColor</li>
 *   <li>startArrow and endArrow (only perform none test; if not none, assuming is
 *   an arrow, i.e. "->" in tikz)</li>
 * </ul>
 */
public class Line extends Geometry {
    /**
     * From attribute strokeWidth (drawio defaults to 1)
     */
    public int strokeWidth;

    public boolean startArrow;
    public boolean endArrow;

    public Line setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
        return this;
    }

    public Line setStartArrow(boolean startArrow) {
        this.startArrow = startArrow;
        return this;
    }

    public Line setEndArrow(boolean endArrow) {
        this.endArrow = endArrow;
        return this;
    }

    /**
     * @param x x coordiate of sourcePoint
     * @param y y coordiate of sourcePoint
     * @param width equals to targetPoint.x - sourcePoint.x
     * @param height equals to targetPoint.y - sourcePoint.y
     * @param drawColor from strokeColor
     */
    public Line(double x, double y, double width, double height, Color drawColor) {
        super(x, y, width, height, drawColor);
        this.strokeWidth = 1;
    }

    @Override
    public String draw(Context ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("\\draw");

        HashMap<String, String> attrList = new HashMap<>();
        attrList.put("line width", strokeWidth + "pt");

        if (startArrow) {
            if (endArrow) {
                attrList.put("<->", null);
            } else {
                attrList.put("<-", null);
            }
        } else {
            if (endArrow) {
                attrList.put("->", null);
            } else {
                /* no arrow */
            }
        }

        if (this.drawColor != null) {
            registerColor(ctx, drawColor);
            String colorName = drawColor.uniqueName();
            attrList.put(colorName, null);
        } /* else tikz default color will be used */
        formatAttrList(attrList, sb);

        /* draw the coordiate of source vertex. */
        double curX = x;
        double curY = y;
        formatCoordinate(curX, curY, sb);
        sb.append(" -- ");

        /* draw the coordiate of target vertex. */
        curX += width;
        curY += height;
        formatCoordinate(curX, curY, sb);
        sb.append(";");

        return sb.toString();
    }
}
