package edu.fudan.drawio2tikz;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * It represents a solid shape, including triangles, rectangles,
 * ellipses, etc.
 *
 * <h3>Implemented Shapes
 * See {@link ShapeKind} for the list of supported shapes.
 * <ul>
 *   <li>rectangle</li>
 *   <li>ellipse</li>
 *   <li>triangle</li>
 *   <li>parallelogram</li>
 *   <li>hexagon</li>
 *   <li>trapezoid</li>
 *   <li>diamond</li>
 * </ul>
 *
 * <h3>Implemented Attributes</h3>
 * <ul>
 *   <li>rounded corners</li>
 *   <li>fill color plus gradient (in limited directions, see {@link Gradient.Direction})</li>
 *   <li>rotation</li>
 * </ul>
 */
public class Shape extends Geometry {
    /**
     * List of supported shape kinds.
     */
    public static enum ShapeKind {
        RECTANGLE,
        ELLIPSE,
        TRIANGLE,
        PARALLELOGRAM,
        HEXAGON,
        TRAPEZOID,
        DIAMOND,
        /**
         * Implement more shapes of draw.io in the future, by computing the
         * coordinates of the shape boundary in the dumpCoordinates method
         * and using appropriate tikz commands in the draw method.
         */
    }
    ;

    /**
     * Collection of methods for calculating the coordinates of shape
     * vertices (before a shape is rotated),
     * which are used for drawing the shape boundary in tikz.
     */
    public static class VertexCalculator {
        public static List<Point> verticesOfRectangle(double width, double height) {
            double dx = width * 0.5;
            double dy = height * 0.5;
            List<Point> ret = new ArrayList<>();
            /**
             * Requirements for the list ordering is specified in document of
             * {@code vertices}.
             */
            ret.add(new Point(-dx, -dy));
            ret.add(new Point(dx, -dy));
            ret.add(new Point(dx, dy));
            ret.add(new Point(-dx, dy));
            return ret;
        }

        public static List<Point> verticesOfTriangle(double width, double height) {
            /**
             * triangle layout on draw.io
             * before rotation:
             * <blockquote>
             * V1
             * |
             * D ---- V3
             * |
             * V2</blockquote>
             * where len(V1--D) == len(D--V2).
             * Its center is the middle point of D and V3.
             */
            double dx = width / 2;
            double dy = height / 2;
            List<Point> ret = new ArrayList<>();
            /* Starting at V2 */
            ret.add(new Point(-dx, -dy));
            ret.add(new Point(dx, 0));
            ret.add(new Point(-dx, dy));
            return ret;
        }

        public static List<Point> verticesOfParallelogram(double width, double height, double size) {
            /**
             * Layout of parallelogram:
             *
             * <blockquote>
             *     A --- B
             *   /     / |
             *  D --- C--E
             * </blockquote>
             * where CE is orthogonal to BE;
             * D, C, B, A are the vertices;
             * width is DE,  height is BE, and size is CE.
             */
            if (size < 0.0) {
                throw new IllegalArgumentException("Size of a parallelogram should be non-negative.");
            }
            double dx = width / 2;
            double dy = height / 2;
            List<Point> ret = new ArrayList<>();
            /* starting from D */
            ret.add(new Point(-dx, -dy));
            ret.add(new Point(dx - size, -dy));
            ret.add(new Point(dx, dy));
            ret.add(new Point(-dx + size, dy));
            return ret;
        }

        public static List<Point> verticesOfHexagon(double width, double height, double size) {
            /**
             * Layout of hexagon:
             *
             * <blockquote>
             *     A --- B
             *   /         \
             *  F --------- C
             *   \         /|
             *     E --- D -G
             * </blockquote>
             * where CG is orthogonal to DG;
             * A, B, C, D, E, F are the vertices;
             * width is FC, height is AE (or BD), and size is DG.
             */
            if (size < 0.0) {
                throw new IllegalArgumentException("Size of a hexagon should be non-negative.");
            }
            double dx = width / 2;
            double dy = height / 2;
            List<Point> ret = new ArrayList<>();
            /* starting from F; then EDCBA */
            ret.add(new Point(-dx, 0));
            ret.add(new Point(-dx + size, -dy));
            ret.add(new Point(dx - size, -dy));
            ret.add(new Point(dx, 0));
            ret.add(new Point(dx - size, dy));
            ret.add(new Point(-dx + size, dy));
            return ret;
        }

