package edu.fudan.jtex;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class TestCommandNode {
    private static CommandNode create(String name, String... arguments) {
        ArrayList<ArgumentBase> args = new ArrayList<ArgumentBase>();
        for (String arg : arguments) {
            args.add(new Argument(new TextNode(arg)));
        }
        return new CommandNode(name, args);
    }

    @Test
    public void testToString() {
        CommandNode node = create("\\command", "arg1", "arg2");
        String expected = "\\command{arg1}{arg2}";
        Assert.assertEquals(expected, node.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCommand() {
        create("command", "arg1", "arg2"); /* construct without leading backslash */
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCommand2() {
        create("", "arg"); /* construct with empty string */
    }

    @Test
    public void testAutoBreakCommands() {
        Format config = new Format();
        config.lineWidth = 8;
        LineKeeper keeper = new LineKeeper();
        Formatter formatter = new Formatter(config, keeper);

        /**
         * Formatting result:
         * <p>\\cmd{</p>
         * <p>aLongArgument</p>
         * <p>}</p>
         */
        CommandNode node = create("\\cmd", "aLongArgument");
        node.appendTo(formatter);
        formatter.finish();

        ArrayList<String> lines = keeper.lines;
        Assert.assertEquals(3, lines.size());
        Assert.assertEquals("\\cmd{\n", lines.get(0));
        Assert.assertEquals("aLongArgument\n", lines.get(1));
        Assert.assertEquals("}\n", lines.get(2));
    }

    @Test
    public void testNoAutoBreakCommands() {
        Format config = new Format();
        config.lineWidth = 8;
        LineKeeper keeper = new LineKeeper();
        Formatter formatter = new Formatter(config, keeper);

        CommandNode node = create("\\cite", "longBibTexId");
        node.appendTo(formatter);
        formatter.finish();

        /**
         * Unlike the previous test, this command is in the noAutoBreakCommands set,
         * so it should not be broken into multiple lines.
         *
         * The expected output is a single line.
         */
        String expected = "\\cite{longBibTexId}\n";
        ArrayList<String> lines = keeper.lines;
        Assert.assertEquals(1, lines.size());
        Assert.assertEquals(expected, lines.get(0));
    }
}
