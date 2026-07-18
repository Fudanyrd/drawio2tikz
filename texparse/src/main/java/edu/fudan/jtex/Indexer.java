package edu.fudan.jtex;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * By its name, an {@code Indexer} does not modify the document tree.
 * However, We made it a subclass of {@link DocRewriter} so that
 * it can be loaded by a parsing {@link Instance}.
 *
 * It recursively traverses the document tree to build &quot;Table
 * of Contents&quot;, &quot;List of Figures&quot;, and &quot;List of
 * Tables&quot;, then dumps these to a text file.
 *
 * <h3>Working with other rewriters</h3>
 * It is recommended that {@code Indexer} is placed <b>after</b>
 * {@link ExpandInclude}-rewriter, which performs input command expansion
 * so that the document tree is complete.
 */
public class Indexer extends DocRewriter {
    private String outputFile = null;

    public static class CatalogItem {
        public static enum CatalogKind {
            DOCUMENT, /* Top Level */
            CHAPTER,
            SECTION,
            RESOURCES, /* figures, tables, etc.  */
        }
        public CatalogKind kind;

        public CatalogItem() { children = new ArrayList<CatalogItem>(); }

        /**
         * Only valid when {@code kind} is {@code SECTION},
         * this stores number of "sub" in its name, e.g.
         * 2 for &quot;\subsubsection&quot;.
         */
        public int subsectionLevel;

        public CatalogItem parent;

        /**
         * The document node which is the source of this
         * catalog item.
         */
        public NodeBase source;

        /**
         * The list of child items.
         */
        public List<CatalogItem> children;

        /**
         * Compare the height of two items in the catalog.
         *
         * @return the sign of (height of {@code lhs} minus
         * height of {@code rhs}).
         */
        public static int compare(CatalogItem lhs, CatalogItem rhs) {
            int kindDiff = lhs.kind.ordinal() - rhs.kind.ordinal();
            if (kindDiff != 0) {
                return kindDiff;
            }
            return lhs.subsectionLevel - rhs.subsectionLevel;
        }

        /**
         * For a chapter/section, it returns its name;
         * else its caption (figure/table).j:w
         */
        @Override
        public String toString() {
            if (kind == CatalogKind.DOCUMENT) {
                return "";
            }
            if (kind != CatalogKind.RESOURCES) {
                CommandNode decl = (CommandNode)source;
                List<ArgumentBase> args = decl.arguments;
                if (args == null || args.size() < 1) {
                    return "(Anonymous)";
                }
                return args.get(0).inner.toString();
            }
            String ret = "(Anonymous)";
            if (source.children == null) {
                return ret;
            }
            /* find a caption command. */
            for (NodeBase child : source.children) {
                if (!(child instanceof CommandNode)) {
                    continue;
                }
                CommandNode cmd = (CommandNode)child;
                if (cmd.name.equals("\\caption")) {
                    List<ArgumentBase> args = cmd.arguments;
                    if (args != null && args.size() > 0) {
                        ret = args.get(0).inner.toString();
                    }
                    break;
                }
            }
            return ret;
        }

        public String xmlTag() {
            switch (kind) {
            case DOCUMENT:
                return "document";
            case CHAPTER:
                return "chapter";
            case SECTION: {
                String ret = "";
                for (int i = 0; i < subsectionLevel; i++) {
                    ret += "sub";
                }
                return ret + "section";
            }
            case RESOURCES:
                return "resource";
            }
            throw new RuntimeException("unreachable");
        }

        /**
         * Dump xml representation recursively.
         */
        public void dump(PrintWriter writer, int indentLevel) throws IOException {
            /**
             * Example output:
             * <section name="1">
             *   <resource name="figure">
             *   </resource>
             * </section>
             */
            for (int i = 0; i < indentLevel; i++) {
                writer.print("  ");
            }
            writer.print("<" + xmlTag() + " name=\"" + toString() + "\">");
            writer.println();
            for (CatalogItem child : children) {
                child.dump(writer, indentLevel + 1);
            }
            for (int i = 0; i < indentLevel; i++) {
                writer.print("  ");
            }
            writer.print("</" + xmlTag() + ">");
            writer.println();
        }
    }

