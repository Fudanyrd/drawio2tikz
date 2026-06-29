package edu.fudan.drawio2tikz;

public class Color {
    private byte channels[];

    public Color(String hex) {
        if (hex.length() != 6) {
            throw new IllegalArgumentException("Hex color must be 6 characters long");
        }
        hex = hex.toUpperCase();
        channels = new byte[3];
        for (int i = 0; i < 3; i++) {
            channels[i] = (byte)Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
    }

    public static final Color BLACK = new Color("000000");
    public static final Color WHITE = new Color("FFFFFF");

    public static Color fromHTMLStyle(String style) {
        String stripped = style.replaceAll("\\s+", "");
        if (stripped.charAt(0) == '#') {
            return new Color(stripped.substring(1));
        } else if (stripped.startsWith("rgb(") && stripped.endsWith(")")) {
            String[] parts = stripped.substring(4, stripped.length() - 1).split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid rgb color format.");
            }
            byte r = (byte)Integer.parseInt(parts[0]);
            byte g = (byte)Integer.parseInt(parts[1]);
            byte b = (byte)Integer.parseInt(parts[2]);
            return new Color(String.format("%02X%02X%02X", r, g, b));
        } else {
            throw new IllegalArgumentException("Unsupported color format: " + style);
        }
    }

    @Override
    public String toString() {
        return String.format("%02X%02X%02X", channels[0], channels[1], channels[2]);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Color) {
            Color other = (Color)obj;
            return this.channels[0] == other.channels[0] && this.channels[1] == other.channels[1] &&
                this.channels[2] == other.channels[2];
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (channels[0] & 0xFF) << 16 | (channels[1] & 0xFF) << 8 | (channels[2] & 0xFF);
    }

    /**
     * Tikz is built on top of package xcolor, which allows us to
     * define colors with its '\\definecolor' macro. To avoid name
     * conflicts, we need to generate unique names for colors.
     * @return
     */
    public String uniqueName() {
        /**
         * The definecolor macro supports defining the same name multiple times,
         * and when this happens, the previously defined color will be overridden.
         */
        return "C" + this.toString();
    }
}
