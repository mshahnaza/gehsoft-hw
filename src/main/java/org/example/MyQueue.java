package org.example;

import java.util.NoSuchElementException;

public class MyQueue<T> {

    private MyLinkedList<T> list = new MyLinkedList<>();

    public void add(T e) {
        list.addLast(e);
    }

    public T remove() {
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.removeFirst();
    }

    public T poll() {
        if (list.isEmpty()) {
            return null;
        }
        return list.removeFirst();
    }

    public T element() {
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.getFirst();
    }

    public T peek() {
        if (list.isEmpty()) {
            return null;
        }
        return list.getFirst();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void display() {
        if (list.isEmpty()) {
            System.out.println("Queue is empty");
            return;
        }
        for (T e : list) {
            System.out.print(e + " ");
        }
    }
}
