package edu.fudan.jtex;

abstract class ClassWrapBase {
    public abstract boolean isInstance(Object other);

    public static ClassWrapBase classof(Object obj) { return new ClassWrap<>(obj.getClass()); }
}
