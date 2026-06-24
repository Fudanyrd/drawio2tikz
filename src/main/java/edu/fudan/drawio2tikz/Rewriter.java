package edu.fudan.drawio2tikz;

import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Rewriter {
    protected NodeList mxCellNodes;
    protected List<Geometry> geometries;
    protected Map<String, Integer> idToIndex;

    public Rewriter(NodeList mxCellNodes, List<Geometry> geometries, Map<String, Integer> idToIndex) {
        this.mxCellNodes = mxCellNodes;
        this.geometries = geometries;
        this.idToIndex = idToIndex;
        int numNodes = mxCellNodes.getLength();
        assert numNodes == geometries.size() : "mxCellNodes and geometries must have the same length";

        for (int i = 0; i < numNodes; i++) {
            this.rewrite(i);
        }
    }

    public abstract void rewrite(int idx);
}
