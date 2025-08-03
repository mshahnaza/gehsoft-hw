package org.example;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

public class MyLinkedListDeque<T> implements Deque<T> {
    
    private MyLinkedList<T> list = new MyLinkedList<>();

    
    public void addFirst(T t) {
        list.addFirst(t);
    }

    
    public void addLast(T t) {
        list.addLast(t);
    }

    
    public boolean offerFirst(T t) {
        return list.offerFirst(t);
    }

    
    public boolean offerLast(T t) {
        return list.offerLast(t);
    }

    
    public T removeFirst() {
        return list.removeFirst();
    }

    
    public T removeLast() {
        return list.removeLast();
    }

    
    public T pollFirst() {
        return list.pollFirst();
    }

    
    public T pollLast() {
        return list.pollLast();
    }

    
    public T getFirst() {
        return list.getFirst();
    }

    
    public T getLast() {
        return list.getLast();
    }

    
    public T peekFirst() {
        return list.peekFirst();
    }

    
    public T peekLast() {
        return list.peekLast();
    }

    
    public boolean removeFirstOccurrence(Object o) {
        return list.removeFirstOccurrence(o);
    }

    
    public boolean removeLastOccurrence(Object o) {
        return list.removeLastOccurrence(o);
    }

    
    public boolean add(T t) {
        return list.add(t);
    }

    
    public boolean offer(T t) {
        return list.offer(t);
    }

    
    public T remove() {
        return list.remove();
    }

    
    public T poll() {
        return list.poll();
    }

    
    public T element() {
        return list.element();
    }

    
    public T peek() {
        return list.peek();
    }

    
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    
    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    
    public void clear() {
        list.clear();
    }

    
    public void push(T t) {
        list.push(t);
    }

    
    public T pop() {
        return list.pop();
    }

    
    public boolean remove(Object o) {
        return list.remove(o);
    }

    
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    
    public boolean contains(Object o) {
        return list.contains(o);
    }

    
    public int size() {
        return list.size();
    }

    
    public boolean isEmpty() {
        return list.isEmpty();
    }

    
    public Iterator<T> iterator() {
        return list.iterator();
    }

    
    public Object[] toArray() {
        return list.toArray();
    }

    
    public <T1> T1[] toArray(T1[] a) {
        return list.toArray(a);
    }

    
    public Iterator<T> descendingIterator() {
        return list.descendingIterator();
    }
}
