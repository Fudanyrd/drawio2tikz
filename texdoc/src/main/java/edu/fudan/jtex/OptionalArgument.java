package edu.fudan.jtex;

public class OptionalArgument extends ArgumentBase {
    public OptionalArgument(NodeBase inner) { this.inner = inner; }

    public String before() { return "["; }
    public String after() { return "]"; }
}
