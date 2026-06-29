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
}
