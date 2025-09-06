package org.example;

import java.util.*;

public class MyHashMapDoubleHash<K,V> implements Map<K,V> {

    private int size;
    private int threshold;
    private float loadFactor;
    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Entry<K, V>[] table;

    public MyHashMapDoubleHash(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                    initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                    loadFactor);
        this.threshold = (int) (initialCapacity * loadFactor);
        this.loadFactor = loadFactor;
        this.table = new Entry[initialCapacity];
    }

    public MyHashMapDoubleHash() {
        this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMapDoubleHash(int initialCapacity) {
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

    private int hash1(Object key) {
        int h = key == null ? 0 : key.hashCode();
        return Math.abs(h) % table.length;
    }

    private int hash2(Object key) {
        int h = key == null ? 0 : key.hashCode();
        return 1 + (Math.abs(h) % (table.length - 1));
    }

    @Override
    public V get(Object key) {
        int h1 = hash1(key);
        int h2 = hash2(key);
        int i = 0;
        while(i < table.length) {
            int index = (h1 + i * h2) % table.length;
            Entry<K, V> e = table[index];
            if (e == null) return null;
            if (!e.deleted && Objects.equals(e.key, key)) return e.value;
            i++;
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        if (size >= threshold) resize();

        int h1 = hash1(key);
        int h2 = hash2(key);
        int i = 0;
        int firstDeletedIndex = -1;

        while (i < table.length) {
            int index = (h1 + i * h2) % table.length;
            Entry<K, V> entry = table[index];

            if (entry == null) {
                if (firstDeletedIndex != -1) {
                    table[firstDeletedIndex] = new Entry<>(key, value);
                    size++;
                    return value;
                } else {
                    table[index] = new Entry<>(key, value);
                    size++;
                    return value;
                }
            } else if (entry.deleted) {
                if (firstDeletedIndex == -1) firstDeletedIndex = index;
            } else if (Objects.equals(entry.key, key)) {
                V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }

            i++;
        }

        if (firstDeletedIndex != -1) {
            table[firstDeletedIndex] = new Entry<>(key, value);
            size++;
            return value;
        }

        return null;
    }


    private void resize() {
        Entry<K, V>[] oldTable = table;
        table = new Entry[oldTable.length * 2];
        threshold = (int) (table.length * loadFactor);
        for(Entry<K, V> entry : oldTable) {
            if(entry != null && !entry.deleted) {
                put(entry.key, entry.value);
            }
        }
    }

    @Override
    public V remove(Object key) {
        int h1 = hash1(key);
        int h2 = hash2(key);
        int i = 0;

        while (i < table.length) {
            int index = (h1 + i * h2) % table.length;
            Entry<K, V> e = table[index];

            if (e == null) return null;
            if (!e.deleted && Objects.equals(e.key, key)) {
                e.deleted = true;
                size--;
                return e.value;
            }

            i++;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        size = 0;
    }

    @Override
    public boolean containsKey(Object key) {
        int h1 = hash1(key);
        int h2 = hash2(key);
        int i = 0;
        while (i < table.length) {
            int index = (h1 + i * h2) % table.length;
            Entry<K, V> e = table[index];
            if (e == null) return false;
            if (!e.deleted && Objects.equals(e.key, key)) return true;
            i++;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for(Entry<K, V> entry : table) {
            if(entry != null && !entry.deleted && Objects.equals(entry.value, value)) return true;
        }
        return false;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for(Entry<K, V> entry : table) {
            if(entry != null && !entry.deleted) {
                keys.add(entry.key);
            }
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = new ArrayList<>();
        for (Entry<K,V> entry : table) {
            if(entry != null && !entry.deleted) {
                values.add(entry.value);
            }
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entries = new HashSet<>();
        for(Entry<K, V> entry : table) {
            if(entry != null && !entry.deleted) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public static class Entry<K,V> implements Map.Entry<K, V> {
        K key;
        V value;
        boolean deleted;

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
            V old = this.value;
            this.value = value;
            return old;
        }

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.deleted = false;
        }
    }
}
