package edu.fudan.drawio2tikz;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <h3>Tikz Code Generator</h3>
 *
 * Notes on using generated tikz code
 * <ul>
 *   <li>Before LaTex's "begin{document}" environment, tikz and xcolor packages should be loaded.</li>
 *   <li>{@link Context} class does book-keeping for all required colors (in HTML format) and tikz libraries. The
 * generator will automatically add colors to the context when processing geometries, so users only need to load the
 * colors defined in the context.</li>
 * </ul>
 */
public class TikzGen {
    private Context context;
    private List<Geometry> geometries;

    private void filterGeometries(Element diagramNode) {
        assert diagramNode.getNodeName().equals("diagram");

        /* diagram -> mxGraphModel -> root */
        Element mxGraphModelNode = (Element)diagramNode.getElementsByTagName("mxGraphModel").item(0);
        Element rootNode = (Element)mxGraphModelNode.getElementsByTagName("root").item(0);
        NodeList mxCellNodes = rootNode.getElementsByTagName("mxCell");
        for (int i = 0; i < mxCellNodes.getLength(); i++) {
            Element mxCellNode = (Element)mxCellNodes.item(i);
            Geometry geometry = GeometryFactory.createGeometry(mxCellNode);
            if (geometry != null) {
                geometries.add(geometry);
            }
        }
    }

    /**
     * Construct a generator object from a path to drawio XML file.
     *
     * @param path path to drawio XML file
     * @param context a context object to be used by the generator. If null,
     * a new context will be created.
     */
    public static TikzGen fromFile(Path path, Context context) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(path.toFile());
            Element diagramNode = (Element)document.getElementsByTagName("diagram").item(0);
            if (context == null)
                return new TikzGen(diagramNode);
            return new TikzGen(context, diagramNode);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public TikzGen(Element diagramNode) {
        context = new Context();
        geometries = new ArrayList<>();
        context.tikzLibraries.add("positioning");
        filterGeometries(diagramNode);
    }

    public TikzGen(Context context, Element diagramNode) {
        this.context = context;
        geometries = new ArrayList<>();
        filterGeometries(diagramNode);
    }

    public String generateTikz() {
        StringBuilder sb = new StringBuilder();

        /* draw the picture. */
        sb.append("\\begin{tikzpicture}").append("\n");
        for (Geometry geometry : geometries) {
            sb.append(geometry.draw(context)).append("\n");
        }
        sb.append("\\end{tikzpicture}");
        return sb.toString();
    }

    /**
     * @return a stand-alone LaTeX document containing the tikz code, which can be compiled directly.
     * This is useful for debugging.
     */
    public String generateDoc() {
        String tikzPicture = generateTikz();
        StringBuilder sb = new StringBuilder();
        sb.append("\\documentclass{standalone}").append("\n");
        if (!context.tikzLibraries.isEmpty()) {
            sb.append("\\usepackage{tikz}").append("\n");
            sb.append("\\usetikzlibrary{");
            boolean first = true;
            for (String lib : context.tikzLibraries) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(lib);
                first = false;
            }
            sb.append("}").append("\n");
        }

        /* (re-)define colors */
        for (Color color : context.colors) {
            sb.append("\\definecolor{")
                .append(color.uniqueName())
                .append("}{HTML}{")
                .append(color.toString())
                .append("}")
                .append("\n");
        }

        sb.append("\\begin{document}").append("\n");
        sb.append(tikzPicture).append("\n");
        sb.append("\\end{document}\n");
        return sb.toString();
    }

    public Context getContext() { return context; }
}
