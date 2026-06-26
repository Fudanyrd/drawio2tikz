package edu.fudan.jtex;

public class Argument extends ArgumentBase {
    public Argument(NodeBase inner) { this.inner = inner; }

    public String before() { return "{"; }
    public String after() { return "}"; }
}
