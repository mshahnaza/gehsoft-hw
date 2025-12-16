package org.example.hw05.decorators;

import org.example.hw01.CustomList;

import java.util.*;

public class CopyOnWriteList<T> implements List<T> {
    private volatile CustomList<T> list;

    public CopyOnWriteList(CustomList<T> list) {
        this.list = list;
    }

    private synchronized void replaceList(CustomList<T> newList) {
        list = newList;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return list.toArray(a);
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public boolean add(T element) {
        synchronized (this) {
            CustomList<T> newList = new CustomList<>(list.size() + 1);
            newList.addAll(list);
            newList.add(element);
            replaceList(newList);
            return true;
        }
    }

    @Override
    public T set(int index, T element) {
        synchronized (this) {
            CustomList<T> newList = new CustomList<>(list.size());
            newList.addAll(list);
            T old = newList.set(index, element);
            replaceList(newList);
            return old;
        }
    }

    @Override
    public T remove(int index) {
        synchronized (this) {
            CustomList<T> newList = new CustomList<>(list.size() - 1);
            newList.addAll(list);
            T removed = newList.remove(index);
            replaceList(newList);
            return removed;
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            CustomList<T> newList = new CustomList<>(list.size());
            newList.addAll(list);
            boolean removed = newList.remove(o);
            replaceList(newList);
            return removed;
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            replaceList(new CustomList<>());
        }
    }

    @Override
    public void add(int index, T element) {
        synchronized (this) {
            CustomList<T> newList = new CustomList<>(list.size() + 1);
            newList.addAll(list);
            newList.add(index, element);
            replaceList(newList);
        }
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        synchronized (this) {
            if (c.isEmpty()) return false;
            CustomList<T> newList = new CustomList<>(list.size() + c.size());
            newList.addAll(list);
            newList.addAll(c);
            replaceList(newList);
            return true;
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        synchronized (this) {
            if (c.isEmpty()) return false;
            CustomList<T> newList = new CustomList<>(list.size() + c.size());
            newList.addAll(list);
            newList.addAll(index, c);
            replaceList(newList);
            return true;
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        synchronized (this) {
            CustomList<T> newList = new CustomList<>(list.size());
            newList.addAll(list);
            boolean removed = newList.removeAll(c);
            replaceList(newList);
            return removed;
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        synchronized (this) {
            CustomList<T> newList = new CustomList<>(list.size());
            newList.addAll(list);
            boolean changed = newList.retainAll(c);
            replaceList(newList);
            return changed;
        }
    }

}
