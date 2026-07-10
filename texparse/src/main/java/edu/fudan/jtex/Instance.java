package edu.fudan.jtex;

public class Instance {
    static class StdoutLineWriter extends Formatter.LineWriter {
        @Override
        public void write(String line) {
            System.out.print(line);
        }

        @Override
        public void close() {
            /* do nothing */
        }
    }

    public static void main(String[] args) {
        TexFile file = new TexFile(args[0]);
        NodeBase document = (new Parser(file)).output;
        (new ParagraphSeparator()).recurse(document);

        Formatter formatter = new Formatter(new Format(), new StdoutLineWriter());
        document.appendTo(formatter);
        formatter.finish();
    }
}
