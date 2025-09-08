package org.example;

import java.util.*;

public class MyLinkedHashMap<K,V> extends MyHashMap<K,V> {

    private Node<K,V> head;
    private Node<K,V> tail;
    private final boolean accessOrder;

    public MyLinkedHashMap() {
        super();
        this.accessOrder = false;
    }

    public MyLinkedHashMap(boolean accessOrder) {
        super();
        this.accessOrder = accessOrder;
    }

    public MyLinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        this.accessOrder = false;
    }

    public MyLinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.accessOrder = false;
    }

    public MyLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }

    public MyLinkedHashMap(Map<? extends K, ? extends V> m) {
        super();
        this.accessOrder = false;
        super.putAll(m);
    }

    @Override
    public V put(K key, V value) {
        V oldValue = super.put(key, value);

        Node<K, V> node = getNode(key);
        if (oldValue == null) {
            linkNodeLast(node);
        } else if (accessOrder) {
            moveToEnd(node);
        }
        return oldValue;
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value != null && accessOrder) {
            Node<K, V> node = getNode(key);
            moveToEnd(node);
        }
        return value;
    }

    @Override
    public V remove(Object key) {
        Node<K, V> node = getNode(key);
        if (node != null) {
            unlink(node);
        }
        return super.remove(key);
    }

    @Override
    public void clear() {
        super.clear();
        head = tail = null;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new LinkedHashSet<>();
        Node<K,V> current = head;
        while(current != null) {
            keys.add((K) current.getKey());
            current = current.after;
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        Node<K,V> current = head;
        while(current != null) {
            values.add((V) current.getValue());
            current = current.after;
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new LinkedHashSet<>();
        Node<K,V> current = head;
        while(current != null) {
            entries.add(current);
            current = current.after;
        }
        return entries;
    }

    private void linkNodeLast(Node<K, V> node) {
        if (tail == null) {
            head = tail = node;
        } else {
            tail.after = node;
            node.before = tail;
            tail = node;
        }
    }

    private void moveToEnd(Node<K, V> node) {
        if (node == tail) return;
        unlink(node);
        linkNodeLast(node);
    }

    private void unlink(Node<K, V> node) {
        Node<K, V> b = node.before;
        Node<K, V> a = node.after;

        if (b == null) head = a;
        else b.after = a;

        if (a == null) tail = b;
        else a.before = b;

        node.before = node.after = null;
    }

    @Override
    protected Node<K,V> getNode(Object key) {
        return (Node<K,V>) super.getNode(key);
    }


    private static class Node<K,V> extends MyHashMap.Node{
        Node<K,V> before,after;

        public Node(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
}
