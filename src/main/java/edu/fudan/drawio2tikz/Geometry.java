package edu.fudan.drawio2tikz;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link Geometry} class represents data members pocessed by
 * all draw.io shapes.
 */
public abstract class Geometry {
    public double x, y;
    public double width, height;
    public Color drawColor;

    /**
     * One unit in tikz picture is roughly 40 units in drawio. This
     * factor is used to scale down the coordinates and dimensions of
     * shapes.
     */
    public static final double SCALE_FACTOR = 1.0 / 40;

    public Geometry(double x, double y, double width, double height, Color drawColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.drawColor = drawColor;
    }

    public abstract String draw(Context ctx);

    protected void registerColor(Context ctx, Color color) {
        if (color != null) {
            ctx.colors.add(color);
        }
    }

    /**
     * Formats the attribute list for a tikz command. For example, if the attribute
     * list contains "color" -> "red" and "thick" -> null, the output will be
     * "[color=red, thick]".
     */
    public static void formatAttrList(HashMap<String, String> attrList, StringBuilder sb) {
        if (attrList.isEmpty()) {
            return;
        }
        sb.append("[");
        boolean first = true;
        for (Map.Entry<String, String> entry : attrList.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            String k = entry.getKey();
            String v = entry.getValue();
            if (v == null) {
                sb.append(k);
            } else {
                sb.append(k).append("=").append(v);
            }
            first = false;
        }
        sb.append("]");
    }

    public static void formatCoordinate(double x, double y, StringBuilder sb) {
        /**
         * in drawio, y coordinate goes downwards, while in tikz it goes upwards,
         * so we need to negate the y coordinate.
         */
        sb.append(String.format("(%.2f, %.2f)", x * SCALE_FACTOR, (-y) * SCALE_FACTOR));
    }

    public static void formatCoordinate(Point p, StringBuilder sb) { formatCoordinate(p.x, p.y, sb); }
}
