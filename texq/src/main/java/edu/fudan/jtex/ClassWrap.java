package edu.fudan.jtex;

public class ClassWrap<T> extends ClassWrapBase {
    private final Class<T> clazz;

    public ClassWrap(Class<T> clazz) { this.clazz = clazz; }

    @Override
    public boolean isInstance(Object other) {
        return clazz.isInstance(other);
    }
}
