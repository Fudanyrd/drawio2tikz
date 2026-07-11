package edu.fudan.jtex;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class TestFormatter {
    @Test
    public void testAppendNewLine() {
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(null, writer);

        for (int i = 0; i < 10; i++) {
            formatter.append(String.format("%d", i));
            formatter.appendNewLine(false);
        }
        formatter.finish();
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(10, actual.size());
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(String.format("%d\n", i), actual.get(i));
        }
    }

    @Test
    public void testLastLineHandling() {
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(null, writer);
        formatter.append("Hello, world!  ");
        formatter.finish();
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(1, actual.size());
        /**
         * The trailing whitespaces should be removed, and a
         * linebreak should be appended to the last line.
         */
        Assert.assertEquals("Hello, world!\n", actual.get(0));
    }

    @Test
    public void testEliminateWhitespace() {
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(null, writer);

        /* the line: "  foo   bar " */
        formatter.appendWhitespaces(" ");
        formatter.appendWhitespaces(" ");
        formatter.append("foo");
        formatter.appendWhitespaces("   ");
        formatter.append("bar");
        formatter.appendWhitespaces(" ");
        formatter.appendNewLine(false);
        formatter.finish();
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(1, actual.size());

        /**
         * Leading/trailing whitespaces removed;
         * expecting "foo bar\n".
         */
        Assert.assertEquals("foo bar\n", actual.get(0));
    }

    @Test
    public void testLineBreak() {
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(null, writer);

        /* creating line break(s) at start of document should have no effect. */
        formatter.appendNewLine(true);

        /* first line: "1" */
        formatter.append("1").appendNewLine(false);

        /* add an empty line. */
        formatter.appendNewLine(true);
        formatter.appendNewLine(false);

        /* second line: "2" */
        formatter.append("2").appendNewLine(false);

        /* try inserting multiple empty lines -- only one can be inserted. */
        formatter.appendNewLine(true).appendNewLine(true).appendNewLine(true);
        formatter.finish();
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(4, actual.size());
        Assert.assertEquals("1\n", actual.get(0));
        Assert.assertEquals("\n", actual.get(1));
        Assert.assertEquals("2\n", actual.get(2));
        Assert.assertEquals("\n", actual.get(3));
    }

    @Test
    public void testAutoBreak() {
        Format config = new Format();
        config.lineWidth = 8;
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(config, writer);

        /**
         * Should create a line break after "bar" because of column width.
         */
        formatter.append("foo")
            .appendWhitespaces(" ")
            .append("bar")
            .appendWhitespaces(" ")
            .append("baz")
            .appendWhitespaces(" ");
        formatter.finish();
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(2, actual.size());
        Assert.assertEquals("foo bar\n", actual.get(0));
        Assert.assertEquals("baz\n", actual.get(1));
    }

    @Test
    public void testAutoBreak2() {
        Format config = new Format();
        config.lineWidth = 8;
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(config, writer);

        formatter.append("123456789").appendWhitespaces(" ").append("123456789").appendWhitespaces(" ");
        formatter.finish();
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(2, actual.size());
        Assert.assertEquals("123456789\n", actual.get(0));
        Assert.assertEquals("123456789\n", actual.get(1));
    }

    @Test
    public void testAutoBreakCompact() {
        /**
         * This checks that the formatter avoids unnecessary line breaks.
         * Formatted document (max column = 8):
         * <p>Mary has</p>
         * <p>a little</p>
         * <p>lamb</p>
         */
        Format config = new Format();
        config.lineWidth = 8;
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(config, writer);

        formatter.append("Mary")
            .appendWhitespaces(" ")
            .append("has")
            .appendWhitespaces(" ")
            .append("a")
            .appendWhitespaces(" ")
            .append("little")
            .appendWhitespaces(" ")
            .append("lamb")
            .appendWhitespaces(" ")
            .finish();
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("Mary has\n", actual.get(0));
        Assert.assertEquals("a little\n", actual.get(1));
        Assert.assertEquals("lamb\n", actual.get(2));
    }

    @Test
    public void testAppendingTextWithLR() {
        /* Try appending a text that ends with line break, the formatter should
           recognize it. */
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(null, writer);

        formatter
            .append("% foo\n")      /* LaTex comment */
            .appendWhitespaces(" ") /* eliminated because it is at the start of the second line. */
            .append("1")
            .appendWhitespaces(" ")
            .append("% bar\n")
            .append("2")
            .finish();
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("% foo\n", actual.get(0));
        Assert.assertEquals("1 % bar\n", actual.get(1));
        Assert.assertEquals("2\n", actual.get(2));
    }

    @Test
    public void testDisablingAutoBreak() {
        Format config = new Format();
        config.lineWidth = 8;
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(config, writer);

        formatter.autoBreakOff();
        formatter.autoBreakOff();
        Assert.assertFalse(formatter.autoBreakEnabled());
        formatter.autoBreakOn();
        Assert.assertFalse(formatter.autoBreakEnabled()); /* still disabled */

        formatter.append("123456789")
            .appendWhitespaces(" ")
            .append("123456789")
            .appendWhitespaces(" \t ")
            .append("123456789")
            .appendNewLine(false);
        formatter.finish();
        formatter.autoBreakOn();
        Assert.assertTrue(formatter.autoBreakEnabled());
        formatter = null;

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(1, actual.size());
        Assert.assertEquals("123456789 123456789 \t 123456789\n", actual.get(0));
    }

    @Test
    public void testIdentataion() {
        Format config = new Format();
        config.indentWidth = 1;
        LineKeeper writer = new LineKeeper();
        Formatter formatter = new Formatter(config, writer);

        formatter.append("1").appendNewLine(false);
        formatter.enter();
        formatter.append("2").appendNewLine(false);
        formatter.enter();
        formatter.append("3").appendNewLine(false);
        formatter.leave();
        formatter.leave();
        formatter.finish();

        ArrayList<String> actual = writer.lines;
        Assert.assertEquals(3, actual.size());
        Assert.assertEquals("1\n", actual.get(0));
        Assert.assertEquals(" 2\n", actual.get(1));
        Assert.assertEquals("  3\n", actual.get(2));
    }
}