    /**
     * A helper class that finds the label belonging to a
     * section, chapter, or environment.
     */
    public static class LabelFilter {
        /**
         * Extract the label name from a \label command.
         *
         * @param labelCmd: the \label command.
         * @return the label name.
         */
        private static String extractLabelName(CommandNode labelCmd) {
            assert labelCmd.name.equals("\\label");
            assert labelCmd.arguments != null && labelCmd.arguments.size() == 1;
            return labelCmd.arguments.get(0).toString();
        }

        /**
         * Descend into a {@link ContainerNode} or {@link ParagraphNode}
         * for a \label command, and add their labels to {@code result}.
         */
        private static void recurseContainerLike(NodeBase container, List<String> result) {
            if (container.children == null) {
                return;
            }
            for (NodeBase child : container.children) {
                if (child instanceof CommandNode) {
                    CommandNode cmd = (CommandNode)child;
                    if (cmd.name.equals("\\label")) {
                        result.add(extractLabelName(cmd));
                    }
                } else if (child instanceof ContainerNode || child instanceof ParagraphNode) {
                    recurseContainerLike(child, result);
                }
            }
        }

        /**
         * @param document: the document environment, containing the section/chapter.
         * @param sectionLike: the command for which the labels are filtered.
         */
        public static List<String> labelsOfSectionLike(EnvironNode document, CommandNode sectionLike) {
            int indexOf = -1;
            int numChildren = 0;
            if (document.children == null) {
            } else {
                numChildren = document.children.size();
                for (int i = 0; i < numChildren; i++) {
                    if (sectionLike == document.children.get(i)) {
                        indexOf = i;
                        break;
                    }
                }
            }
            if (indexOf < 0) {
                throw new IllegalArgumentException("sectionLike is not a child of document");
            }

            /**
             * I have no idea how LaTex's labels work, so I just assumed that a section's label
             * is close to its declaration (sry).
             */
            ArrayList<String> ret = new ArrayList<String>();
            for (indexOf++; indexOf < numChildren; indexOf++) {
                NodeBase cur = document.children.get(indexOf);
                if (cur instanceof CommandNode) {
                    CommandNode cmd = (CommandNode)cur;
                    String name = cmd.name;
                    if (name.equals("\\label")) {
                        ret.add(extractLabelName(cmd));
                    }
                    if (name.equals("\\chapter") || name.endsWith("\\section")) {
                        /* reaching the end of current section. */
                        break;
                    }
                } else if (cur instanceof ContainerNode || cur instanceof ParagraphNode) {
                    recurseContainerLike(cur, ret);
                } else if (cur instanceof EnvironNode) {
                    break;
                }
            }
            return ret;
        }

        /**
         * @param resource: some figures/tables for which the labels are filtered.
         */
        public static List<String> labelOfEnviron(EnvironNode resource) {
            /* Recurse the children of resource, even if it is not "container like". */
            ArrayList<String> ret = new ArrayList<String>();
            recurseContainerLike(resource, ret);
            return ret;
        }
    }

    /**
     * A helper class to build the index from the root node
     * of a document (i.e. \begin{document} ... \end{document})
     */
    public static class IndexBuilder {
        /**
         * Output: label-to-item mapping.
         */
        public HashMap<String, CatalogItem> labels;

        /**
         * Output: root node of the catalog.
         */
        public CatalogItem catalog;

        /**
         * Output: a list of all figures, in the order they appear in
         * the document.
         */
        public List<CatalogItem> listOfFigures;

        /**
         * Output: a list of all tables, in the order they appear in
         * the document.
         */
        public List<CatalogItem> listOfTables;

        private CatalogItem iterator;

        private static void handleDuplicateLabel(String label) {
            /* only give a warning, and the label will be overwritten. */
            System.err.println("Warning: duplicate label \"" + label + "\"");
        }

        private void addResourceItem(EnvironNode resource) {
            boolean isTable = resource.name.equals("table");
            boolean isFigure = resource.name.equals("figure");
            if (!isTable && !isFigure) {
                /* dont record these for now. FIXME: */
                return;
            }
            CatalogItem item = new CatalogItem();
            item.kind = CatalogItem.CatalogKind.RESOURCES;
            item.source = resource;
            if (isTable) {
                listOfTables.add(item);
            } else {
                listOfFigures.add(item);
            }

            iterator.children.add(item);
            item.parent = iterator;

            List<String> resourceLabels = LabelFilter.labelOfEnviron(resource);
            for (String label : resourceLabels) {
                if (this.labels.containsKey(label)) {
                    handleDuplicateLabel(label);
                }
                this.labels.put(label, item);
            }
        }

