package edu.fudan.jtex;

/**
 * The {@code SmallQueue} is used to store a few objects.
 * In the texdoc project, it helps the formatter to keep
 * track of current and past few lines.
 */
public class SmallQueue<T> {
    /**
     * Volume of the queue is implied in `elements.length`.
     */
    private Object[] elements;

    /**
     * Number of elements in the queue.
     */
    private int length;

    /**
     * Index of the head element in the queue.
     * The head element is the first element to be dequeued.
     */
    private int head;

    public SmallQueue(int volume) {
        elements = new Object[volume];
        length = 0;
        head = 0;
    }

    public void push(T element) {
        if (length == elements.length) {
            throw new IllegalStateException("Queue is full");
        }
        elements[(head + length) % elements.length] = element;
        length++;
    }

    public int size() { return length; }

    /**
     * Indexing into the queue. Negative indexing is supported,
     * i.e. index -1 is the last element (the one at the tail),
     * index -2 is the last but two, etc.
     */
    public T getAt(int idx) {
        if (idx < 0) {
            idx += length;
        }
        if (idx < 0 || idx >= length) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }
        return (T)elements[(head + idx) % elements.length];
    }

    public void pop() {
        if (length == 0) {
            throw new IllegalStateException("Queue is empty");
        }
        head = (head + 1) % elements.length;
        length--;
    }

    public void setCurrent(T item) {
        if (length == 0) {
            throw new IllegalStateException("Queue is empty");
        }
        elements[(head + length - 1) % elements.length] = item;
    }
}
