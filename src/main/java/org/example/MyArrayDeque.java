package org.example;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MyArrayDeque<T> implements Deque<T> {

    private T[] elements;
    private int head;
    private int tail;
    private int size;

    public MyArrayDeque() {
        elements = (T[]) new Object[16];
        head = 0;
        tail = 0;
        size = 0;
    }

    public MyArrayDeque(int capacity) {
        elements = (T[]) new Object[capacity];
        head = 0;
        tail = 0;
        size = 0;
    }

    @Override
    public void addFirst(T t) {
        if (size == elements.length) extendCapacity(elements.length);
        head = (head - 1 + elements.length) % elements.length;
        elements[head] = t;
        size++;
    }

    @Override
    public void addLast(T t) {
        if (size == elements.length) extendCapacity(elements.length);
        elements[tail] = t;
        tail = (tail + 1) % elements.length;
        size++;
    }

    @Override
    public boolean offerFirst(T t) {
        addFirst(t);
        return true;
    }

    @Override
    public boolean offerLast(T t) {
        addLast(t);
        return true;
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        T t = elements[head];
        elements[head] = null;
        head = (head + 1) % elements.length;
        size--;
        return t;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) throw new NoSuchElementException();
        tail = (tail - 1 + elements.length) % elements.length;
        T t = elements[tail];
        elements[tail] = null;
        size--;
        return t;
    }

    @Override
    public T pollFirst() {
        return isEmpty() ? null : removeFirst();
    }

    @Override
    public T pollLast() {
        return isEmpty() ? null : removeLast();
    }

    @Override
    public T getFirst() {
        if (isEmpty()) throw new NoSuchElementException();
        return elements[head];
    }

    @Override
    public T getLast() {
        if (isEmpty()) throw new NoSuchElementException();
        return elements[(tail - 1 + elements.length) % elements.length];
    }

    @Override
    public T peekFirst() {
        return isEmpty() ? null : getFirst();
    }

    @Override
    public T peekLast() {
        return isEmpty() ? null : getLast();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        if (o != null) {
            for (int i = 0; i < size; i++) {
                int index = (head + i) % elements.length;
                if (o.equals(elements[index])) {
                    removeAt(i);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        if (o != null) {
            for (int i = size - 1; i >= 0; i--) {
                int index = (head + i) % elements.length;
                if (o.equals(elements[index])) {
                    removeAt(i);
                    return true;
                }
            }
        }
        return false;
    }

    private void removeAt(int index) {
        int realIndex = (head + index) % elements.length;
        for (int i = realIndex; i != tail; i = (i + 1) % elements.length) {
            elements[i] = elements[(i + 1) % elements.length];
        }
        tail = (tail - 1 + elements.length) % elements.length;
        elements[tail] = null;
        size--;
    }

    @Override
    public boolean add(T t) {
        addLast(t);
        return true;
    }

    @Override
    public boolean offer(T t) {
        return offerLast(t);
    }

    @Override
    public T remove() {
        return removeFirst();
    }

    @Override
    public T poll() {
        return pollFirst();
    }

    @Override
    public T element() {
        return getFirst();
    }

    @Override
    public T peek() {
        return peekFirst();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) addLast(t);
        return !c.isEmpty();
    }

    @Override
    public void push(T t) {
        addFirst(t);
    }

    @Override
    public T pop() {
        return removeFirst();
    }

    @Override
    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    @Override
    public boolean contains(Object o) {
        for (int i = 0; i < size; i++) {
            int index = (head + i) % elements.length;
            if (o.equals(elements[index])) return true;
        }
        return false;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public Iterator<T> descendingIterator() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[(head + i) % elements.length] = null;
        }
        head = tail = size = 0;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        for (int i = 0; i < size; i++) {
            result[i] = elements[(head + i) % elements.length];
        }
        return result;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        if (a.length < size) {
            a = (T1[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        for (int i = 0; i < size; i++) {
            a[i] = (T1) elements[(head + i) % elements.length];
        }
        if (a.length > size) a[size] = null;
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) return false;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int origSize = size;
        for (Object o : c) {
            remove(o);
        }
        return origSize != size;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int origSize = size;
        for (int i = 0; i < size; ) {
            int index = (head + i) % elements.length;
            if (!c.contains(elements[index])) {
                removeAt(i);
            } else {
                i++;
            }
        }
        return origSize != size;
    }

    private void extendCapacity(int capacity) {
        int oldCapacity = elements.length;
        int newCapacity = elements.length * 2;
        if (newCapacity < capacity) {
            newCapacity = capacity;
        }

        T[] newElements = (T[]) new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newElements[i] = elements[(head + i) % oldCapacity];
        }

        elements = newElements;
        head = 0;
        tail = size;
    }
}
