package edu.fudan.jtex;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

    /**
     * Example config for a {@link DocRewriter}:
     * <code>
     * <rewriter class="edu.fudan.jtex.ExpandInclude">
     *   <arg>--add-include-path=.</arg>
     * </rewriter>
     * </code>
     *
     * @param rewriterConf
     * @param document
     */
    private static int performRewrite(Node rewriterConf, NodeBase document) {
        NodeList argsNodes = rewriterConf.getChildNodes();
        DocRewriter rewriter = null;
        String[] args = null;
        try {
            String className = rewriterConf.getAttributes().getNamedItem("class").getNodeValue();
            args = new String[argsNodes.getLength()];
            for (int i = 0; i < argsNodes.getLength(); i++) {
                Node argNode = argsNodes.item(i);
                args[i] = argNode.getTextContent();
            }

            Class<?> clazz = Class.forName(className);
            rewriter = (DocRewriter)clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            System.err.println("Error: cannot instantiate rewriter");
            e.printStackTrace();
            return 1;
        }

        try {
            rewriter.parseArgs(args);
        } catch (Exception e) {
            System.err.println("Error: cannot parse arguments");
            System.err.println("Arguments: " + String.join(" ", args));
            return 1;
        }

        rewriter.recurse(document);
        return 0;
    }

    public static void main(String[] args) {
        TexFile file = new TexFile(args[0]);
        String configFile = (args.length > 1) ? args[1] : "texparse.xml";
        NodeBase document = (new Parser(file)).output;

        /* Find rewriters to perform. */
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        NodeList rewriterConfNodes = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document conf = builder.parse(configFile);
            rewriterConfNodes = conf.getElementsByTagName("rewriter");
        } catch (Exception e) {
            System.err.println("Error: cannot parse configuration file " + configFile);
            return;
        }
        int numRewriters = rewriterConfNodes.getLength();
        for (int i = 0; i < numRewriters; i++) {
            Node rewriterConf = rewriterConfNodes.item(i);
            if (rewriterConf.getNodeType() != Node.ELEMENT_NODE) {
                // System.err.println(rewriterConf.getNodeType());
                continue;
            }
            performRewrite(rewriterConf, document);
        }

        Formatter formatter = new Formatter(new Format(), new StdoutLineWriter());
        document.appendTo(formatter);
        formatter.finish();
    }
}
