package org.example;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MyHashMapTest {

    @Test
    void put() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        assertEquals(1, map.put("one", 2));
    }

    @Test
    void get() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        assertEquals(1, map.get("one"));
        assertEquals(null, map.get("two"));
    }

    @Test
    void remove() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        assertEquals(1, map.remove("one"));
        assertEquals(null, map.get("one"));
    }

    @Test
    void clear() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.clear();
        assertEquals(0, map.size());
        assertEquals(null, map.get("one"));
    }

    @Test
    void containsKey() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("two", 2);
        assertTrue(map.containsKey("two"));
        assertFalse(map.containsKey("three"));
    }

    @Test
    void containsValue() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        assertTrue(map.containsValue(1));
        assertFalse(map.containsValue(3));
    }

    @Test
    void entrySet() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        assertEquals(3, entries.size());
    }

    @Test
    void keySet() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        Set<String> keys = map.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("two"));
    }

    @Test
    void values() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        Collection<Integer> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains(1));
    }

    @Test
    void putAll() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        MyHashMap<String, Integer> map2 = new MyHashMap<>();
        map2.putAll(map);

        assertEquals(2, map2.size());
        assertTrue(map2.containsKey("two"));
    }

    @Test
    void isEmpty() {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        assertFalse(map.isEmpty());
    }
}
