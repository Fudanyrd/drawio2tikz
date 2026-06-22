package edu.fudan.drawio2tikz;

/**
 * Represents a point in 2D space, with x and y coordinates.
 */
public class Point {
    public double x;
    public double y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Perform in-place clock-wise rotation of this point around the origin by the given angle in degree.
     * @return itself.
     */
    Point rotateBy(double degree) {
        double rad = Math.toRadians(degree);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double newX = x * cos + y * sin;
        double newY = -x * sin + y * cos;
        x = newX;
        y = newY;
        return this;
    }

    Point addBy(Point other) {
        x += other.x;
        y += other.y;
        return this;
    }

    Point add(Point other) { return new Point(x + other.x, y + other.y); }
    Point add(double dx, double dy) { return new Point(x + dx, y + dy); }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }

    Point negateYAxis() {
        y = -y;
        return this;
    }
}
