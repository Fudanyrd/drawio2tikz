package edu.fudan.drawio2tikz;

import org.junit.Test;

public class TestColor {
    @Test
    public void testEquals() {
        Color c1 = new Color("FF0000");
        Color c2 = new Color("FF0000");
        Color c3 = new Color("0000FF");
        assert c1.toString().equals("FF0000");
        assert c2.toString().equals("FF0000");
        assert c3.toString().equals("0000FF");
        assert c1.equals(c2);
        assert !c1.equals(c3);
        assert !c2.equals(c3);

        assert c1.hashCode() == c2.hashCode();
        assert c1.hashCode() != c3.hashCode();
    }

    @Test
    public void testUniqueName() {
        Color c1 = new Color("FF0000");
        Color c2 = new Color("00FF00");
        assert c1.uniqueName().equals("CFF0000");
        assert c2.uniqueName().equals("C00FF00");
    }
}
