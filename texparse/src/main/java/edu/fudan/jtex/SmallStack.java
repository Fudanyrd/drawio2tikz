package edu.fudan.jtex;

/**
 * A array-like stack supporting integer indexing,
 * push, pop, and dynamic resizing.
 */
public class SmallStack<T> {
    private Object[] array;
    private int _size;

    private static final int INIT_LENGTH = 4;

    /**
     * @param initialCapacity: (optional) if provided,
     * the stack tries to allocate an array of this _size.
     */
    public SmallStack(Integer initialCapacity) {
        array = null;
        _size = 0;
        if (initialCapacity != null) {
            int cap = initialCapacity;
            if (cap < 0) {
                throw new IllegalArgumentException("negative initialCapacity");
            }
            array = new Object[cap];
        }
    }

    public T getAt(int index) {
        if (index < 0 || index >= _size) {
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
                         /* target.start= */ 0, /* length= */ this._size);
        array = newArray;
    }

    /**
     * Adds an element to the stack.
     *
     * @param element: a possibly null element.
     */
    public void push(T element) {
        if (array == null || _size == array.length) {
            resize();
        }
        array[_size++] = element;
    }

    public void pop() {
        if (_size != 0) {
            _size--;
        }
    }

    public T top() {
        if (_size == 0) {
            return null;
        }
        return (T)array[_size - 1];
    }

    public int size() { return _size; }
}
