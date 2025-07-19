package org.example;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.UnaryOperator;

public class MyLinkedList<T> implements List<T>, Deque<T> {

    private int size;

    public MyLinkedList() {}

    public MyLinkedList(Collection<? extends T> c) {
        this();
        addAll(c);
    }

    private static class Node<T> {
        T item;
        Node<T> next;
        Node<T> prev;

        Node(MyLinkedList.Node<T> prev, T element, MyLinkedList.Node<T> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }

    private Node<T> first;
    private Node<T> last;

    @Override
    public int size() {
        return  size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean add(T t) {
        addLast(t);
        return true;
    }

    @Override
    public void add(int index, T element) {
        checkIndex(index);
        if (index == 0) {
            addFirst(element);
        } else if(index == size) {
            addLast(element);
        } else {
            Node<T> nextNode = getNode(index);
            Node<T> prevNode = nextNode.prev;
            Node<T> newNode = new Node<T>(prevNode, element, nextNode);
            nextNode.prev = newNode;
            prevNode.next = newNode;
            size++;
        }
    }

    @Override
    public void addFirst(T t) {
        Node<T> newNode = new Node<T>(null, t, first);
        if (first == null) {
            last = newNode;
        } else {
            first.prev = newNode;
        }
        first = newNode;
        size++;
    }

    @Override
    public void addLast(T t) {
        Node<T> newNode = new Node<T>(last, t, null);
        if (last == null) {
            first = newNode;
        } else {
            last.next = newNode;
        }
        last = newNode;
        size++;
    }

    private Node<T> getNode(int index) {
        checkIndex(index);
        if (index < (size / 2)) {
            Node<T> x = first;
            for (int i = 0; i < index; i++) {
                x = x.next;
            }
            return x;
        } else {
            Node<T> x = last;
            for (int i = size - 1; i > index; i--) {
                x = x.prev;
            }
            return x;
        }
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public T remove() {
        return removeFirst();
    }

    @Override
    public T element() {
        return getFirst();
    }

    @Override
    public boolean remove(Object o) {
        Node<T> currentNode = first;
        while (currentNode != null) {
            if(Objects.equals(currentNode.item, o)) {
                remove(currentNode);
                return true;
            }
            currentNode = currentNode.next;
        }
        return false;
    }

    @Override
    public void clear() {
        size = 0;
        first = null;
        last = null;
    }

    @Override
    public T get(int index) {
        checkIndex(index);
        return getNode(index).item;
    }

    @Override
    public T set(int index, T element) {
        checkIndex(index);
        Node<T> node = getNode(index);
        T old = node.item;
        node.item = element;

        return old;
    }

    @Override
    public T remove(int index) {
        checkIndex(index);
        Node<T> node = getNode(index);
        remove(node);
        return node.item;
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        Node<T> currentNode = first;
        while (currentNode != null) {
            if(Objects.equals(currentNode.item, o)) {
                return index;
            }
            currentNode = currentNode.next;
            index++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = size - 1;
        Node<T> currentNode = last;
        while (currentNode != null) {
            if(Objects.equals(currentNode.item, o)) {
                return index;
            }
            currentNode = currentNode.prev;
            index--;
        }
        return -1;
    }

    @Override
    public T getFirst() {
        if (first == null) throw new NoSuchElementException();
        return first.item;
    }

    @Override
    public T getLast() {
        if (last == null) throw new NoSuchElementException();
        return last.item;
    }

    @Override
    public T removeFirst() {
        if(first == null) throw new NoSuchElementException();
        return remove(0);
    }

    @Override
    public T removeLast() {
        if(last == null) throw new NoSuchElementException();
        return remove(size - 1);
    }

    @Override
    public T poll() {
        return pollFirst();
    }

    @Override
    public T pollFirst() {
        return (first == null) ? null : removeFirst();
    }

    @Override
    public T pollLast() {
        return (last == null) ? null : removeLast();
    }

    @Override
    public T peek() {
        return peekFirst();
    }

    @Override
    public T peekFirst() {
        return (first == null) ? null : first.item;
    }

    @Override
    public T peekLast() {
        return (last == null) ? null : last.item;
    }

    @Override
    public boolean offer(T t) {
        return add(t);
    }

    @Override
    public boolean offerFirst(T t) {
        addFirst(t);
        return true;
    }

    @Override
    public boolean offerLast(T t) {
        return add(t);
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
    public boolean removeFirstOccurrence(Object o) {
        return remove(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        int index = lastIndexOf(o);
        if (index == -1) {
            return false;
        }
        remove(index);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        checkIndex(index);
        Object[] a = c.toArray();
        int arrayLength = a.length;
        if (arrayLength == 0) {
            return false;
        }

        Node<T> newFirst = null, newLast = null;

        for (Object o : a) {
            Node<T> newNode = new Node<>(null, (T) a, null);
            if(newFirst == null) {
                newFirst = newLast = newNode;
            } else {
                newLast.next = newNode;
                newNode.prev = newLast;
                newLast = newNode;
            }
        }

        Node<T> prev = (index == 0) ? null : getNode(index - 1);
        Node<T> next = (index == size) ? null : getNode(index);

        if (prev != null) {
            prev.next = newFirst;
            newFirst.prev = prev;

        } else {
            first = newFirst;
        }

        if (next != null) {
            next.prev = newLast;
            newLast.next = next;
        } else {
            last = newLast;
        }

        size+=arrayLength;
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if(!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        Node<T> currentNode = first;
        while(currentNode != null) {
            Node<T> nextNode = currentNode.next;
            if(c.contains(currentNode.item)) {
                remove(currentNode);
                changed = true;
            }
            currentNode = nextNode;
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Node<T> currentNode = first;
        while(currentNode != null) {
            Node<T> nextNode = currentNode.next;
            if(!c.contains(currentNode.item)) {
                remove(currentNode);
                changed = true;
            }
            currentNode = nextNode;
        }
        return changed;
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size || fromIndex > toIndex)
            throw new IndexOutOfBoundsException();

        MyLinkedList<T> result = new MyLinkedList<>();
        Node<T> currentNode = getNode(fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            result.add(currentNode.item);
            currentNode = currentNode.next;
        }

        return result;
    }

    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        Node<T> currentNode = first;
        while (currentNode != null) {
            result[i++] = currentNode.item;
            currentNode = currentNode.next;
        }
        return result;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        if (a.length < size)
            a = (T1[]) Array.newInstance(a.getClass().getComponentType(), size);
        int i = 0;
        Node<T> currentNode = first;
        while (currentNode != null) {
            a[i++] = (T1) currentNode.item;
            currentNode = currentNode.next;
        }

        if (a.length > size)
            a[size] = null;
        return a;
    }

    @Override
    public MyLinkedList<T> reversed() {
        MyLinkedList<T> result = new MyLinkedList<>();
        Node<T> currentNode = last;
        while (currentNode != null) {
            result.add(currentNode.item);
            currentNode = currentNode.prev;
        }
        return result;
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
    public ListIterator<T> listIterator() {
        return null;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return null;
    }

    private void checkIndex(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException();
    }

    private void remove(Node<T> x) {
        if(x.prev != null) {
            x.prev.next = x.next;
        } else {
            first = x.next;
        }
        if(x.next != null) {
            x.next.prev = x.prev;
        } else {
            last = x.prev;
        }
        size--;
    }
}
