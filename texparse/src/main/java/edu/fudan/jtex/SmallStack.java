package edu.fudan.jtex;

/**
 * A array-like stack supporting integer indexing,
 * push, pop, and dynamic resizing.
 */
public class SmallStack<T> {
    private Object[] array;
    private int size;

    private static final int INIT_LENGTH = 4;

    /**
     * @param initialCapacity: (optional) if provided,
     * the stack tries to allocate an array of this size.
     */
    public SmallStack(Integer initialCapacity) {
        array = null;
        size = 0;
        if (initialCapacity != null) {
            int cap = initialCapacity;
            if (cap < 0) {
                throw new IllegalArgumentException("negative initialCapacity");
            }
            array = new Object[cap];
        }
    }

    public T getAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        return (T)array[index];
    }

    /**
     * It allocates a larger array, and copies old elements to it.
     */
    private void resize() {
        int length = (array == null) ? INIT_LENGTH : (array.length * 2);
        Object[] newArray = new Object[length];
        if (array == null) {
            array = newArray;
            return;
        }
        System.arraycopy(/* src= */ array, /* src.start= */ 0, /* target= */ newArray,
                         /* target.start= */ 0, /* length= */ this.size);
        array = newArray;
    }

    /**
     * Adds an element to the stack.
     *
     * @param element: a possibly null element.
     */
    public void push(T element) {
        if (array == null || size == array.length) {
            resize();
        }
        array[size++] = element;
    }

    public void pop() {
        if (size != 0) {
            size--;
        }
    }

    public T top() {
        if (size == 0) {
            return null;
        }
        return (T)array[size - 1];
    }
}