        private void addSectionLike(CommandNode sectionLike) {
            CatalogItem.CatalogKind kind;
            int subsectionLevel = 0;
            if (sectionLike.name.equals("\\chapter")) {
                kind = CatalogItem.CatalogKind.CHAPTER;
            } else {
                assert sectionLike.name.endsWith("section");
                /* count number of substring "sub" */
                /* len("\\") + len("section") = 8 */
                int subCount = sectionLike.name.length() - 8;
                subCount = subCount / 3; /* 3: len("sub") */
                subsectionLevel = subCount;
                kind = CatalogItem.CatalogKind.SECTION;
            }

            CatalogItem item = new CatalogItem();
            item.kind = kind;
            item.subsectionLevel = subsectionLevel;
            item.source = sectionLike;

            while (CatalogItem.compare(item, iterator) <= 0) {
                /* find the parent of this item. */
                iterator = iterator.parent;
            }
            iterator.children.add(item);
            item.parent = iterator;
            iterator = item;

            List<String> sectionLabels = LabelFilter.labelsOfSectionLike((EnvironNode)catalog.source, sectionLike);
            for (String label : sectionLabels) {
                if (this.labels.containsKey(label)) {
                    handleDuplicateLabel(label);
                }
                this.labels.put(label, item);
            }
        }

        private void searchEnviron(NodeBase current) {
            if (current instanceof EnvironNode) {
                addResourceItem((EnvironNode)current);
                return; /* avoid searching into an environ node. */
            }
            if (current.children == null) {
                return;
            }
            for (NodeBase child : current.children) {
                searchEnviron(child);
            }
        }

        /**
         * Given a document environ node, fill {@code IndexBuilder#labels}
         * and {@code IndexBuilder#catalog}.
         *
         * @throw {@code IllegalArgumentException} if document's name is not
         * &quot;document&quot;.
         */
        public IndexBuilder(EnvironNode document) {
            assert document.name.equals("document");

            labels = new HashMap<String, CatalogItem>();
            catalog = new CatalogItem();
            catalog.kind = CatalogItem.CatalogKind.DOCUMENT;
            catalog.source = document;

            listOfFigures = new ArrayList<CatalogItem>();
            listOfTables = new ArrayList<CatalogItem>();

            iterator = catalog;

            if (document.children == null) {
                return;
            }
            int numChildren = document.children.size();
            for (int i = 0; i < numChildren; i++) {
                NodeBase child = document.children.get(i);
                if (child instanceof CommandNode) {
                    CommandNode cmd = (CommandNode)child;
                    if (cmd.name.equals("chapter") || cmd.name.endsWith("section")) {
                        addSectionLike(cmd);
                    }
                } else if (child instanceof EnvironNode) {
                    addResourceItem((EnvironNode)child);
                } else {
                    /* recursively discover and add environs. */
                    searchEnviron(child);
                }
            }
        }
    }

    @Override
    public void parseArgs(String[] args /* not null */) throws Exception {
        for (String arg : args) {
            if (arg.startsWith("--output=")) {
                this.outputFile = arg.substring("--output=".length());
            }
        }

        if (outputFile == null) {
            throw new RuntimeException("no output file given \"--output=\"");
        }
    }

    @Override
    public boolean rewrite(SmallStack<NodeBase> context) {
        NodeBase top = context.top();
        boolean isDocumentEnv = (top instanceof EnvironNode) && ((EnvironNode)top).name.equals("document");

        /* recurse into all others for a \\begin{document}. */
        if (!isDocumentEnv) {
            return true;
        }

        IndexBuilder index = new IndexBuilder((EnvironNode)top);
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            index.catalog.dump(writer, 0);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("failed to write to " + outputFile, e);
        }

        /* IndexBuilder does recursion, so don't recurse into top. */
        return false;
    }
}
