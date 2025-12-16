package org.example.hw04;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;

public class MyConcurrentSkipListMap<K,V> implements Map<K,V> {
    private static final int MAX_LEVEL = 16;
    private final Random random = new Random();
    private final Node<K, V> head = new Node<>(null, null, MAX_LEVEL);
    private AtomicInteger level = new AtomicInteger(0);
    private AtomicInteger size = new AtomicInteger(0);

    private final Comparator<? super K> comparator;

    public MyConcurrentSkipListMap() {
        this.comparator = null;
    }

    public MyConcurrentSkipListMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return head.next[0].getReference() == null;
    }

    private boolean find(K key, Node<K, V>[] preds, Node<K, V>[] succs) {
        boolean[] marked = {false};
        retry:
        while (true) {
            Node<K, V> pred = head;
            for (int lev = level.get(); lev >= 0; lev--) {
                Node<K, V> curr = pred.next[lev].getReference();
                while (true) {
                    Node<K, V> succ = (curr != null) ? curr.next[lev].get(marked) : null;
                    while (curr != null && marked[0]) {
                        if (!pred.next[lev].compareAndSet(curr, succ, false, false))
                            continue retry;
                        curr = succ;
                        succ = (curr != null) ? curr.next[lev].get(marked) : null;
                    }
                    if (curr != null && compare(curr.key, key) < 0) {
                        pred = curr;
                        curr = succ;
                    } else {
                        break;
                    }
                }
                preds[lev] = pred;
                succs[lev] = (pred.next[lev] != null) ? pred.next[lev].getReference() : null;
            }
            return succs[0] != null && compare(succs[0].key, key) == 0;
        }
    }


    private int randomLevel() {
        int lvl = 0;
        while (random.nextBoolean() && lvl < MAX_LEVEL) lvl++;
        return lvl;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        Node<K, V> x = head.next[0].getReference();
        while (x != null) {
            if (Objects.equals(x.value.get(), value)) return true;
            x = x.next[0].getReference();
        }
        return false;
    }

    @Override
    public V get(Object key) {
        K k = (K) key;
        Node<K, V>[] preds = new Node[MAX_LEVEL + 1];
        Node<K, V>[] succs = new Node[MAX_LEVEL + 1];
        return find(k, preds, succs) ? succs[0].value.get() : null;
    }

    @Override
    public V put(K key, V value) {
        Node<K, V>[] preds = new Node[MAX_LEVEL + 1];
        Node<K, V>[] succs = new Node[MAX_LEVEL + 1];

        while (true) {
            for (int i = 0; i <= MAX_LEVEL; i++) {
                preds[i] = null;
                succs[i] = null;
            }

            if (find(key, preds, succs)) {
                V oldValue = succs[0].value.get();
                succs[0].value.set(value);
                return oldValue;
            } else {
                int lvl = randomLevel();
                Node<K, V> newNode = new Node<>(key, value, lvl);

                for (int i = 0; i <= lvl; i++) {
                    newNode.next[i].set(succs[i], false);
                }

                if (!preds[0].next[0].compareAndSet(succs[0], newNode, false, false)) {
                    continue;
                }

                for (int i = 1; i <= lvl; i++) {
                    while (true) {
                        if (preds[i] == null) {
                            break;
                        }
                        if (!preds[i].next[i].compareAndSet(succs[i], newNode, false, false)) {
                            break;
                        }
                    }
                }

                int currentLevel;
                do {
                    currentLevel = level.get();
                } while (lvl > currentLevel && !level.compareAndSet(currentLevel, lvl));

                size.incrementAndGet();
                return null;
            }
        }
    }

    @Override
    public V remove(Object key) {
        K k = (K) key;
        Node<K, V>[] preds = new Node[MAX_LEVEL + 1];
        Node<K, V>[] succs = new Node[MAX_LEVEL + 1];
        Node<K, V> nodeToRemove;

        while (true) {
            if (!find(k, preds, succs)) return null;

            nodeToRemove = succs[0];

            for (int i = nodeToRemove.next.length - 1; i >= 1; i--) {
                boolean[] marked = {false};
                Node<K, V> next = nodeToRemove.next[i].get(marked);
                while (!marked[0]) {
                    if (nodeToRemove.next[i].compareAndSet(next, next, false, true)) {
                        break;
                    }
                    next = nodeToRemove.next[i].get(marked);
                }
            }

            boolean[] marked = {false};
            Node<K, V> next = nodeToRemove.next[0].get(marked);
            while (true) {
                boolean success = nodeToRemove.next[0].compareAndSet(next, next, false, true);
                if (success) {
                    find(k, preds, succs);
                    V oldValue = nodeToRemove.value.get();
                    size.decrementAndGet();
                    return oldValue;
                }
                next = nodeToRemove.next[0].get(marked);
                if (marked[0]) {
                    return null;
                }
            }
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < head.next.length; i++) head.next[i].set(null, false);
        level.set(0);
        size.set(0);
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new LinkedHashSet<>();
        Node<K, V> x = head.next[0].getReference();
        while (x != null) {
            keys.add(x.key);
            x = x.next[0].getReference();
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        Node<K, V> x = head.next[0].getReference();
        while (x != null) {
            values.add(x.value.get());
            x = x.next[0].getReference();
        }
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new LinkedHashSet<>();
        Node<K, V> x = head.next[0].getReference();
        while (x != null) {
            entries.add(new AbstractMap.SimpleEntry<>(x.key, x.value.get()));
            x = x.next[0].getReference();
        }
        return entries;
    }


    private int compare(K k1, K k2) {
        if (comparator != null) {
            return comparator.compare(k1, k2);
        }
        return ((Comparable<? super K>) k1).compareTo(k2);
    }

    private static class Node<K, V> {
        private K key;
        private AtomicReference<V> value;
        private AtomicMarkableReference<Node<K, V>>[] next;

        Node(K key, V value, int level) {
            this.key = key;
            this.value = new AtomicReference<>(value);
            this.next = new AtomicMarkableReference[level + 1];
            for (int i = 0; i <= level; i++)
                this.next[i] = new AtomicMarkableReference<>(null, false);
        }
    }
}
