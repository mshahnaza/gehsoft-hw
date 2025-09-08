package org.example;



import java.util.*;

public class MyHashMap<K, V> implements Map<K, V> {

    private int size;
    private int threshold;
    private  float loadFactor;
    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Node<K,V>[] table;

    public MyHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        this.threshold = (int) (initialCapacity * loadFactor);
        this.loadFactor = loadFactor;
        this.table = new Node[initialCapacity];
    }

    public MyHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    @Override
    public V get(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;
        if (table[index] != null) {
            for(Node<K, V> node = table[index]; node != null; node = node.next) {
                if(node.hash == hash && Objects.equals(node.key, key)) {
                    return node.value;
                }
            }
        }
        return null;
    }

    protected Node<K, V> getNode(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;
        Node<K, V> node = table[index];
        while (node != null) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                return node;
            }
            node = node.next;
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        if(size >= threshold) {
            resize();
        }

        int hash = hash(key);
        int index = (table.length - 1) & hash;

        if(table[index] == null) {
            table[index] = new Node<>(hash, key, value, null);
            size++;
            return value;
        }
        for(Node<K,V> node = table[index]; node != null; node = node.next) {
            if (node.hash == hash && Objects.equals(node.key, key)) {
                V oldValue = node.value;
                node.value = value;
                return oldValue;
            }
        }
        Node<K,V> newNode = new Node<>(hash, key, value, table[index]);
        table[index] = newNode;
        size++;
        return value;
    }

    private void resize() {
        Node<K, V>[] oldTable = table;
        int newCapacity = oldTable.length * 2;
        table = new Node[newCapacity];
        threshold = (int) (newCapacity * loadFactor);

        for(Node<K, V> node : oldTable) {
            while (node != null) {
                Node<K, V> next = node.next;
                int newIndex = (newCapacity - 1) & node.hash;
                node.next = table[newIndex];
                table[newIndex] = node;
                node = next;
            }
        }
    }

    @Override
    public V remove(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;
        Node<K, V> prev;
        if(table != null && table.length > 0 && (prev = table[index]) != null) {
            Node<K, V> node = null, current;
            if(prev.hash == hash && Objects.equals(prev.key, key)) {
                node = prev;
            } else if ((current = prev.next) != null) {
                while(current != null) {
                    if(current.hash == hash && Objects.equals(current.key, key)) {
                        node = current;
                        break;
                    }
                    prev = current;
                    current = current.next;
                }
            }
            if(node != null) {
                if(node == prev) {
                    table[index] = node.next;
                } else {
                    prev.next = node.next;
                }
                size--;
                return node.value;
            }

        }
        return null;
    }

    @Override
    public void clear() {
        for(int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;
        Node <K,V> node = table[index];
        if(table.length > 0 && table[index] != null) {
            while(node != null) {
                if(node.hash == hash && Objects.equals(node.key, key)) {
                    return true;
                }
                node = node.next;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        Node<K, V> currentNode;
        for(Node<K, V> node : table) {
            currentNode = node;
            while(currentNode != null) {
                if(Objects.equals(currentNode.value, value)) {
                    return true;
                }
                currentNode = currentNode.next;
            }
        }
        return false;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for(Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        Node<K, V> currentNode;
        for(Node<K, V> node : table) {
            currentNode = node;
            while(currentNode != null) {
                keys.add(currentNode.key);
                currentNode = currentNode.next;
            }
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new ArrayList<>();
        Node<K, V> currentNode;
        for(Node<K, V> node : table) {
            currentNode = node;
            while(currentNode != null) {
                values.add(currentNode.value);
                currentNode = currentNode.next;
            }
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new HashSet<>();
        Node<K, V> currentNode;
        for(Node<K, V> node : table) {
            currentNode = node;
            while(currentNode != null) {
                Entry<K, V> entry = currentNode;
                entries.add(entry);
                currentNode = currentNode.next;
            }
        }
        return entries;
    }

    protected static class Node<K, V> implements Map.Entry<K, V> {
        protected int hash;
        protected K key;
        protected V value;
        protected Node<K, V> next;

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
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
