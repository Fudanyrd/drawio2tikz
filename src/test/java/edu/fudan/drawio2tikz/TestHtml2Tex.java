package edu.fudan.drawio2tikz;

import org.junit.Assert;
import org.junit.Test;

public class TestHtml2Tex {
    @Test
    public void testLineBreak() throws Exception {
        String html = "<div><div>Hello</div><div><br/></div><div>World</div></div>";
        String expected = "Hello\\\\\nWorld";
        String actual = Html2Tex.convert(html);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testBoldAndItalic() throws Exception {
        String html = "<div><b>Hello</b> <i>World</i></div>";
        String expected = "\\textbf{Hello}~\\textit{World}";
        String actual = Html2Tex.convert(html);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testParagraph() throws Exception {
        String html = "<div><p>Hello</p><p>World</p></div>";
        String expected = "Hello\\\\\nWorld\\\\\n";
        String actual = Html2Tex.convert(html);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testDeepNest() throws Exception {
        final int level = 16;
        StringBuilder htmlBuilder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            htmlBuilder.append("<div>");
        }
        htmlBuilder.append("Hello");
        for (int i = 0; i < level; i++) {
            htmlBuilder.append("</div>");
        }
        String html = htmlBuilder.toString();
        htmlBuilder = null;
        String expected = "Hello";
        String actual = Html2Tex.convert(html);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testStyleSheetParsing() {
        String style = "text-align: right;   font-size:  18px; color: rgb(255,0 , 0);";
        Html2Tex.StyleSheet ss = new Html2Tex.StyleSheet(style);
        Assert.assertEquals("RIGHT", ss.textAlign.name());
        Assert.assertEquals(18, ss.fontSize);
        Assert.assertEquals(new Color("ff0000"), ss.textColor);
    }

    @Test
    public void testTextManipulate() throws Exception {
        String style = "text-align: center; color: rgb(0, 255, 0);";
        String html = String.format("<div style=\"%s\">Hello</div>", style);
        String expected = "\\begin{center}\n\\textcolor[HTML]{00FF00}{Hello}\n\\end{center}\n";
        Assert.assertEquals(expected, Html2Tex.convert(html));
    }
}
