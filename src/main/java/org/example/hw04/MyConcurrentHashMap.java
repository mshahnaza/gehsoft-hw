package org.example.hw04;



import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class MyConcurrentHashMap<K, V> implements Map<K, V> {

    private AtomicInteger size;
    private int threshold;
    private  float loadFactor;
    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Node<K,V>[] table;
    private ReentrantLock[] locks;
    private final ReentrantLock resizeLock = new ReentrantLock();

    public MyConcurrentHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        this.threshold = (int) (initialCapacity * loadFactor);
        this.loadFactor = loadFactor;
        this.table = new Node[initialCapacity];
        this.size = new AtomicInteger(0);
        this.locks = new ReentrantLock[initialCapacity];
        for (int i = 0; i < initialCapacity; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    public MyConcurrentHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public MyConcurrentHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    @Override
    public V get(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;
        locks[index].lock();
        try {
            if (table[index] != null) {
                for (Node<K, V> node = table[index]; node != null; node = node.next) {
                    if (node.hash == hash && Objects.equals(node.key, key)) {
                        return node.value;
                    }
                }
            }
            return null;
        } finally {
            locks[index].unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;

        resizeLock.lock();
        try {
            if (size.get() >= threshold) {
                resize();
            }
        } finally {
            resizeLock.unlock();
        }

        locks[index].lock();
        try {
            if (table[index] == null) {
                table[index] = new Node<>(hash, key, value, null);
                size.incrementAndGet();
                return value;
            }
            for (Node<K, V> node = table[index]; node != null; node = node.next) {
                if (node.hash == hash && Objects.equals(node.key, key)) {
                    V oldValue = node.value;
                    node.value = value;
                    return oldValue;
                }
            }
            Node<K, V> newNode = new Node<>(hash, key, value, table[index]);
            table[index] = newNode;
            size.incrementAndGet();
            return value;
        } finally {
            locks[index].unlock();
        }
    }

    private void resize() {
        resizeLock.lock();
        try {
            int oldCapacity = table.length;
            int newCapacity = oldCapacity * 2;

            Node<K, V>[] newTable = new Node[newCapacity];
            ReentrantLock[] newLocks = new ReentrantLock[newCapacity];
            for (int i = 0; i < newCapacity; i++) {
                newLocks[i] = new ReentrantLock();
            }

            for (int i = 0; i < oldCapacity; i++) {
                Node<K, V> node = table[i];
                while (node != null) {
                    Node<K, V> next = node.next;
                    int newIndex = (newCapacity - 1) & node.hash;
                    node.next = newTable[newIndex];
                    newTable[newIndex] = node;
                    node = next;
                }
            }

            table = newTable;
            locks = newLocks;
            threshold = (int) (newCapacity * loadFactor);
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public V remove(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;

        locks[index].lock();
        try {
            Node<K, V> prev;
            if (table != null && table.length > 0 && (prev = table[index]) != null) {
                Node<K, V> node = null, current;
                if (prev.hash == hash && Objects.equals(prev.key, key)) {
                    node = prev;
                } else if ((current = prev.next) != null) {
                    while (current != null) {
                        if (current.hash == hash && Objects.equals(current.key, key)) {
                            node = current;
                            break;
                        }
                        prev = current;
                        current = current.next;
                    }
                }
                if (node != null) {
                    if (node == prev) {
                        table[index] = node.next;
                    } else {
                        prev.next = node.next;
                    }
                    size.decrementAndGet();
                    return node.value;
                }

            }
            return null;
        } finally {
            locks[index].unlock();
        }
    }

    @Override
    public void clear() {
        resizeLock.lock();
        try {
            for (int i = 0; i < table.length; i++) {
                table[i] = null;
            }
            size.set(0);
        } finally {
            resizeLock.unlock();
        }
    }

    @Override
    public boolean containsKey(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;
        locks[index].lock();
        try {
            Node<K, V> node = table[index];
            if (table.length > 0 && table[index] != null) {
                while (node != null) {
                    if (node.hash == hash && Objects.equals(node.key, key)) {
                        return true;
                    }
                    node = node.next;
                }
            }
            return false;
        } finally {
            locks[index].unlock();
        }
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 0; i < table.length; i++) {
            locks[i].lock();
            try {
                Node<K, V> currentNode = table[i];
                while (currentNode != null) {
                    if (Objects.equals(currentNode.value, value)) {
                        return true;
                    }
                    currentNode = currentNode.next;
                }
            } finally {
                locks[i].unlock();
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
        for (int i = 0; i < table.length; i++) {
            locks[i].lock();
            try {
                Node<K, V> currentNode = table[i];
                while (currentNode != null) {
                    keys.add(currentNode.key);
                    currentNode = currentNode.next;
                }
            } finally {
                locks[i].unlock();
            }
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new ArrayList<>();
        for (int i = 0; i < table.length; i++) {
            locks[i].lock();
            try {
                Node<K, V> currentNode = table[i];
                while (currentNode != null) {
                    values.add(currentNode.value);
                    currentNode = currentNode.next;
                }
            } finally {
                locks[i].unlock();
            }
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new HashSet<>();
        for (int i = 0; i < table.length; i++) {
            locks[i].lock();
            try {
                Node<K, V> currentNode = table[i];
                while (currentNode != null) {
                    Entry<K, V> entry = currentNode;
                    entries.add(entry);
                    currentNode = currentNode.next;
                }
            } finally {
                locks[i].unlock();
            }
        }
        return entries;
    }

    private static class Node<K, V> implements Map.Entry<K, V> {
        private int hash;
        private K key;
        private V value;
        private Node<K, V> next;

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
