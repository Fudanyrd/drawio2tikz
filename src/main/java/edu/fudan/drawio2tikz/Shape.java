package edu.fudan.drawio2tikz;

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
 * </ul>
 *
 * <h3>Implemented Attributes</h3>
 * <ul>
 *   <li>rounded corners</li>
 *   <li>fill color plus gradient (in limited directions, see {@link Gradient.Direction})</li>
 *   <li>rotation (not for ellipse, since it is not supported by tikz)</li>
 * </ul>
 *
 * <h3>Future Enhancement</h3>
 * Use <blockquote>
 * /tikz/rotate around={degree:coordinate}
 * </blockquote>
 * for rotating a shape around its center, so we do not have to compute coordinates of the shape
 * boundary after rotation by ourselves.
 */
public class Shape extends Geometry {
    /**
     * List of supported shape kinds.
     */
    public static enum ShapeKind {
        RECTANGLE,
        ELLIPSE,
        TRIANGLE,
        /**
         * Implement more shapes of draw.io in the future, by computing the
         * coordinates of the shape boundary in the dumpCoordinates method
         * and using appropriate tikz commands in the draw method.
         */
    }
    ;

    public static ShapeKind createShapeKind(String shapeName) {
        switch (shapeName) {
        case "rectangle":
            return ShapeKind.RECTANGLE;
        case "ellipse":
            return ShapeKind.ELLIPSE;
        case "triangle":
            return ShapeKind.TRIANGLE;
        default:
            return null;
        }
    }
    public static ShapeKind createShapeKind(Map<String, String> style) {
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
        switch (shapeKind) {
        case RECTANGLE: {
            /**
             * Example formatting result (coordinates joint by '--'):
             * (0, 0) -- (0, 100) -- (100, 100) -- (100, 0) -- cycle
             */
            double dx = width * 0.5;
            double dy = height * 0.5;

            Point center = new Point(x + dx, y + dy);
            formatCoordinate(center.add((new Point(-dx, dy)).rotateBy(-rotate)), sb);
            sb.append(" -- ");
            formatCoordinate(center.add((new Point(-dx, -dy)).rotateBy(-rotate)), sb);
            sb.append(" -- ");
            formatCoordinate(center.add(new Point(dx, -dy).rotateBy(-rotate)), sb);
            sb.append(" -- ");
            formatCoordinate(center.add(new Point(dx, dy).rotateBy(-rotate)), sb);
            sb.append(" -- cycle");
            break;
        }
        case ELLIPSE:
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
            break;
        case TRIANGLE: {
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
            Point center = new Point(x + dx, y + dy);
            /* rotate and draw V1 */
            formatCoordinate(center.add(new Point(-dx, dy).rotateBy(-rotate)), sb);
            sb.append(" -- ");
            /* rotate and draw V2 */
            formatCoordinate(center.add(new Point(-dx, -dy).rotateBy(-rotate)), sb);
            sb.append(" -- ");
            /* rotate and draw V3 */
            formatCoordinate(center.add(new Point(dx, 0).rotateBy(-rotate)), sb);
            sb.append(" -- cycle");
            break;
        }
        default:
            /* not implemented. */
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
            sb.append("\n\\draw ");
            formatCoordinate(width, height, sb);
            sb.append(" node[] at ");
            formatCoordinate(center, sb);
            sb.append(" {\n").append(innerTexCode).append("\n};");
        }
        return sb.toString();
    }
}
