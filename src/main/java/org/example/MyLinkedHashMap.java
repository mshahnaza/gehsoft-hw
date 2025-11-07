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
        putAll(m);
    }

    @Override
    public V put(K key, V value) {
        boolean keyExists = containsKey(key);
        V oldValue = super.put(key, value);

        if (!keyExists) {
            Node<K, V> newNode = new Node<>(0, key, value, null);
            linkNodeLast(newNode);
        } else if (accessOrder) {
            Node<K, V> node = findNodeByKey(key);
            if (node != null) {
                moveToEnd(node);
            }
        }
        return oldValue;
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value != null && accessOrder) {
            Node<K, V> node = findNodeByKey(key);
            if (node != null) {
                moveToEnd(node);
            }
        }
        return value;
    }

    @Override
    public V remove(Object key) {
        Node<K, V> node = findNodeByKey(key);
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
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new LinkedHashSet<>();
        Node<K,V> current = head;
        while(current != null) {
            keys.add(current.getKey());
            current = current.after;
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        Node<K,V> current = head;
        while(current != null) {
            values.add(current.getValue());
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

    private Node<K, V> findNodeByKey(Object key) {
        Node<K, V> current = head;
        while (current != null) {
            if (Objects.equals(current.getKey(), key)) {
                return current;
            }
            current = current.after;
        }
        return null;
    }

    private static class Node<K,V> implements Map.Entry<K,V> {
        Node<K,V> before, after;
        private K key;
        private V value;

        public Node(int hash, K key, V value, Node<K,V> next) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}