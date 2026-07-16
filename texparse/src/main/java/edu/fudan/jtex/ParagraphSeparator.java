package edu.fudan.jtex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * It groups nodes into different paragraphs
 * for certain environments.
 */
public class ParagraphSeparator extends DocRewriter {
    public ParagraphSeparator() {}

    public static final HashSet<String> REWRITABLE = new HashSet<String>();
    static {
        REWRITABLE.add("document");
        REWRITABLE.add("center");
        REWRITABLE.add("enumerate");
        REWRITABLE.add("itemize");
    }

    static class ParaSepImpl {
        ParagraphNode current;
        ArrayList<NodeBase> children;
        SmallQueue<NodeBase> history;
        static final int HISTORY_SIZE = 2;

        /**
         * Finish current paragraph.
         */
        private void finish() {
            if (current.children.size() > 0) {
                children.add(current);
                current = new ParagraphNode(new ArrayList<NodeBase>());
            }
        }
        private void updateHistory(NodeBase newest) {
            if (history.size() == HISTORY_SIZE) {
                history.pop();
            }
            history.push(newest);
        }

        static boolean isLineBreakNode(NodeBase node) {
            if (node == null) {
                return false;
            }
            if (node instanceof TextNode) {
                String txt = ((TextNode)node).text;
                return ParserConfig.isLineBreakToken(txt) /* text == \n */
                    ;
            }
            return false;
        }
        static boolean isParagraphCmd(CommandNode cmd) {
            String name = cmd.name;
            return name.equals("\\par") || name.equals("\\paragraph") || name.equals("\\item");
        }
        static boolean isSectCmd(CommandNode cmd) {
            String name = cmd.name;
            return name.equals("\\chapter") || name.endsWith("section");
        }

        /**
         * @param subject: the node to be rewritten.
         */
        ParaSepImpl(NodeBase subject) {
            current = new ParagraphNode(new ArrayList<NodeBase>());
            children = new ArrayList<NodeBase>();
            history = new SmallQueue<NodeBase>(HISTORY_SIZE);

            List<NodeBase> nodes = subject.children;
            int size = nodes.size();

            for (int i = 0; i < size; i++) {
                NodeBase node = nodes.get(i);
                NodeBase previous = history.size() > 0 ? history.getAt(-1) : null;

                /* This should be done regardless of node kind. */
                updateHistory(node);

                /* check whether start new paragraph. */
                if (node instanceof CommandNode) {
                    if (isSectCmd((CommandNode)node)) {
                        finish();
                        children.add(node);
                    } else {
                        if (isParagraphCmd((CommandNode)node)) {
                            finish();
                        }
                        current.children.add(node);
                    }
                } else if (node instanceof EnvironNode) {
                    /* Environments are lateral to paragraphs (really?) */
                    /* FIXME: check inline environs. */
                    finish();
                    children.add(node);
                } else if (isLineBreakNode(node)) {
                    if (isLineBreakNode(previous)) {
                        finish();
                    }
                    /* avoid adding line breaks to the start of paragraph. */
                    if (current.children.size() > 0) {
                        current.children.add(node);
                    }
                } else {
                    current.children.add(node);
                }
            }

            /* manually finish last paragraph. */
            if (current.children.size() > 0) {
                children.add(current);
            }

            /* write the grouping result. */
            subject.children = this.children;
        }
    }

    private static void replaceLFWithBlank(ParagraphNode p) {
        List<NodeBase> children = p.children;
        int length = children.size();
        for (int i = 0; i + 1 < length; i++) {
            NodeBase base = children.get(i);
            if (!(base instanceof TextNode)) {
                continue;
            }
            TextNode text = (TextNode)base;
            if (ParserConfig.isLineBreakToken(text.text)) {
                text.text = " ";
            }
        }
    }

    /**
     * The rewriting process cosists of two steps:
     * firstly separate by paragraphs, then replacing line break
     * inside paragraphs with whitespaces.
     *
     * <p>The second step is for improving formatting quality.</p>
     */
    private void rewriteImpl(NodeBase subject) {
        new ParaSepImpl(subject);
        for (NodeBase child : subject.children) {
            if (child instanceof ParagraphNode) {
                replaceLFWithBlank((ParagraphNode)child);
            }
        }
    }

    private boolean rewriteTop(NodeBase root) {
        for (NodeBase child : root.children) {
            if (!(child instanceof CommandNode)) {
                continue;
            }
            CommandNode cmd = (CommandNode)child;
            if (cmd.name == "\\documentclass") {
                /* possibly config area. do not rewrite. */
                return true;
            }
        }

        /* split root by paragraphs. */
        this.rewriteImpl(root);
        return true;
    }

    @Override
    public boolean rewrite(SmallStack<NodeBase> context) {
        NodeBase top = context.top();
        if (context.size() == 1) {
            boolean ret = rewriteTop(top);
            return ret;
        }
        if (!(top instanceof EnvironNode)) {
            return true;
        }
        EnvironNode env = (EnvironNode)top;
        if (!REWRITABLE.contains(env.name)) {
            return false;
        }

        this.rewriteImpl(env);
        return true;
    }
}