        public static List<Point> verticesOfTrapezoid(double width, double height, double size) {
            /**
             * Layout of trapezoid:
             *
             * <blockquote>
             *  E--A -- B--F
             *  |/        \|
             *  D -------- C
             * </blockquote>
             * where CDEF is a rectangle;
             * A, B, C, D are the vertices;
             * width is DC, height is AD, and size is AE + BF.
             */
            if (size < 0.0) {
                throw new IllegalArgumentException("Size of a trapezoid should be non-negative.");
            }
            double dx = width / 2;
            double dy = height / 2;
            size *= 0.5;
            List<Point> ret = new ArrayList<>();
            /* starting from D; then CBA */
            ret.add(new Point(-dx, -dy));
            ret.add(new Point(dx, -dy));
            ret.add(new Point(dx - size, dy));
            ret.add(new Point(-dx + size, dy));
            return ret;
        }

        public static List<Point> verticesOfDiamond(double width, double height) {
            double dx = width / 2;
            double dy = height / 2;
            List<Point> ret = new ArrayList<>();
            /* starting from the left vertex; then down, right, up */
            ret.add(new Point(-dx, 0));
            ret.add(new Point(0, -dy));
            ret.add(new Point(dx, 0));
            ret.add(new Point(0, dy));
            return ret;
        }

        private static double f64GetOrDefault(Map<String, String> style, double value) {
            if (style == null) {
                return value;
            }
            String valueStr = style.getOrDefault("size", null);
            if (valueStr == null) {
                return value;
            }
            try {
                return Double.parseDouble(valueStr);
            } catch (NumberFormatException e) {
                /* ignore invalid size. */
                System.err.println("cannot parse size value: " + valueStr);
                return value;
            }
        }

        /**
         * @param width: width of the shape.
         * @param height: height of the shape.
         * @param style: style of the shape, which may
         * contain some attributes that affect the shape layout, e.g. "rounded" for rectangle.
         *
         * @return difference to the center of the shape for each of the vertex.
         * For testing and plotting, its return value should:
         * <ul>
         *   <li>Place the point whose x and y coordinates are <b>non-positive</b> at the start;
         *    if more than one such points exist, place the one with <b>largest y coordinate</b>
         *    at the start. </li>
         *   <li>Order the points in counter-clockwise direction.</li>
         * </ul>
         */
        public static List<Point> vertices(ShapeKind kind, double width, double height, Map<String, String> style) {
            switch (kind) {
            case RECTANGLE:
                return verticesOfRectangle(width, height);
            case ELLIPSE:
                /* not implemented, since tikz does not require the coordinates of ellipse vertices. */
                return null;
            case TRIANGLE:
                return verticesOfTriangle(width, height);
            case PARALLELOGRAM: {
                return verticesOfParallelogram(width, height, f64GetOrDefault(style, 0.0));
            }
            case HEXAGON: {
                return verticesOfHexagon(width, height, f64GetOrDefault(style, 0.0));
            }
            case TRAPEZOID: {
                return verticesOfTrapezoid(width, height, f64GetOrDefault(style, 0.0));
            }
            case DIAMOND: {
                return verticesOfDiamond(width, height);
            }
            default: {
                throw new IllegalArgumentException("Unsupported shape kind: " + kind);
            }
            }
        }
    }

    public static ShapeKind createShapeKind(String shapeName) {
        switch (shapeName) {
        case "rectangle":
            return ShapeKind.RECTANGLE;
        case "ellipse":
            return ShapeKind.ELLIPSE;
        case "triangle":
            return ShapeKind.TRIANGLE;
        case "parallelogram": {
            return ShapeKind.PARALLELOGRAM;
        }
        case "hexagon": {
            return ShapeKind.HEXAGON;
        }
        case "trapezoid": {
            return ShapeKind.TRAPEZOID;
        }
        case "rhombus":
        case "diamond": {
            return ShapeKind.DIAMOND;
        }
        default:
            return null;
        }
    }

    /**
     * Certain shape kinds require the style map to compute the coordinates
     * of shape vertices, e.g. size of a hexagon.
     */
    public static boolean requireStyleMap(ShapeKind shapeKind) {
        switch (shapeKind) {
        case PARALLELOGRAM:
        case HEXAGON:
        case TRAPEZOID:
            return true;
        case RECTANGLE:
        case ELLIPSE:
        case TRIANGLE:
        case DIAMOND:
        default:
            return false;
        }
    }

