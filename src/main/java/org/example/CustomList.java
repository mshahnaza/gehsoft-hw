package org.example;

import java.util.*;

public class CustomList implements List {

    private int size;

    private static final int DEFAULT_CAPACITY = 10;

    private Object[] elementData;

    public CustomList() {
        elementData = new Object[DEFAULT_CAPACITY];
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
    public boolean add(Object o) {
        ensureCapacity(size + 1);
        elementData[size++] = o;
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
    public Object get(int index) {
        checkIndex(index);
        return elementData[index];
    }

    @Override
    public Object set(int index, Object element) {
        checkIndex(index);
        Object oldValue = elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    @Override
    public void add(int index, Object element) {
        checkIndex(index);

        ensureCapacity(size + 1);

        for (int i = size; i > index; i--) {
            elementData[i] = elementData[i - 1];
        }

        elementData[index] = element;
        size++;
    }

    @Override
    public Object remove(int index) {
        checkIndex(index);
        Object removed = elementData[index];

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
