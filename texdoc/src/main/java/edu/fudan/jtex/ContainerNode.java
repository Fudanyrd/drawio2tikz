package edu.fudan.jtex;

import java.util.ArrayList;

/**
 * {@code ContainerNode} is used as a container of {@code NodeBase}
 * with autoBreak enabled by default.
 */
public class ContainerNode extends NodeBase {
    public boolean canAutoBreak;
    public ContainerNode() {
        this.children = new ArrayList<NodeBase>();
        this.canAutoBreak = true;
    }

    @Override
    public boolean allowAutoBreak() {
        return canAutoBreak;
    }

    /**
     * Normally, this class is used as a place-holder
     * that stores temporary parsing result, which
     * may be pruned.
     *
     * @return null if no content; pruned node
     * if it contains something.
     */
    public NodeBase prune() {
        if (children == null || children.size() == 0) {
            return null;
        }
        if (children.size() == 1) {
            return children.get(0);
        }
        return this;
    }
}