    public static ShapeKind createShapeKind(Map<String, String> style) {
        if (style.containsKey("shape")) {
            String shapeName = style.get("shape");
            ShapeKind kind = createShapeKind(shapeName);
            return kind;
        }
        for (String key : style.keySet()) {
            if (style.get(key) != null) {
                continue;
            }
            String possibleShapeName = key;
            ShapeKind kind = createShapeKind(possibleShapeName);
            if (kind != null) {
                return kind;
            }
        }
        /* return default shape kind. */
        return ShapeKind.RECTANGLE;
    }

    public static class Gradient {
        /**
         * All directions supported by draw.io
         */
        public static enum Direction {
            WEST,
            EAST,
            NORTH,
            SOUTH,
        }
        ;
        public Direction direction;
        public Color endColor;

        public Gradient(Direction direction, Color endColor) {
            this.direction = direction;
            this.endColor = endColor;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Gradient other = (Gradient)obj;
            return direction == other.direction && endColor.equals(other.endColor);
        }

        public static Gradient createGradientIfSpecified(Map<String, String> style) {
            if (!style.containsKey("gradientColor")) {
                return null;
            }
            String directionStr = style.getOrDefault("gradientDirection", "south");
            Direction direction = Direction.SOUTH;
            switch (directionStr) {
            case "west":
                direction = Direction.WEST;
                break;
            case "east":
                direction = Direction.EAST;
                break;
            case "north":
                direction = Direction.NORTH;
                break;
            }
            Color endColor = new Color(style.get("gradientColor").substring(1));
            return new Gradient(direction, endColor);
        }

        public void draw(StringBuilder sb, Color startColor, double rotation) {
            /**
             * Example (when direction is east):
             * <blockquote><pre>
             *   left color=C0000FF,  % is startColor
             *   right color=CFFFF00 % is this.endColor
             * </pre></blockquote>
             */
            String startDirStr, endDirStr;
            double angle = 0;
            switch (direction) {
            case WEST:
                angle = 180;
                break;
            case EAST:
                angle = 0;
                break;
            case NORTH:
                angle = 90;
                break;
            case SOUTH:
                angle = -90;
                break;
            default:
                return; /* unreachable */
            }

            /* normalize angle to (-180, 180] */
            angle -= rotation;
            if (angle > 180) {
                angle -= 360;
            } else if (angle <= -180) {
                angle += 360;
            }

            /**
             * Tikz shading library only supports gradient
             * in 8 directions, so we need to map the angle to the nearest direction.
             * i.e. It is not guaranteed that the gradient direction in tikz is
             * exactly the same as that in draw.io, but it is the best effort to approximate it.
             */
            if (angle < -135) {
                startDirStr = "right";
                endDirStr = "left";
            } else if (angle < -45) {
                startDirStr = "top";
                endDirStr = "bottom";
            } else if (angle < 45) {
                startDirStr = "left";
                endDirStr = "right";
            } else if (angle < 135) {
                startDirStr = "bottom";
                endDirStr = "top";
            } else {
                startDirStr = "right";
                endDirStr = "left";
            }

            if (startColor != null) {
                sb.append(startDirStr).append(" color=").append(startColor.uniqueName()).append(", ");
            }
            assert endColor != null;
            sb.append(endDirStr).append(" color=").append(endColor.uniqueName());
        }
    };

    public ShapeKind shapeKind;

    /**
     * Corresponds to key "fillColor" in style string.
     */
    public Color fillColor;

    /**
     * Initialize it from key "gradientDirection" and "gradientColor"
     * in style string.
     */
    public Gradient gradient;

    /**
     * Corresponds to key "rounded" in style string.
     */
    public boolean roundedCorners;

    public int strokeWidth;

    /**
     * Clockwise rotation angle in degree, corresponds to key "rotation" in style string.
     */
    public double rotate;

    /**
     * Inner text of a geometry shape.
     */
    public String innerTexCode;

    public Map<String, String> restStyle;

