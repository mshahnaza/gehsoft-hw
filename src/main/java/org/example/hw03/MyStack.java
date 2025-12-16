package org.example.hw03;

import java.util.EmptyStackException;

public class MyStack<T> {

    private MyLinkedList<T> list = new MyLinkedList<>();

    public void push(T e) {
        list.addLast(e);
    }

    public T pop() {
        if (list.isEmpty()) {
            throw new EmptyStackException();
        }
        return list.removeLast();
    }

    public T peek() {
        if (list.isEmpty()) {
            throw new EmptyStackException();
        }
        return list.getLast();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int search(T e) {
        int index = list.lastIndexOf(e);
        if (index == -1) return -1;
        return list.size() - index;
    }
}
