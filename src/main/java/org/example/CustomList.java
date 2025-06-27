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
        return indexOf(o) >= 0;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        if (a.length < size)
            return (T1[]) Arrays.copyOf(elementData, size, a.getClass());
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < size; i++) {
            if (o == null ? elementData[i] == null : o.equals(elementData[i])) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (c.isEmpty())
            return false;

        Object[] a = c.toArray();
        ensureCapacity(size + a.length);
        System.arraycopy(a, 0, elementData, size, a.length);
        size += a.length;
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        checkIndexForAdd(index);
        if (c.isEmpty()) {
            return false;
        }

        int numNew = c.size();
        ensureCapacity(size + numNew);

        System.arraycopy(elementData, index, elementData, index + numNew, size - index);

        int i = index;
        for (T e : c) {
            elementData[i++] = e;
        }

        size += numNew;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int origSize = size;
        for (int i = 0; i < size;) {
            if (c.contains(elementData[i])) {
                remove(i);
            } else {
                i++;
            }

        }
        return size != origSize;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int origSize = size;
        for (int i = 0; i < size;) {
            if (!c.contains(elementData[i])) {
                remove(i);
            } else {
                i++;
            }

        }
        return size != origSize;
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
        checkIndexForAdd(index);

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
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elementData[i])) return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int lastRet = -1;
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size;
            }

            @Override
            public T next() {
                if(index >= size) throw new NoSuchElementException();
                lastRet = index;
                return elementData[index++];
            }

            @Override
            public void remove() {
                if (lastRet < 0)
                    throw new IllegalStateException();
                CustomList.this.remove(lastRet);
                index = lastRet;
                lastRet = -1;
            }
        };
    }

    @Override
    public ListIterator listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator listIterator(int index) {
        checkIndexForAdd(index);
        return new ListIterator<T>() {
            int cursor = index;
            int lastRet = -1;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public boolean hasPrevious() {
                return cursor != 0;
            }

            @Override
            public int nextIndex() {
                return cursor;
            }

            @Override
            public int previousIndex() {
                return cursor - 1;
            }

            @Override
            public T next() {
                if(cursor >= size) throw new NoSuchElementException();
                lastRet = cursor;
                return elementData[cursor++];
            }

            @Override
            public T previous() {
                int i = cursor - 1;
                if (i < 0)
                    throw new NoSuchElementException();

                cursor = i;
                return elementData[lastRet = cursor];
            }

            @Override
            public void remove() {
                if (lastRet < 0)
                    throw new IllegalStateException();
                CustomList.this.remove(lastRet);
                cursor = lastRet;
                lastRet = -1;
            }

            @Override
            public void set(T t) {
                if (lastRet < 0)
                    throw new IllegalStateException();
                CustomList.this.set(lastRet, t);
            }

            @Override
            public void add(T t) {
                CustomList.this.add(cursor, t);
                cursor++;
                lastRet = -1;
            }
        };
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size) {
            throw new IndexOutOfBoundsException("Index out of bounds");
        }

        if (fromIndex > toIndex) throw new IllegalArgumentException();
        CustomList<T> subList = new CustomList<>();

        for (int i = fromIndex; i < toIndex; i++) {
            subList.add(elementData[i]);
        }
        return subList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        CustomList<?> other = (CustomList<?>) obj;

        if (this.size() != other.size()) return false;

        for (int i = 0; i < this.size(); i++) {
            Object e1 = this.get(i);
            Object e2 = other.get(i);
            if (!Objects.equals(e1, e2)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < size; i++) {
            Object e = elementData[i];
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    private void checkIndex(int index) {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

}