    public Shape(double x, double y, double width, double height, Color drawColor) {
        super(x, y, width, height, drawColor);
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Width and height of a shape should be non-negative.");
        }
        fillColor = null;
        gradient = null;
        roundedCorners = false;
        strokeWidth = 1;
        rotate = 0;
        innerTexCode = "";
        restStyle = null;
    }

    private boolean hasRotation() { return Math.abs(rotate) > 1e-4; }

    private void dumpCoordinates(StringBuilder sb) {
        /**
         * Notes on call to {@link Point#rotateBy}:
         *
         * The y-axis in tikz and that in drawio go in opposite directions.
         * Because of this, when drawing pictures in tikz, all y coordinates
         * are negated (see also {@link Geometry#formatCoordinate}).
         * So the roation angle should be negated when rotating points, to make
         * the rotation direction consistent with that in draw.io.
         */
        if (shapeKind == ShapeKind.ELLIPSE) {
            /**
             * Example formatting result:
             * <blockquote>
             * \fill [rotate=45, left color=C0000FF, right color=C33FF33]
             * % formatted here:
             * (0, 0) ellipse [x radius=4, y radius=2];</blockquote>
             */

            double xRadius = width / 2.0;
            double yRadius = height / 2.0;
            formatCoordinate(x + xRadius, y + yRadius, sb);
            sb.append(" ellipse ");
            xRadius *= SCALE_FACTOR;
            yRadius *= SCALE_FACTOR;
            sb.append("[x radius=").append(String.format("%.4f", xRadius)).append(", ");
            sb.append("y radius=").append(String.format("%.4f", yRadius)).append("]");
            return;
        }
        Point center = new Point(x + width / 2, y + height / 2);
        List<Point> vertices = VertexCalculator.vertices(shapeKind, width, height, restStyle);
        double angle = rotate;
        int numVertices = vertices.size();
        assert numVertices >= 3; /* a shape should have at least 3 vertices. */
        for (int i = 0; i < numVertices; i++) {
            Point vertex = vertices.get(i).rotateBy(angle).negateYAxis();
            formatCoordinate(center.add(vertex), sb);
            if (i == 0) {
                sb.append(" -- ");
            } else if (i == numVertices - 1) {
                sb.append(" -- cycle");
            } else {
                sb.append(" -- ");
            }
        }
    }

    @Override
    public String draw(Context ctx) {
        this.registerColor(ctx, drawColor);
        this.registerColor(ctx, fillColor);
        if (gradient != null) {
            this.registerColor(ctx, gradient.endColor);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\\fill[");
        if (gradient != null) {
            gradient.draw(sb, fillColor, rotate);
            ctx.tikzLibraries.add("shadings");
            sb.append(", ");
        } else if (fillColor != null) {
            sb.append("color=").append(fillColor.uniqueName()).append(", ");
        } else {
            Color white = new Color("FFFFFF");
            this.registerColor(ctx, white); /* default to white */
            sb.append("color=").append(white.uniqueName()).append(", ");
        }

        if (roundedCorners) {
            sb.append("rounded corners, ");
        }

        /* stroke color and width */
        if (drawColor != null) {
            sb.append("draw=").append(drawColor.uniqueName()).append(", ");
            sb.append("line width=").append(String.format("%d", strokeWidth)).append("pt");
        } else if (strokeWidth > 0) {
            Color black = new Color("000000");
            this.registerColor(ctx, black);
            sb.append("draw=").append(black.uniqueName()).append(", ");
            sb.append("line width=").append(String.format("%d", strokeWidth)).append("pt");
        }
        /**
         * Ellipse rotation is explicitly coded in the fill method.
         * For other shape kinds, we compute the coordinates of the shape boundary
         * after rotation and then draw the shape boundary by connecting these coordinates,
         * so we don't need to specify rotation angle in tikz command.
         */
        if (shapeKind == ShapeKind.ELLIPSE && hasRotation() && Math.abs(width - height) > 1e-2) {
            sb.append(", ").append("rotate around={").append(String.format("%.3f", -rotate)).append(":");
            formatCoordinate(x + width / 2, y + height / 2, sb);
            sb.append("}");
        }
        sb.append("] ");
        dumpCoordinates(sb);

        sb.append(";");

        /**
         * draw text by creating a separate node.
         *
         * Example:
         * <blockquote>\draw (4, 2) % node size
         * at (2.5, -2) { % node position
         * inner text};</blockquote>
         */
        if (innerTexCode != null && !innerTexCode.isEmpty()) {
            Point center = new Point(x + width / 2, y + height / 2);
            sb.append("\n\\node[text width=")
                .append(String.format("%.4fcm", width * SCALE_FACTOR * CENTIMETER_TO_TIKZ_UNIT));
            /**
             * The text should rotate together with the shape.
             */
            if (hasRotation()) {
                sb.append(", rotate around={").append(String.format("%.3f", -rotate)).append(":");
                formatCoordinate(center, sb);
                sb.append("}] at ");
            } else {
                sb.append("] at ");
            }
            formatCoordinate(center, sb);
            sb.append(" {\n").append(innerTexCode).append("\n};");
        }
        return sb.toString();
    }
}
