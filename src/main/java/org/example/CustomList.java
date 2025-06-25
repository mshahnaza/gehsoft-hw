package org.example;

import java.util.*;

public class CustomList<T> implements List<T> {

    private int size;

    private static final int DEFAULT_CAPACITY = 10;

    private T[] elementData;

    public CustomList() {
        elementData = (T[]) new Object[DEFAULT_CAPACITY];
    }

    public CustomList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        } else if (initialCapacity == 0) {
            elementData = (T[]) new Object[DEFAULT_CAPACITY];
        } else {
            elementData = (T[]) new Object[initialCapacity];
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean add(T element) {
        ensureCapacity(size + 1);
        elementData[size++] = element;
        return true;
    }

    private void ensureCapacity(int minCapacity) {
        if(elementData.length < minCapacity) {
            extendCapacity(minCapacity);
        }
    }

    private void extendCapacity(int minCapacity) {
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity * 2;
        if(newCapacity < minCapacity) {
            newCapacity = minCapacity;
        }
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public Object[] toArray(Object[] a) {
        return new Object[0];
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < size; i++) {
            if (elementData[i].equals(o)) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection c) {
        return false;
    }

    @Override
    public boolean addAll(Collection c) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection c) {
        return false;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++)
            elementData[i] = null;
        size = 0;
    }

    @Override
    public T get(int index) {
        checkIndex(index);
        return elementData[index];
    }

    @Override
    public T set(int index, T element) {
        checkIndex(index);
        T oldValue = elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    @Override
    public void add(int index, T element) {
        checkIndex(index);

        ensureCapacity(size + 1);

        for (int i = size; i > index; i--) {
            elementData[i] = elementData[i - 1];
        }

        elementData[index] = element;
        size++;
    }

    @Override
    public T remove(int index) {
        checkIndex(index);
        T removed = elementData[index];

        for(int i = index; i < size - 1; i++) {
            elementData[i] = elementData[i + 1];
        }

        elementData[size - 1] = null;
        size--;
        return removed;
    }

    @Override
    public int indexOf(Object o) {
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    @Override
    public ListIterator listIterator() {
        return null;
    }

    @Override
    public ListIterator listIterator(int index) {
        return null;
    }

    @Override
    public List subList(int fromIndex, int toIndex) {
        return null;
    }

    private void checkIndex(int index) {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}
