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
